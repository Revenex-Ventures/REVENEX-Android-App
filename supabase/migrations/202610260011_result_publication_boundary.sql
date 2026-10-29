begin;
-- Tighten existing permissions; no rows, profiles, grants or result values are changed.
-- Teacher draft access never implies final publication authority.
create function erp_private.guard_mark_write() returns trigger
language plpgsql security invoker set search_path='' as $$
begin
 perform pg_advisory_xact_lock(hashtextextended(new.student_id||':'||new.session_id||':'||new.term,0));
 if tg_op='UPDATE' and row(new.id,new.school_id,new.session_id,new.section_id,new.student_id,new.term,new.subject)
  is distinct from row(old.id,old.school_id,old.session_id,old.section_id,old.student_id,old.term,old.subject) then
  raise exception 'Mark identity cannot be changed' using errcode='23514';
 end if;
 if new.published and (tg_op='INSERT' or not old.published) and not erp_private.staff() then
  raise exception 'Result publication requires school approval' using errcode='42501';
 end if;
 if tg_op='UPDATE' and old.published and not new.published and not erp_private.staff() then
  raise exception 'Result unpublication requires school approval' using errcode='42501';
 end if;
 if (tg_op='INSERT' or row(new.obtained,new.maximum,new.payload) is distinct from row(old.obtained,old.maximum,old.payload))
  and (new.published or exists(select 1 from public.marks m where m.student_id=new.student_id
   and m.session_id=new.session_id and m.term=new.term and m.published and m.id<>new.id)) then
  raise exception 'Unpublish the term before editing' using errcode='23514';
 end if;
 return new;
end $$;
revoke all on function erp_private.guard_mark_write() from public,anon,authenticated;
create trigger mark_write_boundary before insert or update on public.marks
 for each row execute function erp_private.guard_mark_write();

create or replace function public.set_result_publication(p_student text,p_term text,p_published boolean)
returns void language plpgsql security invoker set search_path='' as $$
declare st public.students; changed int;
begin
 select * into st from public.students where id=p_student;
 if not found or not erp_private.staff() then
  raise exception 'Result publication requires school approval' using errcode='42501'; end if;
 if p_published is null or p_term is null or length(btrim(p_term)) not between 1 and 80 then
  raise exception 'Invalid publication' using errcode='23514'; end if;
 perform pg_advisory_xact_lock(hashtextextended(st.id||':'||st.session_id||':'||btrim(p_term),0));
 update public.marks set published=p_published
 where student_id=st.id and school_id=st.school_id and session_id=st.session_id and term=btrim(p_term);
 get diagnostics changed=row_count;
 if changed=0 then raise exception 'No marks to publish' using errcode='23514'; end if;
end $$;
revoke all on function public.set_result_publication(text,text,boolean) from public,anon;
grant execute on function public.set_result_publication(text,text,boolean) to authenticated;
commit;
