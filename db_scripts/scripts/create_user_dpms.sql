create table if not exists user_dpms
(
    user_dpm_id serial
        constraint new_user_dpms_pk
            primary key,
    create_id   integer                                      not null
        constraint users_id_dpms_create_id_fk
            references users,
    user_id     integer                                      not null
        constraint users_id_dpms_userid
            references users,
    block       varchar(20)                                  not null,
    date        date                                         not null,
    dpm_id      integer
        constraint dpms_id_new_user_dpms_dpm_id_fk not null,
    points      smallint                                     not null,
    notes       text,
    created_at  timestamp with time zone                     not null,
    approved    boolean     default false                    not null,
    location    varchar(10) default 'N/A'::character varying not null,
    start_time  time                                         not null,
    end_time    time                                         not null,
    ignored     boolean     default false                    not null
);

comment on table user_dpms is 'Holds user dpms';

create unique index new_user_dpms_id_uindex
    on user_dpms (user_dpm_id);
