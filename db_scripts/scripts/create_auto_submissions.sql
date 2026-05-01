create table if not exists auto_submissions
(
  submitted          timestamp with time zone not null,
  submitted_date     date not null
    constraint auto_submissions_submitted_date_key
      unique,
  auto_submission_id serial
    constraint auto_submissions_pk
      primary key
);
