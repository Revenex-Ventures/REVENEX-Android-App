begin;
-- Enrollment is one authorized transaction: roster, optional invoice, audit and inbox.
create unique index student_admission_scope on public.students(school_id,session_id,lower(btrim(payload->>'admissionNumber')))
where nullif(btrim(payload->>'admissionNumber'),'') is not null;
create function public.enroll_student(p_id text,p_section text,p_payload jsonb,p_fee_amount bigint default 0) returns text
language plpgsql security definer set search_path='' as $$
declare actor public.profiles; sec public.sections; grade text; clean jsonb;
begin
 select * into actor from public.profiles where id=auth.uid();
 if not found or actor.role not in ('PRINCIPAL','ADMIN') then raise exception 'Enrollment denied' using errcode='42501'; end if;
 select * into sec from public.sections where id=p_section and school_id=actor.school_id and session_id=actor.session_id;
 if not found then raise exception 'Section denied' using errcode='42501'; end if;
 if octet_length(p_payload::text)>65536 then raise exception 'Enrollment too large' using errcode='23514'; end if;
 if p_id is null or length(p_id) not between 1 and 120 or jsonb_typeof(p_payload) is distinct from 'object' or
 length(btrim(coalesce(p_payload->>'name',''))) not between 3 and 120 or
 length(btrim(coalesce(p_payload->>'admissionNumber',''))) not between 1 and 80 or
 p_fee_amount is null or p_fee_amount not between 0 and 1000000000 then raise exception 'Invalid enrollment' using errcode='23514'; end if;
 select c.grade into grade from public.classes c where c.id=sec.class_id;
 select coalesce(jsonb_object_agg(key,value),'{}'::jsonb) into clean from jsonb_each(p_payload)
 where key=any(array['admissionNumber','rollNumber','name','dob','gender','bloodGroup','parentName','parentPhone','parentEmail','address','admissionDate','avatarColorHex']);
 if coalesce(p_payload->>'rollNumber','1') !~ '^[0-9]{1,3}$' or coalesce(p_payload->>'rollNumber','1')::int<1 then raise exception 'Invalid roll number' using errcode='23514'; end if;
 clean=clean||jsonb_build_object('name',btrim(p_payload->>'name'),'admissionNumber',btrim(p_payload->>'admissionNumber'),'rollNumber',coalesce(p_payload->>'rollNumber','1')::int,
 'dob',coalesce(p_payload->>'dob',''),'gender',coalesce(p_payload->>'gender','Unspecified'),'bloodGroup',coalesce(p_payload->>'bloodGroup','Unknown'),
 'parentName',coalesce(p_payload->>'parentName',''),'parentPhone',coalesce(p_payload->>'parentPhone',''),'parentEmail',coalesce(p_payload->>'parentEmail',''),
 'address',coalesce(p_payload->>'address',''),'admissionDate',coalesce(p_payload->>'admissionDate',current_date::text));
 clean=clean||jsonb_build_object('id',p_id,'classGrade',grade,'division',sec.division,'schoolId',actor.school_id,
 'sessionId',actor.session_id,'status','ACTIVE','attendancePercent',0,'rank',0,'gpa',0);
 insert into public.students(id,school_id,session_id,section_id,payload) values(p_id,actor.school_id,actor.session_id,sec.id,clean);
 if p_fee_amount>0 then
 insert into public.fee_records(id,school_id,session_id,section_id,student_id,payload)
 values('enrollment_'||p_id,actor.school_id,actor.session_id,sec.id,p_id,jsonb_build_object(
 'studentId',p_id,'studentName',clean->>'name','classGrade',grade,'division',sec.division,
 'tuitionFee',p_fee_amount,'examFee',0,'transportFee',0,'labLibraryFee',0,'discountScholarship',0,
 'totalFee',p_fee_amount,'paidAmount',0,'status','PENDING','dueDate',current_date::text,'transactions','[]'::jsonb));
 end if;
 insert into public.notifications(school_id,session_id,recipient_id,title,message,category)
 select actor.school_id,actor.session_id,p.id,'Student enrolled',clean->>'name','SYSTEM' from public.profiles p
 where p.school_id=actor.school_id and p.session_id=actor.session_id and p.role in ('PRINCIPAL','ADMIN');
 return p_id;
end $$;
revoke all on function public.enroll_student(text,text,jsonb,bigint) from public,anon;
grant execute on function public.enroll_student(text,text,jsonb,bigint) to authenticated;
-- Only provider-verified failures may enter the permanent school inbox.
create table public.razorpay_failure_events (
 payment_id text primary key, order_id uuid not null references public.razorpay_orders,
 created_at timestamptz not null default now());
