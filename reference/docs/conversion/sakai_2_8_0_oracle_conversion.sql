-- This is the Oracle Sakai 2.7.1 -> 2.8.0 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.7.1 to 2.8.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

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

-- SAM-775
-- If you get an error when running this script, you will need to clean the duplicates first. Please refer to SAM-775.
create UNIQUE INDEX ASSESSMENTGRADINGID ON SAM_ITEMGRADING_T (ASSESSMENTGRADINGID, PUBLISHEDITEMID, PUBLISHEDITEMTEXTID, AGENTID, PUBLISHEDANSWERID);

-- Gradebook2 support
-- SAK-19080 / GRBK-736
alter table GB_GRADE_RECORD_T add USER_ENTERED_GRADE varchar2(127);


--MSGCNTR-309
--Start and End dates on Forums and Topics
alter table MFR_AREA_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_AREA_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_AREA_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) default 0 not null );

alter table MFR_AREA_T add (AVAILABILITY NUMBER(1,0));
update MFR_AREA_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_AREA_T modify (AVAILABILITY NUMBER(1,0) default 0 not null);

alter table MFR_AREA_T add (OPEN_DATE timestamp);

alter table MFR_AREA_T add (CLOSE_DATE timestamp);


alter table MFR_OPEN_FORUM_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_OPEN_FORUM_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) default 0 not null );

alter table MFR_OPEN_FORUM_T add (AVAILABILITY NUMBER(1,0));
update MFR_OPEN_FORUM_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY NUMBER(1,0) default 0 not null );

alter table MFR_OPEN_FORUM_T add (OPEN_DATE timestamp);

alter table MFR_OPEN_FORUM_T add (CLOSE_DATE timestamp);

alter table MFR_TOPIC_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_TOPIC_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) default 0 not null );

alter table MFR_TOPIC_T add (AVAILABILITY NUMBER(1,0));
update MFR_TOPIC_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY NUMBER(1,0) default 0 not null );

alter table MFR_TOPIC_T add (OPEN_DATE timestamp);

alter table MFR_TOPIC_T add (CLOSE_DATE timestamp);


--MSGCNTR-355
insert into MFR_TOPIC_T (ID, UUID, MODERATED, AUTO_MARK_THREADS_READ, SORT_INDEX, MUTABLE, TOPIC_DTYPE, VERSION, CREATED, CREATED_BY, MODIFIED, MODIFIED_BY, TITLE, SHORT_DESCRIPTION, EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID)

	(select MFR_TOPIC_S.nextval as ID, sys_guid() as UUID, MODERATED, 0 as AUTO_MARK_THREADS_READ, 3 as SORT_INDEX, 0 as MUTABLE, TOPIC_DTYPE, 0 as VERSION, sysdate as CREATED, CREATED_BY, sysdate as MODIFIED, MODIFIED_BY, 'pvt_drafts' as TITLE, 'short-desc' as SHORT_DESCRIPTION, 'ext-desc' as EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID from (
		select count(*) as c1, mtt.MODERATED, mtt.TOPIC_DTYPE, mtt.CREATED_BY, mtt.MODIFIED_BY, mtt.TYPE_UUID, mtt.pf_surrogateKey, mtt.USER_ID
		from MFR_PRIVATE_FORUM_T mpft, MFR_TOPIC_T mtt
		where mpft.ID = mtt.pf_surrogateKey and mpft.TYPE_UUID = mtt.TYPE_UUID
		Group By mtt.USER_ID, mtt.pf_surrogateKey, mtt.MODERATED, mtt.TOPIC_DTYPE, mtt.CREATED_BY, mtt.MODIFIED_BY, mtt.TYPE_UUID) s1
	where s1.c1 = 3);
    

--MSGCNTR-360
--Hibernate could have missed this index, if this fails, then the index may already be in the table
CREATE INDEX user_type_context_idx ON MFR_PVT_MSG_USR_T ( USER_ID, TYPE_UUID, CONTEXT_ID, READ_STATUS);

-- New column for Email Template service
-- SAK-18532/SAK-19522
alter table EMAIL_TEMPLATE_ITEM add column EMAILFROM varchar2(255);

-- SAK-18855
alter table POLL_POLL add POLL_IS_PUBLIC Number(1,0) default 0 not null;


-- Profile2 1.3-1.4 upgrade start

-- add company profile table and index (PRFL-224)
create table PROFILE_COMPANY_PROFILES_T (
	ID number(19,0) not null,
	USER_UUID varchar2(99) not null,
	COMPANY_NAME varchar2(255),
	COMPANY_DESCRIPTION varchar2(255),
	COMPANY_WEB_ADDRESS varchar2(255),
	primary key (ID)
);
create sequence COMPANY_PROFILES_S;
create index PROFILE_COMPANY_PROFILES_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);
 
-- add message tables and indexes
create table PROFILE_MESSAGES_T (
	ID varchar2(36) not null,
	FROM_UUID varchar2(99) not null,
	MESSAGE_BODY varchar2(4000) not null,
	MESSAGE_THREAD varchar2(36) not null,
	DATE_POSTED date not null,
	primary key (ID)
);

create table PROFILE_MESSAGE_PARTICIPANTS_T (
	ID number(19,0) not null,
	MESSAGE_ID varchar2(36) not null,
	PARTICIPANT_UUID varchar2(99) not null,
	MESSAGE_READ number(1,0) not null,
	MESSAGE_DELETED number(1,0) not null,
	primary key (ID)
);

