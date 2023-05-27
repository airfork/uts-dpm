create table if not exists user_roles
(
  user_id integer not null
    constraint user_roles_users_null_fk
      references users (id)
      on delete cascade,
  role_id integer not null
    constraint user_roles_roles_null_fk
      references roles (role_id)
      on delete cascade,
  constraint user_roles_pk
    primary key (user_id)
);
