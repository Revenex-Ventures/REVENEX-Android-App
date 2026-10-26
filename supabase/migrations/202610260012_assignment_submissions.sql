begin;
-- A null deadline preserves existing records whose lock was never configured.
alter table public.assignments add column closes_at timestamptz;
alter table public.assignment_submissions
 add column attachment_ref text not null default '',
 add column attachment_name text not null default '',
 add column note text not null default '',
 add column feedback text not null default '',
 add column review_status text not null default 'SUBMITTED' check(review_status in ('SUBMITTED','REVIEWED')),
 add column version integer not null default 0 check(version>=0),
 add column submitted_at timestamptz,
 add column reviewed_at timestamptz,
 add column reviewed_by uuid references auth.users;
alter table public.notifications add column actionable_id text not null default '', add column event_key text;
create unique index notification_event_recipient on public.notifications(school_id,session_id,recipient_id,event_key) where event_key is not null;
create index submissions_assignment_scope on public.assignment_submissions(school_id,session_id,assignment_id,student_id);
alter table public.file_grants drop constraint file_grants_resource_table_check;
alter table public.file_grants add constraint file_grants_resource_table_check check(resource_table in ('assignments','study_materials','marks','assignment_submissions'));
revoke insert,update on public.assignment_submissions from authenticated;

create function public.submit_assignment(p_assignment text,p_attachment text,p_note text,p_expected_version integer)
returns jsonb language plpgsql security definer set search_path='' as $$
declare actor public.profiles; pupil public.students; work public.assignments;
 prior public.assignment_submissions; saved public.assignment_submissions; document public.file_metadata;
begin
 select * into actor from public.profiles where id=auth.uid();
 if not found or actor.role<>'STUDENT' then raise exception 'Student submission access denied' using errcode='42501'; end if;
 select * into pupil from public.students where profile_id=actor.id and school_id=actor.school_id and session_id=actor.session_id;
 if not found then raise exception 'Student link unavailable' using errcode='42501'; end if;
 select * into work from public.assignments where id=p_assignment and school_id=actor.school_id
  and session_id=actor.session_id and section_id=pupil.section_id for share;
 if not found then raise exception 'Assignment unavailable' using errcode='42501'; end if;
 if p_note is null or length(p_note)>8000 or p_attachment is null or length(p_attachment)>500
  or (btrim(p_note)='' and p_attachment='') or p_expected_version is null or p_expected_version<0 or p_expected_version>=2147483647 then
  raise exception 'Add a file or note within the permitted limits' using errcode='23514'; end if;
 perform pg_advisory_xact_lock(hashtextextended(work.id||':'||pupil.id,0));
 select * into prior from public.assignment_submissions where assignment_id=work.id and student_id=pupil.id for update;
 if found and prior.version=p_expected_version+1 and prior.completed and prior.note=btrim(p_note)
  and prior.attachment_ref=p_attachment and prior.review_status='SUBMITTED' then return to_jsonb(prior); end if;
 if coalesce(prior.version,0)<>p_expected_version or prior.review_status='REVIEWED' then
  raise exception 'Submission changed or was reviewed; reload before retrying' using errcode='23514'; end if;
 if work.closes_at is not null and work.closes_at<=now() then
  raise exception 'This assignment is closed for submissions' using errcode='23514'; end if;
 if p_attachment<>'' then
  select * into document from public.file_metadata f where f.object_path=substr(p_attachment,26)
   and p_attachment like 'supabase://erp-documents/%' and f.owner_id=actor.id and f.school_id=actor.school_id and f.session_id=actor.session_id
   and exists(select 1 from storage.objects o where o.bucket_id='erp-documents' and o.name=f.object_path);
  if not found then raise exception 'Submission file unavailable or not owned by this account' using errcode='42501'; end if;
 end if;
 insert into public.assignment_submissions(id,school_id,session_id,section_id,student_id,assignment_id,completed,
  attachment_ref,attachment_name,note,version,submitted_at)
 values(coalesce(prior.id,gen_random_uuid()::text),actor.school_id,actor.session_id,pupil.section_id,pupil.id,work.id,true,
  p_attachment,coalesce(document.name,''),btrim(p_note),p_expected_version+1,now())
 on conflict(assignment_id,student_id) do update set completed=true,attachment_ref=excluded.attachment_ref,
  attachment_name=excluded.attachment_name,note=excluded.note,version=excluded.version,submitted_at=excluded.submitted_at
 returning * into saved;
 return to_jsonb(saved);
end $$;

create function public.review_submission(p_submission text,p_feedback text,p_expected_version integer)
returns jsonb language plpgsql security definer set search_path='' as $$
declare actor public.profiles; saved public.assignment_submissions;
begin
 select * into actor from public.profiles where id=auth.uid();
 if not found or actor.role not in ('PRINCIPAL','ADMIN','TEACHER','CLASS_TEACHER') then
  raise exception 'Submission review access denied' using errcode='42501'; end if;
 select * into saved from public.assignment_submissions where id=p_submission and school_id=actor.school_id and session_id=actor.session_id;
 if not found or not(erp_private.staff() or (erp_private.teaches(saved.section_id) and exists(
  select 1 from public.assignments a join public.teachers t on t.profile_id=actor.id
  and t.school_id=actor.school_id and t.session_id=actor.session_id
  where a.id=saved.assignment_id and exists(select 1 from jsonb_array_elements_text(coalesce(t.payload->'subjects','[]'::jsonb)) subject
   where lower(btrim(subject))=lower(btrim(a.payload->>'subject')))))) then
  raise exception 'Submission review access denied' using errcode='42501'; end if;
 if p_feedback is null or length(btrim(p_feedback)) not between 1 and 4000 or p_expected_version is null or p_expected_version<0 then
  raise exception 'Provide feedback within the permitted limits' using errcode='23514'; end if;
 perform pg_advisory_xact_lock(hashtextextended(saved.assignment_id||':'||saved.student_id,0));
 select * into saved from public.assignment_submissions where id=p_submission for update;
 if saved.review_status='REVIEWED' and saved.version=p_expected_version+1 and saved.feedback=btrim(p_feedback)
  and saved.reviewed_by=actor.id then return to_jsonb(saved); end if;
 if not saved.completed or saved.version<>p_expected_version then
  raise exception 'Submission changed; reload before reviewing' using errcode='23514'; end if;
 update public.assignment_submissions set feedback=btrim(p_feedback),review_status='REVIEWED',reviewed_at=now(),
  reviewed_by=actor.id,version=version+1 where id=saved.id returning * into saved;
 return to_jsonb(saved);
