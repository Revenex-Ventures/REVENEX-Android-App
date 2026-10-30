-- Operator read-only checks AFTER migrations 23/24 have been confirmed.
-- Run in the intended demo project SQL editor. Counts only; no student content.
begin read only;
select school_id,session_id,operation,count(*) as denied_actor_windows
from public.security_events where created_at>now()-interval '1 hour'
group by school_id,session_id,operation;
select school_id,session_id,status,count(*) as pending_over_one_hour
from public.razorpay_orders where status in ('creating','created')
and created_at<now()-interval '1 hour' group by school_id,session_id,status;
select count(*) as paid_orders_without_receipt from public.razorpay_orders o
where status='paid' and not exists(select 1 from public.fee_payment_receipts r where r.order_id=o.id);
select count(*) filter(where staged_at<now()-interval '1 day') as unlinked_staged_over_one_day,
 count(*) filter(where staged_at is null) as historical_unknown_age
from public.file_metadata f where not exists(select 1 from public.file_grants g where g.file_id=f.id)
and (staged_at is null or staged_at<now()-interval '1 day');
select count(*) as stale_or_missing_inspected_objects from public.file_metadata f
left join storage.objects o on o.bucket_id='erp-documents' and o.name=f.object_path
where f.inspected_at is not null and (o.id is distinct from f.content_object_id
or o.updated_at is distinct from f.content_object_updated_at);
rollback;
