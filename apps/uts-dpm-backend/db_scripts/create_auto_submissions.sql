create table if not exists auto_submissions
(
  submitted          timestamp default now(),
  auto_submission_id serial
    constraint auto_submissions_pk
      primary key
);

alter table auto_submissions
  owner to postgres;
