-- Revenex isolated demo schema. Apply as the project operator, never from Android.
begin;
create schema if not exists erp_private;
revoke all on schema erp_private from public;
create type public.erp_role as enum ('PRINCIPAL','ADMIN','ACCOUNTANT','TEACHER','CLASS_TEACHER','PARENT','STUDENT','TRANSPORT_MANAGER','LIBRARIAN');
create table public.schools (id text primary key, name text not null);
create table public.academic_sessions (
 id text primary key, school_id text not null references public.schools, name text not null,
 unique(school_id,id));
create table public.profiles (
 id uuid primary key references auth.users on delete cascade,
 school_id text not null references public.schools, session_id text not null,
 role public.erp_role not null, name text not null, email text not null,
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id),
 unique(school_id,session_id,id));
create table public.classes (
 id text primary key, school_id text not null, session_id text not null, grade text not null,
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id),
 unique(school_id,session_id,id), unique(school_id,session_id,grade));
create table public.sections (
 id text primary key, school_id text not null, session_id text not null, class_id text not null, division text not null,
 foreign key(school_id,session_id,class_id) references public.classes(school_id,session_id,id),
 unique(school_id,session_id,id), unique(school_id,session_id,class_id,division));
create table public.section_teachers (
 id text primary key, school_id text not null, session_id text not null, section_id text not null, profile_id uuid not null,
 foreign key(school_id,session_id,section_id) references public.sections(school_id,session_id,id),
 foreign key(school_id,session_id,profile_id) references public.profiles(school_id,session_id,id),
 unique(section_id,profile_id));
create table public.students (
 id text primary key, school_id text not null, session_id text not null, section_id text not null,
 profile_id uuid unique, payload jsonb not null check(jsonb_typeof(payload)='object'),
 foreign key(school_id,session_id,section_id) references public.sections(school_id,session_id,id),
 foreign key(school_id,session_id,profile_id) references public.profiles(school_id,session_id,id),
 unique(school_id,session_id,id), unique(school_id,session_id,section_id,id));
create table public.teachers (
 id text primary key, school_id text not null, session_id text not null, profile_id uuid unique not null, payload jsonb not null,
 foreign key(school_id,session_id,profile_id) references public.profiles(school_id,session_id,id));
create table public.parent_student_links (
 id text primary key, school_id text not null, session_id text not null, parent_id uuid not null, student_id text not null,
 foreign key(school_id,session_id,parent_id) references public.profiles(school_id,session_id,id),
 foreign key(school_id,session_id,student_id) references public.students(school_id,session_id,id), unique(parent_id,student_id));
create table public.subjects (
 id text primary key, school_id text not null, session_id text not null, name text not null,
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id));
create table public.timetables (
 id text primary key, school_id text not null, session_id text not null, section_id text not null,
 payload jsonb not null check(jsonb_typeof(payload)='object'),
 foreign key(school_id,session_id,section_id) references public.sections(school_id,session_id,id));
create table public.assignments (
 id text primary key, school_id text not null, session_id text not null, section_id text not null,
 payload jsonb not null check(jsonb_typeof(payload)='object'),
 foreign key(school_id,session_id,section_id) references public.sections(school_id,session_id,id));
create table public.exams (
 id text primary key, school_id text not null, session_id text not null, section_id text not null,
 payload jsonb not null check(jsonb_typeof(payload)='object'),
 foreign key(school_id,session_id,section_id) references public.sections(school_id,session_id,id));
create table public.study_materials (
 id text primary key, school_id text not null, session_id text not null, section_id text not null,
 payload jsonb not null check(jsonb_typeof(payload)='object'),
 foreign key(school_id,session_id,section_id) references public.sections(school_id,session_id,id));
create table public.attendance (
 id text primary key, school_id text not null, session_id text not null, section_id text not null, student_id text not null,
 attendance_date text not null, status text not null check(status in ('PRESENT','ABSENT','LATE','LEAVE','EXCUSED')),
 payload jsonb not null, marked_by uuid not null default auth.uid() references auth.users,
 foreign key(school_id,session_id,section_id,student_id) references public.students(school_id,session_id,section_id,id),
 unique(student_id,session_id,attendance_date));
create table public.assignment_submissions (
 id text primary key, school_id text not null, session_id text not null, section_id text not null, student_id text not null,
 assignment_id text not null references public.assignments, completed boolean not null default false,
 foreign key(school_id,session_id,section_id,student_id) references public.students(school_id,session_id,section_id,id),
 unique(assignment_id,student_id));
