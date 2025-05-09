create table if not exists dpm_groups
(
    dpm_group_id serial
        constraint dpm_groups_pk primary key,
    group_name   varchar(500)             not null,
    created_at   timestamp with time zone not null default now(),
    updated_at   timestamp with time zone not null default now()
);