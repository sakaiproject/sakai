ALTER TABLE `signup_meetings`  ADD `allow_attendance` bit(1) default '\0';
ALTER TABLE `signup_ts_attendees`  ADD `attended` bit(1) default '\0';

