-- This is the MYSQL Sakai 2.7.1 -> 2.8.0 conversion script
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
alter table ANNOUNCEMENT_MESSAGE add column MESSAGE_ORDER int(11) default null;

drop index IE_ANNC_MSG_ATTRIB on ANNOUNCEMENT_MESSAGE;
create index IE_ANNC_MSG_ATTRIB on ANNOUNCEMENT_MESSAGE (DRAFT, PUBVIEW, OWNER, MESSAGE_ORDER);

drop index ANNOUNCEMENT_MESSAGE_CDD on ANNOUNCEMENT_MESSAGE;
create index ANNOUNCEMENT_MESSAGE_CDD on ANNOUNCEMENT_MESSAGE (CHANNEL_ID, MESSAGE_DATE, MESSAGE_ORDER, DRAFT);

-- SAK-18532/SAK-19522 new column for Email Template service
alter table EMAIL_TEMPLATE_ITEM add column EMAILFROM varchar(255) default null;

-- SAK-19448
alter table EMAIL_TEMPLATE_ITEM modify HTMLMESSAGE LONGTEXT;

-- SAK-19080 / GRBK-736 Gradebook2 support
alter table GB_GRADE_RECORD_T add column USER_ENTERED_GRADE varchar(255) default null;

-- MSGCNTR-309 start and end dates on Forums and Topics
alter table MFR_AREA_T add column AVAILABILITY_RESTRICTED bit;
update MFR_AREA_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is null;
alter table MFR_AREA_T modify column AVAILABILITY_RESTRICTED bit not null default false;

alter table MFR_AREA_T add column AVAILABILITY bit;
update MFR_AREA_T set AVAILABILITY=1 where AVAILABILITY is null;
alter table MFR_AREA_T modify column AVAILABILITY bit not null default true;

alter table MFR_AREA_T add column OPEN_DATE datetime;

alter table MFR_AREA_T add column CLOSE_DATE datetime;

alter table MFR_OPEN_FORUM_T add column AVAILABILITY_RESTRICTED bit;
update MFR_OPEN_FORUM_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is null;
alter table MFR_OPEN_FORUM_T modify column AVAILABILITY_RESTRICTED bit not null default false;

alter table MFR_OPEN_FORUM_T add column AVAILABILITY bit;
update MFR_OPEN_FORUM_T set AVAILABILITY=1 where AVAILABILITY is null;
alter table MFR_OPEN_FORUM_T modify column AVAILABILITY bit not null default true;

alter table MFR_OPEN_FORUM_T add column OPEN_DATE datetime;

alter table MFR_OPEN_FORUM_T add column CLOSE_DATE datetime;

alter table MFR_TOPIC_T add column AVAILABILITY_RESTRICTED bit;
update MFR_TOPIC_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is null;
alter table MFR_TOPIC_T modify column AVAILABILITY_RESTRICTED bit not null default false;

alter table MFR_TOPIC_T add column AVAILABILITY bit;
update MFR_TOPIC_T set AVAILABILITY=1 where AVAILABILITY is null;
alter table MFR_TOPIC_T modify column AVAILABILITY bit not null default true;

alter table MFR_TOPIC_T add column OPEN_DATE datetime null;
alter table MFR_TOPIC_T add column CLOSE_DATE datetime null;

-- MSGCNTR-355
insert into MFR_TOPIC_T (UUID, MODERATED, AUTO_MARK_THREADS_READ, SORT_INDEX, MUTABLE, TOPIC_DTYPE, VERSION, CREATED, CREATED_BY, MODIFIED, MODIFIED_BY, TITLE, SHORT_DESCRIPTION, EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID)

select UUID, MODERATED, AUTO_MARK_THREADS_READ, 3 as SORT_INDEX, 0 as MUTABLE, TOPIC_DTYPE, VERSION, CREATED, CREATED_BY, MODIFIED, MODIFIED_BY, TITLE, SHORT_DESCRIPTION, EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID from (
    select count(*) as c1, uuid() as UUID, mtt.MODERATED, mtt.AUTO_MARK_THREADS_READ, mtt.TOPIC_DTYPE, 0 as VERSION, mtt.CREATED, mtt.CREATED_BY, mtt.MODIFIED, mtt.MODIFIED_BY, 'pvt_drafts' as TITLE, 'short-desc' as SHORT_DESCRIPTION, 'ext-desc' as EXTENDED_DESCRIPTION, mtt.TYPE_UUID, mtt.pf_surrogateKey, mtt.USER_ID
    from MFR_PRIVATE_FORUM_T mpft, MFR_TOPIC_T mtt
    where mpft.ID = mtt.pf_surrogateKey and mpft.TYPE_UUID = mtt.TYPE_UUID
    Group By mtt.USER_ID, mtt.pf_surrogateKey) s1
where s1.c1 = 3;

-- MSGCNTR-360 Hibernate could have missed this index, if this fails, then the index may already be in the table
create index user_type_context_idx on MFR_PVT_MSG_USR_T (USER_ID(36), TYPE_UUID(36), CONTEXT_ID(36), READ_STATUS);