create table public.marks (
 id text primary key, school_id text not null, session_id text not null, section_id text not null, student_id text not null,
 term text not null, subject text not null, obtained integer not null check(obtained>=0), maximum integer not null check(maximum>0),
 published boolean not null default false, payload jsonb not null default '{}',
 check(obtained<=maximum),
 foreign key(school_id,session_id,section_id,student_id) references public.students(school_id,session_id,section_id,id),
 unique(student_id,session_id,term,subject));
create table public.fee_records (
 id text primary key, school_id text not null, session_id text not null, section_id text not null, student_id text not null,
 payload jsonb not null,
 foreign key(school_id,session_id,section_id,student_id) references public.students(school_id,session_id,section_id,id));
create table public.notices (
 id text primary key, school_id text not null, session_id text not null, payload jsonb not null,
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id));
create table public.notifications (
 id uuid primary key default gen_random_uuid(), school_id text not null, session_id text not null,
 recipient_id uuid not null, title text not null, message text not null, category text not null default 'SYSTEM',
 is_read boolean not null default false, created_at timestamptz not null default now(),
 foreign key(school_id,session_id,recipient_id) references public.profiles(school_id,session_id,id));
create table public.audit_logs (
 id uuid primary key default gen_random_uuid(), school_id text not null, session_id text not null,
 actor_id uuid, action text not null, table_name text not null, record_id text not null, created_at timestamptz not null default now(),
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id));
create table public.file_metadata (
 id uuid primary key default gen_random_uuid(), school_id text not null, session_id text not null,
 owner_id uuid not null, object_path text unique not null, name text not null, mime_type text not null,
 size_bytes bigint not null check(size_bytes>0 and size_bytes<=10485760),
 foreign key(school_id,session_id,owner_id) references public.profiles(school_id,session_id,id),
 check(object_path=school_id||'/'||session_id||'/'||owner_id::text||'/'||id::text));
create table public.leave_requests (
 id text primary key, school_id text not null, session_id text not null, owner_id uuid default auth.uid() references auth.users,
 payload jsonb not null,
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id));
create table public.events (
 id text primary key, school_id text not null, session_id text not null, owner_id uuid default auth.uid() references auth.users,
 payload jsonb not null,
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id));
create table public.transport_routes (
 id text primary key, school_id text not null, session_id text not null, owner_id uuid default auth.uid() references auth.users,
 payload jsonb not null,
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id));
create table public.library_books (
 id text primary key, school_id text not null, session_id text not null, owner_id uuid default auth.uid() references auth.users,
 payload jsonb not null,
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id));
create table public.inventory (
 id text primary key, school_id text not null, session_id text not null, owner_id uuid default auth.uid() references auth.users,
 payload jsonb not null,
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id));
-- Helpers cannot be replaced by clients and never trust JWT user_metadata for roles.
create function erp_private.context_ok(s text, a text) returns boolean
language sql stable security definer set search_path='' as $$
 select exists(select 1 from public.profiles p where p.id=auth.uid() and p.school_id=s and p.session_id=a)
$$;
create function erp_private.has_role(roles public.erp_role[]) returns boolean
language sql stable security definer set search_path='' as $$
 select exists(select 1 from public.profiles p where p.id=auth.uid() and p.role=any(roles))
$$;
create function erp_private.staff() returns boolean language sql stable security definer set search_path='' as $$
 select erp_private.has_role(array['PRINCIPAL','ADMIN']::public.erp_role[])
$$;
create function erp_private.own_student(st text) returns boolean language sql stable security definer set search_path='' as $$
 select exists(select 1 from public.students s join public.profiles p on p.id=auth.uid() and p.school_id=s.school_id and p.session_id=s.session_id
 where s.id=st and (s.profile_id=auth.uid() or exists(select 1 from public.parent_student_links l where l.student_id=s.id and l.parent_id=auth.uid())))
$$;
create function erp_private.teaches(sec text) returns boolean language sql stable security definer set search_path='' as $$
 select erp_private.has_role(array['TEACHER','CLASS_TEACHER']::public.erp_role[]) and exists(
 select 1 from public.section_teachers t join public.profiles p on p.id=auth.uid() and p.school_id=t.school_id and p.session_id=t.session_id
 where t.section_id=sec and t.profile_id=auth.uid())
