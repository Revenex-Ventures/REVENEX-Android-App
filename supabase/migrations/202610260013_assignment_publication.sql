begin;
-- Preserve existing published assignments; new draft state is explicit through the RPC.
alter table public.assignments
 add column publication_state text not null default 'PUBLISHED' check(publication_state in ('DRAFT','PUBLISHED')),
 add column version integer not null default 1 check(version>0),
 add column created_by uuid references auth.users,
 add column published_at timestamptz;

create function erp_private.teaches_subject(sec text,subject_name text) returns boolean
language sql stable security definer set search_path='' as $$
 select erp_private.teaches(sec) and exists(select 1 from public.teachers t join public.profiles p on p.id=auth.uid()
 where t.profile_id=p.id and t.school_id=p.school_id and t.session_id=p.session_id
 and exists(select 1 from jsonb_array_elements_text(coalesce(t.payload->'subjects','[]'::jsonb)) x
 where lower(btrim(x))=lower(btrim(subject_name))))
$$;
revoke all on function erp_private.teaches_subject(text,text) from public,anon;
grant execute on function erp_private.teaches_subject(text,text) to authenticated;
-- The family branch must not grant teachers access to another subject through sees_section.
create function erp_private.assignment_visible(work_id text) returns boolean
language sql stable security definer set search_path='' as $$
 select exists(select 1 from public.assignments a where a.id=work_id and erp_private.context_ok(a.school_id,a.session_id)
 and (erp_private.staff() or erp_private.teaches_subject(a.section_id,a.payload->>'subject') or
 (erp_private.has_role(array['STUDENT','PARENT']::public.erp_role[]) and a.publication_state='PUBLISHED' and erp_private.sees_section(a.section_id))))
$$;
revoke all on function erp_private.assignment_visible(text) from public,anon;
grant execute on function erp_private.assignment_visible(text) to authenticated;
drop policy assignments_read on public.assignments;
create policy assignments_read on public.assignments for select to authenticated using(erp_private.assignment_visible(id));
drop policy assignment_submissions_read on public.assignment_submissions;
create policy assignment_submissions_read on public.assignment_submissions for select to authenticated using(
 erp_private.context_ok(school_id,session_id) and erp_private.assignment_visible(assignment_id)
 and (erp_private.staff() or erp_private.teaches(section_id) or erp_private.own_student(student_id)));
revoke insert,update on public.assignments from authenticated;

create function public.write_assignment(p_id text,p_section text,p_payload jsonb,p_due_date date,p_publish boolean,p_expected_version integer)
returns jsonb language plpgsql security definer set search_path='' as $$
declare actor public.profiles; sec public.sections; prior public.assignments; saved public.assignments;
 clean jsonb; points text; author_name text; deadline timestamptz; desired text; document public.file_metadata; ref text;
