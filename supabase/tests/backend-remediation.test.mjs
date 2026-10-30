import {PGlite} from '@electric-sql/pglite';
import {readFile,readdir} from 'node:fs/promises';
import assert from 'node:assert/strict';
import {before,after,test} from 'node:test';

// Isolated PostgreSQL role emulation only. This suite never contacts hosting.
const db=new PGlite();
const ids={principal:'00000000-0000-0000-0000-000000000001',teacher:'00000000-0000-0000-0000-000000000002',student:'00000000-0000-0000-0000-000000000003',parent:'00000000-0000-0000-0000-000000000004'};
let historicalAssignment,historicalMark;
async function as(role,sql,params=[]) {
 await db.exec('begin');
 try {
  await db.exec('set local role authenticated');
  await db.query("select set_config('request.jwt.claim.sub',$1,true)",[ids[role]]);
  const result=await db.query(sql,params);await db.exec('commit');return result.rows;
 }catch(error){await db.exec('rollback');throw error;}
}
const assignment=(id,ref='',publish=true,version=0)=>as('teacher',
 "select write_assignment($1,'demo_10a',$2::jsonb,'2099-01-01',$3,$4) as saved",
 [id,JSON.stringify({title:id,subject:'Mathematics',instructions:'Synthetic remediation fixture',attachmentUrl:ref}),publish,version]);
const rejected=operation=>assert.rejects(operation,error=>['42501','23514','40001'].includes(error.code));
async function manifest(id,role='teacher',school='revenex_demo') {
 const path=`${school}/demo_2026/${ids[role]}/${id}`;
 await as(role,"insert into file_metadata(id,school_id,session_id,owner_id,object_path,name,mime_type,size_bytes) values($1,$2,'demo_2026',auth.uid(),$3,'synthetic.pdf','application/pdf',20)",[id,school,path]);
 return {path,ref:'supabase://erp-documents/'+path};
}
before(async()=>{
 await db.exec(`create role anon;create role authenticated;create role service_role;
 create schema auth;create table auth.users(id uuid primary key,email text);
 create function auth.uid() returns uuid language sql stable as $$select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid$$;
 grant usage on schema auth to authenticated,anon;grant execute on function auth.uid() to authenticated,anon;
 create schema storage;create table storage.buckets(id text primary key,name text,public boolean,file_size_limit bigint,allowed_mime_types text[]);
 create table storage.objects(id uuid primary key default gen_random_uuid(),bucket_id text,name text);
 alter table storage.objects enable row level security;
 grant usage on schema storage to authenticated;grant select,insert,delete on storage.objects to authenticated;
 create publication supabase_realtime;`);
 for(const [role,id] of Object.entries(ids))await db.query('insert into auth.users values($1,$2)',[id,`${role}@revenex.demo`]);
 for(const name of (await readdir(new URL('../migrations/',import.meta.url))).filter(name=>name.endsWith('.sql')&&name<'202610300020').sort())
  await db.exec(await readFile(new URL('../migrations/'+name,import.meta.url),'utf8'));
 await db.exec(await readFile(new URL('../seed.sql',import.meta.url),'utf8'));
 const oldFile=await manifest('00000000-0000-0000-0000-000000000020');
 await assignment('historical_published',oldFile.ref);
 const oldMark=(await as('teacher',"select save_draft_mark('demo_student','Historical term','Mathematics',70,100,0) as saved"))[0].saved;
 const oldMarkFile=await manifest('00000000-0000-0000-0000-000000000026');
 await as('teacher',"insert into storage.objects(bucket_id,name) values('erp-documents',$1)",[oldMarkFile.path]);
 await as('teacher',"select attach_mark_document($1,'checkedPaperUrl',$2,$3)",[oldMark.id,oldMarkFile.ref,oldMark.version]);
 await as('principal',"select set_result_publication('demo_student','Historical term',true)");
 await db.query("delete from storage.objects where name=$1",[oldMarkFile.path]);
 historicalAssignment=(await db.query("select to_jsonb(a) as row from assignments a where id='historical_published'")).rows[0].row;
 historicalMark=(await db.query("select to_jsonb(m) as row from marks m where term='Historical term'")).rows[0].row;
 if(process.env.REVENEX_SQL_BASELINE!=='1') {
  for(const filename of ['202610300020_backend_integrity.sql','202610300021_attendance_reporting.sql','202610300022_result_batch_publication.sql'])
   await db.exec(await readFile(new URL('../migrations/'+filename,import.meta.url),'utf8'));
 }
 await db.query("insert into subjects values('remediation_science','revenex_demo','demo_2026','Science') on conflict do nothing");
});
after(()=>db.close());