alter table public.razorpay_failure_events enable row level security;
revoke all on public.razorpay_failure_events from anon,authenticated;
grant all on public.razorpay_failure_events to service_role;
create function public.record_razorpay_failure(p_reference uuid,p_payment_id text,p_amount bigint,p_currency text,p_status text,p_captured boolean)
returns boolean language plpgsql security definer set search_path='' as $$
declare o public.razorpay_orders; inserted integer; student_name text;
begin
 select * into o from public.razorpay_orders where id=p_reference for update;
 if not found or o.school_id<>'revenex_demo' or o.session_id<>'demo_2026' or o.provider_order_id is null or
 p_payment_id is null or p_payment_id !~ '^pay_[A-Za-z0-9]+$' or p_amount is distinct from o.amount or
 p_currency is distinct from 'INR' or p_status is distinct from 'failed' or p_captured is distinct from false
 then raise exception 'Invalid failure confirmation' using errcode='23514'; end if;
 if o.status='paid' then return false; end if;
 insert into public.razorpay_failure_events(payment_id,order_id) values(p_payment_id,o.id) on conflict do nothing;
 get diagnostics inserted=row_count;
 if inserted=0 then return false; end if;
 select f.payload->>'studentName' into student_name from public.fee_records f where f.id=o.fee_id;
 insert into public.notifications(school_id,session_id,recipient_id,title,message,category)
 select p.school_id,p.session_id,p.id,'Test payment failed',coalesce(student_name,'Student')||' — Payment '||p_payment_id||'. Fee remains pending.','FEES'
 from public.profiles p where p.school_id=o.school_id and p.session_id=o.session_id and
 (p.role in ('PRINCIPAL','ADMIN','ACCOUNTANT') or p.id=o.payer_id or exists(select 1 from public.students st where st.id=(select student_id from public.fee_records where id=o.fee_id) and
 (st.profile_id=p.id or exists(select 1 from public.parent_student_links l where l.student_id=st.id and l.parent_id=p.id))));
 insert into public.audit_logs(school_id,session_id,actor_id,action,table_name,record_id)
 values(o.school_id,o.session_id,o.payer_id,'RAZORPAY_TEST_FAILED','fee_records',o.fee_id);
 return true;
end $$;
revoke all on function public.record_razorpay_failure(uuid,text,bigint,text,text,boolean) from public,anon,authenticated;
grant execute on function public.record_razorpay_failure(uuid,text,bigint,text,text,boolean) to service_role;
create or replace function public.settle_razorpay_payment(p_reference uuid,p_payment_id text,p_amount bigint,p_currency text,p_status text,p_captured boolean)
returns jsonb language plpgsql security definer set search_path='' as $$
declare o public.razorpay_orders; f public.fee_records; r public.fee_payment_receipts; new_paid bigint; txn jsonb;
begin
 select * into o from public.razorpay_orders where id=p_reference for update;
 if not found or o.school_id<>'revenex_demo' or o.session_id<>'demo_2026' or o.provider_order_id is null or p_payment_id is null or p_payment_id !~ '^pay_[A-Za-z0-9]+$' or p_amount is distinct from o.amount or
 p_currency is distinct from 'INR' or p_status is distinct from 'captured' or p_captured is distinct from true then
  raise exception 'Captured payment mismatch' using errcode='23514'; end if;
 select * into r from public.fee_payment_receipts where order_id=o.id;
 if found then
  if r.payment_id<>p_payment_id then raise exception 'Order already settled' using errcode='23514'; end if;
  return to_jsonb(r);
 end if;
 select * into f from public.fee_records where id=o.fee_id for update;
 new_paid=(f.payload->>'paidAmount')::bigint+o.amount;
 if new_paid>(f.payload->>'totalFee')::bigint then raise exception 'Balance changed; reconcile payment' using errcode='23514'; end if;
 insert into public.fee_payment_receipts(school_id,session_id,order_id,payment_id,receipt_no,amount)
 values(o.school_id,o.session_id,o.id,p_payment_id,'RZP-TEST-'||o.id::text,o.amount) returning * into r;
 txn=jsonb_build_object('transactionId',p_payment_id,'receiptNo',r.receipt_no,'amount',o.amount,
  'date',current_date::text,'method','Razorpay Test','status','SUCCESS','feeHead','School fees','sessionId',o.session_id);
 update public.fee_records set payload=payload||jsonb_build_object('paidAmount',new_paid,
  'status',case when new_paid=(payload->>'totalFee')::bigint then 'PAID' else 'PARTIAL' end,
  'lastPaymentDate',current_date::text,'transactions',jsonb_build_array(txn)||coalesce(payload->'transactions','[]'::jsonb)) where id=f.id;
 update public.razorpay_orders set status='paid' where id=o.id;
 insert into public.audit_logs(school_id,session_id,actor_id,action,table_name,record_id)
 values(o.school_id,o.session_id,o.payer_id,'RAZORPAY_TEST_CAPTURED','fee_records',f.id);
 insert into public.notifications(school_id,session_id,recipient_id,title,message,category)
 select p.school_id,p.session_id,p.id,'Test fee payment verified',coalesce(f.payload->>'studentName','Student')||' — Receipt '||r.receipt_no,'FEES'
 from public.profiles p where p.school_id=o.school_id and p.session_id=o.session_id and
 (p.role in ('PRINCIPAL','ADMIN','ACCOUNTANT') or p.id=o.payer_id or exists(select 1 from public.students st where st.id=f.student_id and
 (st.profile_id=p.id or exists(select 1 from public.parent_student_links l where l.student_id=st.id and l.parent_id=p.id))));
 return to_jsonb(r);
end $$;

commit;
