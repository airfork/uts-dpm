create table if not exists roles
(
  role_id serial
    constraint roles_pk
      primary key,
  name    varchar(100) not null
);
