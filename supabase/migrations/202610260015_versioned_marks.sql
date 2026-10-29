begin;
alter table public.marks
 add column version integer not null default 1 check(version>0),
 add column review_state text not null default 'DRAFT' check(review_state in ('DRAFT','SUBMITTED')),
 add column updated_by uuid references auth.users,
 add column submitted_by uuid references auth.users,
 add column submitted_at timestamptz;
-- Every client write now supplies its observed version. Legacy blind writes fail closed.
revoke insert,update on public.marks from authenticated;
revoke execute on function public.save_draft_mark(text,text,text,int,int) from authenticated;

create function public.save_draft_mark(p_student text,p_term text,p_subject text,p_obtained int,p_maximum int,p_expected_version int)
returns jsonb language plpgsql security definer set search_path='' as $$
declare actor public.profiles; st public.students; prior public.marks; saved public.marks;
begin
 select * into actor from public.profiles where id=auth.uid();
 select * into st from public.students where id=p_student and school_id=actor.school_id and session_id=actor.session_id;
 if st.id is null or not(erp_private.staff() or erp_private.teaches_subject(st.section_id,p_subject)) then
  raise exception 'Mark subject access denied' using errcode='42501'; end if;
 if p_term is null or length(btrim(p_term)) not between 1 and 80 or p_subject is null or length(btrim(p_subject)) not between 1 and 120
 or p_maximum is null or p_maximum not between 1 and 10000 or p_obtained is null or p_obtained not between 0 and p_maximum
 or p_expected_version is null or p_expected_version<0 or p_expected_version>=2147483647 then
  raise exception 'Invalid marks' using errcode='23514'; end if;
 perform pg_advisory_xact_lock(hashtextextended(st.id||':'||st.session_id||':'||btrim(p_term),0));
 select * into prior from public.marks where student_id=st.id and session_id=st.session_id
  and term=btrim(p_term) and subject=btrim(p_subject) for update;
 if prior.id is not null and prior.version=p_expected_version+1 and prior.obtained=p_obtained and prior.maximum=p_maximum
  and prior.updated_by=actor.id and prior.review_state='DRAFT' and not prior.published then return to_jsonb(prior); end if;
 if coalesce(prior.version,0)<>p_expected_version or exists(select 1 from public.marks m
  where m.student_id=st.id and m.session_id=st.session_id and m.term=btrim(p_term) and m.published) then
  raise exception 'Marks changed or term is published; reload before editing' using errcode='23514'; end if;
 insert into public.marks(id,school_id,session_id,section_id,student_id,term,subject,obtained,maximum,published,version,review_state,updated_by)
 values(coalesce(prior.id,gen_random_uuid()::text),st.school_id,st.session_id,st.section_id,st.id,btrim(p_term),btrim(p_subject),
  p_obtained,p_maximum,false,p_expected_version+1,'DRAFT',actor.id)
 on conflict(student_id,session_id,term,subject) do update set obtained=excluded.obtained,maximum=excluded.maximum,
 version=excluded.version,review_state='DRAFT',updated_by=actor.id,submitted_at=null,submitted_by=null returning * into saved;
 return to_jsonb(saved);
end $$;

create function public.submit_mark_review(p_student text,p_term text,p_subject text,p_expected_version int)
returns jsonb language plpgsql security definer set search_path='' as $$
declare actor public.profiles; st public.students; saved public.marks;
begin
 select * into actor from public.profiles where id=auth.uid();
 select * into st from public.students where id=p_student and school_id=actor.school_id and session_id=actor.session_id;
 if st.id is null or not(erp_private.staff() or erp_private.teaches_subject(st.section_id,p_subject)) then
  raise exception 'Mark subject access denied' using errcode='42501'; end if;
 if p_expected_version is null or p_expected_version<0 or p_expected_version>=2147483647 then
  raise exception 'Invalid marks version' using errcode='23514'; end if;
 perform pg_advisory_xact_lock(hashtextextended(st.id||':'||st.session_id||':'||p_term,0));
 select * into saved from public.marks where student_id=st.id and school_id=actor.school_id and session_id=actor.session_id
  and term=p_term and subject=p_subject for update;
 if not found then raise exception 'Save draft marks before submitting' using errcode='23514'; end if;
 if saved.version=p_expected_version+1 and saved.review_state='SUBMITTED' and saved.submitted_by=actor.id and not saved.published then
  return to_jsonb(saved); end if;
 if saved.version<>p_expected_version or exists(select 1 from public.marks m
  where m.student_id=st.id and m.session_id=st.session_id and m.term=p_term and m.published) then
  raise exception 'Marks changed or term is published; reload before submitting' using errcode='23514'; end if;
 update public.marks set review_state='SUBMITTED',submitted_by=actor.id,submitted_at=now(),version=version+1
 where id=saved.id returning * into saved;
 insert into public.notifications(school_id,session_id,recipient_id,title,message,category,actionable_id,event_key)
 select actor.school_id,actor.session_id,p.id,'Marks ready for review',p_term||' · '||p_subject,'EXAMS',st.id,
  'mark-review:'||saved.id||':'||saved.version from public.profiles p
 where p.school_id=actor.school_id and p.session_id=actor.session_id and p.role in ('PRINCIPAL','ADMIN')
 on conflict(school_id,session_id,recipient_id,event_key) where event_key is not null do nothing;
 return to_jsonb(saved);
