-- this script should be used to upgrade your database from any prior version of signups to the 1.0 release.

--Signup-80
ALTER TABLE signup_meetings ADD category varchar2(255) not null;

ALTER TABLE signup_meetings ADD	create_groups number(1,0) default '0' NULL;	

ALTER TABLE signup_ts ADD group_id VARCHAR2(255)  NULL;

-- add the UUID columns: Signup-60
ALTER TABLE signup_meetings ADD vevent_uuid VARCHAR2(255)  NULL;
ALTER TABLE signup_ts ADD vevent_uuid VARCHAR2(255)  NULL;


-- Signup _179-181
ALTER TABLE signup_meetings  ADD  default_send_email_by_owner   number(1,0)  default '0' NULL;
ALTER TABLE signup_meetings  ADD  coordinators_user_Ids   varchar2(1000 char)  default NULL;

