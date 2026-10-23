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
grant usage on schema storage to authenticated;grant select,insert on storage.objects to authenticated;
create publication supabase_realtime; insert into storage.buckets(id,name,public) values('erp-documents','erp-documents',true);`);
for(const [role,id] of Object.entries(ids)) await db.query('insert into auth.users values($1,$2)',[id,`${role}@revenex.demo`]);
for(const f of (await readdir(new URL('../migrations/',import.meta.url))).filter(f=>f.endsWith('.sql')).sort()) {
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
insert into marks values('mark1','revenex_demo','demo_2026','demo_10a','demo_student','Term 1','Math',80,100,false,'{}');`);
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
await check('assigned teacher can publish marks',async()=>assert.equal((await as('teacher',"update marks set published=true where id='mark1' returning id")).length,1));
await check('parent reads published child marks',async()=>assert.equal((await as('parent','select * from marks')).length,1));
await check('student cannot alter marks',async()=>{assert.equal((await as('student',"update marks set obtained=100 where id='mark1' returning id")).length,0);assert.equal((await as('teacher',"select obtained from marks where id='mark1'"))[0].obtained,80);});
await check('invalid mark bounds rejected',()=>denied('teacher',"update marks set obtained=101 where id='mark1'"));
await check('fees visible to parent',async()=>assert.equal((await as('parent','select * from fee_records')).length,1));
await check('teacher cannot read fee balances',async()=>assert.equal((await as('teacher','select * from fee_records')).length,0));
await check('client cannot alter fee balances',()=>denied('principal',"update fee_records set payload='{}'"));
await check('student reads class timetable',async()=>assert.equal((await as('student','select * from timetables')).length,1));
await check('parent reads school notice',async()=>assert.equal((await as('parent','select * from notices')).length,1));
await check('parent cannot publish school notice',()=>denied('parent',"insert into notices values('attack','revenex_demo','demo_2026','{}')"));
await check('teacher cannot mark unassigned section',()=>denied('teacher',"insert into attendance(id,school_id,session_id,section_id,student_id,attendance_date,status,payload) values('attack','revenex_demo','demo_2026','unassigned_sec','unlinked_student','2026-10-20','PRESENT','{}')"));
await check('cross-school FK enforced for attendance',()=>denied('principal',"insert into attendance(id,school_id,session_id,section_id,student_id,attendance_date,status,payload) values('attack','revenex_demo','demo_2026','demo_10a','other_student','2026-10-20','PRESENT','{}')"));
await check('assigned teacher saves attendance',async()=>assert.equal((await as('teacher',"insert into attendance(id,school_id,session_id,section_id,student_id,attendance_date,status,payload) values('att1','revenex_demo','demo_2026','demo_10a','demo_student','2026-10-20','PRESENT','{}') returning id")).length,1));
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
await check('storage upload without manifest denied',()=>denied('student',"insert into storage.objects(bucket_id,name) values('erp-documents','arbitrary/path')"));
const fileId='10000000-0000-0000-0000-000000000001';const path=`revenex_demo/demo_2026/${ids.teacher}/${fileId}`;
await check('teacher creates own file manifest',async()=>assert.equal((await as('teacher',"insert into file_metadata values($1,'revenex_demo','demo_2026',auth.uid(),$2,'Demo.pdf','application/pdf',10) returning id",[fileId,path])).length,1));
await check('teacher uploads object at manifest path',async()=>assert.equal((await as('teacher',"insert into storage.objects(bucket_id,name) values('erp-documents',$1) returning id",[path])).length,1));
await check('unshared file hidden from parent',async()=>assert.equal((await as('parent','select * from storage.objects')).length,0));
await check('school admin reads uploaded object',async()=>assert.equal((await as('principal','select * from storage.objects')).length,1));
await check('other school cannot read uploaded object',async()=>assert.equal((await as('other','select * from storage.objects')).length,0));
await check('student cannot submit for another child',()=>denied('student',"insert into assignment_submissions values('bad','revenex_demo','demo_2026','unassigned_sec','unlinked_student','demo_homework',true)"));
await check('student submits own class assignment',async()=>assert.equal((await as('student',"insert into assignment_submissions values('submit1','revenex_demo','demo_2026','demo_10a','demo_student','demo_homework',true) returning id")).length,1));
await check('assignment publishes a scoped private document grant',async()=>assert.equal((await as('teacher',"update assignments set payload=jsonb_set(payload,'{attachmentUrl}',to_jsonb($1::text)) where id='demo_homework' returning id",[`supabase://erp-documents/${path}`])).length,1));
await check('parent can read document shared with child class',async()=>assert.equal((await as('parent','select * from storage.objects')).length,1));
await check('other session cannot read shared class file',async()=>assert.equal((await as('old','select * from storage.objects')).length,0));
await check('other school cannot read shared class file',async()=>assert.equal((await as('other','select * from storage.objects')).length,0));
await check('removing attachment revokes class file grant',async()=>{await as('teacher',"update assignments set payload=payload-'attachmentUrl' where id='demo_homework'");assert.equal((await as('parent','select * from storage.objects')).length,0);});
await check('client cannot insert document grants directly',()=>denied('teacher',"insert into file_grants(file_id,school_id,session_id,section_id,resource_table,resource_id,field_name) values($1,'revenex_demo','demo_2026','unassigned_sec','assignments','demo_homework','attachmentUrl')",[fileId]));
await check('signed URLs cannot be persisted as permanent school attachments',async()=>{await assert.rejects(()=>as('teacher',"update assignments set payload=jsonb_set(payload,'{attachmentUrl}',to_jsonb($1::text)) where id='demo_homework'",['https://example.com/temporary?token=private']),e=>e.code==='P0001');});
await check('roll-call sessions persist independently on the same date',async()=>{await as('teacher',"insert into attendance(id,school_id,session_id,section_id,student_id,attendance_date,status,payload,session_slot) values('att_lab','revenex_demo','demo_2026','demo_10a','demo_student','2026-10-20','ABSENT','{}','STEM Lab')");assert.equal((await as('parent','select * from attendance')).length,2);});
await check('leave applicant cannot forge an approved request',async()=>{const rows=await as('student',"insert into leave_requests(id,school_id,session_id,payload) values('leave1','revenex_demo','demo_2026',$1::jsonb) returning payload",[JSON.stringify({status:'APPROVED',applicantRole:'PRINCIPAL',applicantId:ids.principal})]);assert.equal(rows[0].payload.status,'PENDING');assert.equal(rows[0].payload.applicantRole,'STUDENT');assert.equal(rows[0].payload.applicantId,ids.student);});
await check('leave applicant cannot self-approve later',async()=>{await assert.rejects(()=>as('student',`update leave_requests set payload=jsonb_set(payload,'{status}',to_jsonb('APPROVED'::text)) where id='leave1'`),e=>e.code==='P0001');});
await check('all nine requested SQL roles exist',async()=>assert.equal((await db.query("select count(*)::int as n from pg_enum e join pg_type t on t.oid=e.enumtypid where t.typname='erp_role'")).rows[0].n,9));
await check('school administrator sees the school roster',async()=>assert.equal((await as('admin','select * from students')).length,2));
await check('accountant reads fee records',async()=>assert.equal((await as('accountant','select * from fee_records')).length,1));
await check('accountant cannot change demo fee balances',()=>denied('accountant',"update fee_records set payload='{}'"));
await check('class teacher sees only assigned students',async()=>assert.deepEqual((await as('class_teacher','select id from students')).map(x=>x.id),['demo_student']));
await check('transport manager does not gain student access',async()=>assert.equal((await as('transport_manager','select * from students')).length,0));
await check('librarian does not gain fee or student access',async()=>{assert.equal((await as('librarian','select * from fee_records')).length,0);assert.equal((await as('librarian','select * from students')).length,0);});
await check('saving unchanged attendance does not duplicate parent notifications',async()=>{const before=(await as('parent','select id from notifications')).length;await as('teacher',"update attendance set status=status where id='att1'");assert.equal((await as('parent','select id from notifications')).length,before);});
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
 await as('teacher',`insert into assignments values('redesign_assignment','revenex_demo','demo_2026','demo_10a','{"title":"Redesign assignment fixture"}')`);
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
 await as('principal',`insert into notices values('redesign_notice','revenex_demo','demo_2026','{"title":"Redesign notice fixture"}')`);
 assert.equal((await as('parent',"select * from notices where id='redesign_notice'")).length,1);
 assert.equal((await as('student',"select * from notifications where message='Redesign notice fixture'")).length,1);
 assert.equal((await as('other',"select * from notifications where message='Redesign notice fixture'")).length,0);
});
const resultInboxBefore={
 student:(await as('student',"select * from notifications where category='EXAMS'")).length,
 parent:(await as('parent',"select * from notifications where category='EXAMS'")).length
};
await check('draft result does not announce to student or parent',async()=>{
 await as('teacher',"update marks set published=false where id='mark1'");
 assert.equal((await as('student',"select * from notifications where category='EXAMS'")).length,resultInboxBefore.student);
 assert.equal((await as('parent',"select * from notifications where category='EXAMS'")).length,resultInboxBefore.parent);
});
await check('publishing result notifies linked recipients and no other school',async()=>{
 await as('teacher',"update marks set published=true where id='mark1'");
 assert.equal((await as('student',"select * from notifications where category='EXAMS'")).length,resultInboxBefore.student+1);
 assert.equal((await as('parent',"select * from notifications where category='EXAMS'")).length,resultInboxBefore.parent+1);
 assert.equal((await as('other',"select * from notifications where category='EXAMS'")).length,0);
});
await check('repeated result publication does not duplicate inbox entries',async()=>{
 await as('teacher',"update marks set published=true where id='mark1'");
 assert.equal((await as('parent',"select * from notifications where category='EXAMS'")).length,resultInboxBefore.parent+1);
});
await check('draft RPC rejects family and wrong school',async()=>{
 await denied('parent',"select save_draft_mark('demo_student','RPC Term','Math',70,100)");
 await denied('other',"select save_draft_mark('demo_student','RPC Term','Math',70,100)");
});
await check('draft save stays private, upserts and validates scores',async()=>{
 await as('teacher',"select save_draft_mark('demo_student','RPC Term','Math',70,100)");
 await as('teacher',"select save_draft_mark('demo_student','RPC Term','Math',71,100)");
 assert.equal((await as('student',"select * from marks where term='RPC Term'")).length,0);
 assert.equal((await as('teacher',"select obtained from marks where term='RPC Term'"))[0].obtained,71);
 await denied('teacher',"select save_draft_mark('demo_student','RPC Term','Math',101,100)");
});
await check('publication RPC is replay safe and blocks editing published terms',async()=>{
 await as('teacher',"select set_result_publication('demo_student','RPC Term',true)");
 const count=(await as('student',"select * from notifications where category='EXAMS'")).length;
 await as('teacher',"select set_result_publication('demo_student','RPC Term',true)");
 assert.equal((await as('student',"select * from notifications where category='EXAMS'")).length,count);
 assert.equal((await as('parent',"select * from marks where term='RPC Term'")).length,1);
 await denied('teacher',"select save_draft_mark('demo_student','RPC Term','English',70,100)");
 await as('teacher',"select set_result_publication('demo_student','RPC Term',false)");
 assert.equal((await as('student',"select * from marks where term='RPC Term'")).length,0);
});
await check('leave owners cannot approve themselves',async()=>{
 await as('parent',`insert into leave_requests(id,school_id,session_id,payload) values('decision_fixture','revenex_demo','demo_2026','{"status":"PENDING"}')`);
 await assert.rejects(()=>as('parent',`update leave_requests set payload='{"status":"APPROVED"}' where id='decision_fixture'`),e=>e.code==='P0001');
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
 await db.exec(await readFile(new URL('../verify_staff_workflows_rollback.sql',import.meta.url),'utf8'));
 assert.equal((await db.query("select count(*)::int as n from marks where term='Rollback verification 20261024'")).rows[0].n,0);
});
console.log(`${passed} PostgreSQL schema/RLS checks passed. Auth/Storage service/Realtime transport require hosted verification.`);
await db.close();
