create table if not exists dpm_order
(
    dpm_order_id serial
        constraint dpm_order_pk primary key,
    dpm_order    varchar                  not null,
    created_at   timestamp with time zone not null default now(),
    updated_at   timestamp with time zone not null default now()
)