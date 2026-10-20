begin;
alter table public.file_metadata add unique(school_id,session_id,id);
alter table public.file_grants add foreign key(school_id,session_id,file_id) references public.file_metadata(school_id,session_id,id);
alter table public.assignments add unique(school_id,session_id,section_id,id);
alter table public.assignment_submissions add foreign key(school_id,session_id,section_id,assignment_id) references public.assignments(school_id,session_id,section_id,id);
alter table public.attendance add column session_slot text not null default 'Morning Roll Call';
alter table public.attendance drop constraint attendance_student_id_session_id_attendance_date_key;
alter table public.attendance add unique(student_id,session_id,attendance_date,session_slot);
-- Role/profile ownership in leave requests is authoritative, not editable JSON.
create function erp_private.validate_leave() returns trigger language plpgsql security definer set search_path='' as $$
declare actor public.profiles;
begin
 select * into actor from public.profiles where id=new.owner_id;
 if tg_op='UPDATE' and (new.owner_id<>old.owner_id or new.school_id<>old.school_id or new.session_id<>old.session_id) then
  raise exception 'Leave ownership cannot change';
 end if;
 if tg_op='INSERT' then
  new.payload:=new.payload||jsonb_build_object('status','PENDING','approverRemark','');
 elsif not erp_private.staff() and
  (new.payload->>'status' is distinct from old.payload->>'status' or new.payload->>'approverRemark' is distinct from old.payload->>'approverRemark') then
  raise exception 'Only school administrators approve leave';
 end if;
 new.payload:=new.payload||jsonb_build_object('applicantId',actor.id::text,'applicantName',actor.name,'applicantRole',actor.role::text,'schoolId',actor.school_id);
 return new;
end $$;
revoke all on function erp_private.validate_leave() from public,authenticated;
create trigger leave_identity before insert or update on public.leave_requests for each row execute function erp_private.validate_leave();
commit;
