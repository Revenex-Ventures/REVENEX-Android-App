import {PGlite} from '@electric-sql/pglite';
import {readFile,readdir} from 'node:fs/promises';
import assert from 'node:assert/strict';
import {test,after} from 'node:test';

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
for(const name of (await readdir(new URL('../migrations/',import.meta.url))).filter(f=>f.endsWith('.sql')&&f<'202610300024').sort()){

 await db.exec(await readFile(new URL('../migrations/'+name,import.meta.url),'utf8'));
}
await db.exec(await readFile(new URL('../seed.sql',import.meta.url),'utf8'));

const file='90000000-0000-0000-0000-000000000001';
const path=`revenex_demo/demo_2026/${ids.teacher}/${file}`;
await db.query("insert into file_metadata(id,school_id,session_id,owner_id,object_path,name,mime_type,size_bytes) values($1,'revenex_demo','demo_2026',$2,$3,'Synthetic.pdf','application/pdf',10)",[file,ids.teacher,path]);
await db.query("insert into storage.objects(bucket_id,name) values('erp-documents',$1)",[path]);
const ref='supabase://erp-documents/'+path;
await db.query("select set_config('request.jwt.claim.sub',$1,false)",[ids.teacher]);
await db.query("insert into study_materials(id,school_id,session_id,section_id,payload) values('legacy','revenex_demo','demo_2026','demo_10a',$1::jsonb)",[JSON.stringify({attachmentUrl:ref})]);
await db.exec(await readFile(new URL('../migrations/202610300024_document_inspection.sql',import.meta.url),'utf8'));
after(()=>db.close());
async function as(role,sql,args=[],actor=ids.teacher){await db.exec('begin');try{await db.exec('set local role '+role);await db.query("select set_config('request.jwt.claim.sub',$1,true)",[actor]);const r=await db.query(sql,args);await db.exec('commit');return r.rows;}catch(e){await db.exec('rollback');throw e;}}
const confirm=async()=>{const o=(await db.query('select id,updated_at from storage.objects where name=$1',[path])).rows[0];return as('service_role','select confirm_document_inspection($1,$2,$3,$4,$5)',[file,ids.teacher,o.id,o.updated_at,'a'.repeat(64)]);};
const share=()=>db.query("insert into study_materials(id,school_id,session_id,section_id,payload) values(gen_random_uuid()::text,'revenex_demo','demo_2026','demo_10a',$1::jsonb)",[JSON.stringify({attachmentUrl:ref})]);
test('upgrade preserves historical published references and unknown age',async()=>{
 assert.equal((await db.query("select payload->>'attachmentUrl' as ref from study_materials where id='legacy'")).rows[0].ref,ref);
 assert.equal((await db.query('select staged_at from file_metadata where id=$1',[file])).rows[0].staged_at,null);
 await db.query("update study_materials set payload=payload||$1::jsonb where id='legacy'",[JSON.stringify({title:'Preserved'})]);
});
test('actual storage existence alone does not permit a newly shared reference',async()=>{await assert.rejects(share,e=>e.code==='23514');});
test('clients cannot forge confirmation or invoke the service inspection API',async()=>{
 for(const role of ['anon','authenticated'])await assert.rejects(()=>as(role,'select document_inspection_identity($1,$2)',[file,ids.teacher]),e=>e.code==='42501');
 await assert.rejects(()=>as('authenticated',"insert into file_metadata(id,school_id,session_id,owner_id,object_path,name,mime_type,size_bytes,content_sha256) values(gen_random_uuid(),'revenex_demo','demo_2026',auth.uid(),'x','x','application/pdf',1,$1)",['a'.repeat(64)]),e=>e.code==='42501');
});
test('only the matching owner and current object version can be confirmed',async()=>{
 await assert.rejects(()=>as('service_role','select document_inspection_identity($1,$2)',[file,ids.student]),e=>e.code==='42501');
 await confirm();await share();
});
test('replacement at the same path invalidates inspection until rechecked',async()=>{
 await db.query("update storage.objects set updated_at=updated_at+interval '1 second' where name=$1",[path]);
 await assert.rejects(share,e=>e.code==='23514');await confirm();await share();
 await db.query('delete from storage.objects where name=$1',[path]);
 await db.query("insert into storage.objects(bucket_id,name) values('erp-documents',$1)",[path]);
 await assert.rejects(share,e=>e.code==='23514');
});

test('student submissions require inspection without expanding document ownership',async()=>{
 const id='90000000-0000-0000-0000-000000000009',name=`revenex_demo/demo_2026/${ids.student}/${id}`;
 await as('authenticated',"insert into file_metadata(id,school_id,session_id,owner_id,object_path,name,mime_type,size_bytes) values($1,'revenex_demo','demo_2026',auth.uid(),$2,'Submission.pdf','application/pdf',10)",[id,name],ids.student);
 await as('authenticated',"insert into storage.objects(bucket_id,name) values('erp-documents',$1)",[name],ids.student);
 const submit=()=>as('authenticated',"select submit_assignment('demo_homework',$1,'Synthetic inspection regression',0)",['supabase://erp-documents/'+name],ids.student);
 await assert.rejects(submit,e=>e.code==='23514');
 const o=(await db.query('select id,updated_at from storage.objects where name=$1',[name])).rows[0];
 await as('service_role','select confirm_document_inspection($1,$2,$3,$4,$5)',[id,ids.student,o.id,o.updated_at,'b'.repeat(64)]);
 await submit();
 assert.equal((await db.query('select staged_at is not null as staged from file_metadata where id=$1',[id])).rows[0].staged,true);
});
test('monitoring queries execute read-only against the prepared schema',async()=>{
 await db.exec(await readFile(new URL('../security_monitor_readonly.sql',import.meta.url),'utf8'));
});
