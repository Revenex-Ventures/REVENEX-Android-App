-- Read-only operator diagnostic. Run the whole query in Supabase SQL Editor.
-- Share only the booleans/count below. No passwords, tokens, UUIDs or personal records are printed.
-- If public.profiles does not exist, apply migrations 1-5 first, then run again.
select
 exists(select 1 from auth.users where lower(email)='teacher@revenex.demo') as teacher_auth_user_exists,
 exists(select 1 from auth.users where lower(email)='teacher@revenex.demo' and email_confirmed_at is not null) as teacher_email_confirmed,
 to_regclass('public.profiles') is not null as profiles_table_exists,
 (select count(*) from public.profiles where email in ('principal@revenex.demo','teacher@revenex.demo','student@revenex.demo','parent@revenex.demo') and school_id='revenex_demo' and session_id='demo_2026') as provisioned_demo_profile_count,
 to_regclass('public.razorpay_orders') is not null as migration_6_payment_orders,
 to_regprocedure('public.enroll_student(text,text,jsonb,bigint)') is not null as migration_7_enrollment;
