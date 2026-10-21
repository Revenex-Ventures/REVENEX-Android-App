begin;
-- Additive only: preserve existing marks, recipients and all RLS policies.
-- A draft must never announce a result. Repeated publication is a no-op.
create function erp_private.notify_published_result() returns trigger
language plpgsql security definer set search_path='' as $$
begin
 if new.published and (tg_op='INSERT' or not old.published) then
  insert into public.notifications(school_id,session_id,recipient_id,title,message,category)
  select p.school_id,p.session_id,p.id,'Result published',
   new.term || ' · ' || new.subject || '. Open Results to view your report.','EXAMS'
  from public.profiles p join public.students st on st.id=new.student_id
  where p.school_id=new.school_id and p.session_id=new.session_id
  and (st.profile_id=p.id or exists(
   select 1 from public.parent_student_links l
   where l.student_id=st.id and l.parent_id=p.id
    and l.school_id=new.school_id and l.session_id=new.session_id));
 end if;
 return new;
end $$;
revoke all on function erp_private.notify_published_result() from public,anon,authenticated;
create trigger notify_published_result after insert or update on public.marks
for each row execute function erp_private.notify_published_result();
commit;
