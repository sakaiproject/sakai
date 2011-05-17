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

-- SAK-8005
alter table ANNOUNCEMENT_MESSAGE add MESSAGE_ORDER INT;

drop index IE_ANNC_MSG_ATTRIB;
create index IE_ANNC_MSG_ATTRIB on ANNOUNCEMENT_MESSAGE (DRAFT, PUBVIEW, OWNER, MESSAGE_ORDER);

drop index ANNOUNCEMENT_MESSAGE_CDD;
create index ANNOUNCEMENT_MESSAGE_CDD on ANNOUNCEMENT_MESSAGE (CHANNEL_ID, MESSAGE_DATE, MESSAGE_ORDER, DRAFT); 

-- SAK-17821 Add additional fields to SakaiPerson
alter table SAKAI_PERSON_T add STAFF_PROFILE varchar2(4000);
alter table SAKAI_PERSON_T add UNIVERSITY_PROFILE_URL varchar2(4000);
alter table SAKAI_PERSON_T add ACADEMIC_PROFILE_URL varchar2(4000);
alter table SAKAI_PERSON_T add PUBLICATIONS varchar2(4000);
alter table SAKAI_PERSON_T add BUSINESS_BIOGRAPHY varchar2(4000);

-- Samigo
-- SAM-666
alter table SAM_ASSESSFEEDBACK_T add FEEDBACKCOMPONENTOPTION number(10,0) default null;
update SAM_ASSESSFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2;
alter table SAM_PUBLISHEDFEEDBACK_T add FEEDBACKCOMPONENTOPTION number(10,0) default null;
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
alter table GB_GRADE_RECORD_T add USER_ENTERED_GRADE varchar2(255 CHAR);


--MSGCNTR-309
--Start and End dates on Forums and Topics
alter table MFR_AREA_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_AREA_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_AREA_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) default 0 not null );

alter table MFR_AREA_T add (AVAILABILITY NUMBER(1,0));
update MFR_AREA_T set AVAILABILITY=1 where AVAILABILITY is NULL;
alter table MFR_AREA_T modify (AVAILABILITY NUMBER(1,0) default 1 not null);

alter table MFR_AREA_T add (OPEN_DATE timestamp);

alter table MFR_AREA_T add (CLOSE_DATE timestamp);


alter table MFR_OPEN_FORUM_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_OPEN_FORUM_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) default 0 not null );

alter table MFR_OPEN_FORUM_T add (AVAILABILITY NUMBER(1,0));
update MFR_OPEN_FORUM_T set AVAILABILITY=1 where AVAILABILITY is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY NUMBER(1,0) default 1 not null );

alter table MFR_OPEN_FORUM_T add (OPEN_DATE timestamp);

alter table MFR_OPEN_FORUM_T add (CLOSE_DATE timestamp);

alter table MFR_TOPIC_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_TOPIC_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) default 0 not null );

alter table MFR_TOPIC_T add (AVAILABILITY NUMBER(1,0));
update MFR_TOPIC_T set AVAILABILITY=1 where AVAILABILITY is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY NUMBER(1,0) default 1 not null );

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
alter table EMAIL_TEMPLATE_ITEM add EMAILFROM varchar2(255 CHAR);

-- SAK-18855
alter table POLL_POLL add POLL_IS_PUBLIC number(1,0);


-- Profile2 1.3-1.4 upgrade start

-- add company profile table and index (PRFL-224)
create table PROFILE_COMPANY_PROFILES_T (
	ID number(19,0) not null,
	USER_UUID varchar2(99 CHAR) not null,
	COMPANY_NAME varchar2(255 CHAR),
	COMPANY_DESCRIPTION varchar2(4000),
	COMPANY_WEB_ADDRESS varchar2(255 CHAR),
	primary key (ID)
);
create sequence COMPANY_PROFILES_S;
create index PROFILE_CP_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);
 
-- add message tables and indexes
create table PROFILE_MESSAGES_T (
	ID varchar2(36 CHAR) not null,
	FROM_UUID varchar2(99 CHAR) not null,
	MESSAGE_BODY varchar2(4000) not null,
	MESSAGE_THREAD varchar2(36 CHAR) not null,
	DATE_POSTED timestamp(6) not null,
	primary key (ID)
);

create table PROFILE_MESSAGE_PARTICIPANTS_T (
	ID number(19,0) not null,
	MESSAGE_ID varchar2(36 CHAR) not null,
	PARTICIPANT_UUID varchar2(99 CHAR) not null,
	MESSAGE_READ number(1,0) not null,
	MESSAGE_DELETED number(1,0) not null,
	primary key (ID)
);

