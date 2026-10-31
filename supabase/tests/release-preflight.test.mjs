import {PGlite} from '@electric-sql/pglite';
import {readFile,readdir} from 'node:fs/promises';
import assert from 'node:assert/strict';
import {test,after} from 'node:test';
import {mkdir,writeFile} from 'node:fs/promises';

// Fresh isolated database only; no hosted connection or provider is used.
const db=new PGlite();
const ids={principal:'00000000-0000-0000-0000-000000000001',teacher:'00000000-0000-0000-0000-000000000002',student:'00000000-0000-0000-0000-000000000003',parent:'00000000-0000-0000-0000-000000000004'};
await db.exec(`create role anon;create role authenticated;create role service_role;
create schema auth;create table auth.users(id uuid primary key,email text);
create function auth.uid() returns uuid language sql stable as $$select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid$$;
grant usage on schema auth to authenticated,anon;grant execute on function auth.uid() to authenticated,anon;
create schema storage;create table storage.buckets(id text primary key,name text,public boolean,file_size_limit bigint,allowed_mime_types text[]);
create table storage.objects(id uuid primary key default gen_random_uuid(),bucket_id text,name text,updated_at timestamptz not null default now());
alter table storage.objects enable row level security;grant usage on schema storage to authenticated;
grant select,insert,delete on storage.objects to authenticated;create publication supabase_realtime;`);
for(const [role,id] of Object.entries(ids))await db.query('insert into auth.users values($1,$2)',[id,role+'@revenex.demo']);

const files=(await readdir(new URL('../migrations/',import.meta.url))).filter(f=>f.endsWith('.sql')).sort();
for(const name of files.filter(f=>f<'202610300020'))await db.exec(await readFile(new URL('../migrations/'+name,import.meta.url),'utf8'));
await db.exec(await readFile(new URL('../seed.sql',import.meta.url),'utf8'));
await db.query("select set_config('request.jwt.claim.sub',$1,false)",[ids.principal]);
await db.exec("select public.save_draft_mark('demo_student','Release historical fixture','Mathematics',50,100,0)");
await db.exec("select public.submit_mark_review('demo_student','Release historical fixture','Mathematics',1)");
await db.exec("select public.set_result_publication('demo_student','Release historical fixture',true)");
const sql=await readFile(new URL('../release_preflight_readonly.sql',import.meta.url),'utf8');
const out=new URL('../../app/build/release/',import.meta.url);await mkdir(out,{recursive:true});
after(()=>db.close());
test('read-only inventory captures stages 19 through 24 and publication/policy preservation',async()=>{
 let before;
 for(let stage=19;stage<=24;stage++){
  if(stage>19){const file=files.find(f=>f.startsWith('2026103000'+stage));assert.ok(file);await db.exec(await readFile(new URL('../migrations/'+file,import.meta.url),'utf8'));}
  await db.exec('begin read only');let state;
  try{state=(await db.query(sql)).rows[0].release_inventory;}finally{await db.exec('rollback');}
  assert.equal(state.synthetic_marker,true);assert.ok(state.published_marks>0);assert.ok(state.published_assignments>0);assert.equal(state.private_bucket,true);assert.equal(state.storage_updated_at,true);
  if(before){assert.equal(state.published_assignments_digest,before.published_assignments_digest);assert.equal(state.published_marks_digest,before.published_marks_digest);
   for(const policy of before.policies)assert.deepEqual(state.policies.find(p=>p.schema===policy.schema&&p.table===policy.table&&p.name===policy.name),policy);
  }
  assert.ok(state.relations.every(r=>r.kind!=='r'||r.rls));
  if(stage>=23)assert.ok(state.functions.some(f=>f.name==='reserve_backend_request'));
  if(stage===24)assert.ok(state.columns.some(c=>c.column==='content_sha256'));
  await writeFile(new URL('schema-stage-'+stage+'.json',out),JSON.stringify(state,null,2));before=state;
 }
});
