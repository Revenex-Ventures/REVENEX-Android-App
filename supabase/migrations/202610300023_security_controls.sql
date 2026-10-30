begin;
-- Shared, transactional actor/tenant budgets. No client-selected quota or tenant.
-- Existing school tables, records and RLS policies are unchanged.
create table public.backend_request_limits (
 actor_id uuid not null,school_id text not null,session_id text not null,
 operation text not null check(operation in ('payment_create','payment_verify','payment_status','document_inspect')),
 window_start timestamptz not null,requests integer not null check(requests between 1 and 30),
 primary key(school_id,session_id,actor_id,operation,window_start),
 foreign key(actor_id) references auth.users(id) on delete cascade,
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id)
);
create index backend_limits_scope_window on public.backend_request_limits(school_id,session_id,operation,window_start);
alter table public.backend_request_limits enable row level security;
revoke all on public.backend_request_limits from public,anon,authenticated;
grant select on public.backend_request_limits to service_role;

create table public.security_events (
 id uuid primary key default gen_random_uuid(),school_id text not null,session_id text not null,
 actor_id uuid not null,event_code text not null check(event_code='RATE_LIMIT_DENIED'),
 operation text not null check(operation in ('payment_create','payment_verify','payment_status','document_inspect')),
 window_start timestamptz not null,created_at timestamptz not null default now(),
 -- Retain historical actor UUIDs even after protected membership changes. Only
 -- this function derives them from profiles; there is no client insert grant.
 foreign key(school_id,session_id) references public.academic_sessions(school_id,id),
 unique(school_id,session_id,actor_id,operation,window_start,event_code)
);
create index security_events_scope_time on public.security_events(school_id,session_id,created_at desc);
alter table public.security_events enable row level security;
revoke all on public.security_events from public,anon,authenticated;
grant select on public.security_events to authenticated,service_role;
create policy security_events_leadership_read on public.security_events for select to authenticated
 using(erp_private.context_ok(school_id,session_id) and erp_private.staff());

create function public.reserve_backend_request(p_actor_id uuid,p_operation text) returns boolean
language plpgsql security definer set search_path='' as $$
declare actor public.profiles; minute_start timestamptz; actor_limit integer; actor_used bigint; school_used bigint;
begin
 actor_limit=case p_operation when 'payment_create' then 5 when 'payment_verify' then 15
  when 'payment_status' then 30 when 'document_inspect' then 5 else null end;
 if actor_limit is null or p_actor_id is null then return false; end if;
 select * into actor from public.profiles where id=p_actor_id;
 if not found then return false; end if;
 -- Edge Functions may call only after Auth verification. Service role is the
 -- only caller: an Android account cannot pass another actor to this operation.
 perform pg_advisory_xact_lock(hashtextextended('backend-quota:'||actor.school_id||'/'||actor.session_id||'/'||p_operation,0));
 -- Compute the minute AFTER waiting for the lock, rather than spending an old
 -- transaction's allowance after the wall clock has advanced to a new window.
 minute_start=date_trunc('minute',clock_timestamp());
 delete from public.backend_request_limits where school_id=actor.school_id and session_id=actor.session_id
  and operation=p_operation and window_start<minute_start-interval '1 day';
 select coalesce(sum(requests),0) into school_used from public.backend_request_limits
  where school_id=actor.school_id and session_id=actor.session_id and operation=p_operation and window_start=minute_start;
 select coalesce(sum(requests),0) into actor_used from public.backend_request_limits
  where school_id=actor.school_id and session_id=actor.session_id and actor_id=actor.id and operation=p_operation and window_start=minute_start;
 if actor_used>=actor_limit or school_used>=200 then
  -- Fixed fields only; no provider response, student content, credential or IP.
  -- Deduplication bounds denial logging even during repeated abuse.
  insert into public.security_events(school_id,session_id,actor_id,event_code,operation,window_start)
  values(actor.school_id,actor.session_id,actor.id,'RATE_LIMIT_DENIED',p_operation,minute_start)
  on conflict(school_id,session_id,actor_id,operation,window_start,event_code) do nothing;
  return false;
 end if;
 insert into public.backend_request_limits(actor_id,school_id,session_id,operation,window_start,requests)
 values(actor.id,actor.school_id,actor.session_id,p_operation,minute_start,1)
 on conflict(school_id,session_id,actor_id,operation,window_start) do update set requests=public.backend_request_limits.requests+1;
 return true;
end $$;
revoke all on function public.reserve_backend_request(uuid,text) from public,anon,authenticated;
grant execute on function public.reserve_backend_request(uuid,text) to service_role;
commit;
