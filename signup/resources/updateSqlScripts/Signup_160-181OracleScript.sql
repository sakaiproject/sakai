ALTER TABLE signup_meetings  ADD  default_send_email_by_owner   number(1,0)  default '0' NULL;
ALTER TABLE signup_meetings  ADD  coordinators_user_Ids   varchar2(1000 char)  default NULL;