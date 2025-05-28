create table if not exists w2w_colors
(
    w2w_color_id serial
        constraint w2w_colors_table_pk primary key,
    color_code   varchar(10)              not null,
    color_name   varchar(100)             not null,
    hex_code     char(7)                  not null,
    active       boolean                           default true not null,
    created_at   timestamp with time zone not null default now(),
    updated_at   timestamp with time zone not null default now()
);

create unique index w2w_colors_table_id_uindex on w2w_colors (w2w_color_id);