alter table auto_submissions
  add column if not exists submitted_date date;

update auto_submissions
set submitted_date = (submitted at time zone 'America/New_York')::date
where submitted_date is null;

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
