create table if not exists auto_submissions
(
  submitted          timestamp with time zone not null,
  auto_submission_id serial
    constraint auto_submissions_pk
      primary key
);
