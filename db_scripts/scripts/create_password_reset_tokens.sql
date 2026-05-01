create table if not exists password_reset_tokens
(
    password_reset_token_id serial
        constraint password_reset_tokens_pk
            primary key,
    user_id                 integer                  not null
        constraint password_reset_tokens_user_id_fk
            references users,
    token_hash              char(64)                 not null,
    expires_at              timestamp with time zone not null,
    used_at                 timestamp with time zone,
    created_at              timestamp with time zone not null default now()
);

create unique index password_reset_tokens_token_hash_uindex
    on password_reset_tokens (token_hash);
