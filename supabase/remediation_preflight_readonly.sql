-- Operator READ ONLY. Confirm dashboard project ref matches public Android config
-- before running. No migration, seed, policy update or school-record mutation.
select
 (select count(*) from public.profiles where school_id='revenex_demo' and session_id='demo_2026') as demo_profiles,
 exists(select 1 from public.students where id='demo_student' and school_id='revenex_demo' and session_id='demo_2026'
  and payload->>'address'='Synthetic demonstration record') as seeded_synthetic_marker,
 to_regclass('public.result_publication_scopes') is not null as result_scope_table,
 to_regclass('public.school_reporting_periods') is not null as reporting_period_table,
 to_regclass('public.attendance_summaries') is not null as attendance_summary_view,
 to_regprocedure('public.publish_result_batch(text,integer,jsonb)') is not null as batch_rpc,
 to_regclass('supabase_migrations.schema_migrations') is not null as migration_history_available;

-- Compare these definitions with prepared source; capability/version alone is
-- not deployed-definition parity. Do not include secret stores or school data.
select p.proname,pg_get_function_identity_arguments(p.oid) as arguments,
 md5(pg_get_functiondef(p.oid)) as definition_fingerprint
from pg_proc p join pg_namespace n on n.oid=p.pronamespace
where n.nspname in ('public','erp_private') and p.proname in
 ('publish_result_batch','set_result_publication','write_assignment','require_stored_documents','erp_capabilities')
order by n.nspname,p.proname,arguments;
