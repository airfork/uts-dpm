create table if not exists dpm_order
(
    dpm_order_id serial
        constraint dpm_order_pk primary key,
    dpm_order    varchar                  not null,
    created_at   timestamp with time zone not null default now(),
    updated_at   timestamp with time zone not null default now()
);

insert into dpm_order (dpm_order)
select '[{"group":1,"dpms":[1,2,3,4,5]},{"group":2,"dpms":[6]},{"group":3,"dpms":[7,8,9,10,11,12,13,14,15,16]},{"group":4,"dpms":[17,18,19,20,21,22,23]},{"group":5,"dpms":[24,25,26,27,28]},{"group":6,"dpms":[29,30]}]'
where not exists (select 1 from dpm_order);

do $$
declare
  orphan_count integer;
begin
  select count(*)
  into orphan_count
  from user_dpms user_dpm
  left join dpms dpm on dpm.dpm_id = user_dpm.dpm_id
  where dpm.dpm_id is null;

  if orphan_count > 0 then
    raise exception 'Cannot add dpms_id_new_user_dpms_dpm_id_fk: % user_dpms rows reference missing dpms rows',
      orphan_count;
  end if;

  if not exists (
    select 1
    from pg_constraint
    where conrelid = 'user_dpms'::regclass
      and conname = 'dpms_id_new_user_dpms_dpm_id_fk'
  ) then
    alter table user_dpms
      add constraint dpms_id_new_user_dpms_dpm_id_fk
        foreign key (dpm_id) references dpms (dpm_id);
  end if;
end
$$;
