create table if not exists users
(
  id        serial
    constraint users_pk
      primary key,
  managerid integer   default 10,
  username  varchar(40)             not null,
  password  char(60)                not null,
  firstname varchar(60)             not null,
  lastname  varchar(60)             not null,
  fulltime  boolean   default false not null,
  changed   boolean   default false not null,
  points    smallint  default 0,
  added     timestamp default now()
);

comment on table users is 'Holds user data';

create unique index users_id_uindex
  on users (id);

alter table users
  owner to postgres;

