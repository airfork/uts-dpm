create table if not exists dpms
(
    dpm_id         serial
        constraint dpms_table_pk primary key,
    dpm_group_id   integer                  not null
        constraint dpm_groups_pk_dpm_id_fk references dpm_groups (dpm_group_id),
    name           varchar(255)             not null,
    points         smallint                 not null,
    active         boolean default true     not null,
    w2w_color_code varchar(5),
    created_at     timestamp with time zone not null default now(),
    updated_at     timestamp with time zone not null default now()
);

create unique index new_dpms_table_id_uindex
    on dpms (dpm_id);
