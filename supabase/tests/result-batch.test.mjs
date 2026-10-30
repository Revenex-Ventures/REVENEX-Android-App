import {PGlite} from '@electric-sql/pglite';
import {readFile,readdir} from 'node:fs/promises';
import {test,after} from 'node:test';
import assert from 'node:assert/strict';
const db=new PGlite();
const ids={principal:'00000000-0000-0000-0000-000000000001',teacher:'00000000-0000-0000-0000-000000000002',student:'00000000-0000-0000-0000-000000000003',parent:'00000000-0000-0000-0000-000000000004'};
await db.exec(`create role anon;create role authenticated;create role service_role;
create schema auth;create table auth.users(id uuid primary key,email text);
create function auth.uid() returns uuid language sql stable as $$select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid$$;
grant usage on schema auth to authenticated,anon;grant execute on function auth.uid() to authenticated,anon;
create schema storage;create table storage.buckets(id text primary key,name text,public boolean,file_size_limit bigint,allowed_mime_types text[]);
create table storage.objects(id uuid primary key default gen_random_uuid(),bucket_id text,name text);alter table storage.objects enable row level security;
grant usage on schema storage to authenticated;grant select,insert,delete on storage.objects to authenticated;create publication supabase_realtime;`);
for(const [role,id] of Object.entries(ids))await db.query('insert into auth.users values($1,$2)',[id,role+'@revenex.demo']);
const migrations=(await readdir(new URL('../migrations/',import.meta.url))).filter(f=>f.endsWith('.sql')).sort();
for(const f of migrations.filter(f=>f<'202610300022'))await db.exec(await readFile(new URL('../migrations/'+f,import.meta.url),'utf8'));
await db.exec(await readFile(new URL('../seed.sql',import.meta.url),'utf8'));
async function as(role,sql,args=[]){await db.exec('begin');try{
 await db.exec('set local role authenticated');await db.query("select set_config('request.jwt.claim.sub',$1,true)",[ids[role]]);
 const r=await db.query(sql,args);await db.exec('commit');return r.rows;
}catch(e){await db.exec('rollback');throw e;}}
await db.query("select set_config('request.jwt.claim.sub',$1,false)",[ids.principal]);
await as('principal',"select save_draft_mark('demo_student','Historical published','Mathematics',50,100,0)");
await as('principal',"select submit_mark_review('demo_student','Historical published','Mathematics',1)");
await as('principal',"select set_result_publication('demo_student','Historical published',true)");
await db.exec("insert into subjects values('science','revenex_demo','demo_2026','Science')");
const legacy=(await db.query("select to_jsonb(m) as value from marks m where term='Historical published'")).rows[0].value;
if(process.env.REVENEX_BATCH_BASELINE!=='1')await db.exec(await readFile(new URL('../migrations/202610300022_result_batch_publication.sql',import.meta.url),'utf8'));
after(()=>db.close());
async function configure(term,subjects=['Mathematics']){
 const roster=(await db.query("select id from students where school_id='revenex_demo' and session_id='demo_2026' and section_id='demo_10a' and coalesce(payload->>'status','ACTIVE')<>'ARCHIVED' order by id")).rows.map(x=>x.id);
 await db.query("insert into exams values($1,'revenex_demo','demo_2026','demo_10a',$2::jsonb)",['exam_'+term,JSON.stringify({term,subjects:subjects.map(subjectName=>({subjectName,maxMarks:100}))})]);
 await db.query("insert into result_publication_scopes(id,school_id,session_id,section_id,exam_id,term,required_subjects,eligible_student_ids) values($1,'revenex_demo','demo_2026','demo_10a',$2,$3,$4,$5)",['scope_'+term,'exam_'+term,term,subjects,roster]);
 return 'scope_'+term;
}
async function mark(term,subject='Mathematics',obtained=0,submit=true){
 let saved=(await as('principal',"select save_draft_mark('demo_student',$1,$2,$3,100,0) as m",[term,subject,obtained]))[0].m;
 if(submit)saved=(await as('principal',"select submit_mark_review('demo_student',$1,$2,$3) as m",[term,subject,saved.version]))[0].m;
 return saved;
}
async function observed(term){return (await as('principal',"select id,version from marks where term=$1 order by id",[term]));}
const publish=(scope,versions,version=1,role='principal')=>as(role,'select publish_result_batch($1,$2,$3::jsonb) as result',[scope,version,JSON.stringify(versions)]);
const denied=(fn,code='23514')=>assert.rejects(fn,e=>e.code===code);