-- SAK-18855
alter table POLL_POLL add column POLL_IS_PUBLIC bit(1) not null default 0;
-- alter table POLL_POLL add column POLL_IS_PUBLIC bit not null default false;

-- Profile2 1.3-1.4 upgrade start

-- PRFL-224 add company profile table and index
create table PROFILE_COMPANY_PROFILES_T (
	ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	COMPANY_NAME varchar(255),
	COMPANY_DESCRIPTION text,
	COMPANY_WEB_ADDRESS varchar(255),
	primary key (ID)
);

create index PROFILE_CP_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);

-- add private messaging tables and indexes
create table PROFILE_MESSAGES_T (
	ID varchar(36) not null,
	FROM_UUID varchar(99) not null,
	MESSAGE_BODY text not null,
	MESSAGE_THREAD varchar(36) not null,
	DATE_POSTED datetime not null,
	primary key (ID)
);

create table PROFILE_MESSAGE_PARTICIPANTS_T (
	ID bigint not null auto_increment,
	MESSAGE_ID varchar(36) not null,
	PARTICIPANT_UUID varchar(99) not null,
	MESSAGE_READ bit not null,
	MESSAGE_DELETED bit not null,
	primary key (ID)
);

create table PROFILE_MESSAGE_THREADS_T (
	ID varchar(36) not null,
	SUBJECT varchar(255) not null,
	primary key (ID)
);
    
create index PROFILE_M_THREAD_I on PROFILE_MESSAGES_T (MESSAGE_THREAD);
create index PROFILE_M_DATE_POSTED_I on PROFILE_MESSAGES_T (DATE_POSTED);
create index PROFILE_M_FROM_UUID_I on PROFILE_MESSAGES_T (FROM_UUID);
create index PROFILE_M_P_UUID_I on PROFILE_MESSAGE_PARTICIPANTS_T (PARTICIPANT_UUID);
create index PROFILE_M_P_MESSAGE_ID_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_ID);
create index PROFILE_M_P_DELETED_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_DELETED);
create index PROFILE_M_P_READ_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_READ);

-- PRFL-134, PRFL-171 add gallery table and indexes
create table PROFILE_GALLERY_IMAGES_T (
	ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	RESOURCE_MAIN text not null,
	RESOURCE_THUMB text not null,
	DISPLAY_NAME varchar(255) not null,
	primary key (ID)
);
create index PROFILE_GI_USER_UUID_I on PROFILE_GALLERY_IMAGES_T (USER_UUID);

-- Data type changes
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN text not null;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB text not null;

-- PRFL-252, PRFL-224 add social networking table
create table PROFILE_SOCIAL_INFO_T (
	USER_UUID varchar(99) not null,
	FACEBOOK_URL varchar(255),
	LINKEDIN_URL varchar(255),
	MYSPACE_URL varchar(255),
	SKYPE_USERNAME varchar(255),
	TWITTER_URL varchar(255),
	primary key (USER_UUID)
);

-- add official image table
create table PROFILE_IMAGES_OFFICIAL_T (
	USER_UUID varchar(99) not null,
	URL text not null,
	primary key (USER_UUID)
);

-- add kudos table
create table PROFILE_KUDOS_T (
	USER_UUID varchar(99) not null,
	SCORE integer not null,
	PERCENTAGE numeric(19,2) not null,
	DATE_ADDED datetime not null,
	primary key (USER_UUID)
);

-- PRFL-152, PRFL-186 add the new email message preference columns, default to 0
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_NEW bit not null default false;
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_REPLY bit not null default false;

-- PRFL-285 add social networking privacy column
alter table PROFILE_PRIVACY_T add SOCIAL_NETWORKING_INFO int not null default 0;

-- PRFL-171 add the new gallery column
alter table PROFILE_PRIVACY_T add MY_PICTURES int not null default 0;

-- PRFL-194 add the new messages column, default to 1 (PRFL-593)
alter table PROFILE_PRIVACY_T add MESSAGES int not null default 1;

-- PRFL-210 add the new businessInfo column
alter table PROFILE_PRIVACY_T add BUSINESS_INFO int not null default 0;

-- PRFL-267 add the new staff and student info columns 
-- and copy old ACADEMIC_INFO value into them to maintain privacy
alter table PROFILE_PRIVACY_T add STAFF_INFO int not null default 0;
alter table PROFILE_PRIVACY_T add STUDENT_INFO int not null default 0;
update PROFILE_PRIVACY_T set STAFF_INFO = ACADEMIC_INFO;
update PROFILE_PRIVACY_T set STUDENT_INFO = ACADEMIC_INFO;
alter table PROFILE_PRIVACY_T drop ACADEMIC_INFO;

-- PRFL-90 add the new useOfficialImage column
alter table PROFILE_PREFERENCES_T add USE_OFFICIAL_IMAGE bit not null default false;

-- PRFL-293 remove search privacy setting
alter table PROFILE_PRIVACY_T drop SEARCH;

-- PRFL-336 add kudos preference
alter table PROFILE_PREFERENCES_T add SHOW_KUDOS bit not null default true;