$$;
create function erp_private.sees_section(sec text) returns boolean language sql stable security definer set search_path='' as $$
 select erp_private.staff() or erp_private.teaches(sec) or exists(select 1 from public.students s where s.section_id=sec and erp_private.own_student(s.id))
$$;
revoke all on all functions in schema erp_private from public;
grant usage on schema erp_private to authenticated;
grant execute on all functions in schema erp_private to authenticated;
-- Revoke default API grants, enable RLS before creating any client grants.
alter table public.schools enable row level security;
revoke all on public.schools from anon, authenticated;
alter table public.academic_sessions enable row level security;
revoke all on public.academic_sessions from anon, authenticated;
alter table public.profiles enable row level security;
revoke all on public.profiles from anon, authenticated;
alter table public.classes enable row level security;
revoke all on public.classes from anon, authenticated;
alter table public.sections enable row level security;
revoke all on public.sections from anon, authenticated;
alter table public.section_teachers enable row level security;
revoke all on public.section_teachers from anon, authenticated;
alter table public.students enable row level security;
revoke all on public.students from anon, authenticated;
alter table public.teachers enable row level security;
revoke all on public.teachers from anon, authenticated;
alter table public.parent_student_links enable row level security;
revoke all on public.parent_student_links from anon, authenticated;
alter table public.subjects enable row level security;
revoke all on public.subjects from anon, authenticated;
alter table public.timetables enable row level security;
revoke all on public.timetables from anon, authenticated;
alter table public.attendance enable row level security;
revoke all on public.attendance from anon, authenticated;
alter table public.assignments enable row level security;
revoke all on public.assignments from anon, authenticated;
alter table public.assignment_submissions enable row level security;
revoke all on public.assignment_submissions from anon, authenticated;
alter table public.exams enable row level security;
revoke all on public.exams from anon, authenticated;
alter table public.marks enable row level security;
revoke all on public.marks from anon, authenticated;
alter table public.fee_records enable row level security;
revoke all on public.fee_records from anon, authenticated;
alter table public.notices enable row level security;
revoke all on public.notices from anon, authenticated;
alter table public.notifications enable row level security;
revoke all on public.notifications from anon, authenticated;
alter table public.audit_logs enable row level security;
revoke all on public.audit_logs from anon, authenticated;
alter table public.file_metadata enable row level security;
revoke all on public.file_metadata from anon, authenticated;
alter table public.study_materials enable row level security;
revoke all on public.study_materials from anon, authenticated;
alter table public.leave_requests enable row level security;
revoke all on public.leave_requests from anon, authenticated;
alter table public.events enable row level security;
revoke all on public.events from anon, authenticated;
alter table public.transport_routes enable row level security;
revoke all on public.transport_routes from anon, authenticated;
alter table public.library_books enable row level security;
revoke all on public.library_books from anon, authenticated;
alter table public.inventory enable row level security;
revoke all on public.inventory from anon, authenticated;
create policy school_read on public.schools for select to authenticated using (exists(select 1 from public.profiles p where p.id=auth.uid() and p.school_id=schools.id));
create policy profile_self on public.profiles for select to authenticated using (id=auth.uid());
create policy session_read on public.academic_sessions for select to authenticated using (erp_private.context_ok(school_id,id));
create policy classes_read on public.classes for select to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or exists(select 1 from public.sections sec where sec.class_id=classes.id and erp_private.sees_section(sec.id))));
grant select on public.classes to authenticated;
create policy sections_read on public.sections for select to authenticated using (erp_private.context_ok(school_id,session_id) and erp_private.sees_section(id));
grant select on public.sections to authenticated;
create policy section_teachers_read on public.section_teachers for select to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or profile_id=auth.uid()));
grant select on public.section_teachers to authenticated;
create policy students_read on public.students for select to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id) or erp_private.own_student(id)));
grant select on public.students to authenticated;
create policy teachers_read on public.teachers for select to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or profile_id=auth.uid()));
grant select on public.teachers to authenticated;
create policy parent_student_links_read on public.parent_student_links for select to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or parent_id=auth.uid()));
grant select on public.parent_student_links to authenticated;
create policy subjects_read on public.subjects for select to authenticated using (erp_private.context_ok(school_id,session_id) and true);
grant select on public.subjects to authenticated;
create policy timetables_read on public.timetables for select to authenticated using (erp_private.context_ok(school_id,session_id) and erp_private.sees_section(section_id));
grant select on public.timetables to authenticated;
create policy attendance_read on public.attendance for select to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id) or erp_private.own_student(student_id)));
create policy attendance_insert on public.attendance for insert to authenticated with check (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id)) and marked_by=auth.uid());
create policy attendance_update on public.attendance for update to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id))) with check (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id)));
grant insert, update on public.attendance to authenticated;
grant select on public.attendance to authenticated;
create policy assignments_read on public.assignments for select to authenticated using (erp_private.context_ok(school_id,session_id) and erp_private.sees_section(section_id));
create policy assignments_insert on public.assignments for insert to authenticated with check (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id)));
create policy assignments_update on public.assignments for update to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id))) with check (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id)));
grant insert, update on public.assignments to authenticated;
grant select on public.assignments to authenticated;
create policy assignment_submissions_read on public.assignment_submissions for select to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id) or erp_private.own_student(student_id)));
create policy assignment_submissions_insert on public.assignment_submissions for insert to authenticated with check (erp_private.context_ok(school_id,session_id) and (erp_private.has_role(array['STUDENT']::public.erp_role[]) and erp_private.own_student(student_id) and exists(select 1 from public.assignments a where a.id=assignment_id and a.school_id=assignment_submissions.school_id and a.session_id=assignment_submissions.session_id and a.section_id=assignment_submissions.section_id)));
create policy assignment_submissions_update on public.assignment_submissions for update to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.has_role(array['STUDENT']::public.erp_role[]) and erp_private.own_student(student_id) and exists(select 1 from public.assignments a where a.id=assignment_id and a.school_id=assignment_submissions.school_id and a.session_id=assignment_submissions.session_id and a.section_id=assignment_submissions.section_id))) with check (erp_private.context_ok(school_id,session_id) and (erp_private.has_role(array['STUDENT']::public.erp_role[]) and erp_private.own_student(student_id) and exists(select 1 from public.assignments a where a.id=assignment_id and a.school_id=assignment_submissions.school_id and a.session_id=assignment_submissions.session_id and a.section_id=assignment_submissions.section_id)));
grant insert, update on public.assignment_submissions to authenticated;
grant select on public.assignment_submissions to authenticated;
create policy exams_read on public.exams for select to authenticated using (erp_private.context_ok(school_id,session_id) and erp_private.sees_section(section_id));
create policy exams_insert on public.exams for insert to authenticated with check (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id)));
create policy exams_update on public.exams for update to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id))) with check (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id)));
grant insert, update on public.exams to authenticated;
grant select on public.exams to authenticated;
create policy marks_read on public.marks for select to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id) or (published and erp_private.own_student(student_id))));
create policy marks_insert on public.marks for insert to authenticated with check (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id)));
create policy marks_update on public.marks for update to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id))) with check (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id)));
grant insert, update on public.marks to authenticated;
grant select on public.marks to authenticated;
create policy fee_records_read on public.fee_records for select to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.has_role(array['ACCOUNTANT']::public.erp_role[]) or erp_private.own_student(student_id)));
grant select on public.fee_records to authenticated;
create policy notices_read on public.notices for select to authenticated using (erp_private.context_ok(school_id,session_id) and true);
create policy notices_insert on public.notices for insert to authenticated with check (erp_private.context_ok(school_id,session_id) and erp_private.staff());
create policy notices_update on public.notices for update to authenticated using (erp_private.context_ok(school_id,session_id) and erp_private.staff()) with check (erp_private.context_ok(school_id,session_id) and erp_private.staff());
grant insert, update on public.notices to authenticated;
grant select on public.notices to authenticated;
create policy notifications_read on public.notifications for select to authenticated using (erp_private.context_ok(school_id,session_id) and recipient_id=auth.uid());
grant select on public.notifications to authenticated;
create policy audit_logs_read on public.audit_logs for select to authenticated using (erp_private.context_ok(school_id,session_id) and erp_private.staff());
grant select on public.audit_logs to authenticated;
create policy file_metadata_read on public.file_metadata for select to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or owner_id=auth.uid()));
create policy file_metadata_insert on public.file_metadata for insert to authenticated with check (erp_private.context_ok(school_id,session_id) and owner_id=auth.uid());
grant insert on public.file_metadata to authenticated;
grant select on public.file_metadata to authenticated;
create policy study_materials_read on public.study_materials for select to authenticated using (erp_private.context_ok(school_id,session_id) and erp_private.sees_section(section_id));
create policy study_materials_insert on public.study_materials for insert to authenticated with check (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id)));
create policy study_materials_update on public.study_materials for update to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id))) with check (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.teaches(section_id)));
grant insert, update on public.study_materials to authenticated;
grant select on public.study_materials to authenticated;
create policy leave_requests_read on public.leave_requests for select to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or owner_id=auth.uid()));
create policy leave_requests_insert on public.leave_requests for insert to authenticated with check (erp_private.context_ok(school_id,session_id) and owner_id=auth.uid());
create policy leave_requests_update on public.leave_requests for update to authenticated using (erp_private.context_ok(school_id,session_id) and owner_id=auth.uid()) with check (erp_private.context_ok(school_id,session_id) and owner_id=auth.uid());
grant insert, update on public.leave_requests to authenticated;
grant select on public.leave_requests to authenticated;
create policy events_read on public.events for select to authenticated using (erp_private.context_ok(school_id,session_id) and true);
create policy events_insert on public.events for insert to authenticated with check (erp_private.context_ok(school_id,session_id) and erp_private.staff());
create policy events_update on public.events for update to authenticated using (erp_private.context_ok(school_id,session_id) and erp_private.staff()) with check (erp_private.context_ok(school_id,session_id) and erp_private.staff());
grant insert, update on public.events to authenticated;
grant select on public.events to authenticated;
create policy transport_routes_read on public.transport_routes for select to authenticated using (erp_private.context_ok(school_id,session_id) and (erp_private.staff() or erp_private.has_role(array['TRANSPORT_MANAGER']::public.erp_role[])));
grant select on public.transport_routes to authenticated;
create policy library_books_read on public.library_books for select to authenticated using (erp_private.context_ok(school_id,session_id) and true);
create policy library_books_insert on public.library_books for insert to authenticated with check (erp_private.context_ok(school_id,session_id) and erp_private.staff());
create policy library_books_update on public.library_books for update to authenticated using (erp_private.context_ok(school_id,session_id) and erp_private.staff()) with check (erp_private.context_ok(school_id,session_id) and erp_private.staff());
grant insert, update on public.library_books to authenticated;
grant select on public.library_books to authenticated;
create policy inventory_read on public.inventory for select to authenticated using (erp_private.context_ok(school_id,session_id) and erp_private.staff());
create policy inventory_insert on public.inventory for insert to authenticated with check (erp_private.context_ok(school_id,session_id) and erp_private.staff());
create policy inventory_update on public.inventory for update to authenticated using (erp_private.context_ok(school_id,session_id) and erp_private.staff()) with check (erp_private.context_ok(school_id,session_id) and erp_private.staff());
grant insert, update on public.inventory to authenticated;
grant select on public.inventory to authenticated;
grant select on public.schools,public.academic_sessions,public.profiles to authenticated;
create policy notification_read_update on public.notifications for update to authenticated
 using (erp_private.context_ok(school_id,session_id) and recipient_id=auth.uid())
 with check (erp_private.context_ok(school_id,session_id) and recipient_id=auth.uid());
