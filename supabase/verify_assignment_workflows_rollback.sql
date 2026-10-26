-- Operator-only synthetic acceptance. Database role simulation, not Android/Auth transport.
-- Requires the existing demo links. Every temporary write is rolled back.
begin;
do $$
declare teacher_id uuid; principal_id uuid; student_id uuid; parent_id uuid;
 work_id text := 'rollback_assignment_'||gen_random_uuid()::text;
 work jsonb; solution jsonb; reviewed jsonb; n integer;
begin
 select profile_id into teacher_id from public.teachers where school_id='revenex_demo' and session_id='demo_2026'
  and exists(select 1 from jsonb_array_elements_text(payload->'subjects') s where s='Mathematics') limit 1;
 select id into principal_id from public.profiles where school_id='revenex_demo' and session_id='demo_2026' and role='PRINCIPAL' limit 1;
 select profile_id into student_id from public.students where id='demo_student' and school_id='revenex_demo' and session_id='demo_2026';
 select l.parent_id into parent_id from public.parent_student_links l where l.student_id='demo_student' and l.school_id='revenex_demo' and l.session_id='demo_2026' limit 1;
 if teacher_id is null or principal_id is null or student_id is null or parent_id is null then
  raise exception 'Existing synthetic accounts/links required; no setup changed'; end if;
 execute 'set local role authenticated';
 perform set_config('request.jwt.claim.sub',teacher_id::text,true);
 work=public.write_assignment(work_id,'demo_10a','{"title":"Rollback-only exercise","subject":"Mathematics","instructions":"Synthetic verification only"}',date '2099-01-01',false,0);
 perform set_config('request.jwt.claim.sub',student_id::text,true);
 if exists(select 1 from public.assignments where id=work_id) then raise exception 'Private draft leaked'; end if;
 begin
  perform public.submit_assignment(work_id,'','Must not submit private work',0);
  raise exception 'Private draft accepted submission';
 exception when insufficient_privilege then null; end;
 perform set_config('request.jwt.claim.sub',teacher_id::text,true);
 work=public.write_assignment(work_id,'demo_10a',work->'payload',date '2099-01-01',true,(work->>'version')::int);
 perform set_config('request.jwt.claim.sub',student_id::text,true);
 if (select count(*) from public.assignments where id=work_id)<>1 then raise exception 'Published work unavailable'; end if;
 select count(*) into n from public.notifications where actionable_id=work_id and event_key like 'assignment:%';
 if n<>1 then raise exception 'Student publication inbox missing or duplicate'; end if;
 solution=public.submit_assignment(work_id,'','My synthetic solution',0);
 if public.submit_assignment(work_id,'','My synthetic solution',0)<>solution then raise exception 'Submission retry changed record'; end if;
 begin
  perform public.submit_assignment(work_id,'','Stale changed answer',0);
  raise exception 'Stale answer overwrote saved work';
 exception when check_violation then null; end;
 perform set_config('request.jwt.claim.sub',parent_id::text,true);
 if (select count(*) from public.assignment_submissions where assignment_id=work_id and note='My synthetic solution')<>1 then
  raise exception 'Linked guardian cannot read child solution'; end if;
 begin
  perform public.review_submission(solution->>'id','Unauthorized guardian review',(solution->>'version')::int);
  raise exception 'Guardian reviewed solution';
 exception when insufficient_privilege then null; end;
 perform set_config('request.jwt.claim.sub',teacher_id::text,true);
 reviewed=public.review_submission(solution->>'id','Checked synthetic solution',(solution->>'version')::int);
 if public.review_submission(solution->>'id','Checked synthetic solution',(solution->>'version')::int)<>reviewed then
  raise exception 'Review retry changed record'; end if;
 perform set_config('request.jwt.claim.sub',student_id::text,true);
 if (select count(*) from public.notifications where actionable_id=work_id and title='Assignment feedback')<>1 then
  raise exception 'Student feedback inbox missing or duplicate'; end if;
 if not exists(select 1 from public.assignment_submissions where assignment_id=work_id and feedback='Checked synthetic solution' and review_status='REVIEWED') then
  raise exception 'Feedback not persisted'; end if;
 execute 'reset role';
end $$;
rollback;
select 'PASS: draft privacy, publication inbox, versioned submission/review retries and guardian scope; all temporary writes rolled back' as result;
