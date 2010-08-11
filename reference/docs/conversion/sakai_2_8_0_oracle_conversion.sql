-- SAK-16835 columns for new quartz version
alter table QRTZ_TRIGGERS add PRIORITY number(2);
alter table QRTZ_FIRED_TRIGGERS add PRIORITY number(2); 

-- SAK-17821 Add additional fields to SakaiPerson
alter table SAKAI_PERSON_T add STAFF_PROFILE varchar2(4000);
alter table SAKAI_PERSON_T add UNIVERSITY_PROFILE_URL varchar2(4000);
alter table SAKAI_PERSON_T add ACADEMIC_PROFILE_URL varchar2(4000);
alter table SAKAI_PERSON_T add PUBLICATIONS varchar2(4000);
alter table SAKAI_PERSON_T add BUSINESS_BIOGRAPHY varchar2(4000);

-- Samigo
-- SAM-666
alter table SAM_ASSESSFEEDBACK_T add FEEDBACKCOMPONENTOPTION number default null;
update SAM_ASSESSFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2;
alter table SAM_PUBLISHEDFEEDBACK_T add FEEDBACKCOMPONENTOPTION number default null;
update SAM_PUBLISHEDFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2;
 
-- SAM-756 (SAK-16822): oracle only
alter table SAM_ITEMTEXT_T add (TEMP_CLOB_TEXT clob);
update SAM_ITEMTEXT_T SET TEMP_CLOB_TEXT = TEXT;
alter table SAM_ITEMTEXT_T drop column TEXT;
alter table SAM_ITEMTEXT_T rename column TEMP_CLOB_TEXT to TEXT;
 	  
alter table SAM_PUBLISHEDITEMTEXT_T add (TEMP_CLOB_TEXT clob);
update SAM_PUBLISHEDITEMTEXT_T SET TEMP_CLOB_TEXT = TEXT;
alter table SAM_PUBLISHEDITEMTEXT_T drop column TEXT;
alter table SAM_PUBLISHEDITEMTEXT_T rename column TEMP_CLOB_TEXT to TEXT;
 	  
alter table SAM_ITEMGRADING_T add (TEMP_CLOB_TEXT clob);
update SAM_ITEMGRADING_T SET TEMP_CLOB_TEXT = ANSWERTEXT;
alter table SAM_ITEMGRADING_T drop column ANSWERTEXT;
alter table SAM_ITEMGRADING_T rename column TEMP_CLOB_TEXT to ANSWERTEXT; 

-- SAM-971
alter table SAM_ASSESSMENTGRADING_T add LASTVISITEDPART number(10,0) default null;
alter table SAM_ASSESSMENTGRADING_T add LASTVISITEDQUESTION number(10,0) default null;
