begin;
-- Applicant identity and day count are never accepted from the Android client.
revoke insert,update on public.leave_requests from authenticated;
create function public.request_leave(p_id text,p_type text,p_start date,p_end date,p_reason text)
returns jsonb language plpgsql security definer set search_path='' as $$
declare actor public.profiles; prior public.leave_requests; saved public.leave_requests; clean jsonb;
begin
 select * into actor from public.profiles where id=auth.uid();
 if not found then raise exception 'School sign-in required' using errcode='42501'; end if;
 if p_id is null or length(p_id) not between 1 and 200 or p_type is null or p_type not in
  ('Medical Leave','Casual Leave','Family Event Leave','Sports Leave','Personal Leave')
  or p_start is null or p_end is null or p_start<date '2000-01-01' or p_end>date '2100-12-31'
  or p_end<p_start or p_end-p_start>365 or p_reason is null or length(btrim(p_reason)) not between 1 and 4000 then
  raise exception 'Invalid leave dates, type or reason' using errcode='23514'; end if;
 perform pg_advisory_xact_lock(hashtextextended('leave:'||p_id,0));
 select * into prior from public.leave_requests where id=p_id for update;
 if prior.id is not null and (prior.owner_id<>actor.id or prior.school_id<>actor.school_id or prior.session_id<>actor.session_id) then
  raise exception 'Leave request unavailable' using errcode='42501'; end if;
 if prior.id is not null then
  if prior.payload->>'leaveType'=p_type and prior.payload->>'startDate'=to_char(p_start,'DD Mon YYYY')
   and prior.payload->>'endDate'=to_char(p_end,'DD Mon YYYY') and prior.payload->>'reason'=btrim(p_reason) then
   return to_jsonb(prior); end if;
  raise exception 'Request already exists with different contents; review it before submitting again' using errcode='23514';
 end if;
 clean=jsonb_build_object('id',p_id,'applicantId',actor.id::text,'applicantName',actor.name,'applicantRole',actor.role::text,
  'classOrDept',case when actor.role='STUDENT' then coalesce((select c.grade||'-'||s.division from public.students st
   join public.sections s on s.id=st.section_id join public.classes c on c.id=s.class_id
   where st.profile_id=actor.id and st.school_id=actor.school_id and st.session_id=actor.session_id limit 1),'Student') else actor.role::text end,
  'leaveType',p_type,'startDate',to_char(p_start,'DD Mon YYYY'),'endDate',to_char(p_end,'DD Mon YYYY'),
  'daysCount',p_end-p_start+1,'reason',btrim(p_reason),'appliedDate',to_char(now() at time zone 'Asia/Kolkata','DD Mon YYYY'),
  'status','PENDING','approverRemark','','schoolId',actor.school_id);
 insert into public.leave_requests(id,school_id,session_id,owner_id,payload)
 values(p_id,actor.school_id,actor.session_id,actor.id,clean) returning * into saved;
 return to_jsonb(saved);
end $$;
revoke all on function public.request_leave(text,text,date,date,text) from public,anon;
grant execute on function public.request_leave(text,text,date,date,text) to authenticated;

drop trigger audit_leave_requests on public.leave_requests;
create function erp_private.leave_inbox_event() returns trigger language plpgsql security definer set search_path='' as $$
begin
 if tg_op='UPDATE' and to_jsonb(new)=to_jsonb(old) then return new; end if;
 insert into public.audit_logs(school_id,session_id,actor_id,action,table_name,record_id)
 values(new.school_id,new.session_id,auth.uid(),tg_op,'leave_requests',new.id);
 if tg_op='INSERT' then
  insert into public.notifications(school_id,session_id,recipient_id,title,message,category,actionable_id,event_key)
  select new.school_id,new.session_id,p.id,'Leave request',new.payload->>'applicantName','LEAVE',new.id,'leave:'||new.id||':requested'
  from public.profiles p where p.school_id=new.school_id and p.session_id=new.session_id and p.role in ('PRINCIPAL','ADMIN')
  on conflict(school_id,session_id,recipient_id,event_key) where event_key is not null do nothing;
 end if;
 return new;
end $$;
revoke all on function erp_private.leave_inbox_event() from public,anon,authenticated;
create trigger leave_inbox_event after insert or update on public.leave_requests for each row execute function erp_private.leave_inbox_event();

create or replace function public.erp_capabilities() returns jsonb language plpgsql security definer set search_path='' as $$
begin
 if not exists(select 1 from public.profiles where id=auth.uid()) then raise exception 'School sign-in required' using errcode='42501'; end if;
 return jsonb_build_object('loaded',true,'schema_version',19,
  'assignments',to_regprocedure('public.write_assignment(text,text,jsonb,date,boolean,integer)') is not null,
  'submissions',to_regprocedure('public.submit_assignment(text,text,text,integer)') is not null and to_regprocedure('public.review_submission(text,text,integer)') is not null,
  'marks',to_regprocedure('public.save_draft_mark(text,text,text,integer,integer,integer)') is not null,
  'targeted_notices',to_regprocedure('public.write_notice(text,jsonb,text,text,integer)') is not null,
  'attendance',to_regprocedure('public.save_roll_call(text,date,text,jsonb)') is not null,
  'leaves',to_regprocedure('public.request_leave(text,text,date,date,text)') is not null);
end $$;
revoke all on function public.erp_capabilities() from public,anon;
grant execute on function public.erp_capabilities() to authenticated;
commit;