create table PROFILE_MESSAGE_THREADS_T (
	ID varchar2(36 CHAR) not null,
	SUBJECT varchar2(255 CHAR) not null,
	primary key (ID)
);

create sequence PROFILE_MESSAGE_PARTICIPANTS_S;
create index PROFILE_M_THREAD_I on PROFILE_MESSAGES_T (MESSAGE_THREAD);
create index PROFILE_M_DATE_POSTED_I on PROFILE_MESSAGES_T (DATE_POSTED);
create index PROFILE_M_FROM_UUID_I on PROFILE_MESSAGES_T (FROM_UUID);
create index PROFILE_M_P_UUID_I on PROFILE_MESSAGE_PARTICIPANTS_T (PARTICIPANT_UUID);
create index PROFILE_M_P_MESSAGE_ID_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_ID);
create index PROFILE_M_P_DELETED_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_DELETED);
create index PROFILE_M_P_READ_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_READ);

-- add gallery table and indexes (PRFL-134, PRFL-171)
create table PROFILE_GALLERY_IMAGES_T (
	ID number(19,0) not null,
	USER_UUID varchar2(99 CHAR) not null,
	RESOURCE_MAIN varchar2(4000) not null,
	RESOURCE_THUMB varchar2(4000) not null,
	DISPLAY_NAME varchar2(255 CHAR) not null,
	primary key (ID)
);
create sequence GALLERY_IMAGES_S;
create index PROFILE_GI_USER_UUID_I on PROFILE_GALLERY_IMAGES_T (USER_UUID);

-- add social networking table (PRFL-252, PRFL-224)
create table PROFILE_SOCIAL_INFO_T (
	USER_UUID varchar2(99 CHAR) not null,
	FACEBOOK_URL varchar2(255 CHAR),
	LINKEDIN_URL varchar2(255 CHAR),
	MYSPACE_URL varchar2(255 CHAR),
	SKYPE_USERNAME varchar2(255 CHAR),
	TWITTER_URL varchar2(255 CHAR),
	primary key (USER_UUID)
);

-- add official image table
create table PROFILE_IMAGES_OFFICIAL_T (
	USER_UUID varchar2(99 CHAR) not null,
	URL varchar2(4000) not null,
	primary key (USER_UUID)
);

-- add kudos table
create table PROFILE_KUDOS_T (
	USER_UUID varchar2(99 CHAR) not null,
	SCORE number(10,0) not null,
	PERCENTAGE number(19,2) not null,
	DATE_ADDED timestamp(6) not null,
	primary key (USER_UUID)
);

-- add the new email message preference columns, default to 0, (PRFL-152, PRFL-186)
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_NEW number(1,0) default 0 not null;
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_REPLY number(1,0) default 0 not null;

-- add social networking privacy column (PRFL-285)
alter table PROFILE_PRIVACY_T add SOCIAL_NETWORKING_INFO number(10,0) default 0 not null;

-- add the new gallery column (PRFL-171)
alter table PROFILE_PRIVACY_T add MY_PICTURES number(10,0) default 0 not null;

-- add the new message column (PRFL-194), default to 1 (PRFL-593)
alter table PROFILE_PRIVACY_T add MESSAGES number(10,0) default 1 not null;

-- add the new businessInfo column (PRFL-210)
alter table PROFILE_PRIVACY_T add BUSINESS_INFO number(10,0) default 0 not null;

-- add the new staff and student info columns and copy old ACADEMIC_INFO value into them to maintain privacy (PRFL-267)
alter table PROFILE_PRIVACY_T add STAFF_INFO number(10,0) default 0 not null;
alter table PROFILE_PRIVACY_T add STUDENT_INFO number(10,0) default 0 not null;
update PROFILE_PRIVACY_T set STAFF_INFO = ACADEMIC_INFO;
update PROFILE_PRIVACY_T set STUDENT_INFO = ACADEMIC_INFO;
alter table PROFILE_PRIVACY_T drop column ACADEMIC_INFO;

-- add the new useOfficialImage column (PRFL-90)
alter table PROFILE_PREFERENCES_T add USE_OFFICIAL_IMAGE number(1,0) default 0 not null;

-- remove search privacy setting (PRFL-293)
alter table PROFILE_PRIVACY_T drop column SEARCH;

-- add kudos preference (PRFL-336)
alter table PROFILE_PREFERENCES_T add SHOW_KUDOS number(1,0) default 1 not null;

