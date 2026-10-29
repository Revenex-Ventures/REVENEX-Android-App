-- Operator-only database acceptance, NOT an Android/Auth/Realtime transport test.
-- Uses the existing synthetic school links. All temporary writes roll back.
-- Never replace this with COMMIT, and never run against non-demo student IDs.
begin;
do $$
declare principal_id uuid; teacher_id uuid; parent_id uuid; student_profile uuid; parent_update uuid;
begin
 select id into principal_id from public.profiles where school_id='revenex_demo' and session_id='demo_2026' and role='PRINCIPAL' limit 1;
 select t.profile_id into teacher_id from public.section_teachers t join public.profiles p on p.id=t.profile_id
 where t.school_id='revenex_demo' and t.session_id='demo_2026' and t.section_id='demo_10a' and p.role='TEACHER' limit 1;
 select l.parent_id into parent_id from public.parent_student_links l join public.profiles p on p.id=l.parent_id
 where l.school_id='revenex_demo' and l.session_id='demo_2026' and l.student_id='demo_student' and p.role='PARENT' limit 1;
 select profile_id into student_profile from public.students where id='demo_student' and school_id='revenex_demo' and session_id='demo_2026';
 if principal_id is null or teacher_id is null or parent_id is null or student_profile is null then
  raise exception 'Synthetic teacher/student/parent links are required; no setup was changed'; end if;
 execute 'set local role authenticated';
 perform set_config('request.jwt.claim.sub',teacher_id::text,true);
 perform public.save_draft_mark('demo_student','Rollback verification 20261024','Mathematics',72,100);
 begin
  perform public.set_result_publication('demo_student','Rollback verification 20261024',true);
  raise exception 'Teacher publication was not denied';
 exception when insufficient_privilege then null;
 end;
 perform set_config('request.jwt.claim.sub',parent_id::text,true);
 if exists(select 1 from public.marks where student_id='demo_student' and term='Rollback verification 20261024') then
  raise exception 'Draft leaked to parent'; end if;
 perform set_config('request.jwt.claim.sub',principal_id::text,true);
 perform public.set_result_publication('demo_student','Rollback verification 20261024',true);
 perform public.set_result_publication('demo_student','Rollback verification 20261024',true);
 perform set_config('request.jwt.claim.sub',parent_id::text,true);
 if (select count(*) from public.marks where student_id='demo_student' and term='Rollback verification 20261024')<>1 then
  raise exception 'Published result unavailable to parent'; end if;
 if (select count(*) from public.notifications where category='EXAMS' and message like 'Rollback verification 20261024%')<>1 then
  raise exception 'Parent inbox publication missing or duplicated'; end if;
 select id into parent_update from public.notifications where category='EXAMS' and message like 'Rollback verification 20261024%';
 update public.notifications set is_read=true where id=parent_update;
 perform set_config('request.jwt.claim.sub',student_profile::text,true);
 if (select count(*) from public.notifications where category='EXAMS' and message like 'Rollback verification 20261024%' and not is_read)<>1 then
  raise exception 'Student inbox missing, duplicated or read by parent'; end if;
 if exists(select 1 from public.notifications where id=parent_update) then raise exception 'Recipient inbox isolation failed'; end if;
 execute 'reset role';
end $$;
rollback;
select 'PASS: private draft, publication, replay and recipient read isolation; temporary writes rolled back' as result;
