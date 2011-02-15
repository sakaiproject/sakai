-- SAK-16835 columns for new quartz version
alter table QRTZ_TRIGGERS add PRIORITY int;
alter table QRTZ_FIRED_TRIGGERS add PRIORITY int; 

-- SAK-17821 Add additional fields to SakaiPerson
alter table SAKAI_PERSON_T add STAFF_PROFILE varchar(4000);
alter table SAKAI_PERSON_T add UNIVERSITY_PROFILE_URL varchar(4000);
alter table SAKAI_PERSON_T add ACADEMIC_PROFILE_URL varchar(4000);
alter table SAKAI_PERSON_T add PUBLICATIONS varchar(4000);
alter table SAKAI_PERSON_T add BUSINESS_BIOGRAPHY varchar(4000);

-- New column for Email Template service
-- SAK-18532/SAK-19522
alter table EMAIL_TEMPLATE_ITEM add column EMAILFROM varchar2(255);

-- SAK-19951 added create statment for scheduler_trigger_events
create table scheduler_trigger_events (uuid varchar(36) not null, eventType varchar(255) not null, jobName varchar(255) not null, triggerName varchar(255) null, eventTime datetime not null, message text null, primary key (uuid));
