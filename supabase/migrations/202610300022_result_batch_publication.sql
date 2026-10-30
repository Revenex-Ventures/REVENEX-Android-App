begin;
-- No backfill or publication change. An operator must explicitly configure an
-- exam scope before a NEW batch can be published. Existing publications remain valid.
alter table public.exams add constraint exams_scope_identity unique(school_id,session_id,section_id,id);
create table public.result_publication_scopes (
 id text primary key,school_id text not null,session_id text not null,section_id text not null,
 exam_id text not null,term text not null,required_subjects text[] not null,eligible_student_ids text[] not null,
 version integer not null default 1 check(version>0),published_at timestamptz,published_by uuid references auth.users,
 publication_request jsonb,
 unique(school_id,session_id,section_id,term),
 foreign key(school_id,session_id,section_id,exam_id) references public.exams(school_id,session_id,section_id,id),
 check(length(btrim(term)) between 1 and 80),
 check(cardinality(required_subjects) between 1 and 100),
 check(cardinality(eligible_student_ids) between 1 and 10000),
 check(cardinality(required_subjects)*cardinality(eligible_student_ids)<=10000)
);
alter table public.result_publication_scopes enable row level security;
revoke all on public.result_publication_scopes from public,anon,authenticated;
grant select on public.result_publication_scopes to authenticated;
create policy result_scope_read on public.result_publication_scopes for select to authenticated
 using(erp_private.context_ok(school_id,session_id) and erp_private.staff());

create function erp_private.guard_result_scope() returns trigger language plpgsql set search_path='' as $$
begin
 if tg_op='UPDATE' then
  if old.published_at is not null and to_jsonb(new) is distinct from to_jsonb(old) then
   raise exception 'Published scope changes require the unavailable correction/review workflow' using errcode='23514'; end if;
  if row(new.id,new.school_id,new.session_id,new.section_id,new.exam_id,new.term,new.required_subjects,new.eligible_student_ids)
   is distinct from row(old.id,old.school_id,old.session_id,old.section_id,old.exam_id,old.term,old.required_subjects,old.eligible_student_ids) then
   new.version=old.version+1;
  end if;
 end if;
 return new;
end $$;
revoke all on function erp_private.guard_result_scope() from public,anon,authenticated;
create trigger result_scope_guard before update on public.result_publication_scopes for each row execute function erp_private.guard_result_scope();

create function public.publish_result_batch(p_scope text,p_expected_scope_version integer,p_expected_marks jsonb)
returns jsonb language plpgsql security definer set search_path='' as $$
declare actor public.profiles; scope public.result_publication_scopes; exam public.exams;
 roster text[]; subjects text[]; required_subject text; pupil text; expected integer; request jsonb; mark public.marks;
begin
 select * into actor from public.profiles where id=auth.uid();
 if actor.id is null or actor.role not in ('PRINCIPAL','ADMIN') then raise exception 'Leadership publication access required' using errcode='42501'; end if;
 select * into scope from public.result_publication_scopes where id=p_scope and school_id=actor.school_id and session_id=actor.session_id for update;
 if not found then raise exception 'Configure an authorized exam publication scope first' using errcode='42501'; end if;
 if p_expected_scope_version is null or jsonb_typeof(p_expected_marks) is distinct from 'array'
  or jsonb_array_length(p_expected_marks)<>cardinality(scope.required_subjects)*cardinality(scope.eligible_student_ids)
  or exists(select 1 from jsonb_array_elements(p_expected_marks) x where jsonb_typeof(x->'id') is distinct from 'string'
    or coalesce(x->>'version','') !~ '^[1-9][0-9]{0,8}$')
  or (select count(distinct x->>'id') from jsonb_array_elements(p_expected_marks) x)<>jsonb_array_length(p_expected_marks) then
  raise exception 'Complete observed mark identities and versions are required' using errcode='23514'; end if;
 select jsonb_agg(x order by x->>'id') into request from jsonb_array_elements(p_expected_marks) x;
 if scope.published_at is not null and p_expected_scope_version=scope.version-1 and request=scope.publication_request then
  return jsonb_build_object('scope_id',scope.id,'version',scope.version,'published',true,'mark_count',jsonb_array_length(request)); end if;
 if p_expected_scope_version<>scope.version then raise exception 'Publication scope changed; reload the batch' using errcode='40001'; end if;
 perform pg_advisory_xact_lock(hashtextextended('class-roster:'||scope.section_id,0));
 select array_agg(id order by id) into roster from public.students where school_id=actor.school_id and session_id=actor.session_id
  and section_id=scope.section_id and coalesce(payload->>'status','ACTIVE')<>'ARCHIVED';
 if roster is distinct from (select array_agg(x order by x) from unnest(scope.eligible_student_ids) x)
  or cardinality(roster)<>(select count(distinct x) from unnest(scope.eligible_student_ids) x) then
  raise exception 'Eligible roster changed; operator review and a new scope version are required' using errcode='40001'; end if;
 select * into exam from public.exams where id=scope.exam_id for share;
 if exam.payload->>'term' is distinct from scope.term or jsonb_typeof(exam.payload->'subjects') is distinct from 'array'
  or jsonb_array_length(exam.payload->'subjects')=0 then raise exception 'Exam subject policy is not configured' using errcode='23514'; end if;
 select array_agg(x->>'subjectName' order by x->>'subjectName') into subjects from jsonb_array_elements(exam.payload->'subjects') x;
 if subjects is distinct from (select array_agg(x order by x) from unnest(scope.required_subjects) x)
  or cardinality(subjects)<>(select count(distinct lower(btrim(x))) from unnest(subjects) x)
  or exists(select 1 from unnest(subjects) x where not exists(select 1 from public.subjects s
   where s.school_id=actor.school_id and s.session_id=actor.session_id and s.name=x))
  or exists(select 1 from jsonb_array_elements(exam.payload->'subjects') x where coalesce(x->>'maxMarks','') !~ '^[1-9][0-9]{0,3}$') then
  raise exception 'Required subjects and valid exam maxima must be configured independently of marks' using errcode='23514'; end if;
 foreach pupil in array roster loop
  perform pg_advisory_xact_lock(hashtextextended(pupil||':'||scope.session_id||':'||scope.term,0));
 end loop;
 -- The roster/required-subject cross product, not the existing marks, defines completeness.
 foreach pupil in array roster loop
  foreach required_subject in array subjects loop
   select * into mark from public.marks where school_id=scope.school_id and session_id=scope.session_id
    and section_id=scope.section_id and student_id=pupil and term=scope.term and marks.subject=required_subject for update;
   if not found then raise exception 'Required mark missing; zero is valid but missing/absence/exemption needs defined school policy' using errcode='23514'; end if;
   select (x->>'version')::integer into expected from jsonb_array_elements(request) x where x->>'id'=mark.id;
   if expected is null or expected<>mark.version then raise exception 'Mark changed; reload every observed version before publishing' using errcode='40001'; end if;
   if mark.maximum<>(select (x->>'maxMarks')::integer from jsonb_array_elements(exam.payload->'subjects') x where x->>'subjectName'=required_subject)
    or mark.obtained<0 or mark.obtained>mark.maximum then raise exception 'Mark does not match exam scoring policy' using errcode='23514'; end if;
   if coalesce(mark.payload->>'assessmentStatus','SCORED')<>'SCORED' then
    raise exception 'Absence/exemption publication policy is undefined; no result was published' using errcode='23514'; end if;
   if not mark.published and mark.review_state<>'SUBMITTED' then raise exception 'Every required mark must be submitted for review' using errcode='23514'; end if;
  end loop;
 end loop;
 if scope.published_at is null then
  update public.marks m set published=true,version=m.version+1 where m.school_id=scope.school_id and m.session_id=scope.session_id
   and m.section_id=scope.section_id and m.term=scope.term and m.student_id=any(roster) and m.subject=any(subjects) and not m.published;
  update public.result_publication_scopes set published_at=now(),published_by=actor.id,publication_request=request,version=version+1
   where id=scope.id returning * into scope;
 end if;
 return jsonb_build_object('scope_id',scope.id,'version',scope.version,'published',true,'mark_count',jsonb_array_length(request));