test('migration and historical no-op publication never alter existing published records',async()=>{
 await as('principal',"select set_result_publication('demo_student','Historical published',true)");
 assert.deepEqual((await db.query("select to_jsonb(m) as value from marks m where term='Historical published'")).rows[0].value,legacy);
 await denied(()=>as('principal',"select set_result_publication('demo_student','Historical published',false)"));
});
test('publication requires the complete expected subject grid; numeric zero is a real mark',async()=>{
 const scope=await configure('Complete',['Mathematics','Science']);const saved=await mark('Complete');
 await denied(()=>publish(scope,[{id:saved.id,version:saved.version},{id:'missing-science',version:1}]));
 assert.equal((await as('student',"select id from marks where term='Complete'")).length,0);
});
test('complete submitted batch publishes atomically and retries do not duplicate inbox events',async()=>{
 await mark('Complete','Science',80);const versions=await observed('Complete');
 const first=(await publish('scope_Complete',versions))[0].result;assert.equal(first.mark_count,2);
 const notices=(await db.query("select count(*)::int as n from notifications where title='Result published'")).rows[0].n;
 assert.deepEqual((await publish('scope_Complete',versions))[0].result,first);
 assert.equal((await db.query("select count(*)::int as n from notifications where title='Result published'")).rows[0].n,notices);
 const rows=await as('student',"select obtained,published from marks where term='Complete' order by subject");
 assert.deepEqual(rows.map(x=>x.obtained),[0,80]);assert.ok(rows.every(x=>x.published));
});
test('stale versions and an unsubmitted mark reject the whole batch',async()=>{
 const scope=await configure('Stale',['Mathematics','Science']);await mark('Stale');await mark('Stale','Science',70,false);
 const versions=await observed('Stale');await denied(()=>publish(scope,versions));
 assert.equal((await as('student',"select id from marks where term='Stale'")).length,0);
 const science=(await as('principal',"select * from marks where term='Stale' and subject='Science'"))[0];
 await as('principal',"select submit_mark_review('demo_student','Stale','Science',$1)",[science.version]);
 await denied(()=>publish(scope,versions),'40001');
 assert.equal((await as('student',"select id from marks where term='Stale'")).length,0);
});
test('ordinary callers cannot publish or configure scopes; legacy API cannot bypass completeness',async()=>{
 const scope=await configure('Permissions');await mark('Permissions');const versions=await observed('Permissions');
 for(const role of ['teacher','student','parent'])await denied(()=>publish(scope,versions,1,role),'42501');
 await denied(()=>as('student',"update result_publication_scopes set version=9"),'42501');
 await denied(()=>as('principal',"select set_result_publication('demo_student','Permissions',true)"));
});
test('editing a submitted unpublished mark requires resubmission',async()=>{
 const scope=await configure('Resubmit');const saved=await mark('Resubmit');
 const edited=(await as('principal',"select save_draft_mark('demo_student','Resubmit','Mathematics',1,100,$1) as m",[saved.version]))[0].m;
 assert.equal(edited.review_state,'DRAFT');await denied(()=>publish(scope,[{id:edited.id,version:edited.version}]));
});
test('undefined absence or exemption policy cannot be represented as an invented score',async()=>{
 const scope=await configure('Exemption');const saved=await mark('Exemption');
 await db.query("update marks set payload=payload||'{\"assessmentStatus\":\"EXEMPT\"}'::jsonb where id=$1",[saved.id]);
 await denied(async()=>publish(scope,await observed('Exemption')));
 assert.equal((await as('student',"select id from marks where term='Exemption'")).length,0);
});
test('exam scoring maxima are authoritative and published marks cannot be silently edited',async()=>{
 const scope=await configure('Maximum');
 const saved=(await as('principal',"select save_draft_mark('demo_student','Maximum','Mathematics',50,90,0) as m"))[0].m;
 await as('principal',"select submit_mark_review('demo_student','Maximum','Mathematics',$1)",[saved.version]);
 await denied(async()=>publish(scope,await observed('Maximum')));
 const published=(await as('principal',"select * from marks where term='Complete' and subject='Mathematics'"))[0];
 await denied(()=>as('principal',"select save_draft_mark('demo_student','Complete','Mathematics',99,100,$1)",[published.version]));
});
test('roster changes and scope reconfiguration invalidate observed publication',async()=>{
 const scope=await configure('Roster');await mark('Roster');const versions=await observed('Roster');
 await db.exec("insert into students(id,school_id,session_id,section_id,payload) values('new_pupil','revenex_demo','demo_2026','demo_10a','{}')");
 await denied(()=>publish(scope,versions),'40001');
 await db.query("update result_publication_scopes set eligible_student_ids=array['demo_student','new_pupil'] where id=$1",[scope]);
 await denied(()=>publish(scope,versions),'23514');
 assert.equal((await db.query('select version from result_publication_scopes where id=$1',[scope])).rows[0].version,2);
});
