begin;
-- Durable references, with authorization checked again whenever a download is requested.
create table public.file_grants (
 id uuid primary key default gen_random_uuid(), file_id uuid not null references public.file_metadata,
 school_id text not null, session_id text not null, section_id text not null, student_id text,
 resource_table text not null check(resource_table in ('assignments','study_materials','marks')),
 resource_id text not null, field_name text not null,
 foreign key(school_id,session_id,section_id) references public.sections(school_id,session_id,id),
 foreign key(school_id,session_id,section_id,student_id) references public.students(school_id,session_id,section_id,id),
 unique(resource_table,resource_id,field_name));
alter table public.file_grants enable row level security;
revoke all on public.file_grants from anon,authenticated;
create policy grant_read on public.file_grants for select to authenticated using (
 erp_private.context_ok(school_id,session_id) and
 (erp_private.staff() or erp_private.teaches(section_id) or
 (student_id is null and erp_private.sees_section(section_id)) or erp_private.own_student(student_id)));
grant select on public.file_grants to authenticated;
create index on public.file_grants(file_id);
create function erp_private.file_visible(f uuid) returns boolean language sql stable security definer set search_path='' as $$
 select exists(select 1 from public.file_grants g where g.file_id=f and erp_private.context_ok(g.school_id,g.session_id)
 and (erp_private.staff() or erp_private.teaches(g.section_id) or
 (g.student_id is null and erp_private.sees_section(g.section_id)) or
 (erp_private.own_student(g.student_id) and exists(select 1 from public.marks m where m.id=g.resource_id and m.published))))
$$;
revoke all on function erp_private.file_visible(uuid) from public;
grant execute on function erp_private.file_visible(uuid) to authenticated;
drop policy file_metadata_read on public.file_metadata;
create policy file_metadata_read on public.file_metadata for select to authenticated using (
 erp_private.context_ok(school_id,session_id) and (owner_id=auth.uid() or erp_private.staff() or erp_private.file_visible(id)));
create function erp_private.link_document() returns trigger language plpgsql security definer set search_path='' as $$
declare field text; ref text; file_row public.file_metadata; fields text[];
begin
 fields:=case when tg_table_name='marks' then array['checkedPaperUrl','answerKeyUrl'] else array['attachmentUrl'] end;
 foreach field in array fields loop
  ref:=coalesce(new.payload->>field,'');
  delete from public.file_grants where resource_table=tg_table_name and resource_id=new.id and field_name=field;
  if ref<>'' then
   if left(ref,25)<>'supabase://erp-documents/' then raise exception 'Use a managed private document reference'; end if;
   select * into file_row from public.file_metadata where object_path=substr(ref,26)
    and school_id=new.school_id and session_id=new.session_id;
   if not found or (file_row.owner_id<>auth.uid() and not erp_private.staff()) then
    raise exception 'Document does not belong to this account and school context';
   end if;
   insert into public.file_grants(file_id,school_id,session_id,section_id,student_id,resource_table,resource_id,field_name)
    values(file_row.id,new.school_id,new.session_id,new.section_id,
      case when tg_table_name='marks' then to_jsonb(new)->>'student_id' else null end,tg_table_name,new.id,field);
  end if;
 end loop;
 return new;
end $$;
revoke all on function erp_private.link_document() from public,authenticated;
create trigger assignment_document after insert or update on public.assignments for each row execute function erp_private.link_document();
create trigger study_document after insert or update on public.study_materials for each row execute function erp_private.link_document();
create trigger mark_document after insert or update on public.marks for each row execute function erp_private.link_document();
commit;
