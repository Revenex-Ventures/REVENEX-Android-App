begin;
-- Database events create recipient-owned inbox rows. No device push or external service.
create function erp_private.capture_change() returns trigger language plpgsql security definer set search_path='' as $$
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
revoke all on function erp_private.capture_change() from public,authenticated;
create trigger audit_students after insert or update on public.students for each row execute function erp_private.capture_change();
create trigger audit_teachers after insert or update on public.teachers for each row execute function erp_private.capture_change();
create trigger audit_attendance after insert or update on public.attendance for each row execute function erp_private.capture_change();
create trigger audit_assignments after insert or update on public.assignments for each row execute function erp_private.capture_change();
create trigger audit_assignment_submissions after insert or update on public.assignment_submissions for each row execute function erp_private.capture_change();
create trigger audit_marks after insert or update on public.marks for each row execute function erp_private.capture_change();
create trigger audit_notices after insert or update on public.notices for each row execute function erp_private.capture_change();
create trigger audit_file_metadata after insert or update on public.file_metadata for each row execute function erp_private.capture_change();
create trigger audit_study_materials after insert or update on public.study_materials for each row execute function erp_private.capture_change();
create trigger audit_leave_requests after insert or update on public.leave_requests for each row execute function erp_private.capture_change();
create trigger audit_library_books after insert or update on public.library_books for each row execute function erp_private.capture_change();
-- Only intended data is replicated; SELECT policies still apply to every subscriber.
alter publication supabase_realtime add table public.students;
alter publication supabase_realtime add table public.teachers;
alter publication supabase_realtime add table public.classes;
alter publication supabase_realtime add table public.sections;
alter publication supabase_realtime add table public.timetables;
alter publication supabase_realtime add table public.attendance;
alter publication supabase_realtime add table public.assignments;
alter publication supabase_realtime add table public.assignment_submissions;
alter publication supabase_realtime add table public.exams;
alter publication supabase_realtime add table public.marks;
alter publication supabase_realtime add table public.fee_records;
alter publication supabase_realtime add table public.notices;
alter publication supabase_realtime add table public.notifications;
alter publication supabase_realtime add table public.audit_logs;
alter publication supabase_realtime add table public.study_materials;
alter publication supabase_realtime add table public.leave_requests;
alter publication supabase_realtime add table public.events;
alter publication supabase_realtime add table public.transport_routes;
alter publication supabase_realtime add table public.library_books;
alter publication supabase_realtime add table public.inventory;
insert into storage.buckets(id,name,public,file_size_limit,allowed_mime_types)
 values('erp-documents','erp-documents',false,10485760,array['application/pdf','image/jpeg','image/png','application/vnd.openxmlformats-officedocument.wordprocessingml.document'])
 on conflict(id) do update set public=false, file_size_limit=excluded.file_size_limit, allowed_mime_types=excluded.allowed_mime_types;
create policy erp_file_upload on storage.objects for insert to authenticated with check (
 bucket_id='erp-documents' and exists(select 1 from public.file_metadata m
 where m.object_path=storage.objects.name and m.owner_id=auth.uid() and erp_private.context_ok(m.school_id,m.session_id)));
create policy erp_file_read on storage.objects for select to authenticated using (
 bucket_id='erp-documents' and exists(select 1 from public.file_metadata m where m.object_path=storage.objects.name));
-- File visibility comes from metadata RLS: uploader and school administrators only.
-- No public buckets, overwrites, anonymous reads, or shared-secret download tokens.
commit;
