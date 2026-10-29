-- Operator-only acceptance on existing synthetic accounts. No Android/Auth transport claim.
-- Refuses a mixed roster; no seeding, deletion, or retained writes.
begin;
do $$
declare teacher_id uuid; principal_id uuid; student_id uuid; parent_id uuid;
 notice_id text='rollback_notice_'||gen_random_uuid()::text;
 leave_id text='rollback_leave_'||gen_random_uuid()::text;
 entry jsonb; saved jsonb; notice jsonb; leave_record jsonb; n integer; caps jsonb;
begin
 select t.profile_id into teacher_id from public.section_teachers t join public.profiles p on p.id=t.profile_id
  where t.section_id='demo_10a' and t.school_id='revenex_demo' and t.session_id='demo_2026' and p.role='TEACHER' limit 1;
 select id into principal_id from public.profiles where school_id='revenex_demo' and session_id='demo_2026' and role='PRINCIPAL' limit 1;
 select profile_id into student_id from public.students where id='demo_student' and school_id='revenex_demo' and session_id='demo_2026';
 select l.parent_id into parent_id from public.parent_student_links l where l.student_id='demo_student' and l.school_id='revenex_demo' and l.session_id='demo_2026' limit 1;
 if teacher_id is null or principal_id is null or student_id is null or parent_id is null then raise exception 'Existing synthetic accounts/links required'; end if;
 if (select count(*) from public.students where section_id='demo_10a' and school_id='revenex_demo' and session_id='demo_2026'
  and coalesce(payload->>'status','ACTIVE')<>'ARCHIVED')<>1 then raise exception 'Single synthetic roster required; no setup changed'; end if;
 execute 'set local role authenticated';
 perform set_config('request.jwt.claim.sub',teacher_id::text,true);
 caps=public.erp_capabilities();
 if not(caps->>'assignments'='true' and caps->>'submissions'='true' and caps->>'marks'='true'
  and caps->>'attendance'='true' and caps->>'targeted_notices'='true' and caps->>'leaves'='true') then raise exception 'Required workflow API missing'; end if;
 entry=jsonb_build_array(jsonb_build_object('student_id','demo_student','status','PRESENT','remark','',
  'expected_version',coalesce((select a.version from public.attendance a where a.student_id='demo_student'
  and a.record_date=(now() at time zone 'Asia/Kolkata')::date and a.session_slot='Morning Roll Call'),0)));
 saved=public.save_roll_call('demo_10a',(now() at time zone 'Asia/Kolkata')::date,'Morning Roll Call',entry);
 if public.save_roll_call('demo_10a',(now() at time zone 'Asia/Kolkata')::date,'Morning Roll Call',entry)<>saved then raise exception 'Attendance retry changed record'; end if;
 begin
  perform public.save_roll_call('demo_10a',(now() at time zone 'Asia/Kolkata')::date,'Morning Roll Call',
   jsonb_build_array(jsonb_build_object('student_id','demo_student','status','ABSENT','expected_version',((saved->0->>'version')::int-1))));
  raise exception 'Stale attendance update accepted';
 exception when check_violation then null; end;
 perform set_config('request.jwt.claim.sub',parent_id::text,true);
 if not exists(select 1 from public.attendance where id=saved->0->>'id' and status='PRESENT') then raise exception 'Guardian attendance read failed'; end if;
 begin
  perform public.save_roll_call('demo_10a',(now() at time zone 'Asia/Kolkata')::date,'Morning Roll Call',entry);
  raise exception 'Guardian changed register';
 exception when insufficient_privilege then null; end;
 perform set_config('request.jwt.claim.sub',principal_id::text,true);
 notice=public.write_notice(notice_id,'{"title":"Rollback staff notice","content":"Synthetic verification only","category":"CIRCULAR"}', 'Teachers',null,0);
 if public.write_notice(notice_id,'{"title":"Rollback staff notice","content":"Synthetic verification only","category":"CIRCULAR"}', 'Teachers',null,0)<>notice then raise exception 'Notice retry changed record'; end if;
 perform set_config('request.jwt.claim.sub',student_id::text,true);
 if exists(select 1 from public.notices where id=notice_id) or exists(select 1 from public.notifications where actionable_id=notice_id) then raise exception 'Staff notice leaked to student'; end if;
 perform set_config('request.jwt.claim.sub',teacher_id::text,true);
 if (select count(*) from public.notifications where actionable_id=notice_id)<>1 then raise exception 'Staff notice inbox missing or duplicate'; end if;
 leave_record=public.request_leave(leave_id,'Medical Leave',date '2099-01-01',date '2099-01-02','Rollback-only synthetic request');
 if public.request_leave(leave_id,'Medical Leave',date '2099-01-01',date '2099-01-02','Rollback-only synthetic request')<>leave_record then raise exception 'Leave retry changed request'; end if;
 perform set_config('request.jwt.claim.sub',principal_id::text,true);
 if (select count(*) from public.notifications where actionable_id=leave_id)<>1 then raise exception 'Leave review inbox missing or duplicate'; end if;
 perform public.decide_leave(leave_id,'APPROVED','Rollback-only synthetic approval');
 perform public.decide_leave(leave_id,'APPROVED','Rollback-only synthetic approval');
 perform set_config('request.jwt.claim.sub',teacher_id::text,true);
 if public.request_leave(leave_id,'Medical Leave',date '2099-01-01',date '2099-01-02','Rollback-only synthetic request')->'payload'->>'status'<>'APPROVED' then
  raise exception 'Leave retry reset an approved request'; end if;
 execute 'reset role';
end $$;
rollback;
select 'PASS: versioned attendance, guardian boundaries, targeted notice inbox and confirmed leave retries/decision; all temporary writes rolled back' as result;