-- add kudos privacy (PRFL-336)
alter table PROFILE_PRIVACY_T add MY_KUDOS number(10,0) default 0 not null;

-- add gallery feed preference (PRFL-382)
alter table PROFILE_PREFERENCES_T add SHOW_GALLERY_FEED number(1,0) default 1 not null;

-- adjust size of the profile images resource uri columns (PRFL-392)
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN varchar2(4000);
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB varchar2(4000);
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN varchar2(4000);
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB varchar2(4000);

-- add indexes to commonly searched columns (PRFL-540)
create index PROFILE_FRIENDS_CONFIRMED_I on PROFILE_FRIENDS_T (CONFIRMED);
create index PROFILE_STATUS_DATE_ADDED_I on PROFILE_STATUS_T (DATE_ADDED);

-- Profile2 1.3-1.4 upgrade end

-- SHORTURL-26 shortenedurlservice 1.0
create table URL_RANDOMISED_MAPPINGS_T (
	ID number(19,0) not null,
	TINY varchar2(255 CHAR) not null,
	URL varchar2(4000) not null,
	primary key (ID)
);

create index URL_INDEX on URL_RANDOMISED_MAPPINGS_T (URL);
create index KEY_INDEX on URL_RANDOMISED_MAPPINGS_T (TINY);
create sequence URL_RANDOMISED_MAPPINGS_S;

-- SAK-18864/SAK-19951/SAK-19965 added create statement for scheduler_trigger_events
create table SCHEDULER_TRIGGER_EVENTS (
	UUID varchar2(36 CHAR) NOT NULL,
	EVENTTYPE varchar2(255 CHAR) NOT NULL,
	JOBNAME varchar2(255 CHAR) NOT NULL,
	TRIGGERNAME varchar2(255 CHAR),
	EVENTTIME timestamp NOT NULL,
	MESSAGE clob,
	primary key (UUID)
);

-- STAT-241: Tracking of time spent in site
create table SST_PRESENCES (
	ID number(19,0) not null,
	SITE_ID varchar2(99 char) not null,
	USER_ID varchar2(99 char) not null,
	P_DATE date not null,
	DURATION number(19,0) default 0 not null,
	LAST_VISIT_START_TIME timestamp default null,
	primary key (ID)
);

-- STAT-286: missing SiteStats sequence
create sequence SST_PRESENCE_ID;

-- SAK-20076: missing Sitestats indexes
create index SST_PRESENCE_DATE_IX on SST_PRESENCES (P_DATE);
create index SST_PRESENCE_USER_ID_IX on SST_PRESENCES (USER_ID);
create index SST_PRESENCE_SITE_ID_IX on SST_PRESENCES (SITE_ID);
create index SST_PRESENCE_SUD_ID_IX on SST_PRESENCES (SITE_ID, USER_ID, P_DATE);

-- KNL-563: dynamic bundling loading
CREATE TABLE SAKAI_MESSAGE_BUNDLE(
        ID NUMBER(19) NOT NULL,
        MODULE_NAME VARCHAR2(255 CHAR) NOT NULL,
        BASENAME VARCHAR2(255 CHAR) NOT NULL,
        PROP_NAME VARCHAR2(255 CHAR) NOT NULL,
        PROP_VALUE VARCHAR2(4000 CHAR),
        LOCALE VARCHAR2(255 CHAR) NOT NULL,
        DEFAULT_VALUE VARCHAR2(4000 CHAR) NOT NULL,
        PRIMARY KEY (ID)
);
create sequence SAKAI_MESSAGEBUNDLE_S; 
create index SMB_SEARCH on sakai_message_bundle (BASENAME, MODULE_NAME, LOCALE, PROP_NAME); 

-- RES-2: table structure for validationaccount_item
CREATE TABLE VALIDATIONACCOUNT_ITEM (
        ID NUMBER(19) NOT NULL,
        USER_ID VARCHAR2(255 CHAR) NOT NULL,
        VALIDATION_TOKEN VARCHAR2(255 CHAR) NOT NULL,
        VALIDATION_SENT TIMESTAMP(6),
        VALIDATION_RECEIVED TIMESTAMP(6),
        VALIDATIONS_SENT NUMBER(10),
        STATUS NUMBER(10),
        FIRST_NAME VARCHAR2(255 CHAR) NOT NULL,
        SURNAME VARCHAR2(255 CHAR) NOT NULL,
        ACCOUNT_STATUS NUMBER(10),
        PRIMARY KEY (ID)
);

create sequence VALIDATIONACCOUNT_ITEM_ID_SEQ;

