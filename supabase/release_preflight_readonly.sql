-- SELECT-only operator inventory. No role changes, DDL, history repair or data writes.
-- Compare with local schema-stage snapshots. A missing history row is NOT a
-- pending migration when its objects already exist; partial/drifted state is HOLD.
select pg_catalog.jsonb_build_object(
 'server_version',pg_catalog.current_setting('server_version'),
 'synthetic_marker',exists(select 1 from public.students where id='demo_student'
  and school_id='revenex_demo' and session_id='demo_2026'
  and payload->>'address'='Synthetic demonstration record'),
 'tenant_profiles',(select count(*) from public.profiles where school_id='revenex_demo' and session_id='demo_2026'),
 'migration_history_available',pg_catalog.to_regclass('supabase_migrations.schema_migrations') is not null,
 'published_assignments',(select count(*) from public.assignments where publication_state='PUBLISHED'),
 'published_assignments_digest',(select pg_catalog.md5(coalesce(pg_catalog.string_agg(pg_catalog.to_jsonb(a)::text,'' order by id),'')) from public.assignments a where publication_state='PUBLISHED'),
 'published_marks',(select count(*) from public.marks where published),
 'published_marks_digest',(select pg_catalog.md5(coalesce(pg_catalog.string_agg(pg_catalog.to_jsonb(m)::text,'' order by id),'')) from public.marks m where published),
 'private_bucket',(select not public from storage.buckets where id='erp-documents'),
 'storage_updated_at',exists(select 1 from information_schema.columns where table_schema='storage' and table_name='objects' and column_name='updated_at'),
 'relations',(select pg_catalog.jsonb_agg(pg_catalog.jsonb_build_object('schema',n.nspname,'name',c.relname,'kind',c.relkind,'rls',c.relrowsecurity,'forced_rls',c.relforcerowsecurity,'options',c.reloptions) order by n.nspname,c.relname)
  from pg_catalog.pg_class c join pg_catalog.pg_namespace n on n.oid=c.relnamespace where n.nspname='public' and c.relkind in ('r','v')),
 'columns',(select pg_catalog.jsonb_agg(pg_catalog.jsonb_build_object('table',table_name,'column',column_name,'type',udt_name,'nullable',is_nullable,'default_md5',pg_catalog.md5(coalesce(column_default,''))) order by table_name,ordinal_position)
  from information_schema.columns where table_schema='public'),
 'functions',(select pg_catalog.jsonb_agg(pg_catalog.jsonb_build_object('schema',n.nspname,'name',p.proname,'arguments',pg_catalog.pg_get_function_identity_arguments(p.oid),'definer',p.prosecdef,'config_md5',pg_catalog.md5(coalesce(p.proconfig::text,'')),'acl',p.proacl::text,'definition_md5',pg_catalog.md5(pg_catalog.pg_get_functiondef(p.oid))) order by n.nspname,p.proname,p.oid)
  from pg_catalog.pg_proc p join pg_catalog.pg_namespace n on n.oid=p.pronamespace where n.nspname in ('public','erp_private') and p.prokind='f'),
 'policies',(select pg_catalog.jsonb_agg(pg_catalog.jsonb_build_object('schema',schemaname,'table',tablename,'name',policyname,'roles',roles,'command',cmd,'permissive',permissive,'using_md5',pg_catalog.md5(coalesce(qual,'')),'check_md5',pg_catalog.md5(coalesce(with_check,''))) order by schemaname,tablename,policyname)
  from pg_catalog.pg_policies where schemaname='public' or (schemaname='storage' and tablename='objects')),
 'triggers',(select pg_catalog.jsonb_agg(pg_catalog.jsonb_build_object('schema',n.nspname,'table',c.relname,'name',t.tgname,'enabled',t.tgenabled,'definition_md5',pg_catalog.md5(pg_catalog.pg_get_triggerdef(t.oid))) order by n.nspname,c.relname,t.tgname)
  from pg_catalog.pg_trigger t join pg_catalog.pg_class c on c.oid=t.tgrelid join pg_catalog.pg_namespace n on n.oid=c.relnamespace where not t.tgisinternal and n.nspname='public'),
 'constraints',(select pg_catalog.jsonb_agg(pg_catalog.jsonb_build_object('table',c.relname,'name',k.conname,'validated',k.convalidated,'definition_md5',pg_catalog.md5(pg_catalog.pg_get_constraintdef(k.oid))) order by c.relname,k.conname)
  from pg_catalog.pg_constraint k join pg_catalog.pg_class c on c.oid=k.conrelid join pg_catalog.pg_namespace n on n.oid=c.relnamespace where n.nspname='public')
) as release_inventory;
