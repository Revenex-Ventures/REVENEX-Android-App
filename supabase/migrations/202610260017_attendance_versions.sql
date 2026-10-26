begin;
-- Preserve legacy ISO/English dates without silently merging or deleting any records.
create function erp_private.school_date(value text) returns date
language plpgsql immutable strict set search_path='' as $$
declare parts text[]; month_number integer;
begin
 if value ~ '^\d{4}-\d{2}-\d{2}$' then
  return make_date(substr(value,1,4)::int,substr(value,6,2)::int,substr(value,9,2)::int);
 end if;
 parts=regexp_match(value,'^(\d{1,2}) ([A-Za-z]{3}) (\d{4})$');
 if parts is null then raise exception 'Unsupported attendance date; operator review required' using errcode='23514'; end if;
 month_number=array_position(array['jan','feb','mar','apr','may','jun','jul','aug','sep','oct','nov','dec'],lower(parts[2]));
 if month_number is null then raise exception 'Invalid attendance month' using errcode='23514'; end if;
 return make_date(parts[3]::int,month_number,parts[1]::int);
end $$;
revoke all on function erp_private.school_date(text) from public,anon;
grant execute on function erp_private.school_date(text) to authenticated;
alter table public.attendance
 add column record_date date generated always as (erp_private.school_date(attendance_date)) stored not null,
 add column version integer not null default 1 check(version>0);
-- Fails safely if historical date aliases conflict. Never guess which record to keep.
create unique index attendance_school_day_slot on public.attendance(student_id,session_id,record_date,session_slot);
revoke insert,update on public.attendance from authenticated;

create function public.save_roll_call(p_section text,p_date date,p_slot text,p_entries jsonb)
returns jsonb language plpgsql security definer set search_path='' as $$
declare actor public.profiles; sec public.sections; entry jsonb; pupil public.students;
 prior public.attendance; result jsonb='[]'::jsonb; saved public.attendance;
 expected integer; new_status text; remark text; roster_count integer; entry_count integer;
begin
 select * into actor from public.profiles where id=auth.uid();
 select * into sec from public.sections where id=p_section and school_id=actor.school_id and session_id=actor.session_id;
 if actor.id is null or sec.id is null or not(erp_private.staff() or erp_private.teaches(sec.id)) then
  raise exception 'Attendance access denied' using errcode='42501'; end if;
 if p_date is distinct from (now() at time zone 'Asia/Kolkata')::date then
  raise exception 'Historical attendance requires an approved correction' using errcode='23514'; end if;
 if p_slot is null or length(btrim(p_slot)) not between 1 and 120 or jsonb_typeof(p_entries) is distinct from 'array' then
  raise exception 'Invalid attendance register' using errcode='23514'; end if;
 if p_slot not in ('Morning Roll Call','STEM Lab','Senior Practicum') and not exists(
  select 1 from public.attendance where section_id=sec.id and school_id=actor.school_id and session_id=actor.session_id and session_slot=p_slot) then
  raise exception 'School attendance slot is not configured' using errcode='23514'; end if;
 -- Serialize same-class saves and enrollment changes. Version checks still protect stale clients.
 perform pg_advisory_xact_lock(hashtextextended('class-roster:'||sec.id,0));
 perform pg_advisory_xact_lock(hashtextextended('rollcall:'||sec.id||':'||p_date||':'||p_slot,0));
 select count(*) into roster_count from public.students where section_id=sec.id and school_id=actor.school_id
  and session_id=actor.session_id and coalesce(payload->>'status','ACTIVE')<>'ARCHIVED';
 entry_count=jsonb_array_length(p_entries);
 if roster_count=0 or entry_count<>roster_count or entry_count<>(select count(distinct x->>'student_id') from jsonb_array_elements(p_entries) x) then
  raise exception 'Class roster changed; reload the complete register' using errcode='23514'; end if;
 for entry in select value from jsonb_array_elements(p_entries) loop
  select * into pupil from public.students where id=entry->>'student_id' and section_id=sec.id and school_id=actor.school_id
   and session_id=actor.session_id and coalesce(payload->>'status','ACTIVE')<>'ARCHIVED';
  if not found then raise exception 'Student outside the permitted register' using errcode='42501'; end if;
  new_status=entry->>'status';remark=coalesce(entry->>'remark','');
  if new_status is null or new_status not in ('PRESENT','ABSENT','LATE','EXCUSED','LEAVE') or length(remark)>2000
   or coalesce(entry->>'expected_version','') !~ '^[0-9]{1,9}$' then
   raise exception 'Invalid attendance fields' using errcode='23514'; end if;
  expected=(entry->>'expected_version')::integer;
  select * into prior from public.attendance where student_id=pupil.id and session_id=actor.session_id
   and record_date=p_date and session_slot=p_slot for update;
  if prior.id is not null and prior.version=expected+1 and prior.marked_by=actor.id
   and prior.status=new_status and coalesce(prior.payload->>'remark','')=remark then
   result=result||jsonb_build_array(to_jsonb(prior));continue; end if;
  if coalesce(prior.version,0)<>expected then raise exception 'Attendance changed; reload before saving' using errcode='23514'; end if;
  if prior.id is not null and prior.status=new_status and coalesce(prior.payload->>'remark','')=remark then
   result=result||jsonb_build_array(to_jsonb(prior));continue; end if;
  insert into public.attendance(id,school_id,session_id,section_id,student_id,attendance_date,session_slot,status,marked_by,payload,version)
  values(coalesce(prior.id,gen_random_uuid()::text),actor.school_id,actor.session_id,sec.id,pupil.id,to_char(p_date,'DD Mon YYYY'),p_slot,new_status,actor.id,
   jsonb_build_object('studentId',pupil.id,'studentName',pupil.payload->>'name','rollNumber',pupil.payload->'rollNumber','status',new_status,'remark',remark),expected+1)
  on conflict(id) do update set status=excluded.status,marked_by=excluded.marked_by,payload=excluded.payload,version=excluded.version returning * into saved;
  result=result||jsonb_build_array(to_jsonb(saved));
 end loop;
 return result;
end $$;
revoke all on function public.save_roll_call(text,date,text,jsonb) from public,anon;
grant execute on function public.save_roll_call(text,date,text,jsonb) to authenticated;

create function erp_private.lock_student_roster() returns trigger language plpgsql security definer set search_path='' as $$
begin
 if tg_op='UPDATE' and old.section_id is distinct from new.section_id then
  -- Deterministic order avoids deadlocks when two operators exchange class allocations.
  perform pg_advisory_xact_lock(hashtextextended('class-roster:'||least(old.section_id,new.section_id),0));
  perform pg_advisory_xact_lock(hashtextextended('class-roster:'||greatest(old.section_id,new.section_id),0));
 else
  perform pg_advisory_xact_lock(hashtextextended('class-roster:'||case when tg_op='DELETE' then old.section_id else new.section_id end,0));
 end if;
 return case when tg_op='DELETE' then old else new end;
end $$;
revoke all on function erp_private.lock_student_roster() from public,anon,authenticated;
create trigger student_roster_lock before insert or update or delete on public.students for each row execute function erp_private.lock_student_roster();
commit;