test('migration preserves historical published assignments and marks byte-for-byte',async()=>{
 assert.deepEqual((await db.query("select to_jsonb(a) as row from assignments a where id='historical_published'")).rows[0].row,historicalAssignment);
 assert.deepEqual((await db.query("select to_jsonb(m) as row from marks m where term='Historical term'")).rows[0].row,historicalMark);
});
test('future assignment inserts default to draft without exposing an implicit publication',async()=>{
 const result=await db.query("insert into assignments(id,school_id,session_id,section_id,payload) values('future_default','revenex_demo','demo_2026','demo_10a','{\"title\":\"Synthetic future draft\",\"subject\":\"Mathematics\"}') returning publication_state");
 assert.equal(result.rows[0].publication_state,'DRAFT');
 assert.equal((await as('student',"select id from assignments where id='future_default'")).length,0);
});
test('metadata without an uploaded object cannot publish an assignment',async()=>{
 const file=await manifest('00000000-0000-0000-0000-000000000021');
 await rejected(()=>assignment('missing_object',file.ref));
 assert.equal((await as('principal',"select id from assignments where id='missing_object'")).length,0);
});
test('an uploaded owned object can publish and its grant remains student-readable',async()=>{
 const file=await manifest('00000000-0000-0000-0000-000000000022');
 await as('teacher',"insert into storage.objects(bucket_id,name) values('erp-documents',$1)",[file.path]);
 const saved=(await assignment('stored_object',file.ref))[0].saved;
 assert.equal(saved.publication_state,'PUBLISHED');
 assert.equal((await as('student',"select name from storage.objects where name=$1",[file.path])).length,1);
});
test('a matching object name in another bucket cannot authorize an attachment',async()=>{
 const file=await manifest('00000000-0000-0000-0000-000000000023');
 await db.query("insert into storage.objects(bucket_id,name) values('foreign-bucket',$1)",[file.path]);
 await rejected(()=>assignment('foreign_bucket',file.ref));
});
test('study-material sharing cannot create a grant for metadata-only files',async()=>{
 const file=await manifest('00000000-0000-0000-0000-000000000024');
 await rejected(()=>as('teacher',"insert into study_materials values('missing_material','revenex_demo','demo_2026','demo_10a',$1::jsonb)",[JSON.stringify({title:'Synthetic missing material',attachmentUrl:file.ref})]));
 assert.equal((await as('principal',"select id from study_materials where id='missing_material'")).length,0);
});
test('publishing an existing draft validates even its unchanged file reference',async()=>{
 const file=await manifest('00000000-0000-0000-0000-000000000025');
 await as('teacher',"insert into storage.objects(bucket_id,name) values('erp-documents',$1)",[file.path]);
 const draft=(await assignment('draft_before_missing',file.ref,false))[0].saved;
 // Operator simulates an interrupted/lost object, without changing the school row.
 await db.query("delete from storage.objects where name=$1",[file.path]);
 await rejected(()=>assignment('draft_before_missing',file.ref,true,draft.version));
 assert.equal((await as('principal',"select publication_state from assignments where id='draft_before_missing'"))[0].publication_state,'DRAFT');
});
async function scope(id,term,required=['Mathematics'],students=['demo_student']) {
 const exam='exam_'+id;
 await db.query("insert into exams(id,school_id,session_id,section_id,payload) values($1,'revenex_demo','demo_2026','demo_10a',$2::jsonb)",[exam,JSON.stringify({term,subjects:required.map(subjectName=>({subjectName,maxMarks:100}))})]);
 await db.query("insert into result_publication_scopes(id,school_id,session_id,section_id,exam_id,term,required_subjects,eligible_student_ids) values($1,'revenex_demo','demo_2026','demo_10a',$2,$3,array(select jsonb_array_elements_text($4::jsonb)),array(select jsonb_array_elements_text($5::jsonb)))",[id,exam,term,JSON.stringify(required),JSON.stringify(students)]);
}
async function observed(id) {
 return (await as('principal',"select m.id,m.version from marks m join result_publication_scopes s on s.id=$1 and m.school_id=s.school_id and m.session_id=s.session_id and m.section_id=s.section_id and m.term=s.term and m.student_id=any(s.eligible_student_ids) and m.subject=any(s.required_subjects) order by m.id",[id]));
}
const publish=async(id,version=1,marks=null,role='principal')=>as(role,"select publish_result_batch($1,$2,$3::jsonb) as saved",[id,version,JSON.stringify(marks??await observed(id))]);
async function draft(term,subject='Mathematics',value=70,student='demo_student') {
 return (await as(subject==='Mathematics'?'teacher':'principal',"select save_draft_mark($1,$2,$3,$4,100,0) as saved",[student,term,subject,value]))[0].saved;
}
async function submit(mark,role='teacher') {
 return (await as(role,"select submit_mark_review($1,$2,$3,$4) as saved",[mark.student_id,mark.term,mark.subject,mark.version]))[0].saved;
}