grant update(is_read) on public.notifications to authenticated;
-- No client fee balance writes or payment API; fees are read-only in this demo.
-- Indexed predicates are also used by Realtime RLS checks.
create index on public.academic_sessions(school_id);
create index on public.profiles(school_id,session_id);
create index on public.classes(school_id,session_id);
create index on public.sections(school_id,session_id);
create index on public.section_teachers(school_id,session_id);
create index on public.students(school_id,session_id);
create index on public.teachers(school_id,session_id);
create index on public.parent_student_links(school_id,session_id);
create index on public.subjects(school_id,session_id);
create index on public.timetables(school_id,session_id);
create index on public.attendance(school_id,session_id);
create index on public.assignments(school_id,session_id);
create index on public.assignment_submissions(school_id,session_id);
create index on public.exams(school_id,session_id);
create index on public.marks(school_id,session_id);
create index on public.fee_records(school_id,session_id);
create index on public.notices(school_id,session_id);
create index on public.notifications(school_id,session_id);
create index on public.audit_logs(school_id,session_id);
create index on public.file_metadata(school_id,session_id);
create index on public.study_materials(school_id,session_id);
create index on public.leave_requests(school_id,session_id);
create index on public.events(school_id,session_id);
create index on public.transport_routes(school_id,session_id);
create index on public.library_books(school_id,session_id);
create index on public.inventory(school_id,session_id);
create index on public.parent_student_links(parent_id,student_id);
create index on public.section_teachers(profile_id,section_id);
create index on public.notifications(recipient_id,created_at desc);
commit;
