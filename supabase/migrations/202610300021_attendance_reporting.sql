begin;
-- Explicit reporting dates are operator-managed configuration, never guessed
-- from term names. No existing school/session/mark/publication row is rewritten.
-- Empty term configures the session bounds; named terms narrow those bounds.
create table public.school_reporting_periods (
 school_id text not null, session_id text not null, term text not null,
 starts_on date not null, ends_on date not null,
 primary key(school_id,session_id,term),
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id),
 check(starts_on<=ends_on),check(length(term)<=80)
);
alter table public.school_reporting_periods enable row level security;
revoke all on public.school_reporting_periods from public,anon,authenticated;
grant select on public.school_reporting_periods to authenticated;
create policy reporting_period_read on public.school_reporting_periods for select to authenticated
 using(erp_private.context_ok(school_id,session_id));

-- Invoker permissions preserve every underlying student/attendance RLS policy.
-- The database aggregates all permitted rows before PostgREST paginates results.
create view public.attendance_summaries with (security_invoker=true,security_barrier=true) as
with context as (
 select p.school_id,p.session_id,(now() at time zone 'Asia/Kolkata')::date as today,
  session_period.starts_on,session_period.ends_on
 from public.profiles p left join public.school_reporting_periods session_period
  on session_period.school_id=p.school_id and session_period.session_id=p.session_id and session_period.term=''
 where p.id=auth.uid()
), periods as (
 select school_id,session_id,''::text as term,starts_on,least(ends_on,today) as ends_on,'SESSION_TO_DATE'::text as scope from context
 union all
 select c.school_id,c.session_id,p.term,greatest(c.starts_on,p.starts_on),least(c.ends_on,p.ends_on,c.today),'TERM'
 from context c join public.school_reporting_periods p on p.school_id=c.school_id and p.session_id=c.session_id and p.term<>''
)
select jsonb_build_array(st.id,p.term)::text as id,st.school_id,st.session_id,st.id as student_id,p.term,
 jsonb_build_object('recorded',count(a.id),'attended',count(a.id) filter(where a.status in ('PRESENT','LATE','EXCUSED')),
  'scope',p.scope,'startDate',p.starts_on,'endDate',p.ends_on) as payload
from public.students st join periods p on p.school_id=st.school_id and p.session_id=st.session_id
left join public.attendance a on a.student_id=st.id and a.school_id=st.school_id and a.session_id=st.session_id
 and (p.starts_on is null or a.record_date>=p.starts_on) and a.record_date<=p.ends_on
group by st.id,st.school_id,st.session_id,p.term,p.scope,p.starts_on,p.ends_on;
revoke all on public.attendance_summaries from public,anon,authenticated;
grant select on public.attendance_summaries to authenticated;

create or replace function public.erp_capabilities() returns jsonb language plpgsql security definer set search_path='' as $$
begin
 if not exists(select 1 from public.profiles where id=auth.uid()) then raise exception 'School sign-in required' using errcode='42501'; end if;
 return jsonb_build_object('loaded',true,'schema_version',21,
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
