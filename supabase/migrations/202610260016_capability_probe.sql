begin;
-- Availability metadata only. Every operation still enforces its own backend permissions.
-- Independent of migration15 so a rolling client can use verified assignment APIs on schema14.
create function public.erp_capabilities() returns jsonb language plpgsql security definer set search_path='' as $$
begin
 if not exists(select 1 from public.profiles where id=auth.uid()) then
  raise exception 'School sign-in required' using errcode='42501'; end if;
 return jsonb_build_object('loaded',true,'schema_version',case when to_regprocedure('public.save_draft_mark(text,text,text,integer,integer,integer)') is not null then 15 else 14 end,
  'assignments',to_regprocedure('public.write_assignment(text,text,jsonb,date,boolean,integer)') is not null,
  'submissions',to_regprocedure('public.submit_assignment(text,text,text,integer)') is not null and to_regprocedure('public.review_submission(text,text,integer)') is not null,
  'marks',to_regprocedure('public.save_draft_mark(text,text,text,integer,integer,integer)') is not null,
  'targeted_notices',to_regprocedure('public.write_notice(text,jsonb,text,text,integer)') is not null,
  'attendance',to_regprocedure('public.save_roll_call(text,date,text,jsonb)') is not null);
end $$;
revoke all on function public.erp_capabilities() from public,anon;
grant execute on function public.erp_capabilities() to authenticated;
commit;