end $$;

create or replace function public.set_result_publication(p_student text,p_term text,p_published boolean)
returns void language plpgsql security definer set search_path='' as $$
declare actor public.profiles; st public.students; changed int;
begin
 select * into actor from public.profiles where id=auth.uid();
 if actor.id is null or actor.role not in ('PRINCIPAL','ADMIN') then
  raise exception 'Result publication requires school approval' using errcode='42501'; end if;
 select * into st from public.students where id=p_student and school_id=actor.school_id and session_id=actor.session_id;
 if not found then raise exception 'Student unavailable' using errcode='42501'; end if;
 if p_published is null or p_term is null or length(btrim(p_term)) not between 1 and 80 then
  raise exception 'Invalid publication' using errcode='23514'; end if;
 perform pg_advisory_xact_lock(hashtextextended(st.id||':'||st.session_id||':'||btrim(p_term),0));
 update public.marks set version=version+case when published is distinct from p_published then 1 else 0 end,published=p_published
 where student_id=st.id and school_id=actor.school_id and session_id=actor.session_id and term=btrim(p_term);
 get diagnostics changed=row_count;
 if changed=0 then raise exception 'No marks to publish' using errcode='23514'; end if;
end $$;

create function public.attach_mark_document(p_id text,p_field text,p_ref text,p_expected_version int)
returns jsonb language plpgsql security definer set search_path='' as $$
declare actor public.profiles; saved public.marks; document public.file_metadata;
begin
 select * into actor from public.profiles where id=auth.uid();
 select * into saved from public.marks where id=p_id and school_id=actor.school_id and session_id=actor.session_id;
 if not found or not(erp_private.staff() or erp_private.teaches_subject(saved.section_id,saved.subject)) then
  raise exception 'Mark document access denied' using errcode='42501'; end if;
 if p_field is null or p_field not in ('checkedPaperUrl','answerKeyUrl') or p_ref is null or length(p_ref)>500
  or p_expected_version is null or p_expected_version<0 then raise exception 'Invalid document change' using errcode='23514'; end if;
 perform pg_advisory_xact_lock(hashtextextended(saved.student_id||':'||saved.session_id||':'||saved.term,0));
 select * into saved from public.marks where id=p_id for update;
 if saved.version=p_expected_version+1 and saved.payload->>p_field=p_ref and saved.updated_by=actor.id and not saved.published then return to_jsonb(saved); end if;
 if saved.version<>p_expected_version or exists(select 1 from public.marks m where m.student_id=saved.student_id
  and m.session_id=saved.session_id and m.term=saved.term and m.published) then
  raise exception 'Marks changed or term is published' using errcode='23514'; end if;
 if p_ref<>'' then
  select * into document from public.file_metadata where object_path=substr(p_ref,26)
   and p_ref like 'supabase://erp-documents/%' and school_id=actor.school_id and session_id=actor.session_id
   and exists(select 1 from storage.objects o where o.bucket_id='erp-documents' and o.name=file_metadata.object_path);
  if not found or(document.owner_id<>actor.id and not erp_private.staff()) then
   raise exception 'Mark document unavailable' using errcode='42501'; end if;
 end if;
 update public.marks set payload=jsonb_set(payload,array[p_field],to_jsonb(p_ref)),version=version+1,updated_by=actor.id,
  review_state='DRAFT',submitted_at=null,submitted_by=null where id=saved.id returning * into saved;
 return to_jsonb(saved);
end $$;
revoke all on function public.save_draft_mark(text,text,text,int,int,int),public.submit_mark_review(text,text,text,int),
 public.attach_mark_document(text,text,text,int),public.set_result_publication(text,text,boolean) from public,anon;
grant execute on function public.save_draft_mark(text,text,text,int,int,int),public.submit_mark_review(text,text,text,int),
 public.attach_mark_document(text,text,text,int),public.set_result_publication(text,text,boolean) to authenticated;
commit;
