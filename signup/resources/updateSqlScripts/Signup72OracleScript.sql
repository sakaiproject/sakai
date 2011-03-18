ALTER TABLE signup_meetings  ADD  allow_attendance   number(1,0)  default '0' NULL;
ALTER TABLE signup_ts_attendees  ADD  attended   number(1,0)  default '0' NULL;
