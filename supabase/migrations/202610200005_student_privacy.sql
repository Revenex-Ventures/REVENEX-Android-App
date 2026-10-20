begin;
-- Upgrade earlier demo installs to the no-op-safe notification trigger as well.
create or replace function erp_private.capture_change() returns trigger language plpgsql security definer set search_path='' as $$
begin
 -- Retried/no-op saves must not spam the inbox or manufacture audit events.
 if tg_op='UPDATE' and to_jsonb(new)=to_jsonb(old) then return new; end if;
 insert into public.audit_logs(school_id,session_id,actor_id,action,table_name,record_id)
 values(new.school_id,new.session_id,auth.uid(),tg_op,tg_table_name,new.id::text);
 if tg_op='INSERT' and tg_table_name in ('notices','assignments') then
  insert into public.notifications(school_id,session_id,recipient_id,title,message,category)
  select p.school_id,p.session_id,p.id,
   case when tg_table_name='notices' then 'School notice' else 'New assignment' end,
   coalesce(new.payload->>'title','School update'),
   case when tg_table_name='notices' then 'ANNOUNCEMENTS' else 'HOMEWORK' end
  from public.profiles p where p.school_id=new.school_id and p.session_id=new.session_id
  and (tg_table_name='notices' or p.role in ('PRINCIPAL','ADMIN') or exists(
   select 1 from public.students st where st.section_id=(to_jsonb(new)->>'section_id')
   and (st.profile_id=p.id or exists(select 1 from public.parent_student_links l where l.student_id=st.id and l.parent_id=p.id)))
   or exists(select 1 from public.section_teachers t where t.section_id=(to_jsonb(new)->>'section_id') and t.profile_id=p.id));
 elsif tg_table_name='attendance' then
  insert into public.notifications(school_id,session_id,recipient_id,title,message,category)
  select p.school_id,p.session_id,p.id,'Attendance updated',new.status||' on '||new.attendance_date,'ATTENDANCE'
  from public.profiles p join public.students st on st.id=new.student_id
  where p.school_id=new.school_id and p.session_id=new.session_id and
  (st.profile_id=p.id or exists(select 1 from public.parent_student_links l where l.student_id=st.id and l.parent_id=p.id));
 end if;
 return new;
end $$;
update storage.buckets set public=false where id='erp-documents';
-- Financial data belongs exclusively to fee_records and its RLS boundary.
-- Legacy UI models are reconstructed client-side from authorized fee records.
create function erp_private.sanitize_student_payload() returns trigger language plpgsql set search_path='' as $$
begin
 new.payload:=new.payload-array['feePendingAmount','feeStatus'];
 return new;
end $$;
revoke all on function erp_private.sanitize_student_payload() from public,authenticated;
create trigger student_payload_boundary before insert or update on public.students for each row execute function erp_private.sanitize_student_payload();
update public.students set payload=payload-array['feePendingAmount','feeStatus'] where payload ?| array['feePendingAmount','feeStatus'];
alter table public.students add constraint student_no_financial_payload check (not(payload ?| array['feePendingAmount','feeStatus']));
commit;
