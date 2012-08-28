ALTER TABLE 'signup_meetings'  ADD  `default_send_email_by_owner` bit(1) default '\0';
ALTER TABLE 'signup_meetings'  ADD  'coordinators_user_Ids'   varchar(1000) default NULL;

