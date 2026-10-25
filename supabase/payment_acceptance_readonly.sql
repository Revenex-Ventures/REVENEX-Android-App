-- Read-only aggregate audit for the synthetic demo. Does not create/settle an order.
-- Counts establish persisted state only, not actual provider webhook delivery.
with orders as (
 select * from public.razorpay_orders
 where school_id='revenex_demo' and session_id='demo_2026'
), receipts as (
 select * from public.fee_payment_receipts
 where school_id='revenex_demo' and session_id='demo_2026'
)
select
 (select count(*) from orders) as orders,
 (select count(*) from orders where status='creating') as creating,
 (select count(*) from orders where status='created') as created,
 (select count(*) from orders where provider_order_id ~ '^order_[A-Za-z0-9]+$') as provider_bound,
 (select count(*) from orders where status='paid') as paid,
 (select count(*) from receipts) as receipts,
 (select count(*) from orders o where status='paid' and not exists(select 1 from receipts r where r.order_id=o.id)) as paid_without_receipt,
 (select count(*) from (select payment_id from receipts group by payment_id having count(*)>1) d) as duplicate_payments,
 (select count(*) from public.razorpay_failure_events f join orders o on o.id=f.order_id) as failed_events;
