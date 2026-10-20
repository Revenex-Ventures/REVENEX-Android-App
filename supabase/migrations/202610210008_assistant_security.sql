begin;
alter table public.profiles add column preferred_language text not null default 'en'
 check(preferred_language ~ '^[A-Za-z][A-Za-z0-9-]{1,19}$');
-- No broad profile UPDATE grant: this RPC changes only the caller's language preference.
create function public.set_assistant_language(p_language text) returns void
language plpgsql security definer set search_path='' as $$
begin
 if auth.uid() is null or p_language is null or p_language !~ '^[A-Za-z][A-Za-z0-9-]{1,19}$'
 then raise exception 'Invalid language preference' using errcode='42501'; end if;
 update public.profiles set preferred_language=p_language where id=auth.uid();
 if not found then raise exception 'School profile required' using errcode='42501'; end if;
end $$;
revoke all on function public.set_assistant_language(text) from public,anon;
grant execute on function public.set_assistant_language(text) to authenticated;
create table public.assistant_request_limits(
 actor_id uuid not null,school_id text not null,session_id text not null,
 minute timestamptz not null,requests integer not null check(requests between 1 and 5),
 primary key(actor_id,minute),
 foreign key(school_id,session_id,actor_id) references public.profiles(school_id,session_id,id));
alter table public.assistant_request_limits enable row level security;
revoke all on public.assistant_request_limits from anon,authenticated;
grant all on public.assistant_request_limits to service_role;
create index assistant_limits_time on public.assistant_request_limits(minute);
create function public.reserve_assistant_request() returns boolean
language plpgsql security definer set search_path='' as $$
declare p public.profiles; current_minute timestamptz=date_trunc('minute',now());
begin
 select * into p from public.profiles where id=auth.uid();
 if not found then return false; end if;
 -- Serialize quota decisions per school. No client-supplied actor, school or session.
 perform pg_advisory_xact_lock(hashtextextended(p.school_id||'/'||p.session_id,0));
 delete from public.assistant_request_limits where minute<now()-interval '1 day';
 if (select coalesce(sum(requests),0) from public.assistant_request_limits where school_id=p.school_id and session_id=p.session_id and minute=current_minute)>=30
 or (select coalesce(sum(requests),0) from public.assistant_request_limits where actor_id=p.id and minute>=date_trunc('day',now()))>=100 then return false; end if;
 insert into public.assistant_request_limits(actor_id,school_id,session_id,minute,requests) values(p.id,p.school_id,p.session_id,current_minute,1)
 on conflict(actor_id,minute) do update set requests=public.assistant_request_limits.requests+1 where public.assistant_request_limits.requests<5;
 return found;
end $$;
revoke all on function public.reserve_assistant_request() from public,anon;
grant execute on function public.reserve_assistant_request() to authenticated;
commit;
