begin;
-- Defaults affect future rows only. Never rewrite historical publication state.
alter table public.assignments alter column publication_state set default 'DRAFT';

-- A metadata row is not proof that an upload completed. Validate new references
-- and newly published references without rewriting or invalidating old records.
create function erp_private.require_stored_documents() returns trigger
language plpgsql security definer set search_path='' as $$
declare field text; ref text; fields text[]; changed boolean; publishing boolean;
begin
 fields=case when tg_table_name='marks' then array['checkedPaperUrl','answerKeyUrl'] else array['attachmentUrl'] end;
 publishing=case when tg_op='INSERT' then true
  when tg_table_name='assignments' then to_jsonb(new)->>'publication_state'='PUBLISHED' and to_jsonb(old)->>'publication_state'<>'PUBLISHED'
  when tg_table_name='marks' then (to_jsonb(new)->>'published')::boolean and not (to_jsonb(old)->>'published')::boolean
  else false end;
 foreach field in array fields loop
  ref=coalesce(new.payload->>field,'');
  changed=tg_op='INSERT' or coalesce(old.payload->>field,'')<>ref;
  if ref<>'' and (changed or publishing) then
   if left(ref,25)<>'supabase://erp-documents/' or not exists(
    select 1 from public.file_metadata f where f.object_path=substr(ref,26)
     and f.school_id=new.school_id and f.session_id=new.session_id) then
    raise exception 'Managed document reference unavailable' using errcode='42501';
   end if;
   -- Retain a row lock until the transaction commits so a concurrent delete
   -- cannot remove the object between existence validation and publication.
   perform 1 from storage.objects o where o.bucket_id='erp-documents' and o.name=substr(ref,26) for key share;
   if not found then raise exception 'Document upload is incomplete; upload the file before sharing' using errcode='23514'; end if;
  end if;
 end loop;
 return new;
end $$;
revoke all on function erp_private.require_stored_documents() from public,anon,authenticated;
create trigger assignment_stored_object before insert or update on public.assignments
 for each row execute function erp_private.require_stored_documents();
create trigger material_stored_object before insert or update on public.study_materials
 for each row execute function erp_private.require_stored_documents();
create trigger mark_stored_object before insert or update on public.marks
 for each row execute function erp_private.require_stored_documents();

create or replace function public.set_result_publication(p_student text,p_term text,p_published boolean)
returns void language plpgsql security definer set search_path='' as $$
declare actor public.profiles; st public.students;
begin
 select * into actor from public.profiles where id=auth.uid();
 if actor.id is null or actor.role not in ('PRINCIPAL','ADMIN') then
  raise exception 'Result publication requires school approval' using errcode='42501'; end if;
 select * into st from public.students where id=p_student and school_id=actor.school_id and session_id=actor.session_id;
 if not found then raise exception 'Student unavailable' using errcode='42501'; end if;
 if p_published is null or p_term is null or length(btrim(p_term)) not between 1 and 80 then
  raise exception 'Invalid publication' using errcode='23514'; end if;
 perform pg_advisory_xact_lock(hashtextextended(st.id||':'||st.session_id||':'||btrim(p_term),0));
 if not exists(select 1 from public.marks where student_id=st.id and school_id=actor.school_id
  and session_id=actor.session_id and term=btrim(p_term)) then
  raise exception 'No marks to publish' using errcode='23514'; end if;
 -- Previously published legacy marks are not retroactively invalidated. Only
 -- a new transition from unpublished to published requires completed review input.
 if p_published and exists(select 1 from public.marks where student_id=st.id and school_id=actor.school_id
  and session_id=actor.session_id and term=btrim(p_term) and not published and review_state<>'SUBMITTED') then
  raise exception 'Submit every unpublished mark for review before publication' using errcode='23514'; end if;
 update public.marks set version=version+1,published=p_published
 where student_id=st.id and school_id=actor.school_id and session_id=actor.session_id
  and term=btrim(p_term) and published is distinct from p_published;
end $$;
revoke all on function public.set_result_publication(text,text,boolean) from public,anon;
grant execute on function public.set_result_publication(text,text,boolean) to authenticated;
commit;
