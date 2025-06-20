-- Code 14 for Good, 9 for DNS
insert into public.dpms (dpm_group_id, name, points, w2w_color_id)
values (1, 'Picked Up Block', 1, 15),
       (1, 'Good!', 1, null),
       (1, 'Voluntary Clinic/Road Test Passed', 2, null),
       (1, '200 Hours Safe', 2, null),
       (1, 'Custom', 5, null),
       (2, '1-5 Minutes Late to OFF', -1, null),
       (3, '1-5 Minutes Late to BLK', -1, null),
       (3, 'Missed Email Announcement', -2, null),
       (3, 'Improper Shutdown', -2, null),
       (3, 'Off-Route', -2, null),
       (3, '6-15 Minutes Late to BLK', -3, null),
       (3, 'Out of Uniform', -5, null),
       (3, 'Improper Radio Procedure', -2, null),
       (3, 'Improper Bus Log', -5, null),
       (3, 'Timesheet/Improper Book Change', -5, null),
       (3, 'Custom', -5, null),
       (4, 'Passenger Inconvenience', -5, null),
       (4, '16+ Minutes Late', -5, null),
       (4, 'Attendance Infraction', -10, null),
       (4, 'Moving Down Bus', -10, null),
       (4, 'Improper 10-50 Procedure', -10, null),
       (4, 'Failed Ride-Along/Road Test', -10, null),
       (4, 'Custom', -10, null),
       (5, 'Failure to Report 10-50', -15, null),
       (5, 'Insubordination', -15, null),
       (5, 'Safety Offense', -15, null),
       (5, 'Preventable Accident 1, 2', -15, null),
       (5, 'Custom', -15, null),
       (6, 'DNS/Did Not Show', -10, 10),
       (6, 'Preventable Accident 3, 4', -20, null)
;