test('legacy endpoint cannot publish a new draft or silently unpublish history',async()=>{
 await draft('Legacy bypass');
 await rejected(()=>as('principal',"select set_result_publication('demo_student','Legacy bypass',true)"));
 await rejected(()=>as('principal',"select set_result_publication('demo_student','Historical term',false)"));
 assert.deepEqual((await db.query("select to_jsonb(m) as row from marks m where term='Historical term'")).rows[0].row,historicalMark);
});
test('already-published historical rows remain idempotently published without review backfill',async()=>{
 await as('principal',"select set_result_publication('demo_student','Historical term',true)");
 assert.deepEqual((await db.query("select to_jsonb(m) as row from marks m where term='Historical term'")).rows[0].row,historicalMark);
});
test('required subjects derive from protected scope rather than existing marks',async()=>{
 await scope('required_grid','Required grid',['Mathematics','Science']);
 await submit(await draft('Required grid'));
 await rejected(()=>publish('required_grid'));
 assert.equal((await as('student',"select id from marks where term='Required grid'")).length,0);
});
test('zero is a valid submitted mark and differs from a missing required mark',async()=>{
 await scope('zero_grid','Zero grid',['Mathematics','Science']);
 await submit(await draft('Zero grid','Mathematics',0));
 await rejected(()=>publish('zero_grid'));
 await submit(await draft('Zero grid','Science',0),'principal');
 const saved=(await publish('zero_grid'))[0].saved;
 assert.equal(saved.mark_count,2);
 assert.deepEqual((await as('student',"select obtained,published from marks where term='Zero grid' order by subject")).map(m=>[m.obtained,m.published]),[[0,true],[0,true]]);
});
test('an unsubmitted required subject blocks the entire batch atomically',async()=>{
 await scope('review_grid','Review grid',['Mathematics','Science']);
 await submit(await draft('Review grid'));
 const science=await draft('Review grid','Science');
 await rejected(()=>publish('review_grid'));
 assert.equal((await as('student',"select id from marks where term='Review grid'")).length,0);
 await submit(science,'principal');
 await publish('review_grid');
 assert.equal((await as('student',"select id from marks where term='Review grid'")).length,2);
});
test('stale mark and scope versions cannot publish any part of a batch',async()=>{
 await scope('stale_grid','Stale grid');
 await submit(await draft('Stale grid'));
 const marks=await observed('stale_grid');
 await rejected(()=>publish('stale_grid',0,marks));
 await rejected(()=>publish('stale_grid',1,marks.map(m=>({...m,version:m.version-1}))));
 assert.equal((await as('student',"select id from marks where term='Stale grid'")).length,0);
});
test('only leadership can publish a complete batch; clients cannot edit scope configuration',async()=>{
 await scope('staff_grid','Staff grid');await submit(await draft('Staff grid'));
 await rejected(()=>publish('staff_grid',1,null,'teacher'));
 await rejected(()=>publish('staff_grid',1,null,'student'));
 await rejected(()=>as('teacher',"update result_publication_scopes set required_subjects=array['Mathematics'] where id='staff_grid'"));
 await publish('staff_grid');
});
test('publication retries preserve row versions and one recipient event per mark',async()=>{
 await scope('replay_grid','Replay grid');await submit(await draft('Replay grid'));
 const original=await observed('replay_grid');
 await publish('replay_grid',1,original);
 const marks=await observed('replay_grid');
 const events=(await as('student',"select id from notifications where message like 'Replay grid%'"));
 await publish('replay_grid',1,original);
 await publish('replay_grid',2,marks);
 assert.deepEqual(await observed('replay_grid'),marks);
 assert.deepEqual(await as('student',"select id from notifications where message like 'Replay grid%'"),events);
 await rejected(()=>publish('replay_grid',2,original));
});
test('a missing stored mark object aborts new publication without exposing results',async()=>{
 await scope('file_grid','File grid');
 const mark=await draft('File grid');
 const file=await manifest('00000000-0000-0000-0000-000000000027');
 await as('teacher',"insert into storage.objects(bucket_id,name) values('erp-documents',$1)",[file.path]);
 const attached=(await as('teacher',"select attach_mark_document($1,'checkedPaperUrl',$2,$3) as saved",[mark.id,file.ref,mark.version]))[0].saved;
 await submit(attached);await db.query("delete from storage.objects where name=$1",[file.path]);
 await rejected(()=>publish('file_grid'));
 assert.equal((await as('student',"select id from marks where term='File grid'")).length,0);
});
test('operator scope edits advance versions and published scopes cannot be rewritten',async()=>{
 await scope('config_grid','Config grid');
 await db.query("update result_publication_scopes set required_subjects=array['Mathematics','Science'] where id='config_grid'");
 await db.query("update exams set payload=$1::jsonb where id='exam_config_grid'",[JSON.stringify({term:'Config grid',subjects:['Mathematics','Science'].map(subjectName=>({subjectName,maxMarks:100}))})]);
 assert.equal((await as('principal',"select version from result_publication_scopes where id='config_grid'"))[0].version,2);
 await submit(await draft('Config grid'));await submit(await draft('Config grid','Science'),'principal');
 await rejected(()=>publish('config_grid',1));
 await publish('config_grid',2);
 await rejected(()=>db.query("update result_publication_scopes set required_subjects=array['Mathematics'] where id='config_grid'"));
});
test('scope roster must match the current eligible class roster and every pupil needs every subject',async()=>{
 await scope('roster_grid','Roster grid');await submit(await draft('Roster grid'));
 await db.query("insert into students(id,school_id,session_id,section_id,payload) values('scope_peer','revenex_demo','demo_2026','demo_10a','{\"name\":\"Synthetic scope peer\",\"status\":\"ACTIVE\"}')");
 await rejected(()=>publish('roster_grid'));
 await db.query("update result_publication_scopes set eligible_student_ids=array['demo_student','scope_peer'] where id='roster_grid'");
 await rejected(()=>publish('roster_grid',2));
 await submit(await draft('Roster grid','Mathematics',0,'scope_peer'));
 await publish('roster_grid',2);
 assert.equal((await as('principal',"select id from marks where term='Roster grid' and published")).length,2);
});
