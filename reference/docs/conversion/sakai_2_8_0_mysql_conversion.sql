-- SAK-16835 columns for new quartz version
alter table QRTZ_TRIGGERS add column PRIORITY int;
alter table QRTZ_FIRED_TRIGGERS add column PRIORITY int;

-- SAK-17821 Add additional fields to SakaiPerson
alter table SAKAI_PERSON_T add STAFF_PROFILE text;
alter table SAKAI_PERSON_T add UNIVERSITY_PROFILE_URL text;
alter table SAKAI_PERSON_T add ACADEMIC_PROFILE_URL text;
alter table SAKAI_PERSON_T add PUBLICATIONS text;
alter table SAKAI_PERSON_T add BUSINESS_BIOGRAPHY text;

-- Samigo
-- SAM-666
alter table SAM_ASSESSFEEDBACK_T add FEEDBACKCOMPONENTOPTION int(11) default null;
update SAM_ASSESSFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2;
alter table SAM_PUBLISHEDFEEDBACK_T add FEEDBACKCOMPONENTOPTION int(11) default null;
update SAM_PUBLISHEDFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2; 
