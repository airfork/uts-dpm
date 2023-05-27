create table if not exists dpms
(
    id        serial
        constraint dpms_pk
            primary key,
    createid  integer                                      not null
        constraint users_id_dpms_createid_fk
            references users,
    userid    integer                                      not null
        constraint users_id_dpms_userid
            references users,
    block     varchar(20)                                  not null,
    date      date                                         not null,
    dpmtype   text                                         not null,
    points    smallint                                     not null,
    notes     text,
    created   timestamp with time zone                     not null,
    approved  boolean     default false                    not null,
    location  varchar(10) default 'N/A'::character varying not null,
    starttime time                                         not null,
    endtime   time                                         not null,
    ignored   boolean     default false                    not null
);

comment on table dpms is 'Holds user dpms';

create unique index dpms_id_uindex
    on dpms (id);
