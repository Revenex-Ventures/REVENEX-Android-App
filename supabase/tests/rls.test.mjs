import { PGlite } from '@electric-sql/pglite';
import { readFile, readdir } from 'node:fs/promises';
import assert from 'node:assert/strict';
const db=new PGlite();
let passed=0;
const ids={principal:'00000000-0000-0000-0000-000000000001',teacher:'00000000-0000-0000-0000-000000000002',student:'00000000-0000-0000-0000-000000000003',parent:'00000000-0000-0000-0000-000000000004',other:'00000000-0000-0000-0000-000000000005',old:'00000000-0000-0000-0000-000000000006',admin:'00000000-0000-0000-0000-000000000007',accountant:'00000000-0000-0000-0000-000000000008',class_teacher:'00000000-0000-0000-0000-000000000009',transport_manager:'00000000-0000-0000-0000-000000000010',librarian:'00000000-0000-0000-0000-000000000011'};
await db.exec(`create role anon; create role authenticated; create role service_role;
create schema auth; create table auth.users(id uuid primary key,email text);
create function auth.uid() returns uuid language sql stable as $$ select nullif(current_setting('request.jwt.claim.sub',true),'')::uuid $$;
grant usage on schema auth to authenticated,anon; grant execute on function auth.uid() to authenticated,anon;
create schema storage; create table storage.buckets(id text primary key,name text,public boolean,file_size_limit bigint,allowed_mime_types text[]);
create table storage.objects(id uuid primary key default gen_random_uuid(),bucket_id text,name text);
alter table storage.objects enable row level security;
grant usage on schema storage to authenticated;grant select,insert,delete on storage.objects to authenticated;
create publication supabase_realtime; insert into storage.buckets(id,name,public) values('erp-documents','erp-documents',true);`);
for(const [role,id] of Object.entries(ids)) await db.query('insert into auth.users values($1,$2)',[id,`${role}@revenex.demo`]);
// Exercise legacy contracts first, then verify the strict publication upgrade
// below. Migration22 intentionally removes the old unpublish/per-student API.
for(const f of (await readdir(new URL('../migrations/',import.meta.url))).filter(f=>f.endsWith('.sql')&&f<'202610300022').sort()) {
 await db.exec(await readFile(new URL(`../migrations/${f}`,import.meta.url),'utf8'));
 console.log(`APPLIED ${f}`);
}
await db.exec(await readFile(new URL('../seed.sql',import.meta.url),'utf8'));
for(const role of ['admin','accountant','class_teacher','transport_manager','librarian']) await db.query("insert into profiles(id,school_id,session_id,role,name,email) values($1,'revenex_demo','demo_2026',$2::erp_role,$3,$4)",[ids[role],role.toUpperCase(),role,`${role}@revenex.demo`]);
await db.query("insert into section_teachers values('class_teacher_assignment','revenex_demo','demo_2026','demo_10a',$1)",[ids.class_teacher]);
await db.exec(`insert into schools values('other_school','Other school');
insert into academic_sessions values('other_session','other_school','Other'),('old_session','revenex_demo','Old');
insert into profiles(id,school_id,session_id,role,name,email) values('${ids.other}','other_school','other_session','PRINCIPAL','Other principal','other@revenex.demo'),('${ids.old}','revenex_demo','old_session','STUDENT','Old session','old@revenex.demo');
insert into classes values('other_class','other_school','other_session','10'),('old_class','revenex_demo','old_session','10'),('unassigned_class','revenex_demo','demo_2026','9');
insert into sections values('other_sec','other_school','other_session','other_class','A'),('old_sec','revenex_demo','old_session','old_class','A'),('unassigned_sec','revenex_demo','demo_2026','unassigned_class','B');
insert into students values('other_student','other_school','other_session','other_sec',null,'{}'),('old_student','revenex_demo','old_session','old_sec','${ids.old}','{}'),('unlinked_student','revenex_demo','demo_2026','unassigned_sec',null,'{}');
insert into marks values('mark1','revenex_demo','demo_2026','demo_10a','demo_student','Term 1','Mathematics',80,100,false,'{}');`);
async function as(role,sql,params=[]) {
 await db.exec('begin');
 try {
  await db.exec(`set local role ${role==='anon'?'anon':'authenticated'}`);
  if(role!=='anon') await db.query("select set_config('request.jwt.claim.sub',$1,true)",[ids[role]]);
  const result=await db.query(sql,params);await db.exec('commit');return result.rows;
 }catch(e){await db.exec('rollback');throw e;}
}
async function check(name,fn){await fn();passed++;console.log(`PASS ${name}`);}
async function denied(role,sql,params=[]){await assert.rejects(()=>as(role,sql,params),e=>['42501','23503','23514'].includes(e.code));}
async function saveAssignment(id,fields={},role='teacher') {
 const prior=(await as(role,'select payload,version from assignments where id=$1',[id]))[0];
 const payload={subject:'Mathematics',instructions:'Synthetic school exercise.',...prior?.payload,...fields};
 return (await as(role,"select write_assignment($1,'demo_10a',$2::jsonb,'2099-01-01',true,$3) as result",[id,JSON.stringify(payload),prior?.version??0]))[0].result;
}
async function saveMark(role,term,subject,obtained,maximum) {
 const prior=(await as(role,'select version from marks where student_id=$1 and term=$2 and subject=$3',['demo_student',term,subject]))[0];
 return as(role,'select save_draft_mark($1,$2,$3,$4,$5,$6) as result',['demo_student',term,subject,obtained,maximum,prior?.version??0]);
}
// Positive publication fixtures follow the current submitted-review contract.
async function publishReviewedTerm(term) {
 const drafts=await as('principal',"select subject,version from marks where student_id='demo_student' and term=$1 and not published and review_state='DRAFT'",[term]);
 for(const draft of drafts)await as('principal',"select submit_mark_review('demo_student',$1,$2,$3)",[term,draft.subject,draft.version]);
 return as('principal',"select set_result_publication('demo_student',$1,true)",[term]);
}
await check('existing document bucket is forced private',async()=>assert.equal((await db.query("select public from storage.buckets where id='erp-documents'")).rows[0].public,false));
await check('RLS enabled on every public table',async()=>assert.equal((await db.query("select count(*)::int as n from pg_class c join pg_namespace n on n.oid=c.relnamespace where n.nspname='public' and c.relkind='r' and not c.relrowsecurity")).rows[0].n,0));
await check('anonymous cannot read students',()=>denied('anon','select * from students'));
await check('principal sees own school/session only',async()=>assert.deepEqual((await as('principal','select id from students order by id')).map(x=>x.id),['demo_student','unlinked_student']));
await check('other school cannot read demo students',async()=>assert.deepEqual((await as('other','select id from students')).map(x=>x.id),['other_student']));
await check('old session cannot read current students',async()=>assert.deepEqual((await as('old','select id from students')).map(x=>x.id),['old_student']));
await check('teacher sees assigned section only',async()=>assert.deepEqual((await as('teacher','select id from students')).map(x=>x.id),['demo_student']));
await check('student sees self only',async()=>assert.deepEqual((await as('student','select id from students')).map(x=>x.id),['demo_student']));
await check('parent sees linked child only',async()=>assert.deepEqual((await as('parent','select id from students')).map(x=>x.id),['demo_student']));
await check('student cannot self-promote',()=>denied('student',"update profiles set role='PRINCIPAL' where id=auth.uid()"));
await check('parent cannot link another child',()=>denied('parent',"insert into parent_student_links values('attack','revenex_demo','demo_2026',auth.uid(),'unlinked_student')"));
await check('client cannot rewrite session membership',()=>denied('principal',"update profiles set session_id='old_session' where id=auth.uid()"));
await check('unpublished marks hidden from student',async()=>assert.equal((await as('student','select * from marks')).length,0));
await check('assigned teacher sees draft marks',async()=>assert.equal((await as('teacher','select * from marks')).length,1));
await check('principal can publish marks',async()=>assert.equal((await publishReviewedTerm('Term 1')).length,1));
await check('parent reads published child marks',async()=>assert.equal((await as('parent','select * from marks')).length,1));
await check('student cannot alter marks',async()=>{await denied('student',"update marks set obtained=100 where id='mark1'");assert.equal((await as('teacher',"select obtained from marks where id='mark1'"))[0].obtained,80);});
await check('invalid mark bounds rejected',()=>denied('teacher',"update marks set obtained=101 where id='mark1'"));
await check('fees visible to parent',async()=>assert.equal((await as('parent','select * from fee_records')).length,1));
await check('teacher cannot read fee balances',async()=>assert.equal((await as('teacher','select * from fee_records')).length,0));
await check('client cannot alter fee balances',()=>denied('principal',"update fee_records set payload='{}'"));
await check('student reads class timetable',async()=>assert.equal((await as('student','select * from timetables')).length,1));
await check('parent reads school notice',async()=>assert.equal((await as('parent','select * from notices')).length,1));
await check('parent cannot publish school notice',()=>denied('parent',"insert into notices values('attack','revenex_demo','demo_2026','{}')"));
await check('teacher cannot mark unassigned section',()=>denied('teacher',"insert into attendance(id,school_id,session_id,section_id,student_id,attendance_date,status,payload) values('attack','revenex_demo','demo_2026','unassigned_sec','unlinked_student','2026-10-20','PRESENT','{}')"));
await check('cross-school FK enforced for attendance',()=>denied('principal',"insert into attendance(id,school_id,session_id,section_id,student_id,attendance_date,status,payload) values('attack','revenex_demo','demo_2026','demo_10a','other_student','2026-10-20','PRESENT','{}')"));
await check('assigned teacher saves complete attendance register',async()=>assert.equal((await as('teacher',"select save_roll_call('demo_10a',(now() at time zone 'Asia/Kolkata')::date,'Morning Roll Call',$1::jsonb) as saved",[JSON.stringify([{student_id:'demo_student',status:'PRESENT',expected_version:0}])]))[0].saved.length,1));
await check('parent reads child attendance',async()=>assert.equal((await as('parent','select * from attendance')).length,1));
await check('attendance creates parent inbox row',async()=>assert.ok((await as('parent',"select * from notifications where category='ATTENDANCE'")).length>0));
await check('each account only sees own inbox',async()=>assert.ok((await as('parent','select * from notifications')).every(n=>n.recipient_id===ids.parent)));
await check('notification recipient cannot be changed',()=>denied('parent',"update notifications set recipient_id=$1",[ids.student]));
await check('notification read flag persists for recipient',async()=>assert.ok((await as('parent','update notifications set is_read=true returning id')).length>0));
await check('read state is isolated between accounts',async()=>assert.ok((await as('student','select * from notifications')).every(n=>!n.is_read)));
await check('clients cannot forge inbox messages',()=>denied('student',"insert into notifications(school_id,session_id,recipient_id,title,message) values('revenex_demo','demo_2026',auth.uid(),'fake','fake')"));
await check('clients cannot forge audit logs',()=>denied('principal',"insert into audit_logs(school_id,session_id,action,table_name,record_id) values('revenex_demo','demo_2026','fake','students','x')"));
await check('student cannot read audit logs',async()=>assert.equal((await as('student','select * from audit_logs')).length,0));
await check('audit trigger records attendance write',async()=>assert.ok((await as('principal',"select * from audit_logs where table_name='attendance'")).length>0));
await check('connected workflow rollback acceptance retains the preexisting attendance',async()=>{
 const before=(await db.query('select to_jsonb(a) as row from attendance a order by id')).rows;
 await db.exec(await readFile(new URL('../verify_connected_workflows_rollback.sql',import.meta.url),'utf8'));
 assert.deepEqual((await db.query('select to_jsonb(a) as row from attendance a order by id')).rows,before);
});
await check('storage upload without manifest denied',()=>denied('student',"insert into storage.objects(bucket_id,name) values('erp-documents','arbitrary/path')"));
const fileId='10000000-0000-0000-0000-000000000001';const path=`revenex_demo/demo_2026/${ids.teacher}/${fileId}`;
await check('teacher creates own file manifest',async()=>assert.equal((await as('teacher',"insert into file_metadata values($1,'revenex_demo','demo_2026',auth.uid(),$2,'Demo.pdf','application/pdf',10) returning id",[fileId,path])).length,1));
await check('teacher uploads object at manifest path',async()=>assert.equal((await as('teacher',"insert into storage.objects(bucket_id,name) values('erp-documents',$1) returning id",[path])).length,1));
await check('unshared file hidden from parent',async()=>assert.equal((await as('parent','select * from storage.objects')).length,0));
await check('school admin reads uploaded object',async()=>assert.equal((await as('principal','select * from storage.objects')).length,1));
await check('other school cannot read uploaded object',async()=>assert.equal((await as('other','select * from storage.objects')).length,0));
await check('student cannot submit for another child',()=>denied('student',"insert into assignment_submissions values('bad','revenex_demo','demo_2026','unassigned_sec','unlinked_student','demo_homework',true)"));
await check('student submits own class assignment through validated RPC',async()=>assert.equal((await as('student',"select submit_assignment('demo_homework','','Synthetic completion fixture',0) as result"))[0].result.completed,true));
await check('assignment publishes a scoped private document grant',async()=>assert.equal((await saveAssignment('demo_homework',{attachmentUrl:`supabase://erp-documents/${path}`})).id,'demo_homework'));
await check('parent can read document shared with child class',async()=>assert.equal((await as('parent','select * from storage.objects')).length,1));
await check('other session cannot read shared class file',async()=>assert.equal((await as('old','select * from storage.objects')).length,0));
await check('other school cannot read shared class file',async()=>assert.equal((await as('other','select * from storage.objects')).length,0));
await check('removing attachment revokes class file grant',async()=>{await saveAssignment('demo_homework',{attachmentUrl:''});assert.equal((await as('parent','select * from storage.objects')).length,0);});
await check('client cannot insert document grants directly',()=>denied('teacher',"insert into file_grants(file_id,school_id,session_id,section_id,resource_table,resource_id,field_name) values($1,'revenex_demo','demo_2026','unassigned_sec','assignments','demo_homework','attachmentUrl')",[fileId]));
await check('signed URLs cannot be persisted as permanent school attachments',async()=>{await assert.rejects(()=>saveAssignment('demo_homework',{attachmentUrl:'https://example.com/temporary?token=private'}),e=>e.code==='42501');});
await check('roll-call sessions persist independently on the same date',async()=>{await as('teacher',"select save_roll_call('demo_10a',(now() at time zone 'Asia/Kolkata')::date,'STEM Lab',$1::jsonb)",[JSON.stringify([{student_id:'demo_student',status:'ABSENT',expected_version:0}])]);assert.equal((await as('parent','select * from attendance')).length,2);});
await check('leave creation derives applicant identity and pending status on server',async()=>{const rows=await as('student',"select request_leave('leave1','Medical Leave','2099-01-01','2099-01-02','Synthetic request') as saved");assert.equal(rows[0].saved.payload.status,'PENDING');assert.equal(rows[0].saved.payload.applicantRole,'STUDENT');assert.equal(rows[0].saved.payload.applicantId,ids.student);});
await check('leave applicant cannot self-approve later',()=>denied('student',`update leave_requests set payload=jsonb_set(payload,'{status}',to_jsonb('APPROVED'::text)) where id='leave1'`));
await check('all nine requested SQL roles exist',async()=>assert.equal((await db.query("select count(*)::int as n from pg_enum e join pg_type t on t.oid=e.enumtypid where t.typname='erp_role'")).rows[0].n,9));
await check('school administrator sees the school roster',async()=>assert.equal((await as('admin','select * from students')).length,2));
await check('accountant reads fee records',async()=>assert.equal((await as('accountant','select * from fee_records')).length,1));
await check('accountant cannot change demo fee balances',()=>denied('accountant',"update fee_records set payload='{}'"));
await check('class teacher sees only assigned students',async()=>assert.deepEqual((await as('class_teacher','select id from students')).map(x=>x.id),['demo_student']));
await check('transport manager does not gain student access',async()=>assert.equal((await as('transport_manager','select * from students')).length,0));
await check('librarian does not gain fee or student access',async()=>{assert.equal((await as('librarian','select * from fee_records')).length,0);assert.equal((await as('librarian','select * from students')).length,0);});
await check('saving unchanged attendance does not duplicate parent notifications',async()=>{const before=(await as('parent','select id from notifications')).length;await as('teacher',"select save_roll_call('demo_10a',(now() at time zone 'Asia/Kolkata')::date,'Morning Roll Call',$1::jsonb)",[JSON.stringify([{student_id:'demo_student',status:'PRESENT',expected_version:1}])]);assert.equal((await as('parent','select id from notifications')).length,before);});
await check('teacher roster cannot leak fee amounts through duplicated student payload fields',async()=>{const rows=await as('teacher',"select payload from students where id='demo_student'");assert.ok(!('feePendingAmount' in rows[0].payload));assert.ok(!('feeStatus' in rows[0].payload));});
async function server(sql,params=[]) {
 await db.exec('begin;set local role service_role');
 try {const result=await db.query(sql,params);await db.exec('commit');return result.rows;}
 catch(e){await db.exec('rollback');throw e;}
}
await check('payment tables reject anonymous access',()=>denied('anon','select * from razorpay_orders'));
await check('Android cannot reserve server payment orders',()=>denied('parent',"select reserve_razorpay_order('demo_fee',$1,'rzp_test_unit')",[ids.parent]));
await check('trusted reservation still rejects a teacher actor',async()=>assert.rejects(()=>server("select reserve_razorpay_order('demo_fee',$1,'rzp_test_unit')",[ids.teacher]),e=>e.code==='42501'));
await check('trusted reservation rejects another school actor',async()=>assert.rejects(()=>server("select reserve_razorpay_order('demo_fee',$1,'rzp_test_unit')",[ids.other]),e=>e.code==='42501'));
let reserved;
await check('server derives payment amount and context from fee record',async()=>{reserved=(await server("select reserve_razorpay_order('demo_fee',$1,'rzp_test_unit') as value",[ids.parent]))[0].value;assert.equal(reserved.amount,500000);assert.equal(reserved.currency,'INR');assert.equal(reserved.school_id,'revenex_demo');});
await check('repeated order creation reuses the same reservation',async()=>{const again=(await server("select reserve_razorpay_order('demo_fee',$1,'rzp_test_unit') as value",[ids.parent]))[0].value;assert.equal(again.id,reserved.id);assert.equal(again.new_reservation,false);});
await check('another payer cannot take over an active fee checkout',async()=>assert.rejects(()=>server("select reserve_razorpay_order('demo_fee',$1,'rzp_test_unit')",[ids.student]),e=>e.code==='23514'));
await check('only server can bind the provider order',async()=>{await denied('parent',"select bind_razorpay_order($1,'order_Test123')",[reserved.id]);await server("select bind_razorpay_order($1,'order_Test123')",[reserved.id]);});
await check('another school cannot read payment orders',async()=>assert.equal((await as('other','select * from razorpay_orders')).length,0));
await check('client cannot forge captured receipts or fee credits',()=>denied('parent',"select settle_razorpay_payment($1,'pay_Test123',500000,'INR','captured',true)",[reserved.id]));
await check('wrong captured amount cannot credit the ledger',async()=>assert.rejects(()=>server("select settle_razorpay_payment($1,'pay_Test123',1,'INR','captured',true)",[reserved.id]),e=>e.code==='23514'));
await check('authorized but uncaptured payments cannot credit fees',async()=>assert.rejects(()=>server("select settle_razorpay_payment($1,'pay_Test123',500000,'INR','authorized',false)",[reserved.id]),e=>e.code==='23514'));
await check('client cannot forge failed payment notifications',()=>denied('student',"select record_razorpay_failure($1,'pay_Failed',500000,'INR','failed',false)",[reserved.id]));
await check('server confirmed failure notifies principal without crediting fee',async()=>{await server("select record_razorpay_failure($1,'pay_Failed',500000,'INR','failed',false)",[reserved.id]);assert.equal((await as('parent','select payload from fee_records'))[0].payload.paidAmount,0);assert.equal((await as('principal',"select * from notifications where title='Test payment failed'")).length,1);});
await check('failed webhook replay is deduplicated',async()=>{await server("select record_razorpay_failure($1,'pay_Failed',500000,'INR','failed',false)",[reserved.id]);assert.equal((await as('principal',"select * from notifications where title='Test payment failed'")).length,1);});
let receipt;
await check('verified captured payment atomically credits the correct fee',async()=>{receipt=(await server("select settle_razorpay_payment($1,'pay_Test123',500000,'INR','captured',true) as value",[reserved.id]))[0].value;const fee=(await as('parent','select payload from fee_records'))[0].payload;assert.equal(fee.paidAmount,500000);assert.equal(fee.status,'PAID');assert.equal(fee.transactions[0].receiptNo,receipt.receipt_no);});
await check('callback or webhook replay does not double-credit',async()=>{const again=(await server("select settle_razorpay_payment($1,'pay_Test123',500000,'INR','captured',true) as value",[reserved.id]))[0].value;assert.equal(again.receipt_no,receipt.receipt_no);assert.equal((await as('parent','select payload from fee_records'))[0].payload.transactions.length,1);assert.equal((await as('parent','select * from fee_payment_receipts')).length,1);});
await check('different payment cannot overwrite a settled order',async()=>assert.rejects(()=>server("select settle_razorpay_payment($1,'pay_Different',500000,'INR','captured',true)",[reserved.id]),e=>e.code==='23514'));
await check('captured fees create one parent inbox update',async()=>assert.equal((await as('parent',"select * from notifications where title='Test fee payment verified'")).length,1));
await check('receipt records cannot be rewritten by Android',()=>denied('principal',"update fee_payment_receipts set amount=1"));
await check('captured payment notifies principal with student name',async()=>{const notices=await as('principal',"select * from notifications where title='Test fee payment verified'");assert.equal(notices.length,1);assert.ok(notices[0].message.includes('Demo Student'));});
await check('paid student leaves outstanding fee query',async()=>assert.equal((await as('principal',"select id from fee_records where (payload->>'totalFee')::bigint>(payload->>'paidAmount')::bigint")).length,0));
const enrollment={name:'New Student',admissionNumber:'NEW-001',rollNumber:2,dob:'01/01/2011',parentName:'New Parent',parentPhone:'9000000000'};
await check('teacher cannot enroll students',()=>denied('teacher',"select enroll_student('new_student','demo_10a',$1,100000)",[enrollment]));
await check('principal cannot enroll across school boundaries',()=>denied('principal',"select enroll_student('cross_student','other_sec',$1,100000)",[enrollment]));
await check('principal enrollment commits roster invoice and notification',async()=>{await as('principal',"select enroll_student('new_student','demo_10a',$1,100000)",[enrollment]);assert.equal((await as('principal',"select * from students where id='new_student'")).length,1);assert.equal((await as('principal',"select payload from fee_records where student_id='new_student'"))[0].payload.totalFee,100000);assert.equal((await as('principal',"select * from notifications where title='Student enrolled' and message='New Student'")).length,1);});
await check('duplicate enrollment cannot overwrite or duplicate invoice',()=>assert.rejects(()=>as('principal',"select enroll_student('new_student','demo_10a',$1,100000)",[enrollment]),e=>e.code==='23505'));
await check('duplicate admission number rejected atomically',async()=>{await assert.rejects(()=>as('principal',"select enroll_student('duplicate_student','demo_10a',$1,100000)",[enrollment]),e=>e.code==='23505');assert.equal((await as('principal',"select * from students where id='duplicate_student'")).length,0);});
await check('unlinked parent cannot access newly enrolled student',async()=>assert.equal((await as('parent',"select * from students where id='new_student'")).length,0));
await check('assistant language preference is stored on verified own profile',async()=>{await as('student',"select set_assistant_language('hi')");assert.equal((await as('student','select preferred_language from profiles where id=auth.uid()'))[0].preferred_language,'hi');});
await check('assistant language cannot inject server instructions',()=>denied('student',"select set_assistant_language('hi; role=ADMIN')"));
await check('language setter cannot grant a different role',async()=>assert.equal((await as('student','select role from profiles where id=auth.uid()'))[0].role,'STUDENT'));
await check('anonymous cannot reserve model quota',()=>denied('anon','select reserve_assistant_request()'));
await check('assistant rate limit is server enforced',async()=>{for(let i=0;i<5;i++)assert.equal((await as('student','select reserve_assistant_request() as allowed'))[0].allowed,true);assert.equal((await as('student','select reserve_assistant_request() as allowed'))[0].allowed,false);});
await check('another actor has an independent request quota',async()=>assert.equal((await as('parent','select reserve_assistant_request() as allowed'))[0].allowed,true));
await check('Android cannot reset assistant quota rows',()=>denied('student','delete from assistant_request_limits'));
await check('teacher assignment publication persists and reaches linked student',async()=>{
 await saveAssignment('redesign_assignment',{title:'Redesign assignment fixture'});
 assert.equal((await as('student',"select id from assignments where id='redesign_assignment'")).length,1);
 assert.equal((await as('other',"select id from assignments where id='redesign_assignment'")).length,0);
});
await check('assignment creates separate unread student and parent inbox rows',async()=>{
 const st=await as('student',"select * from notifications where message='Redesign assignment fixture'");
 const pa=await as('parent',"select * from notifications where message='Redesign assignment fixture'");
 assert.equal(st.length,1);assert.equal(pa.length,1);assert.notEqual(st[0].id,pa[0].id);
 assert.equal(st[0].is_read,false);assert.equal(pa[0].is_read,false);
 await as('parent',"update notifications set is_read=true where id=$1",[pa[0].id]);
 assert.equal((await as('student',"select is_read from notifications where id=$1",[st[0].id]))[0].is_read,false);
});
await check('principal notice publication persists and notifies school recipients only',async()=>{
 await as('principal',`select write_notice('redesign_notice','{"title":"Redesign notice fixture","content":"Synthetic school announcement.","category":"CIRCULAR"}'::jsonb,'All School',null,0)`);
 assert.equal((await as('parent',"select * from notices where id='redesign_notice'")).length,1);
 assert.equal((await as('student',"select * from notifications where message='Redesign notice fixture'")).length,1);
 assert.equal((await as('other',"select * from notifications where message='Redesign notice fixture'")).length,0);
});
const resultInboxBefore={
 student:(await as('student',"select * from notifications where category='EXAMS'")).length,
 parent:(await as('parent',"select * from notifications where category='EXAMS'")).length
};
await check('draft result does not announce to student or parent',async()=>{
 await as('principal',"select set_result_publication('demo_student','Term 1',false)");
 assert.equal((await as('student',"select * from notifications where category='EXAMS'")).length,resultInboxBefore.student);
 assert.equal((await as('parent',"select * from notifications where category='EXAMS'")).length,resultInboxBefore.parent);
});
await check('publishing result notifies linked recipients and no other school',async()=>{
 await publishReviewedTerm('Term 1');
 assert.equal((await as('student',"select * from notifications where category='EXAMS'")).length,resultInboxBefore.student+1);
 assert.equal((await as('parent',"select * from notifications where category='EXAMS'")).length,resultInboxBefore.parent+1);
 assert.equal((await as('other',"select * from notifications where category='EXAMS'")).length,0);
});
await check('repeated result publication does not duplicate inbox entries',async()=>{
 await publishReviewedTerm('Term 1');
 assert.equal((await as('parent',"select * from notifications where category='EXAMS'")).length,resultInboxBefore.parent+1);
});
await check('draft RPC rejects family and wrong school',async()=>{
 await denied('parent',"select save_draft_mark('demo_student','RPC Term','Mathematics',70,100,0)");
 await denied('other',"select save_draft_mark('demo_student','RPC Term','Mathematics',70,100,0)");
});
await check('draft save stays private, upserts and validates scores',async()=>{
 await saveMark('teacher','RPC Term','Mathematics',70,100);
 await saveMark('teacher','RPC Term','Mathematics',71,100);
 assert.equal((await as('student',"select * from marks where term='RPC Term'")).length,0);
 assert.equal((await as('teacher',"select obtained from marks where term='RPC Term'"))[0].obtained,71);
 await denied('teacher',"select save_draft_mark('demo_student','RPC Term','Mathematics',101,100,2)");
});
await check('publication RPC is replay safe and blocks editing published terms',async()=>{
 await publishReviewedTerm('RPC Term');
 const count=(await as('student',"select * from notifications where category='EXAMS'")).length;
 await publishReviewedTerm('RPC Term');
 assert.equal((await as('student',"select * from notifications where category='EXAMS'")).length,count);
 assert.equal((await as('parent',"select * from marks where term='RPC Term'")).length,1);
 await denied('teacher',"select save_draft_mark('demo_student','RPC Term','English',70,100,0)");
 await as('principal',"select set_result_publication('demo_student','RPC Term',false)");
 assert.equal((await as('student',"select * from marks where term='RPC Term'")).length,0);
});
await check('leave owners cannot approve themselves',async()=>{
 await as('parent',"select request_leave('decision_fixture','Medical Leave','2099-01-01','2099-01-02','Synthetic guardian request')");
 await denied('parent',`update leave_requests set payload='{"status":"APPROVED"}' where id='decision_fixture'`);
 await denied('parent',"select decide_leave('decision_fixture','APPROVED','')");
 await denied('other',"select decide_leave('decision_fixture','APPROVED','')");
});
await check('admin leave decision persists and notifies once',async()=>{
 const before=(await as('parent',"select * from notifications where category='LEAVE'")).length;
 await as('admin',"select decide_leave('decision_fixture','APPROVED','Approved by school')");
 await as('admin',"select decide_leave('decision_fixture','APPROVED','Approved by school')");
 assert.equal((await as('parent',"select payload from leave_requests where id='decision_fixture'"))[0].payload.status,'APPROVED');
 assert.equal((await as('parent',"select * from notifications where category='LEAVE'")).length,before+1);
 await denied('admin',"select decide_leave('decision_fixture','REJECTED','Changed')");
});
await check('operator rollback acceptance script validates without retaining test rows',async()=>{
 await db.exec(await readFile(new URL('../verify_versioned_marks_rollback.sql',import.meta.url),'utf8'));
 assert.equal((await db.query("select count(*)::int as n from marks where term='Rollback verification 20261026'")).rows[0].n,0);
});
await check('teacher cannot publish results through the RPC or direct writes',async()=>{
 await denied('teacher',"select set_result_publication('demo_student','RPC Term',true)");
 await denied('teacher',"update marks set published=true where term='RPC Term'");
 await denied('teacher',"insert into marks(id,school_id,session_id,section_id,student_id,term,subject,obtained,maximum,published) values('unauthorized_publish','revenex_demo','demo_2026','demo_10a','demo_student','Unauthorized','Mathematics',80,100,true)");
});
await check('published result values and identity are locked against direct-write bypass',async()=>{
 await publishReviewedTerm('RPC Term');
 await denied('teacher',"update marks set published=false where term='RPC Term'");
 await denied('teacher',"update marks set obtained=72 where term='RPC Term'");
 await denied('principal',"update marks set obtained=72 where term='RPC Term'");
 await denied('teacher',"insert into marks(id,school_id,session_id,section_id,student_id,term,subject,obtained,maximum) values('late_subject','revenex_demo','demo_2026','demo_10a','demo_student','RPC Term','English',70,100)");
 await as('principal',"select set_result_publication('demo_student','RPC Term',false)");
 await denied('teacher',"update marks set term='Replacement' where term='RPC Term'");
 await saveMark('teacher','RPC Term','Mathematics',73,100);
 assert.equal((await as('teacher',"select obtained from marks where term='RPC Term'"))[0].obtained,73);
 assert.equal((await as('student',"select * from marks where term='RPC Term'")).length,0);
});
await check('student submits a recoverable note and teacher reads the same versioned record',async()=>{
 await saveAssignment('submission_work',{title:'Submission fixture'});
 const first=(await as('student',"select submit_assignment('submission_work','','My solution',0) as result"))[0].result;
 assert.equal(first.version,1);assert.equal(first.note,'My solution');assert.equal(first.student_id,'demo_student');
 assert.equal((await as('teacher',"select note from assignment_submissions where assignment_id='submission_work'"))[0].note,'My solution');
 const again=(await as('student',"select submit_assignment('submission_work','','My solution',0) as result"))[0].result;
 assert.equal(again.version,1);
 await denied('student',"select submit_assignment('submission_work','','Changed solution',0)");
});
await check('submission review requires assigned staff and locks reviewed work',async()=>{
 const id=(await as('student',"select id from assignment_submissions where assignment_id='submission_work'"))[0].id;
 await denied('student',"select review_submission($1,'Forged review',1)",[id]);
 await denied('other',"select review_submission($1,'Foreign review',1)",[id]);
 const reviewed=(await as('teacher',"select review_submission($1,'Clear working',1) as result",[id]))[0].result;
 assert.equal(reviewed.review_status,'REVIEWED');assert.equal(reviewed.version,2);
 assert.equal((await as('student',"select feedback from assignment_submissions where id=$1",[id]))[0].feedback,'Clear working');
 assert.equal((await as('teacher',"select review_submission($1,'Clear working',1) as result",[id]))[0].result.version,2);
 await denied('student',"select submit_assignment('submission_work','','Changed after review',2)");
});
await check('direct completion writes and forged ownership cannot bypass submission validation',async()=>{
 await denied('student',"update assignment_submissions set completed=false where assignment_id='submission_work'");
 await denied('parent',"select submit_assignment('submission_work','','Guardian cannot impersonate student',0)");
 await denied('student',"select submit_assignment('submission_work','supabase://erp-documents/foreign','','0')");
 await denied('student',"select submit_assignment('submission_work','','',0)");
});
const studentFile='10000000-0000-0000-0000-000000000002';
const studentPath=`revenex_demo/demo_2026/${ids.student}/${studentFile}`;
await check('private submission file is visible only to its student, linked guardian and assigned staff',async()=>{
 ids.peer='00000000-0000-0000-0000-000000000012';
 await db.query('insert into auth.users values($1,$2)',[ids.peer,'peer@revenex.demo']);
 await db.query("insert into profiles values($1,'revenex_demo','demo_2026','STUDENT','Peer','peer@revenex.demo')",[ids.peer]);
 await db.query("insert into students values('peer_student','revenex_demo','demo_2026','demo_10a',$1,'{}')",[ids.peer]);
 await as('student',"insert into file_metadata values($1,'revenex_demo','demo_2026',auth.uid(),$2,'solution.pdf','application/pdf',10)",[studentFile,studentPath]);
 await as('student',"insert into storage.objects(bucket_id,name) values('erp-documents',$1)",[studentPath]);
 assert.equal((await as('teacher',"select * from storage.objects where name=$1",[studentPath])).length,0);
 await saveAssignment('file_work',{title:'Private file fixture'});
 const saved=(await as('student',"select submit_assignment('file_work',$1,'',0) as result",[`supabase://erp-documents/${studentPath}`]))[0].result;
 assert.equal(saved.attachment_name,'solution.pdf');
 for(const role of ['student','parent','teacher','principal'])assert.equal((await as(role,"select * from storage.objects where name=$1",[studentPath])).length,1,role);
 for(const role of ['peer','other','old'])assert.equal((await as(role,"select * from storage.objects where name=$1",[studentPath])).length,0,role);
 assert.equal((await as('peer',"select * from assignment_submissions where assignment_id='file_work'")).length,0);
 assert.equal((await as('teacher',"select * from notifications where actionable_id='file_work' and title='Assignment submitted'")).length,1);
 await as('student',"select submit_assignment('file_work',$1,'',0)",[`supabase://erp-documents/${studentPath}`]);
 assert.equal((await as('teacher',"select * from notifications where actionable_id='file_work' and title='Assignment submitted'")).length,1);
});
await check('foreign files, expired assignments and stale resubmission versions are denied',async()=>{
 await denied('peer',"select submit_assignment('file_work',$1,'',0)",[`supabase://erp-documents/${studentPath}`]);
 await denied('student',"select submit_assignment('file_work',$1,'',1)",[`supabase://erp-documents/${path}`]);
 await db.query("update assignments set closes_at=now()-interval '1 minute' where id='file_work'");
 await denied('student',"select submit_assignment('file_work','','New solution after closure',1)");
 await db.query("update assignments set closes_at=null where id='file_work'");
 const changed=(await as('student',"select submit_assignment('file_work','','Revised note',1) as result"))[0].result;
 assert.equal(changed.version,2);assert.equal(changed.attachment_ref,'');
 assert.equal((await as('teacher',"select * from storage.objects where name=$1",[studentPath])).length,0);
 await denied('student',"select submit_assignment('file_work','','Old version would overwrite work',1)");
});
await check('assignment drafts stay private until versioned publication',async()=>{
 const payload=JSON.stringify({title:'Versioned draft fixture',subject:'Mathematics',instructions:'Complete the exercise.'});
 const draft=(await as('teacher',"select write_assignment('draft_work','demo_10a',$1::jsonb,'2099-01-01',false,0) as result",[payload]))[0].result;
 assert.equal(draft.publication_state,'DRAFT');assert.equal(draft.version,1);
 assert.equal((await as('student',"select * from assignments where id='draft_work'")).length,0);
 assert.equal((await as('student',"select * from notifications where message='Versioned draft fixture'")).length,0);
 await denied('student',"select submit_assignment('draft_work','','Guessing draft ID',0)");
 const published=(await as('teacher',"select write_assignment('draft_work','demo_10a',$1::jsonb,'2099-01-01',true,1) as result",[payload]))[0].result;
 assert.equal(published.version,2);assert.equal(published.publication_state,'PUBLISHED');
 assert.equal((await as('student',"select * from assignments where id='draft_work'")).length,1);
 await as('teacher',"select write_assignment('draft_work','demo_10a',$1::jsonb,'2099-01-01',true,1)",[payload]);
 assert.equal((await as('student',"select * from notifications where actionable_id='draft_work'")).length,1);
});
await check('draft attachments stay private and authoring cannot bypass subject or version permissions',async()=>{
 const payload=JSON.stringify({title:'Private draft document',subject:'Mathematics',instructions:'Private until published',attachmentUrl:`supabase://erp-documents/${path}`});
 await as('teacher',"select write_assignment('draft_file','demo_10a',$1::jsonb,'2099-01-01',false,0)",[payload]);
 assert.equal((await as('student',"select * from storage.objects where name=$1",[path])).length,0);
 await denied('teacher',"update assignments set publication_state='PUBLISHED' where id='draft_file'");
 await denied('teacher',"select write_assignment('wrong_subject','demo_10a',$1::jsonb,'2099-01-01',true,0)",[JSON.stringify({title:'Science attempt',subject:'Science',instructions:'Not assigned'})]);
 await denied('teacher',"select write_assignment('draft_file','demo_10a',$1::jsonb,'2099-01-01',true,0)",[payload]);
 await denied('other',"select write_assignment('draft_file','demo_10a',$1::jsonb,'2099-01-01',true,1)",[payload]);
});
await check('teacher cannot read or review a different subject even in an assigned class',async()=>{
 await db.query(`insert into assignments(id,school_id,session_id,section_id,payload,publication_state) values('science_work','revenex_demo','demo_2026','demo_10a','{"title":"Science fixture","subject":"Science"}','PUBLISHED')`);
 const submission=(await as('student',"select submit_assignment('science_work','','Science solution',0) as result"))[0].result;
 assert.equal((await as('teacher',"select * from assignments where id='science_work'")).length,0);
 assert.equal((await as('teacher',"select * from assignment_submissions where id=$1",[submission.id])).length,0);
 await denied('teacher',"select review_submission($1,'Not my subject',1)",[submission.id]);
 assert.equal((await as('teacher',"select * from notifications where actionable_id='science_work'")).length,0);
});
await check('teacher draft marks are restricted to protected subject allocations',async()=>{
 await db.query("insert into marks(id,school_id,session_id,section_id,student_id,term,subject,obtained,maximum) values('science_private','revenex_demo','demo_2026','demo_10a','demo_student','Private science','Science',70,100)");
 assert.equal((await as('teacher',"select * from marks where id='science_private'")).length,0);
 await denied('teacher',"select save_draft_mark('demo_student','Wrong subject','Science',75,100,0)");
 await denied('teacher',"insert into marks(id,school_id,session_id,section_id,student_id,term,subject,obtained,maximum) values('wrong_subject_mark','revenex_demo','demo_2026','demo_10a','demo_student','Wrong subject','Science',75,100)");
});
await check('versioned draft marks reject stale edits and submit for authorized review once',async()=>{
 const saved=(await as('teacher',"select save_draft_mark('demo_student','Versioned marks','Mathematics',82,100,0) as result"))[0].result;
 assert.equal(saved.version,1);
 assert.equal((await as('teacher',"select save_draft_mark('demo_student','Versioned marks','Mathematics',82,100,0) as result"))[0].result.version,1);
 await denied('teacher',"select save_draft_mark('demo_student','Versioned marks','Mathematics',83,100,0)");
 await denied('student',"select submit_mark_review('demo_student','Versioned marks','Mathematics',1)");
 const submitted=(await as('teacher',"select submit_mark_review('demo_student','Versioned marks','Mathematics',1) as result"))[0].result;
 assert.equal(submitted.review_state,'SUBMITTED');assert.equal(submitted.version,2);
 await as('teacher',"select submit_mark_review('demo_student','Versioned marks','Mathematics',1)");
 assert.equal((await as('principal',"select * from notifications where event_key like $1 and actionable_id='demo_student'",['mark-review:'+saved.id+':%'])).length,1);
 assert.equal((await as('student',"select * from marks where term='Versioned marks'")).length,0);
 await publishReviewedTerm('Versioned marks');
 assert.equal((await as('student',"select obtained from marks where term='Versioned marks'"))[0].obtained,82);
 await denied('teacher',"select save_draft_mark('demo_student','Versioned marks','Mathematics',84,100,2)");
});
await check('publication changes invalidate observed mark versions and invisible subjects lock the term',async()=>{
 await as('principal',"select set_result_publication('demo_student','Versioned marks',false)");
 await denied('teacher',"select save_draft_mark('demo_student','Versioned marks','Mathematics',84,100,2)");
 await publishReviewedTerm('Private science');
 await denied('teacher',"select save_draft_mark('demo_student','Private science','Mathematics',84,100,0)");
 await denied('teacher',"select save_draft_mark('demo_student','Legacy blind update','Mathematics',84,100)");
});
await check('private mark documents respect subject privacy and unchanged references survive permitted edits',async()=>{
 await as('principal',"select set_result_publication('demo_student','Private science',false)");
 const file='10000000-0000-0000-0000-000000000003';const objectPath=`revenex_demo/demo_2026/${ids.principal}/${file}`;
 await as('principal',"insert into file_metadata values($1,'revenex_demo','demo_2026',auth.uid(),$2,'checked.pdf','application/pdf',10)",[file,objectPath]);
 await as('principal',"insert into storage.objects(bucket_id,name) values('erp-documents',$1)",[objectPath]);
 const current=(await as('principal',"select version from marks where id='science_private'"))[0].version;
 await as('principal',"select attach_mark_document('science_private','checkedPaperUrl',$1,$2)",[`supabase://erp-documents/${objectPath}`,current]);
 assert.equal((await as('teacher',"select * from storage.objects where name=$1",[objectPath])).length,0);
 assert.equal((await as('student',"select * from storage.objects where name=$1",[objectPath])).length,0);
 await publishReviewedTerm('Private science');
 assert.equal((await as('parent',"select * from storage.objects where name=$1",[objectPath])).length,1);
 assert.equal((await as('teacher',"select * from storage.objects where name=$1",[objectPath])).length,0);
 const math=(await saveMark('teacher','Shared paper','Mathematics',80,100))[0].result;
 const linked=(await as('principal',"select attach_mark_document($1,'checkedPaperUrl',$2,$3) as result",[math.id,`supabase://erp-documents/${objectPath}`,math.version]))[0].result;
 const edited=(await as('teacher',"select save_draft_mark('demo_student','Shared paper','Mathematics',81,100,$1) as result",[linked.version]))[0].result;
 assert.equal(edited.payload.checkedPaperUrl,`supabase://erp-documents/${objectPath}`);
 assert.equal((await as('teacher',"select * from storage.objects where name=$1",[objectPath])).length,1);
});
await check('teacher cannot adopt a hidden legacy assignment by changing its subject',async()=>{
 await denied('teacher',"select write_assignment('science_work','demo_10a',$1::jsonb,'2099-01-01',true,1)",[JSON.stringify({title:'Unauthorized adoption',subject:'Mathematics',instructions:'Attempt to take another subject'})]);
});
await check('hosted assignment rollback acceptance script passes without retaining records',async()=>{
 const before=(await db.query('select count(*)::int as n from assignments')).rows[0].n;
 await db.exec(await readFile(new URL('../verify_assignment_workflows_rollback.sql',import.meta.url),'utf8'));
 assert.equal((await db.query('select count(*)::int as n from assignments')).rows[0].n,before);
});
await check('capability metadata requires a provisioned authenticated profile',async()=>{
 await denied('anon','select erp_capabilities()');
 const result=(await as('teacher','select erp_capabilities() as c'))[0].c;
 assert.equal(result.assignments,true);assert.equal(result.submissions,true);assert.equal(result.marks,true);
 assert.equal(result.targeted_notices,true);assert.equal(result.attendance,true);
});
await check('attendance aliases resolve to one canonical day',async()=>{
 const result=(await db.query("select erp_private.school_date('26 Oct 2026')=erp_private.school_date('2026-10-26') as same")).rows[0];
 assert.equal(result.same,true);
 await assert.rejects(()=>db.query("select erp_private.school_date('31 Feb 2026')"));
});
const fullRegister=[{student_id:'new_student',status:'PRESENT',expected_version:0},{student_id:'demo_student',status:'ABSENT',expected_version:0},{student_id:'peer_student',status:'PRESENT',expected_version:0}];
const rollCall="select save_roll_call('demo_10a',(now() at time zone 'Asia/Kolkata')::date,'Senior Practicum',$1::jsonb) as saved";
await check('whole-class attendance rejects partial and duplicate rosters',async()=>{
 await denied('teacher',rollCall,[JSON.stringify(fullRegister.slice(0,1))]);
 await denied('teacher',rollCall,[JSON.stringify([fullRegister[0],fullRegister[0]])]);
 assert.equal((await as('teacher',"select id from attendance where session_slot='Senior Practicum'")).length,0);
});
await check('guardian cannot save attendance and teacher cannot write a foreign section',async()=>{
 await denied('parent',rollCall,[JSON.stringify(fullRegister)]);
 await denied('teacher',"select save_roll_call('unassigned_sec',(now() at time zone 'Asia/Kolkata')::date,'Morning Roll Call',$1::jsonb)",[JSON.stringify([{student_id:'unlinked_student',status:'PRESENT',expected_version:0}])]);
});
await check('attendance confirmation retries retain versions and recipient inbox counts',async()=>{
 const first=(await as('teacher',rollCall,[JSON.stringify(fullRegister)]))[0].saved;
 const before=(await as('parent',"select id from notifications where category='ATTENDANCE'")).length;
 const retry=(await as('teacher',rollCall,[JSON.stringify(fullRegister)]))[0].saved;
 assert.deepEqual(retry,first);
 assert.equal((await as('parent',"select id from notifications where category='ATTENDANCE'")).length,before);
});
await check('a stale row rolls back every change in the attendance batch',async()=>{
 const stale=[{...fullRegister[0],status:'LEAVE',expected_version:1},{...fullRegister[1],status:'PRESENT',expected_version:0},{...fullRegister[2],expected_version:1}];
 await denied('teacher',rollCall,[JSON.stringify(stale)]);
 const stored=await as('teacher',"select student_id,status,version from attendance where session_slot='Senior Practicum' order by student_id");
 assert.deepEqual(stored,[{student_id:'demo_student',status:'ABSENT',version:1},{student_id:'new_student',status:'PRESENT',version:1},{student_id:'peer_student',status:'PRESENT',version:1}]);
});
await check('historical attendance cannot bypass correction approval through the register RPC',async()=>{
 await denied('teacher',"select save_roll_call('demo_10a',((now() at time zone 'Asia/Kolkata')::date-1),'Morning Roll Call',$1::jsonb)",[JSON.stringify(fullRegister)]);
 await denied('principal',"update attendance set status='PRESENT'");
});
await check('teacher-only notices stay out of family reads and inboxes',async()=>{
 const payload=JSON.stringify({title:'Staff-only synthetic notice',content:'Meeting details for assigned staff.',category:'CIRCULAR'});
 const result=(await as('principal',"select write_notice('target_staff',$1::jsonb,'Teachers',null,0) as saved",[payload]))[0].saved;
 assert.equal(result.payload.authorName,'Demo Principal');
 assert.equal((await as('teacher',"select id from notices where id='target_staff'")).length,1);
 assert.equal((await as('parent',"select id from notices where id='target_staff'")).length,0);
 assert.equal((await as('student',"select id from notifications where actionable_id='target_staff'")).length,0);
 assert.equal((await as('other',"select id from notices where id='target_staff'")).length,0);
 const before=(await as('teacher',"select id from notifications where actionable_id='target_staff'")).length;
 await as('principal',"select write_notice('target_staff',$1::jsonb,'Teachers',null,0)",[payload]);
 assert.equal((await as('teacher',"select id from notifications where actionable_id='target_staff'")).length,before);
});
await check('class notices target the allocated class and linked guardians only',async()=>{
 const payload=JSON.stringify({title:'Class notice',content:'Synthetic class meeting.',category:'EVENT'});
 await as('principal',"select write_notice('target_class',$1::jsonb,'Class','demo_10a',0)",[payload]);
 assert.equal((await as('parent',"select id from notices where id='target_class'")).length,1);
 assert.equal((await as('old',"select id from notices where id='target_class'")).length,0);
 assert.equal((await as('student',"select id from notifications where actionable_id='target_class'")).length,1);
 await denied('teacher',"select write_notice('teacher_attempt',$1::jsonb,'All School',null,0)",[payload]);
 await denied('principal',"select write_notice('foreign_target',$1::jsonb,'Class','other_sec',0)",[payload]);
});
await check('only the staging owner can remove an unlinked object then its manifest',async()=>{
 const id='80000000-0000-0000-0000-000000000001';const name=`revenex_demo/demo_2026/${ids.student}/${id}`;
 await as('student',"insert into file_metadata values($1,'revenex_demo','demo_2026',auth.uid(),$2,'Staged.pdf','application/pdf',10)",[id,name]);
 await as('student',"insert into storage.objects(bucket_id,name) values('erp-documents',$1)",[name]);
 assert.equal((await as('parent',"delete from storage.objects where name=$1 returning id",[name])).length,0);
 assert.equal((await as('student',"delete from file_metadata where id=$1 returning id",[id])).length,0);
 assert.equal((await as('student',"delete from storage.objects where name=$1 returning id",[name])).length,1);
 assert.equal((await as('student',"delete from file_metadata where id=$1 returning id",[id])).length,1);
});
await check('an attached teacher document cannot be deleted through staging cleanup',async()=>{
 assert.equal((await as('teacher',"delete from storage.objects where name=$1 returning id",[path])).length,0);
 assert.equal((await as('teacher',"delete from file_metadata where id=$1 returning id",[fileId])).length,0);
});
await check('leave retries keep one request and one leadership inbox entry per recipient',async()=>{
 const sql="select request_leave('stable_leave','Casual Leave','2099-01-04','2099-01-04','Synthetic one-day request') as saved";
 const first=(await as('teacher',sql))[0].saved;
 assert.equal(first.payload.daysCount,1);assert.equal(first.payload.applicantId,ids.teacher);
 assert.deepEqual((await as('teacher',sql))[0].saved,first);
 assert.equal((await as('principal',"select * from notifications where actionable_id='stable_leave'")).length,1);
 await denied('student',sql);
 await denied('teacher',"select request_leave('stable_leave','Casual Leave','2099-01-04','2099-01-04','Changed contents')");
 await denied('teacher',"select request_leave('invalid_leave','Medical Leave','2099-01-05','2099-01-01','Invalid interval')");
 await as('principal',"select decide_leave('stable_leave','APPROVED','Synthetic approval')");
 assert.equal((await as('teacher',sql))[0].saved.payload.status,'APPROVED');
});
await db.exec(await readFile(new URL('../migrations/202610300022_result_batch_publication.sql',import.meta.url),'utf8'));
await check('strict publication upgrade blocks legacy per-student writes and unpublication',async()=>{
 await denied('principal',"select set_result_publication('demo_student','Versioned marks',false)");
 await denied('teacher',"select set_result_publication('demo_student','Versioned marks',true)");
});
await check('strict publication upgrade retains existing school and student isolation',async()=>{
 assert.deepEqual((await as('other','select id from students')).map(x=>x.id),['other_student']);
 assert.deepEqual((await as('student','select id from students')).map(x=>x.id),['demo_student']);
 assert.equal((await as('student','select id from result_publication_scopes')).length,0);
});
console.log(`${passed} PostgreSQL schema/RLS checks passed. Auth/Storage service/Realtime transport require hosted verification.`);
await db.close();