begin
 select * into actor from public.profiles where id=auth.uid();
 select * into sec from public.sections where id=p_section and school_id=actor.school_id and session_id=actor.session_id;
 if actor.id is null or sec.id is null or not(erp_private.staff() or erp_private.teaches_subject(sec.id,p_payload->>'subject')) then
  raise exception 'Assignment authoring access denied' using errcode='42501'; end if;
 if p_id is null or length(p_id) not between 1 and 200 or jsonb_typeof(p_payload) is distinct from 'object'
 or jsonb_typeof(p_payload->'title') is distinct from 'string' or length(btrim(p_payload->>'title')) not between 1 and 200
 or jsonb_typeof(p_payload->'subject') is distinct from 'string' or length(btrim(p_payload->>'subject')) not between 1 and 120
 or jsonb_typeof(p_payload->'instructions') is distinct from 'string' or length(p_payload->>'instructions')>8000
 or p_due_date is null or p_due_date not between date '2000-01-01' and date '2100-12-31'
 or p_publish is null or p_expected_version is null or p_expected_version<0 or p_expected_version>=2147483647 then
  raise exception 'Invalid assignment fields' using errcode='23514'; end if;
 points=coalesce(p_payload->>'maxPoints','20');
 if points !~ '^[0-9]{1,5}$' then raise exception 'Invalid points' using errcode='23514'; end if;
 if points::int not between 1 and 10000 then raise exception 'Invalid points' using errcode='23514'; end if;
 deadline=(p_due_date+1)::timestamp at time zone 'Asia/Kolkata';
 desired=case when p_publish then 'PUBLISHED' else 'DRAFT' end;
 perform pg_advisory_xact_lock(hashtextextended('assignment:'||p_id,0));
 select * into prior from public.assignments where id=p_id for update;
 if prior.id is not null and (prior.school_id<>actor.school_id or prior.session_id<>actor.session_id
  or prior.section_id<>sec.id or (not erp_private.staff() and (not erp_private.teaches_subject(prior.section_id,prior.payload->>'subject')
   or (prior.created_by is not null and prior.created_by<>actor.id)))) then
  raise exception 'Assignment update access denied' using errcode='42501'; end if;
 if prior.id is not null and prior.payload->>'subject' is distinct from btrim(p_payload->>'subject')
  and exists(select 1 from public.assignment_submissions s where s.assignment_id=prior.id) then
  raise exception 'Assignments with submissions cannot change subject' using errcode='23514'; end if;
 author_name=case when prior.id is null then actor.name else coalesce(prior.payload->>'teacherName',actor.name) end;
 ref=coalesce(p_payload->>'attachmentUrl','');
 if ref<>'' then
  select * into document from public.file_metadata where object_path=substr(ref,26)
   and ref like 'supabase://erp-documents/%' and school_id=actor.school_id and session_id=actor.session_id;
  if not found or (document.owner_id<>actor.id and not erp_private.staff()) then
   raise exception 'Assignment attachment unavailable' using errcode='42501'; end if;
 end if;
 clean=jsonb_build_object('id',p_id,'title',btrim(p_payload->>'title'),'subject',btrim(p_payload->>'subject'),
  'instructions',p_payload->>'instructions','maxPoints',points::int,'teacherName',author_name,
  'classGrade',(select grade from public.classes where id=sec.class_id),'division',sec.division,
  'schoolId',actor.school_id,'sessionId',actor.session_id,'dueDate',to_char(p_due_date,'DD Mon YYYY'),
  'assignedDate',coalesce(prior.payload->>'assignedDate',to_char(now() at time zone 'Asia/Kolkata','DD Mon YYYY')),
  'attachmentUrl',ref,'attachmentName',coalesce(document.name,''),'attachmentType',coalesce(document.mime_type,''));
 if prior.id is not null and prior.version=p_expected_version+1 and prior.payload=clean
  and prior.publication_state=desired and prior.closes_at=deadline then return to_jsonb(prior); end if;
 if coalesce(prior.version,0)<>p_expected_version then raise exception 'Assignment changed; reload before retrying' using errcode='23514'; end if;
 insert into public.assignments(id,school_id,session_id,section_id,payload,closes_at,publication_state,version,created_by,published_at)
 values(p_id,actor.school_id,actor.session_id,sec.id,clean,deadline,desired,p_expected_version+1,coalesce(prior.created_by,actor.id),
  case when p_publish then coalesce(prior.published_at,now()) else prior.published_at end)
 on conflict(id) do update set payload=excluded.payload,closes_at=excluded.closes_at,publication_state=excluded.publication_state,
  version=excluded.version,created_by=excluded.created_by,published_at=excluded.published_at returning * into saved;
 return to_jsonb(saved);
end $$;
revoke all on function public.write_assignment(text,text,jsonb,date,boolean,integer) from public,anon;
grant execute on function public.write_assignment(text,text,jsonb,date,boolean,integer) to authenticated;

create function erp_private.assignment_publication_event() returns trigger language plpgsql security definer set search_path='' as $$
begin
 if tg_op='UPDATE' and to_jsonb(new)=to_jsonb(old) then return new; end if;
 insert into public.audit_logs(school_id,session_id,actor_id,action,table_name,record_id)
 values(new.school_id,new.session_id,auth.uid(),tg_op,'assignments',new.id);
 if new.publication_state='PUBLISHED' and (tg_op='INSERT' or old.publication_state='DRAFT') then
  insert into public.notifications(school_id,session_id,recipient_id,title,message,category,actionable_id,event_key)
  select new.school_id,new.session_id,p.id,'New assignment',new.payload->>'title','HOMEWORK',new.id,'assignment:'||new.id||':published'
  from public.profiles p where p.school_id=new.school_id and p.session_id=new.session_id and
  (p.role in ('PRINCIPAL','ADMIN') or exists(select 1 from public.students st where st.section_id=new.section_id
    and (st.profile_id=p.id or exists(select 1 from public.parent_student_links l where l.student_id=st.id and l.parent_id=p.id)))
   or exists(select 1 from public.section_teachers t join public.teachers teacher on teacher.profile_id=t.profile_id
    where t.section_id=new.section_id and t.profile_id=p.id and exists(
     select 1 from jsonb_array_elements_text(coalesce(teacher.payload->'subjects','[]'::jsonb)) subject
     where lower(btrim(subject))=lower(btrim(new.payload->>'subject')))))
  on conflict(school_id,session_id,recipient_id,event_key) where event_key is not null do nothing;
 end if;
 return new;