end $$;

create function erp_private.submission_documents_and_inbox() returns trigger
language plpgsql security definer set search_path='' as $$
declare document_id uuid;
begin
 if tg_op='UPDATE' and to_jsonb(new)=to_jsonb(old) then return new; end if;
 delete from public.file_grants where resource_table='assignment_submissions' and resource_id=new.id;
 if new.attachment_ref<>'' then
  select id into document_id from public.file_metadata where object_path=substr(new.attachment_ref,26)
   and school_id=new.school_id and session_id=new.session_id;
  if document_id is null then raise exception 'Submission document unavailable' using errcode='23514'; end if;
  insert into public.file_grants(file_id,school_id,session_id,section_id,student_id,resource_table,resource_id,field_name)
  values(document_id,new.school_id,new.session_id,new.section_id,new.student_id,'assignment_submissions',new.id,'attachment_ref');
 end if;
 if new.completed then
  insert into public.notifications(school_id,session_id,recipient_id,title,message,category,actionable_id,event_key)
  select new.school_id,new.session_id,p.id,
   case when new.review_status='REVIEWED' then 'Assignment feedback' else 'Assignment submitted' end,
   case when new.review_status='REVIEWED' then 'Feedback is available for your submission.' else 'A student submitted class work for review.' end,
   'HOMEWORK',new.assignment_id,'submission:'||new.id||':'||new.version
  from public.profiles p where p.school_id=new.school_id and p.session_id=new.session_id and
   ((new.review_status='REVIEWED' and exists(select 1 from public.students st where st.id=new.student_id and
    (st.profile_id=p.id or exists(select 1 from public.parent_student_links l where l.student_id=st.id and l.parent_id=p.id))))
   or (new.review_status='SUBMITTED' and (p.role in ('PRINCIPAL','ADMIN') or exists(
    select 1 from public.section_teachers t join public.teachers teacher on teacher.profile_id=t.profile_id
     and teacher.school_id=new.school_id and teacher.session_id=new.session_id
    join public.assignments work on work.id=new.assignment_id
    where t.section_id=new.section_id and t.profile_id=p.id and exists(
     select 1 from jsonb_array_elements_text(coalesce(teacher.payload->'subjects','[]'::jsonb)) subject
     where lower(btrim(subject))=lower(btrim(work.payload->>'subject')))))))
  on conflict(school_id,session_id,recipient_id,event_key) where event_key is not null do nothing;
 end if;
 return new;
end $$;
revoke all on function erp_private.submission_documents_and_inbox() from public,anon,authenticated;
create trigger submission_document_inbox after insert or update on public.assignment_submissions
 for each row execute function erp_private.submission_documents_and_inbox();

create or replace function erp_private.file_visible(f uuid) returns boolean language sql stable security definer set search_path='' as $$
 select exists(select 1 from public.file_grants g where g.file_id=f and erp_private.context_ok(g.school_id,g.session_id)
 and (erp_private.staff() or erp_private.teaches(g.section_id) or
 (g.student_id is null and erp_private.sees_section(g.section_id)) or
 (erp_private.own_student(g.student_id) and (g.resource_table='assignment_submissions' or exists(
  select 1 from public.marks m where m.id=g.resource_id and m.published)))))
$$;
-- Owners can clean up only unlinked staging files, never attached records.
create function erp_private.owns_unlinked_file(path text) returns boolean language sql stable security definer set search_path='' as $$
 select exists(select 1 from public.file_metadata f where f.object_path=path and f.owner_id=auth.uid()
 and erp_private.context_ok(f.school_id,f.session_id) and not exists(select 1 from public.file_grants g where g.file_id=f.id))
$$;
revoke all on function erp_private.owns_unlinked_file(text) from public,anon;
grant execute on function erp_private.owns_unlinked_file(text) to authenticated;
create policy erp_unlinked_file_delete on storage.objects for delete to authenticated using(
 bucket_id='erp-documents' and erp_private.owns_unlinked_file(name));
create policy unlinked_metadata_delete on public.file_metadata for delete to authenticated using(
 erp_private.owns_unlinked_file(object_path) and not exists(select 1 from storage.objects o
 where o.bucket_id='erp-documents' and o.name=file_metadata.object_path));
grant delete on public.file_metadata to authenticated;
revoke all on function public.submit_assignment(text,text,text,integer) from public,anon;
revoke all on function public.review_submission(text,text,integer) from public,anon;
grant execute on function public.submit_assignment(text,text,text,integer) to authenticated;
grant execute on function public.review_submission(text,text,integer) to authenticated;
commit;