-- PRFL-336 add kudos privacy
alter table PROFILE_PRIVACY_T add MY_KUDOS int not null default 0;

-- PRFL-382 add gallery feed preference
alter table PROFILE_PREFERENCES_T add SHOW_GALLERY_FEED bit not null default true;

-- PRFL-392 adjust size of the profile images resource uri columns
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN text;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB text;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN text;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB text;

-- PRFL-540 add indexes to commonly searched columns
create index PROFILE_FRIENDS_CONFIRMED_I on PROFILE_FRIENDS_T (CONFIRMED);
create index PROFILE_STATUS_DATE_ADDED_I on PROFILE_STATUS_T (DATE_ADDED);

-- Profile2 1.3-1.4 upgrade end

-- SAK-18864/SAK-19951/SAK-19965 adds missing scheduler_trigger_events table for new persistent jobscheduler event feature
create table scheduler_trigger_events (
    uuid varchar(36) not null, 
    eventType varchar(255) not null, 
    jobName varchar(255) not null, 
    triggerName varchar(255) default null, 
    eventTime datetime not null, 
    message text,
    primary key (uuid)
);

-- SAK-17821 Add additional fields to SakaiPerson
alter table SAKAI_PERSON_T add column STAFF_PROFILE text;
alter table SAKAI_PERSON_T add column UNIVERSITY_PROFILE_URL text;
alter table SAKAI_PERSON_T add column ACADEMIC_PROFILE_URL text;
alter table SAKAI_PERSON_T add column PUBLICATIONS text;
alter table SAKAI_PERSON_T add column BUSINESS_BIOGRAPHY text;

-- SAM-666
alter table SAM_ASSESSFEEDBACK_T add column FEEDBACKCOMPONENTOPTION integer default null;
update SAM_ASSESSFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2;
alter table SAM_PUBLISHEDFEEDBACK_T add column FEEDBACKCOMPONENTOPTION integer default null;
update SAM_PUBLISHEDFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2; 

-- SAM-971
alter table SAM_ASSESSMENTGRADING_T add column LASTVISITEDPART integer default null;
alter table SAM_ASSESSMENTGRADING_T add column LASTVISITEDQUESTION integer default null;

-- SAM-775
-- If you get an error when running this script, you will need to clean the duplicates first. Please refer to SAM-775.
create unique index ASSESSMENTGRADINGID on SAM_ITEMGRADING_T (ASSESSMENTGRADINGID, PUBLISHEDITEMID, PUBLISHEDITEMTEXTID, AGENTID, PUBLISHEDANSWERID);

-- SHORTURL-26 shortenedurlservice 1.0
create table URL_RANDOMISED_MAPPINGS_T (
	ID bigint not null auto_increment,
	TINY varchar(255) not null,
	URL text not null,
	primary key (ID)
);

create index URL_INDEX on URL_RANDOMISED_MAPPINGS_T (URL(200));
create index KEY_INDEX on URL_RANDOMISED_MAPPINGS_T (TINY);

-- KNL-563 table structure for sakai_message_bundle
create table SAKAI_MESSAGE_BUNDLE (
    ID bigint(20) not null auto_increment,
    MODULE_NAME varchar(255) not null,
    BASENAME varchar(255) not null,
    PROP_NAME varchar(255) not null,
    PROP_VALUE text,
    LOCALE varchar(255) not null,
    DEFAULT_VALUE text not null,
    primary key (ID)
);

create index SMB_SEARCH on SAKAI_MESSAGE_BUNDLE (BASENAME, MODULE_NAME, LOCALE, PROP_NAME); 

-- STAT-241 table structure for sst_presences
create table SST_PRESENCES (
    ID bigint(20) not null auto_increment,
    SITE_ID varchar(99) not null,
    USER_ID varchar(99) not null,
    P_DATE date not null,
    DURATION bigint(20) not null default '0',
    LAST_VISIT_START_TIME datetime default null,
    primary key (ID)
);

-- SAK-20076: missing Sitestats indexes
create index SST_PRESENCE_DATE_IX on SST_PRESENCES (P_DATE);
create index SST_PRESENCE_USER_ID_IX on SST_PRESENCES (USER_ID);
create index SST_PRESENCE_SITE_ID_IX on SST_PRESENCES (SITE_ID);
create index SST_PRESENCE_SUD_ID_IX on SST_PRESENCES (SITE_ID, USER_ID, P_DATE);

--  RES-2: table structure for validationaccount_item
create table VALIDATIONACCOUNT_ITEM (
    id bigint(20) not null auto_increment,
    USER_ID varchar(255) not null,
    VALIDATION_TOKEN varchar(255) not null,
    VALIDATION_SENT datetime default null,
    VALIDATION_RECEIVED datetime default null,
    VALIDATIONS_SENT int(11) default null,
    STATUS int(11) default null,
    FIRST_NAME varchar(255) not null,
    SURNAME varchar(255) not null,
    ACCOUNT_STATUS int(11) default null,
    PRIMARY KEY (id)
);