create table PROFILE_MESSAGE_THREADS_T (
	ID varchar2(36) not null,
	SUBJECT varchar2(255) not null,
	primary key (ID)
);

create sequence PROFILE_MESSAGE_PARTICIPANTS_S;
create index PROFILE_MESSAGES_THREAD_I on PROFILE_MESSAGES_T (MESSAGE_THREAD);
create index PROFILE_MESSAGES_DATE_POSTED_I on PROFILE_MESSAGES_T (DATE_POSTED);
create index PROFILE_MESSAGES_FROM_UUID_I on PROFILE_MESSAGES_T (FROM_UUID);
create index PROFILE_MESSAGE_PARTICIPANT_UUID_I on PROFILE_MESSAGE_PARTICIPANTS_T (PARTICIPANT_UUID);
create index PROFILE_MESSAGE_PARTICIPANT_MESSAGE_ID_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_ID);
create index PROFILE_MESSAGE_PARTICIPANT_DELETED_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_DELETED);
create index PROFILE_MESSAGE_PARTICIPANT_READ_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_READ);

-- add gallery table and indexes (PRFL-134, PRFL-171)
create table PROFILE_GALLERY_IMAGES_T (
	ID number(19,0) not null,
	USER_UUID varchar2(99) not null,
	RESOURCE_MAIN varchar2(255) not null,
	RESOURCE_THUMB varchar2(255) not null,
	DISPLAY_NAME varchar2(255) not null,
	primary key (ID)
);
create sequence GALLERY_IMAGES_S;
create index PROFILE_GALLERY_IMAGES_USER_UUID_I on PROFILE_GALLERY_IMAGES_T (USER_UUID);

-- add social networking table (PRFL-252, PRFL-224)
create table PROFILE_SOCIAL_INFO_T (
	USER_UUID varchar2(99) not null,
	FACEBOOK_USERNAME varchar2(255),
	LINKEDIN_USERNAME varchar2(255),
	MYSPACE_USERNAME varchar2(255),
	SKYPE_USERNAME varchar2(255),
	TWITTER_USERNAME varchar2(255),
	primary key (USER_UUID)
);

-- add official image table
create table PROFILE_IMAGES_OFFICIAL_T (
	USER_UUID varchar2(99) not null,
	URL varchar2(4000) not null,
	primary key (USER_UUID)
);

-- add kudos table
create table PROFILE_KUDOS_T (
	USER_UUID varchar2(99) not null,
	SCORE number(10,0) not null,
	PERCENTAGE number(19,2) not null,
	DATE_ADDED date not null,
	primary key (USER_UUID)
);

-- add the new email message preference columns, default to 0, (PRFL-152, PRFL-186)
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_NEW number(1,0) default 0;
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_REPLY number(1,0) default 0;

-- add social networking privacy column (PRFL-285)
alter table PROFILE_PRIVACY_T add SOCIAL_NETWORKING_INFO number(1,0) default 0;

-- add the new gallery column (PRFL-171)
alter table PROFILE_PRIVACY_T add MY_PICTURES number(1,0) default 0;

-- add the new message column (PRFL-194)
alter table PROFILE_PRIVACY_T add MESSAGES number(1,0) default 0;

-- add the new businessInfo column (PRFL-210)
alter table PROFILE_PRIVACY_T add BUSINESS_INFO number(1,0) default 0;

-- add the new staff and student info columns and copy old ACADEMIC_INFO value into them to maintain privacy (PRFL-267)
alter table PROFILE_PRIVACY_T add STAFF_INFO number(1,0) default 0;
alter table PROFILE_PRIVACY_T add STUDENT_INFO number(1,0) default 0;
update PROFILE_PRIVACY_T set STAFF_INFO = ACADEMIC_INFO;
update PROFILE_PRIVACY_T set STUDENT_INFO = ACADEMIC_INFO;
alter table PROFILE_PRIVACY_T drop ACADEMIC_INFO;

-- add the new useOfficialImage column (PRFL-90)
alter table PROFILE_PREFERENCES_T add USE_OFFICIAL_IMAGE number(1,0) default 0;

-- remove search privacy setting (PRFL-293)
alter table PROFILE_PRIVACY_T drop SEARCH;

-- add kudos preference (PRFL-336)
alter table PROFILE_PREFERENCES_T add SHOW_KUDOS number(1,0) default 1;

-- add kudos privacy (PRFL-336)
alter table PROFILE_PRIVACY_T add MY_KUDOS number(1,0) default 0;

-- add gallery feed preference (PRFL-382)
alter table PROFILE_PREFERENCES_T add SHOW_GALLERY_FEED number(1,0) default 1;

-- Profile2 1.3-1.4 upgrade end

-- ShortenedUrlService 1.0.0 db creation start

create table URL_RANDOMISED_MAPPINGS_T (
	ID number(19,0) not null,
	TINY varchar2(255) not null,
	URL varchar2(4000) not null,
	primary key (ID)
);

create index URL_INDEX on URL_RANDOMISED_MAPPINGS_T (URL);
create index KEY_INDEX on URL_RANDOMISED_MAPPINGS_T (TINY);
create sequence URL_RANDOMISED_MAPPINGS_S;

-- ShortenedUrlService 1.0.0 db creation end