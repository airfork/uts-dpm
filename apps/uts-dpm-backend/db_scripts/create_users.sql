create table users
(
  id         serial                  not null
    constraint users_pk
      primary key,
  managerid  integer   default 10,
  username   varchar(40)             not null,
  password   char(60)                not null,
  firstname  varchar(60)             not null,
  lastname   varchar(60)             not null,
  fulltime   boolean   default false not null,
  changed    boolean   default false,
  admin      boolean   default false,
  sup        boolean   default false,
  analyst    boolean   default false,
  points     smallint  default 0,
  sessionkey varchar(60)             not null,
  added      timestamp default now()
);

comment on table users is 'Holds user data';

alter table users
  owner to postgres;

create unique index users_id_uindex
  on users (id);

