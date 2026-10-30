import {PGlite} from '@electric-sql/pglite';
import {readFile,readdir} from 'node:fs/promises';
import assert from 'node:assert/strict';
import {test,after} from 'node:test';

const db=new PGlite();
await db.exec(`create role anon;create role authenticated;create role service_role;
create schema auth;create table auth.users(id uuid primary key,email text);
create function auth.uid() returns uuid language sql stable as $$select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid$$;
grant usage on schema auth to authenticated,anon;grant execute on function auth.uid() to authenticated,anon;
create schema storage;create table storage.buckets(id text primary key,name text,public boolean,file_size_limit bigint,allowed_mime_types text[]);
create table storage.objects(id uuid primary key default gen_random_uuid(),bucket_id text,name text);
alter table storage.objects enable row level security;grant usage on schema storage to authenticated;
grant select,insert,delete on storage.objects to authenticated;create publication supabase_realtime;`);
const roles=['principal','teacher','student','parent'];
for(let i=0;i<roles.length;i++)await db.query('insert into auth.users values($1,$2)',[`00000000-0000-0000-0000-00000000000${i+1}`,`${roles[i]}@revenex.demo`]);
for(const file of (await readdir(new URL('../migrations/',import.meta.url))).filter(f=>f.endsWith('.sql')).sort()){
 if(process.env.REVENEX_REPORTING_BASELINE==='1'&&file.startsWith('202610300021'))continue;
 await db.exec(await readFile(new URL('../migrations/'+file,import.meta.url),'utf8'));
}
await db.exec(await readFile(new URL('../seed.sql',import.meta.url),'utf8'));
await db.exec(`select set_config('request.jwt.claim.sub','00000000-0000-0000-0000-000000000002',false);
insert into academic_sessions values('old_session','revenex_demo','Old session');
insert into classes values('old_class','revenex_demo','old_session','10');
insert into sections values('old_section','revenex_demo','old_session','old_class','A');
insert into students(id,school_id,session_id,section_id,payload) values('old_student','revenex_demo','old_session','old_section','{}');
insert into attendance(id,school_id,session_id,section_id,student_id,attendance_date,status,payload)
values('present','revenex_demo','demo_2026','demo_10a','demo_student','2026-09-01','PRESENT','{}'),
('late','revenex_demo','demo_2026','demo_10a','demo_student','2026-10-01','LATE','{}'),
('absent','revenex_demo','demo_2026','demo_10a','demo_student','2026-10-02','ABSENT','{}'),
('leave','revenex_demo','demo_2026','demo_10a','demo_student','2026-10-03','LEAVE','{}'),
('excused','revenex_demo','demo_2026','demo_10a','demo_student','2026-10-04','EXCUSED','{}'),
('future','revenex_demo','demo_2026','demo_10a','demo_student','2099-01-01','ABSENT','{}'),
('old','revenex_demo','old_session','old_section','old_student','2026-10-01','ABSENT','{}');`);
async function as(role,sql,args=[]){
 await db.exec('begin');try{
  await db.exec('set local role authenticated');
  await db.query("select set_config('request.jwt.claim.sub',$1,true)",[`00000000-0000-0000-0000-00000000000${roles.indexOf(role)+1}`]);
  const result=await db.query(sql,args);await db.exec('commit');return result.rows;
 }catch(e){await db.exec('rollback');throw e;}
}
after(()=>db.close());
test('server aggregate counts real sessions, includes leave, excludes other academic sessions and future records',async()=>{
 const rows=await as('student',"select * from attendance_summaries where term=''");
 assert.equal(rows.length,1);assert.equal(rows[0].student_id,'demo_student');
 assert.equal(rows[0].payload.recorded,5);assert.equal(rows[0].payload.attended,3);
});
test('explicit reporting periods use inclusive bounds and do not overwrite the session dashboard aggregate',async()=>{
 await db.exec(`insert into school_reporting_periods(school_id,session_id,term,starts_on,ends_on)
 values('revenex_demo','demo_2026','Term 1','2026-10-01','2026-10-02');`);
 const rows=await as('student',"select * from attendance_summaries order by term");
 assert.equal(rows.length,2);
 const term=rows.find(r=>r.term==='Term 1');assert.equal(term.payload.recorded,2);assert.equal(term.payload.attended,1);
 assert.equal(term.payload.scope,'TERM');assert.equal(term.payload.startDate,'2026-10-01');assert.equal(term.payload.endDate,'2026-10-02');
 assert.equal(rows.find(r=>r.term==='').payload.recorded,5);
});
test('reporting configuration is operator managed and RLS still hides unrelated student totals',async()=>{
 await assert.rejects(()=>as('student',"insert into school_reporting_periods values('revenex_demo','demo_2026','Forged','2026-01-01','2099-01-01')"),e=>e.code==='42501');
 await db.exec(`insert into students(id,school_id,session_id,section_id,payload) values('peer','revenex_demo','demo_2026','demo_10a','{}');`);
 assert.equal((await as('student','select distinct student_id from attendance_summaries')).length,1);
 assert.equal((await as('principal','select distinct student_id from attendance_summaries')).length,2);
});
