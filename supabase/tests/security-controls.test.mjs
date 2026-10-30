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
create table storage.objects(id uuid primary key default gen_random_uuid(),bucket_id text,name text);
alter table storage.objects enable row level security;grant usage on schema storage to authenticated;
grant select,insert,delete on storage.objects to authenticated;create publication supabase_realtime;`);
for(const [role,id] of Object.entries(ids))await db.query('insert into auth.users values($1,$2)',[id,role+'@revenex.demo']);
for(const name of (await readdir(new URL('../migrations/',import.meta.url))).filter(f=>f.endsWith('.sql')&&f<'202610300024').sort()){
 if(name.startsWith('202610300023')&&process.env.REVENEX_SECURITY_BASELINE==='1')continue;
 await db.exec(await readFile(new URL('../migrations/'+name,import.meta.url),'utf8'));
}
await db.exec(await readFile(new URL('../seed.sql',import.meta.url),'utf8'));
await db.exec(`insert into schools values('quota_school','Synthetic quota school');
insert into academic_sessions values('quota_session','quota_school','Synthetic quota session');`);
const peers=[];
for(let n=10;n<18;n++){
 const id='00000000-0000-0000-0000-'+String(n).padStart(12,'0');peers.push(id);
 await db.query('insert into auth.users values($1,$2)',[id,'quota'+n+'@revenex.demo']);
 await db.query("insert into profiles(id,school_id,session_id,role,name,email) values($1,'quota_school','quota_session','STUDENT','Synthetic quota actor',$2)",[id,'quota'+n+'@revenex.demo']);
}
// The burst assertions intentionally exercise one fixed-minute window. Avoid
// starting their sub-second requests at the edge of a real clock boundary.
const seconds=new Date().getUTCSeconds();
if(seconds>=55)await new Promise(resolve=>setTimeout(resolve,(61-seconds)*1000));
after(()=>db.close());
async function as(role,sql,args=[],actor=ids.student){
 await db.exec('begin');try{
  await db.exec('set local role '+role);await db.query("select set_config('request.jwt.claim.sub',$1,true)",[actor]);
  const result=await db.query(sql,args);await db.exec('commit');return result.rows;
 }catch(e){await db.exec('rollback');throw e;}
}
const reserve=async(actor,operation)=>((await as('service_role','select reserve_backend_request($1,$2) as allowed',[actor,operation]))[0].allowed);
const clear=()=>db.exec('delete from backend_request_limits;delete from security_events;');

test('each fixed operation budget is enforced without trusting a caller-supplied limit',async()=>{
 for(const [operation,limit] of Object.entries({payment_create:5,payment_verify:15,payment_status:30,document_inspect:5})){
  await clear();for(let n=0;n<limit;n++)assert.equal(await reserve(ids.student,operation),true);
  assert.equal(await reserve(ids.student,operation),false);
  assert.equal((await db.query('select requests from backend_request_limits')).rows[0].requests,limit);
 }
});
test('limits are actor-specific, operation-specific and school-session-scoped',async()=>{
 await clear();for(let n=0;n<5;n++)await reserve(ids.student,'payment_create');
 assert.equal(await reserve(ids.student,'payment_create'),false);
 assert.equal(await reserve(ids.parent,'payment_create'),true);
 assert.equal(await reserve(ids.student,'payment_status'),true);
 assert.equal(await reserve(peers[0],'payment_create'),true);
});
test('school aggregate rejects the 201st request while another school remains available',async()=>{
 await clear();let accepted=0;
 for(const actor of peers)for(let n=0;n<30;n++)if(await reserve(actor,'payment_status'))accepted++;
 assert.equal(accepted,200);assert.equal(await reserve(ids.student,'payment_status'),true);
});
test('old windows do not block a new minute and expired counters are pruned only in their own scope',async()=>{
 await clear();await reserve(ids.student,'payment_create');
 await db.exec("update backend_request_limits set window_start=date_trunc('minute',now())-interval '2 days',requests=5");
 assert.equal(await reserve(ids.student,'payment_create'),true);
 assert.equal((await db.query('select count(*)::int as n from backend_request_limits')).rows[0].n,1);
});
test('anonymous and signed-in clients cannot forge counters, actor identities or security events',async()=>{
 await clear();
 for(const role of ['anon','authenticated'])await assert.rejects(()=>as(role,"select reserve_backend_request($1,'payment_create')",[ids.principal]),e=>e.code==='42501');
 await assert.rejects(()=>as('authenticated',"insert into backend_request_limits(actor_id,school_id,session_id,operation,window_start,requests) values($1,'revenex_demo','demo_2026','payment_create',now(),1)",[ids.student]),e=>e.code==='42501');
 await assert.rejects(()=>as('authenticated',"insert into security_events(school_id,session_id,actor_id,event_code,operation,window_start) values('revenex_demo','demo_2026',$1,'RATE_LIMIT_DENIED','payment_create',now())",[ids.student]),e=>e.code==='42501');
 assert.equal(await reserve('00000000-0000-0000-0000-999999999999','payment_create'),false);
 assert.equal(await reserve(ids.student,'invented_override'),false);
 assert.equal((await db.query('select count(*)::int as n from backend_request_limits')).rows[0].n,0);
});
test('denials produce one sanitized event and leadership reads cannot cross scope',async()=>{
 await clear();for(let n=0;n<10;n++)await reserve(ids.student,'payment_create');
 const events=(await as('authenticated','select * from security_events',[],ids.principal));
 assert.equal(events.length,1);assert.equal(events[0].event_code,'RATE_LIMIT_DENIED');assert.equal(events[0].operation,'payment_create');
 assert.deepEqual((await as('authenticated','select * from security_events',[],ids.student)),[]);
 assert.deepEqual((await as('authenticated','select * from security_events',[],peers[0])),[]);
 const columns=(await db.query("select column_name from information_schema.columns where table_schema='public' and table_name='security_events'")).rows.map(r=>r.column_name);
 assert.ok(!columns.some(c=>/token|password|secret|payload|ip|message/i.test(c)));
});
test('a queued burst cannot exceed the stored budget or duplicate denied events',async()=>{
 await clear();await db.exec('set role service_role');
 let burst;
 try{burst=await Promise.all(Array.from({length:20},()=>db.query("select reserve_backend_request($1,'payment_create') as allowed",[ids.student])));}finally{await db.exec('reset role');}
 assert.equal(burst.filter(r=>r.rows[0].allowed).length,5);
 assert.equal((await db.query('select count(*)::int as n from security_events')).rows[0].n,1);
});
test('protected membership changes are not blocked by quota history and never relabel old events',async()=>{
 await clear();const actor=peers[7];for(let n=0;n<6;n++)await reserve(actor,'payment_create');
 await db.query("update profiles set school_id='revenex_demo',session_id='demo_2026' where id=$1",[actor]);
 assert.equal(await reserve(actor,'payment_create'),true);
 const old=(await db.query('select school_id,session_id from security_events where actor_id=$1',[actor])).rows;
 assert.deepEqual(old,[{school_id:'quota_school',session_id:'quota_session'}]);
 assert.equal((await db.query('select count(*)::int as n from backend_request_limits where actor_id=$1',[actor])).rows[0].n,2);
});
