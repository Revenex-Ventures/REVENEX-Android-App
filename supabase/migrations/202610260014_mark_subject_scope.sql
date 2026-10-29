begin;
-- A class allocation alone does not authorize every subject's private marks.
drop policy marks_read on public.marks;
create policy marks_read on public.marks for select to authenticated using(
 erp_private.context_ok(school_id,session_id) and
 (erp_private.staff() or erp_private.teaches_subject(section_id,subject) or (published and erp_private.own_student(student_id))));
drop policy marks_insert on public.marks;
create policy marks_insert on public.marks for insert to authenticated with check(
 erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches_subject(section_id,subject)));
drop policy marks_update on public.marks;
create policy marks_update on public.marks for update to authenticated using(
 erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches_subject(section_id,subject)))
 with check(erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches_subject(section_id,subject)));

create function erp_private.guard_mark_subject() returns trigger
language plpgsql security definer set search_path='' as $$
begin
 -- Direct operator fixtures/migrations retain normal ownership privileges; clients never get this path.
 if auth.uid() is null and current_setting('role') in ('none','postgres','supabase_admin') then return new; end if;
 if not erp_private.context_ok(new.school_id,new.session_id) or
 not(erp_private.staff() or erp_private.teaches_subject(new.section_id,new.subject)) or
 not exists(select 1 from public.students st where st.id=new.student_id and st.school_id=new.school_id
  and st.session_id=new.session_id and st.section_id=new.section_id) then
  raise exception 'Mark subject access denied' using errcode='42501'; end if;
 perform pg_advisory_xact_lock(hashtextextended(new.student_id||':'||new.session_id||':'||new.term,0));
 -- This check must see published subjects hidden by the teacher's SELECT policy.
 if (tg_op='INSERT' or row(new.obtained,new.maximum,new.payload) is distinct from row(old.obtained,old.maximum,old.payload))
 and exists(select 1 from public.marks m where m.student_id=new.student_id and m.school_id=new.school_id
  and m.session_id=new.session_id and m.term=new.term and m.published) then
  raise exception 'Unpublish the term before editing' using errcode='23514'; end if;
 return new;
end $$;
revoke all on function erp_private.guard_mark_subject() from public,anon,authenticated;
create trigger mark_subject_boundary before insert or update on public.marks
 for each row execute function erp_private.guard_mark_subject();
commit;
