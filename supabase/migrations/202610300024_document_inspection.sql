begin;
-- Historical records keep unknown inspection/staging age. No backfill, deletion,
-- publication change or broader client grant is introduced.
alter table public.file_metadata add column staged_at timestamptz,
 add column content_object_id uuid,add column content_object_updated_at timestamptz,
 add column content_sha256 text check(content_sha256 is null or content_sha256 ~ '^[0-9a-f]{64}$'),
 add column inspected_at timestamptz;
create function erp_private.guard_document_inspection_fields() returns trigger
language plpgsql set search_path='' as $$
begin
 if current_setting('role') in ('authenticated','anon') and
  (new.content_object_id is not null or new.content_object_updated_at is not null or new.content_sha256 is not null or new.inspected_at is not null) then
  raise exception 'Inspection state is server managed' using errcode='42501'; end if;
 new.staged_at=now();
 return new;
end $$;
revoke all on function erp_private.guard_document_inspection_fields() from public,anon,authenticated;
create trigger document_inspection_fields before insert on public.file_metadata
 for each row execute function erp_private.guard_document_inspection_fields();

create function public.document_inspection_identity(p_file uuid,p_actor uuid) returns jsonb
language plpgsql security definer set search_path='' as $$
declare file public.file_metadata; object storage.objects;
begin
 select f.* into file from public.file_metadata f join public.profiles p on p.id=p_actor and p.id=f.owner_id
  and p.school_id=f.school_id and p.session_id=f.session_id where f.id=p_file;
 if not found then raise exception 'Owned school document unavailable' using errcode='42501'; end if;
 select * into object from storage.objects where bucket_id='erp-documents' and name=file.object_path;
 if not found or object.updated_at is null then raise exception 'Stored object unavailable' using errcode='23514'; end if;
 return jsonb_build_object('id',object.id,'updated_at',object.updated_at);
end $$;
create function public.confirm_document_inspection(p_file uuid,p_actor uuid,p_object uuid,p_updated_at timestamptz,p_sha256 text)
returns jsonb language plpgsql security definer set search_path='' as $$
declare file public.file_metadata; object storage.objects;
begin
 if p_sha256 is null or p_sha256 !~ '^[0-9a-f]{64}$' then raise exception 'Invalid document digest' using errcode='23514'; end if;
 select f.* into file from public.file_metadata f join public.profiles p on p.id=p_actor and p.id=f.owner_id
  and p.school_id=f.school_id and p.session_id=f.session_id where f.id=p_file for update of f;
 if not found then raise exception 'Owned school document unavailable' using errcode='42501'; end if;
 select * into object from storage.objects where bucket_id='erp-documents' and name=file.object_path for update;
 if not found or object.id is distinct from p_object or object.updated_at is distinct from p_updated_at or p_updated_at is null then
  raise exception 'Stored object changed during inspection' using errcode='40001'; end if;
 update public.file_metadata set content_object_id=object.id,content_object_updated_at=object.updated_at,
  content_sha256=p_sha256,inspected_at=now() where id=file.id;
 return jsonb_build_object('validated',true);
end $$;
revoke all on function public.document_inspection_identity(uuid,uuid),
 public.confirm_document_inspection(uuid,uuid,uuid,timestamptz,text) from public,anon,authenticated;
grant execute on function public.document_inspection_identity(uuid,uuid),
 public.confirm_document_inspection(uuid,uuid,uuid,timestamptz,text) to service_role;

create function erp_private.require_inspected_documents() returns trigger
language plpgsql security definer set search_path='' as $$
declare field text; ref text; fields text[]; publishing boolean;
begin
 fields=case when tg_table_name='marks' then array['checkedPaperUrl','answerKeyUrl'] else array['attachmentUrl'] end;
 publishing=case when tg_op='INSERT' then true
  when tg_table_name='assignments' then to_jsonb(new)->>'publication_state'='PUBLISHED' and to_jsonb(old)->>'publication_state'<>'PUBLISHED'
  when tg_table_name='marks' then (to_jsonb(new)->>'published')::boolean and not (to_jsonb(old)->>'published')::boolean
  else false end;
 foreach field in array fields loop
  ref=coalesce(new.payload->>field,'');
  if ref<>'' and (tg_op='INSERT' or coalesce(old.payload->>field,'')<>ref or publishing) then
   if left(ref,25)<>'supabase://erp-documents/' then raise exception 'Managed document reference required' using errcode='23514'; end if;
   perform 1 from (
    select 1 from public.file_metadata f join storage.objects o on o.bucket_id='erp-documents' and o.name=f.object_path
    where f.object_path=substr(ref,26) and f.school_id=new.school_id and f.session_id=new.session_id
    and f.inspected_at is not null and f.content_sha256 is not null
    and f.content_object_id=o.id and f.content_object_updated_at=o.updated_at for share of o) inspected;
   if not found then
    raise exception 'Document inspection required before sharing this object version' using errcode='23514'; end if;
  end if;
 end loop;
 return new;
end $$;
revoke all on function erp_private.require_inspected_documents() from public,anon,authenticated;
create trigger assignment_inspected_object before insert or update on public.assignments for each row execute function erp_private.require_inspected_documents();
create trigger material_inspected_object before insert or update on public.study_materials for each row execute function erp_private.require_inspected_documents();
create trigger mark_inspected_object before insert or update on public.marks for each row execute function erp_private.require_inspected_documents();
-- Student submissions also carry references outside payload; use the same
-- immutable object-version evidence without changing their ownership policies.
create function erp_private.require_inspected_submission() returns trigger
language plpgsql security definer set search_path='' as $$
begin
 if new.attachment_ref<>'' and (tg_op='INSERT' or new.attachment_ref is distinct from old.attachment_ref) then
  perform 1 from public.file_metadata f join storage.objects o on o.bucket_id='erp-documents' and o.name=f.object_path
  where 'supabase://erp-documents/'||f.object_path=new.attachment_ref and f.school_id=new.school_id and f.session_id=new.session_id
  and f.inspected_at is not null and f.content_sha256 is not null and f.content_object_id=o.id and f.content_object_updated_at=o.updated_at for share of o;
  if not found then raise exception 'Submission document inspection required' using errcode='23514'; end if;
 end if;
 return new;
end $$;
revoke all on function erp_private.require_inspected_submission() from public,anon,authenticated;
create trigger submission_inspected_object before insert or update on public.assignment_submissions for each row execute function erp_private.require_inspected_submission();
create index unlinked_document_staging_age on public.file_metadata(staged_at) where staged_at is not null;
commit;