end $$;
revoke all on function erp_private.assignment_publication_event() from public,anon,authenticated;
drop trigger audit_assignments on public.assignments;
create trigger assignment_publication_event after insert or update on public.assignments for each row execute function erp_private.assignment_publication_event();
create function erp_private.submission_requires_publication() returns trigger language plpgsql security definer set search_path='' as $$
begin
 if not exists(select 1 from public.assignments a where a.id=new.assignment_id and a.publication_state='PUBLISHED') then
  raise exception 'Assignment is not published' using errcode='42501'; end if;
 return new;
end $$;
revoke all on function erp_private.submission_requires_publication() from public,anon,authenticated;
create trigger submission_requires_publication before insert or update on public.assignment_submissions for each row execute function erp_private.submission_requires_publication();

create or replace function erp_private.file_visible(f uuid) returns boolean language sql stable security definer set search_path='' as $$
 select exists(select 1 from public.file_grants g where g.file_id=f and erp_private.context_ok(g.school_id,g.session_id) and
 (erp_private.staff() or
  (g.resource_table in ('assignments','assignment_submissions') and exists(select 1 from public.assignments a
   where a.id=case when g.resource_table='assignments' then g.resource_id else
    (select assignment_id from public.assignment_submissions where id=g.resource_id) end
   and erp_private.assignment_visible(a.id) and (erp_private.teaches_subject(g.section_id,a.payload->>'subject') or
    (a.publication_state='PUBLISHED' and ((g.student_id is null and erp_private.sees_section(g.section_id)) or erp_private.own_student(g.student_id))))))
  or (g.resource_table='marks' and exists(select 1 from public.marks m where m.id=g.resource_id and
   (erp_private.teaches_subject(g.section_id,m.subject) or (m.published and erp_private.own_student(g.student_id)))))
  or (g.resource_table='study_materials' and erp_private.sees_section(g.section_id))))
$$;
drop policy grant_read on public.file_grants;
create policy grant_read on public.file_grants for select to authenticated using(erp_private.context_ok(school_id,session_id) and erp_private.file_visible(file_id));
-- Preserve an already-authorized reference when editing the same resource, without permitting re-sharing.
create or replace function erp_private.link_document() returns trigger language plpgsql security definer set search_path='' as $$
declare field text; ref text; file_row public.file_metadata; fields text[]; unchanged boolean;
begin
 if tg_op='UPDATE' and row(new.id,new.school_id,new.session_id,new.section_id,to_jsonb(new)->>'student_id')
  is distinct from row(old.id,old.school_id,old.session_id,old.section_id,to_jsonb(old)->>'student_id') then
  raise exception 'Document resource identity cannot change' using errcode='23514'; end if;
 fields=case when tg_table_name='marks' then array['checkedPaperUrl','answerKeyUrl'] else array['attachmentUrl'] end;
 foreach field in array fields loop
  ref=coalesce(new.payload->>field,'');
  unchanged=tg_op='UPDATE' and coalesce(old.payload->>field,'')=ref;
  if ref<>'' then
   if left(ref,25)<>'supabase://erp-documents/' then raise exception 'Use a managed private document reference'; end if;
   select * into file_row from public.file_metadata where object_path=substr(ref,26) and school_id=new.school_id and session_id=new.session_id;
   if not found or (not unchanged and file_row.owner_id is distinct from auth.uid() and not erp_private.staff()) then
    raise exception 'Document access denied' using errcode='42501'; end if;
  end if;
  delete from public.file_grants where resource_table=tg_table_name and resource_id=new.id and field_name=field;
  if ref<>'' then
   insert into public.file_grants(file_id,school_id,session_id,section_id,student_id,resource_table,resource_id,field_name)
   values(file_row.id,new.school_id,new.session_id,new.section_id,case when tg_table_name='marks' then to_jsonb(new)->>'student_id' else null end,tg_table_name,new.id,field);
  end if;
 end loop;
 return new;
end $$;
commit;