end $$;
revoke all on function public.publish_result_batch(text,integer,jsonb) from public,anon;
grant execute on function public.publish_result_batch(text,integer,jsonb) to authenticated;

-- Retain the historical endpoint solely for an authorized no-op on already
-- published records. It cannot bypass batch completeness or unpublish results.
create or replace function public.set_result_publication(p_student text,p_term text,p_published boolean)
returns void language plpgsql security definer set search_path='' as $$
declare actor public.profiles;
begin
 select * into actor from public.profiles where id=auth.uid();
 if actor.id is null or actor.role not in ('PRINCIPAL','ADMIN') or not exists(select 1 from public.students
  where id=p_student and school_id=actor.school_id and session_id=actor.session_id) then raise exception 'Leadership publication access required' using errcode='42501'; end if;
 if p_published is distinct from true then raise exception 'Published result corrections require a reasoned review workflow, which is not configured' using errcode='23514'; end if;
 if p_term is null or not exists(select 1 from public.marks where student_id=p_student and school_id=actor.school_id and session_id=actor.session_id and term=p_term)
  or exists(select 1 from public.marks where student_id=p_student and school_id=actor.school_id and session_id=actor.session_id and term=p_term and not published) then
  raise exception 'Publish the complete configured exam batch with observed versions' using errcode='23514'; end if;
end $$;
revoke all on function public.set_result_publication(text,text,boolean) from public,anon;
grant execute on function public.set_result_publication(text,text,boolean) to authenticated;
-- Keep the existing capability contract, now advertising the batch requirement.
create or replace function public.erp_capabilities() returns jsonb language plpgsql security definer set search_path='' as $$
begin
 if not exists(select 1 from public.profiles where id=auth.uid()) then raise exception 'School sign-in required' using errcode='42501'; end if;
 return jsonb_build_object('loaded',true,'schema_version',22,
  'assignments',to_regprocedure('public.write_assignment(text,text,jsonb,date,boolean,integer)') is not null,
  'submissions',to_regprocedure('public.submit_assignment(text,text,text,integer)') is not null and to_regprocedure('public.review_submission(text,text,integer)') is not null,
  'marks',to_regprocedure('public.save_draft_mark(text,text,text,integer,integer,integer)') is not null,
  'targeted_notices',to_regprocedure('public.write_notice(text,jsonb,text,text,integer)') is not null,
  'attendance',to_regprocedure('public.save_roll_call(text,date,text,jsonb)') is not null,
  'leaves',to_regprocedure('public.request_leave(text,text,date,date,text)') is not null,
  'attendance_summaries',to_regclass('public.attendance_summaries') is not null,
  'result_batches',to_regprocedure('public.publish_result_batch(text,integer,jsonb)') is not null);
end $$;
revoke all on function public.erp_capabilities() from public,anon;
grant execute on function public.erp_capabilities() to authenticated;
commit;
