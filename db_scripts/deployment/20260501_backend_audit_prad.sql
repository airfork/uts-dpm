-- UTS DPM backend audit triage deployment migration.
-- Target: existing PRAD PostgreSQL database.
-- Run from the repository root with:
--   psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f db_scripts/deployment/20260501_backend_audit_prad.sql
--
-- This script is intentionally idempotent where Postgres supports it. It combines
-- the schema changes required by the backend audit PR so deployment does not
-- require manually applying several local migration files.

begin;

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

create unique index if not exists password_reset_tokens_token_hash_uindex
    on password_reset_tokens (token_hash);

alter table auto_submissions
  add column if not exists submitted_date date;

update auto_submissions
set submitted_date = (submitted at time zone 'America/New_York')::date
where submitted_date is null;

-- Keep the newest submission row for each app-local day before enforcing the
-- one-autogen-submission-per-day constraint.
delete from auto_submissions old_submission
using auto_submissions newer_submission
where old_submission.submitted_date = newer_submission.submitted_date
  and old_submission.auto_submission_id < newer_submission.auto_submission_id;

alter table auto_submissions
  alter column submitted_date set not null;

do $$
begin
  if not exists (
    select 1
    from pg_constraint
    where conrelid = 'auto_submissions'::regclass
      and conname = 'auto_submissions_submitted_date_key'
  ) then
    alter table auto_submissions
      add constraint auto_submissions_submitted_date_key unique (submitted_date);
  end if;
end
$$;

do $$
declare
  duplicate_color_count integer;
begin
  select count(*)
  into duplicate_color_count
  from (
    select w2w_color_id
    from dpms
    where active
      and w2w_color_id is not null
    group by w2w_color_id
    having count(*) > 1
  ) duplicate_colors;

  if duplicate_color_count > 0 then
    raise exception 'Cannot create dpms_active_w2w_color_id_uindex: % active W2W colors are assigned to multiple DPMs',
      duplicate_color_count;
  end if;
end
$$;

create unique index if not exists dpms_active_w2w_color_id_uindex
    on dpms (w2w_color_id)
    where active and w2w_color_id is not null;

create table if not exists dpm_order
(
    dpm_order_id serial
        constraint dpm_order_pk primary key,
    dpm_order    varchar                  not null,
    created_at   timestamp with time zone not null default now(),
    updated_at   timestamp with time zone not null default now()
);

insert into dpm_order (dpm_order)
select '[{"group":1,"dpms":[1,2,3,4,5]},{"group":2,"dpms":[6]},{"group":3,"dpms":[7,8,9,10,11,12,13,14,15,16]},{"group":4,"dpms":[17,18,19,20,21,22,23]},{"group":5,"dpms":[24,25,26,27,28]},{"group":6,"dpms":[29,30]}]'
where not exists (select 1 from dpm_order);

do $$
declare
  orphan_count integer;
begin
  select count(*)
  into orphan_count
  from user_dpms user_dpm
  left join dpms dpm on dpm.dpm_id = user_dpm.dpm_id
  where dpm.dpm_id is null;

  if orphan_count > 0 then
    raise exception 'Cannot add dpms_id_new_user_dpms_dpm_id_fk: % user_dpms rows reference missing dpms rows',
      orphan_count;
  end if;

  if not exists (
    select 1
    from pg_constraint
    where conrelid = 'user_dpms'::regclass
      and conname = 'dpms_id_new_user_dpms_dpm_id_fk'
  ) then
    alter table user_dpms
      add constraint dpms_id_new_user_dpms_dpm_id_fk
        foreign key (dpm_id) references dpms (dpm_id);
  end if;
end
$$;

commit;
