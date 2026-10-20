-- Test-mode payments. Only authenticated Edge Functions holding the server role
-- may reserve/bind/settle orders. Android retains read-only fee access.
begin;
alter table public.fee_records add constraint fee_records_context_id unique(school_id,session_id,id);
create table public.razorpay_orders (
 id uuid primary key default gen_random_uuid(),
 school_id text not null, session_id text not null, fee_id text not null,
 payer_id uuid not null, amount bigint not null check(amount between 100 and 1000000000),
 currency text not null default 'INR' check(currency='INR'),
 key_id text not null check(key_id ~ '^rzp_test_[A-Za-z0-9]+$'),
 provider_order_id text unique, status text not null default 'creating' check(status in ('creating','created','paid')),
 created_at timestamptz not null default now(),
 foreign key(school_id,session_id,fee_id) references public.fee_records(school_id,session_id,id),
 foreign key(school_id,session_id,payer_id) references public.profiles(school_id,session_id,id),
 unique(school_id,session_id,id)
);
create unique index razorpay_one_pending_fee on public.razorpay_orders(fee_id) where status in ('creating','created');
create table public.fee_payment_receipts (
 id uuid primary key default gen_random_uuid(), school_id text not null, session_id text not null,
 order_id uuid not null unique, payment_id text not null unique,
 receipt_no text not null unique, amount bigint not null check(amount>0),
 created_at timestamptz not null default now(),
 foreign key(school_id,session_id,order_id) references public.razorpay_orders(school_id,session_id,id)
);
alter table public.razorpay_orders enable row level security;
alter table public.fee_payment_receipts enable row level security;
revoke all on public.razorpay_orders,public.fee_payment_receipts from anon,authenticated;
create policy payment_orders_read on public.razorpay_orders for select to authenticated using (
 erp_private.context_ok(school_id,session_id) and
 (payer_id=auth.uid() or erp_private.staff() or erp_private.has_role(array['ACCOUNTANT']::public.erp_role[]))
);
create policy payment_receipts_read on public.fee_payment_receipts for select to authenticated using (
 exists(select 1 from public.razorpay_orders o where o.id=order_id)
);
grant select on public.razorpay_orders,public.fee_payment_receipts to authenticated,service_role;

create function public.reserve_razorpay_order(p_fee_id text,p_actor_id uuid,p_key_id text) returns jsonb
language plpgsql security definer set search_path='' as $$
declare f public.fee_records; o public.razorpay_orders; due bigint;
begin
 select * into f from public.fee_records where id=p_fee_id for update;
 if not found or not exists(
  select 1 from public.profiles p where p.id=p_actor_id and p.school_id=f.school_id and p.session_id=f.session_id
  and (p.role in ('PRINCIPAL','ADMIN','ACCOUNTANT') or exists(
   select 1 from public.students st where st.id=f.student_id and
   (st.profile_id=p.id or exists(select 1 from public.parent_student_links l where l.parent_id=p.id and l.student_id=st.id))))
 ) then raise exception 'Fee access denied' using errcode='42501'; end if;
 if f.school_id<>'revenex_demo' or f.session_id<>'demo_2026' then raise exception 'Synthetic demo fees only' using errcode='23514'; end if;
 if p_key_id is null or p_key_id !~ '^rzp_test_[A-Za-z0-9]+$' then raise exception 'Test keys required' using errcode='23514'; end if;
 if coalesce(f.payload->>'totalFee','') !~ '^[0-9]+$' or coalesce(f.payload->>'paidAmount','') !~ '^[0-9]+$' then
  raise exception 'Invalid fee balance' using errcode='23514'; end if;
 due=(f.payload->>'totalFee')::bigint-(f.payload->>'paidAmount')::bigint;
 if due<100 or due>1000000000 then raise exception 'No payable test balance' using errcode='23514'; end if;
 select * into o from public.razorpay_orders where fee_id=f.id and status in ('creating','created');
 if found then
  if o.payer_id<>p_actor_id or o.amount<>due or o.key_id<>p_key_id then
   raise exception 'Pending order requires reconciliation' using errcode='23514'; end if;
  return to_jsonb(o)||jsonb_build_object('new_reservation',false);
 end if;
 insert into public.razorpay_orders(school_id,session_id,fee_id,payer_id,amount,key_id)
 values(f.school_id,f.session_id,f.id,p_actor_id,due,p_key_id) returning * into o;
 return to_jsonb(o)||jsonb_build_object('new_reservation',true);
end $$;

create function public.bind_razorpay_order(p_reference uuid,p_provider_id text) returns jsonb
language plpgsql security definer set search_path='' as $$
declare o public.razorpay_orders;
begin
 select * into o from public.razorpay_orders where id=p_reference for update;
 if not found or p_provider_id is null or p_provider_id !~ '^order_[A-Za-z0-9]+$' then raise exception 'Invalid order' using errcode='23514'; end if;
 if o.provider_order_id is not null and o.provider_order_id<>p_provider_id then
  raise exception 'Order already bound' using errcode='23514'; end if;
 if o.status='creating' then
  update public.razorpay_orders set provider_order_id=p_provider_id,status='created' where id=o.id returning * into o;
 end if;
 return to_jsonb(o);
end $$;

create function public.settle_razorpay_payment(p_reference uuid,p_payment_id text,p_amount bigint,p_currency text,p_status text,p_captured boolean)
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
 select p.school_id,p.session_id,p.id,'Test fee payment verified','Receipt '||r.receipt_no,'FEES'
 from public.profiles p where p.school_id=o.school_id and p.session_id=o.session_id and
 (p.id=o.payer_id or exists(select 1 from public.students st where st.id=f.student_id and
 (st.profile_id=p.id or exists(select 1 from public.parent_student_links l where l.student_id=st.id and l.parent_id=p.id))));
 return to_jsonb(r);
end $$;
revoke all on function public.reserve_razorpay_order(text,uuid,text),public.bind_razorpay_order(uuid,text),
 public.settle_razorpay_payment(uuid,text,bigint,text,text,boolean) from public,anon,authenticated;
grant execute on function public.reserve_razorpay_order(text,uuid,text),public.bind_razorpay_order(uuid,text),
 public.settle_razorpay_payment(uuid,text,bigint,text,text,boolean) to service_role;
commit;
