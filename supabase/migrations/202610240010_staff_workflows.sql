begin;
-- Existing marks RLS is the authority. No new table grants or broader policies.
-- Serialize term edits/publication through these client-facing operations.
create function public.save_draft_mark(p_student text,p_term text,p_subject text,p_obtained int,p_maximum int)
returns void language plpgsql security invoker set search_path='' as $$
declare st public.students;
begin
 select * into st from public.students where id=p_student;
 if not found or not (erp_private.staff() or erp_private.teaches(st.section_id)) then
  raise exception 'Marks access denied' using errcode='42501'; end if;
 if p_term is null or length(btrim(p_term)) not between 1 and 80 or
 p_subject is null or length(btrim(p_subject)) not between 1 and 120 or
 p_maximum is null or p_maximum not between 1 and 10000 or
 p_obtained is null or p_obtained not between 0 and p_maximum then
  raise exception 'Invalid marks' using errcode='23514'; end if;
 perform pg_advisory_xact_lock(hashtextextended(st.id||':'||st.session_id||':'||btrim(p_term),0));
 if exists(select 1 from public.marks where student_id=st.id and session_id=st.session_id and term=btrim(p_term) and published) then
  raise exception 'Unpublish the term before editing' using errcode='23514'; end if;
 insert into public.marks(id,school_id,session_id,section_id,student_id,term,subject,obtained,maximum,published)
 values(gen_random_uuid()::text,st.school_id,st.session_id,st.section_id,st.id,btrim(p_term),btrim(p_subject),p_obtained,p_maximum,false)
 on conflict(student_id,session_id,term,subject) do update
 set obtained=excluded.obtained,maximum=excluded.maximum;
end $$;

create function public.set_result_publication(p_student text,p_term text,p_published boolean)
returns void language plpgsql security invoker set search_path='' as $$
declare st public.students; changed int;
begin
 select * into st from public.students where id=p_student;
 if not found or not (erp_private.staff() or erp_private.teaches(st.section_id)) then
  raise exception 'Result access denied' using errcode='42501'; end if;
 if p_published is null or p_term is null or length(btrim(p_term)) not between 1 and 80 then
  raise exception 'Invalid publication' using errcode='23514'; end if;
 perform pg_advisory_xact_lock(hashtextextended(st.id||':'||st.session_id||':'||btrim(p_term),0));
 update public.marks set published=p_published
 where student_id=st.id and school_id=st.school_id and session_id=st.session_id and term=btrim(p_term);
 get diagnostics changed=row_count;
 if changed=0 then raise exception 'No marks to publish' using errcode='23514'; end if;
end $$;

-- Existing leave_identity trigger retains authoritative applicant identity and pending-only creation.
create function public.decide_leave(p_id text,p_status text,p_remark text)
returns void language plpgsql security definer set search_path='' as $$
declare actor public.profiles; request public.leave_requests;
begin
 select * into actor from public.profiles where id=auth.uid();
 if not found or actor.role not in ('PRINCIPAL','ADMIN') then
  raise exception 'Leave decision denied' using errcode='42501'; end if;
 if p_status is null or p_status not in ('APPROVED','REJECTED') or p_remark is null or length(p_remark)>1000 then
  raise exception 'Invalid leave decision' using errcode='23514'; end if;
 select * into request from public.leave_requests
 where id=p_id and school_id=actor.school_id and session_id=actor.session_id for update;
 if not found then raise exception 'Leave request unavailable' using errcode='42501'; end if;
 if request.payload->>'status'=p_status then return; end if;
 if coalesce(request.payload->>'status','PENDING')<>'PENDING' then
  raise exception 'Leave already decided' using errcode='23514'; end if;
 update public.leave_requests set payload=payload||jsonb_build_object('status',p_status,'approverRemark',btrim(p_remark)) where id=request.id;
 insert into public.notifications(school_id,session_id,recipient_id,title,message,category)
 select actor.school_id,actor.session_id,p.id,'Leave '||lower(p_status),
  'Your leave request was '||lower(p_status)||'. '||btrim(p_remark),'LEAVE'
 from public.profiles p where p.id=request.owner_id and p.school_id=actor.school_id and p.session_id=actor.session_id;
end $$;

revoke all on function public.save_draft_mark(text,text,text,int,int) from public,anon;
revoke all on function public.set_result_publication(text,text,boolean) from public,anon;
revoke all on function public.decide_leave(text,text,text) from public,anon;
grant execute on function public.save_draft_mark(text,text,text,int,int) to authenticated;
grant execute on function public.set_result_publication(text,text,boolean) to authenticated;
grant execute on function public.decide_leave(text,text,text) to authenticated;
commit;
