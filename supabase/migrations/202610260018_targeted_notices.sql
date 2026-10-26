begin;
alter table public.notices
 add column audience text not null default 'All School' check(audience in ('All School','Teachers','Students','Guardians','Class')),
 add column section_id text,
 add column version integer not null default 1 check(version>0),
 add column created_by uuid references auth.users;
alter table public.notices add foreign key(section_id,school_id,session_id) references public.sections(id,school_id,session_id);
alter table public.notices add check((audience='Class')=(section_id is not null));
create function erp_private.notice_recipient(who uuid,aud text,sec text,school text,session text) returns boolean
language sql stable security definer set search_path='' as $$
 select exists(select 1 from public.profiles p where p.id=who and p.school_id=school and p.session_id=session and
  (p.role in ('PRINCIPAL','ADMIN') or aud='All School'
   or (aud='Teachers' and p.role in ('TEACHER','CLASS_TEACHER'))
   or (aud='Students' and p.role='STUDENT') or (aud='Guardians' and p.role='PARENT')
   or (aud='Class' and (exists(select 1 from public.section_teachers t where t.profile_id=p.id and t.section_id=sec
    and t.school_id=school and t.session_id=session) or exists(select 1 from public.students st where st.section_id=sec
    and st.school_id=school and st.session_id=session and (st.profile_id=p.id or exists(
     select 1 from public.parent_student_links l where l.student_id=st.id and l.parent_id=p.id and l.school_id=school and l.session_id=session)))))))
$$;
revoke all on function erp_private.notice_recipient(uuid,text,text,text,text) from public,anon,authenticated;
create function erp_private.notice_visible(work text) returns boolean language sql stable security definer set search_path='' as $$
 select exists(select 1 from public.notices n where n.id=work and erp_private.context_ok(n.school_id,n.session_id)
  and erp_private.notice_recipient(auth.uid(),n.audience,n.section_id,n.school_id,n.session_id))
$$;
revoke all on function erp_private.notice_visible(text) from public,anon;
grant execute on function erp_private.notice_visible(text) to authenticated;
drop policy notices_read on public.notices;
create policy notices_read on public.notices for select to authenticated using(erp_private.notice_visible(id));
revoke insert,update on public.notices from authenticated;

create function public.write_notice(p_id text,p_payload jsonb,p_audience text,p_section text,p_expected_version integer)
returns jsonb language plpgsql security definer set search_path='' as $$
declare actor public.profiles; prior public.notices; saved public.notices; clean jsonb;
begin
 select * into actor from public.profiles where id=auth.uid();
 if actor.id is null or not erp_private.staff() then raise exception 'Notice publication access denied' using errcode='42501'; end if;
 if p_id is null or length(p_id) not between 1 and 200 or jsonb_typeof(p_payload) is distinct from 'object'
  or jsonb_typeof(p_payload->'title') is distinct from 'string' or length(btrim(p_payload->>'title')) not between 1 and 200
  or jsonb_typeof(p_payload->'content') is distinct from 'string' or length(btrim(p_payload->>'content')) not between 1 and 12000
  or coalesce(p_payload->>'category','') not in ('CIRCULAR','URGENT','EXAM','HOLIDAY','EVENT','SPORTS')
  or p_audience is null or p_audience not in ('All School','Teachers','Students','Guardians','Class')
  or p_expected_version is null or p_expected_version<0 or p_expected_version>=2147483647 then
  raise exception 'Invalid notice fields' using errcode='23514'; end if;
 if (p_audience='Class' and not exists(select 1 from public.sections where id=p_section and school_id=actor.school_id and session_id=actor.session_id))
  or (p_audience<>'Class' and p_section is not null) then raise exception 'Notice class unavailable' using errcode='42501'; end if;
 perform pg_advisory_xact_lock(hashtextextended('notice:'||p_id,0));
 select * into prior from public.notices where id=p_id for update;
 if prior.id is not null and (prior.school_id<>actor.school_id or prior.session_id<>actor.session_id) then
  raise exception 'Notice unavailable' using errcode='42501'; end if;
 clean=jsonb_build_object('id',p_id,'title',btrim(p_payload->>'title'),'content',btrim(p_payload->>'content'),
  'category',p_payload->>'category','isImportant',coalesce((p_payload->>'isImportant')::boolean,false),
  'publishedDate',coalesce(prior.payload->>'publishedDate',to_char(now() at time zone 'Asia/Kolkata','DD Mon YYYY')),
  'authorName',actor.name,'authorRole',actor.role::text,'targetAudience',p_audience,'schoolId',actor.school_id,
  'targetClass',coalesce((select c.grade||'-'||s.division from public.sections s join public.classes c on c.id=s.class_id where s.id=p_section),''));
 if prior.id is not null and prior.version=p_expected_version+1 and prior.payload=clean and prior.audience=p_audience
  and prior.section_id is not distinct from p_section then return to_jsonb(prior); end if;
 if coalesce(prior.version,0)<>p_expected_version then raise exception 'Notice changed; reload before retrying' using errcode='23514'; end if;
 insert into public.notices(id,school_id,session_id,payload,audience,section_id,version,created_by)
 values(p_id,actor.school_id,actor.session_id,clean,p_audience,p_section,p_expected_version+1,coalesce(prior.created_by,actor.id))
 on conflict(id) do update set payload=excluded.payload,audience=excluded.audience,section_id=excluded.section_id,
  version=excluded.version returning * into saved;
 return to_jsonb(saved);
end $$;
revoke all on function public.write_notice(text,jsonb,text,text,integer) from public,anon;
grant execute on function public.write_notice(text,jsonb,text,text,integer) to authenticated;

drop trigger audit_notices on public.notices;
create function erp_private.notice_inbox_event() returns trigger language plpgsql security definer set search_path='' as $$
begin
 if tg_op='UPDATE' and to_jsonb(new)=to_jsonb(old) then return new; end if;
 insert into public.audit_logs(school_id,session_id,actor_id,action,table_name,record_id)
 values(new.school_id,new.session_id,auth.uid(),tg_op,'notices',new.id);
 insert into public.notifications(school_id,session_id,recipient_id,title,message,category,actionable_id,event_key)
 select new.school_id,new.session_id,p.id,'School notice',new.payload->>'title','ANNOUNCEMENTS',new.id,'notice:'||new.id||':'||new.version
 from public.profiles p where erp_private.notice_recipient(p.id,new.audience,new.section_id,new.school_id,new.session_id)
 on conflict(school_id,session_id,recipient_id,event_key) where event_key is not null do nothing;
 return new;
end $$;
revoke all on function erp_private.notice_inbox_event() from public,anon,authenticated;
create trigger notice_inbox_event after insert or update on public.notices for each row execute function erp_private.notice_inbox_event();
commit;
