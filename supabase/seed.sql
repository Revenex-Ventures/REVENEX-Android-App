-- Run AFTER creating these four users in Authentication > Users, with your own passwords.
-- Addresses are synthetic. No password, service key or privileged token is stored here.
begin;
do $$ begin
 if (select count(*) from auth.users where email in ('principal@revenex.demo','teacher@revenex.demo','student@revenex.demo','parent@revenex.demo'))<>4 then
 raise exception 'Create all four demo Auth accounts in the dashboard first (see supabase/README.md).'; end if;
end $$;
insert into public.schools values('revenex_demo','Revenex Demonstration School') on conflict do nothing;
insert into public.academic_sessions values('demo_2026','revenex_demo','2026–2027') on conflict do nothing;
insert into public.profiles(id,school_id,session_id,role,name,email)
 select id,'revenex_demo','demo_2026',case email
 when 'principal@revenex.demo' then 'PRINCIPAL'::public.erp_role
 when 'teacher@revenex.demo' then 'TEACHER'::public.erp_role
 when 'student@revenex.demo' then 'STUDENT'::public.erp_role
 else 'PARENT'::public.erp_role end,
 case email when 'principal@revenex.demo' then 'Demo Principal' when 'teacher@revenex.demo' then 'Demo Teacher'
 when 'student@revenex.demo' then 'Demo Student' else 'Demo Parent' end,email
 from auth.users where email in ('principal@revenex.demo','teacher@revenex.demo','student@revenex.demo','parent@revenex.demo')
 on conflict do nothing;
insert into public.classes values('demo_class10','revenex_demo','demo_2026','10') on conflict do nothing;
insert into public.sections values('demo_10a','revenex_demo','demo_2026','demo_class10','A') on conflict do nothing;
insert into public.section_teachers select 'demo_teacher_10a','revenex_demo','demo_2026','demo_10a',id from public.profiles where email='teacher@revenex.demo' on conflict do nothing;
insert into public.subjects values('demo_math','revenex_demo','demo_2026','Mathematics') on conflict do nothing;
insert into public.students select 'demo_student','revenex_demo','demo_2026','demo_10a',id,'{"id": "demo_student", "admissionNumber": "DEMO-001", "rollNumber": 1, "name": "Demo Student", "classGrade": "10", "division": "A", "dob": "01 Jan 2011", "gender": "Unspecified", "bloodGroup": "Not recorded", "parentName": "Demo Parent", "parentPhone": "", "parentEmail": "parent@revenex.demo", "address": "Synthetic demonstration record", "admissionDate": "01 Apr 2026", "attendancePercent": 0.0, "feeStatus": "PENDING", "feePendingAmount": 500000, "schoolId": "revenex_demo", "sessionId": "demo_2026"}'::jsonb from public.profiles where email='student@revenex.demo' on conflict do nothing;
insert into public.teachers select 'demo_teacher','revenex_demo','demo_2026',id,'{"id": "demo_teacher", "employeeId": "DEMO-T01", "name": "Demo Teacher", "dob": "", "department": "Mathematics", "designation": "Teacher", "qualification": "Demonstration faculty", "email": "teacher@revenex.demo", "phone": "", "assignedClasses": ["10-A"], "subjects": ["Mathematics"], "weeklyPeriods": 1, "attendancePercent": 0.0, "experienceYears": 0, "joiningDate": "01 Apr 2026", "schoolId": "revenex_demo", "sessionId": "demo_2026"}'::jsonb from public.profiles where email='teacher@revenex.demo' on conflict do nothing;
insert into public.parent_student_links select 'demo_parent_link','revenex_demo','demo_2026',id,'demo_student' from public.profiles where email='parent@revenex.demo' on conflict do nothing;
insert into public.timetables values('demo_period1','revenex_demo','demo_2026','demo_10a','{"dayOfWeek": "Monday", "periodNumber": 1, "startTime": "09:00", "endTime": "09:45", "subject": "Mathematics", "teacherName": "Demo Teacher", "roomNumber": "101", "classGrade": "10", "division": "A", "id": "demo_period1", "schoolId": "revenex_demo", "sessionId": "demo_2026"}'::jsonb) on conflict do nothing;
insert into public.assignments(id,school_id,session_id,section_id,payload,publication_state) values('demo_homework','revenex_demo','demo_2026','demo_10a','{"title": "Fractions practice", "subject": "Mathematics", "classGrade": "10", "division": "A", "teacherName": "Demo Teacher", "assignedDate": "20 Oct 2026", "dueDate": "23 Oct 2026", "instructions": "Complete exercises 1\u20135. Synthetic demo assignment.", "id": "demo_homework", "schoolId": "revenex_demo", "sessionId": "demo_2026"}'::jsonb,'PUBLISHED') on conflict do nothing;
insert into public.notices values('demo_notice','revenex_demo','demo_2026','{"title": "Welcome to the Supabase demo", "content": "All data in this school is synthetic. Payments and device push are disabled.", "category": "CIRCULAR", "publishedDate": "20 Oct 2026", "authorName": "Demo Principal", "authorRole": "Principal", "targetAudience": "All", "id": "demo_notice", "schoolId": "revenex_demo", "sessionId": "demo_2026"}'::jsonb) on conflict do nothing;
insert into public.fee_records values('demo_fee','revenex_demo','demo_2026','demo_10a','demo_student','{"id": "demo_fee", "studentId": "demo_student", "studentName": "Demo Student", "classGrade": "10", "division": "A", "tuitionFee": 500000, "examFee": 0, "transportFee": 0, "labLibraryFee": 0, "discountScholarship": 0, "totalFee": 500000, "paidAmount": 0, "status": "PENDING", "dueDate": "30 Nov 2026", "schoolId": "revenex_demo", "sessionId": "demo_2026"}'::jsonb ) on conflict do nothing;
commit;
