-- SAK-16835 columns for new quartz version
-- alter table QRTZ_TRIGGERS add column PRIORITY int;
-- alter table QRTZ_FIRED_TRIGGERS add column PRIORITY int;

-- SAK-18864, SAK-19951 adds missing scheduler_trigger_events table for new persistent jobscheduler event feature
create table scheduler_trigger_events (uuid varchar(36) PRIMARY KEY NOT NULL, type varchar(255) NOT NULL, jobName varchar(255) NOT NULL, triggerName varchar(255) DEFAULT NULL, time datetime NOT NULL, message text);

-- SAK-17821 Add additional fields to SakaiPerson
alter table SAKAI_PERSON_T add column STAFF_PROFILE text;
alter table SAKAI_PERSON_T add column UNIVERSITY_PROFILE_URL text;
alter table SAKAI_PERSON_T add column ACADEMIC_PROFILE_URL text;
alter table SAKAI_PERSON_T add column PUBLICATIONS text;
alter table SAKAI_PERSON_T add column BUSINESS_BIOGRAPHY text;

-- Samigo
-- SAM-666
alter table SAM_ASSESSFEEDBACK_T add column FEEDBACKCOMPONENTOPTION int(11) default null;
update SAM_ASSESSFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2;
alter table SAM_PUBLISHEDFEEDBACK_T add column FEEDBACKCOMPONENTOPTION int(11) default null;
update SAM_PUBLISHEDFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2; 

-- SAM-971
alter table SAM_ASSESSMENTGRADING_T add column LASTVISITEDPART integer default null;
alter table SAM_ASSESSMENTGRADING_T add column LASTVISITEDQUESTION integer default null;

-- Gradebook2 support
-- SAK-19080 / GRBK-736
alter table GB_GRADE_RECORD_T add column USER_ENTERED_GRADE varchar(127);

-- MSGCNTR-309
-- Start and End dates on Forums and Topics
alter table MFR_AREA_T add column AVAILABILITY_RESTRICTED bit;
update MFR_AREA_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_AREA_T modify column AVAILABILITY_RESTRICTED bit NOT NULL DEFAULT '';

alter table MFR_AREA_T add column AVAILABILITY bit;
update MFR_AREA_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_AREA_T modify column AVAILABILITY bit NOT NULL DEFAULT '';

alter table MFR_AREA_T add column OPEN_DATE datetime;

alter table MFR_AREA_T add column CLOSE_DATE datetime;

alter table MFR_OPEN_FORUM_T add column AVAILABILITY_RESTRICTED bit;
update MFR_OPEN_FORUM_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_OPEN_FORUM_T modify column AVAILABILITY_RESTRICTED bit NOT NULL DEFAULT '';

alter table MFR_OPEN_FORUM_T add column AVAILABILITY bit;
update MFR_OPEN_FORUM_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_OPEN_FORUM_T modify column AVAILABILITY bit NOT NULL DEFAULT '';

alter table MFR_OPEN_FORUM_T add column OPEN_DATE datetime;

alter table MFR_OPEN_FORUM_T add column CLOSE_DATE datetime;

alter table MFR_TOPIC_T add column AVAILABILITY_RESTRICTED bit;
update MFR_TOPIC_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_TOPIC_T modify column AVAILABILITY_RESTRICTED bit NOT NULL DEFAULT '';

alter table MFR_TOPIC_T add column AVAILABILITY bit;
update MFR_TOPIC_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_TOPIC_T modify column AVAILABILITY bit NOT NULL DEFAULT '';

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

-- MSGCNTR-360
-- Hibernate could have missed this index, if this fails, then the index may already be in the table
CREATE INDEX user_type_context_idx ON MFR_PVT_MSG_USR_T ( USER_ID(36), TYPE_UUID(36), CONTEXT_ID(36), READ_STATUS);

-- SAK-18532/SAK-19522 new column for Email Template service
alter table EMAIL_TEMPLATE_ITEM add column EMAILFROM text;

-- SAK-18855
alter table POLL_POLL add column POLL_IS_PUBLIC bit not null default false;

-- Profile2 1.3-1.4 upgrade start

-- add company profile table and index (PRFL-224)
create table PROFILE_COMPANY_PROFILES_T (
	ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	COMPANY_NAME varchar(255),
	COMPANY_DESCRIPTION text,
	COMPANY_WEB_ADDRESS varchar(255),
	primary key (ID)
);
create index PROFILE_COMPANY_PROFILES_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);

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
    
create index PROFILE_MESSAGES_THREAD_I on PROFILE_MESSAGES_T (MESSAGE_THREAD);
create index PROFILE_MESSAGES_DATE_POSTED_I on PROFILE_MESSAGES_T (DATE_POSTED);
create index PROFILE_MESSAGES_FROM_UUID_I on PROFILE_MESSAGES_T (FROM_UUID);
create index PROFILE_MESSAGE_PARTICIPANT_UUID_I on PROFILE_MESSAGE_PARTICIPANTS_T (PARTICIPANT_UUID);
create index PROFILE_MESSAGE_PARTICIPANT_MESSAGE_ID_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_ID);
create index PROFILE_MESSAGE_PARTICIPANT_DELETED_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_DELETED);
create index PROFILE_MESSAGE_PARTICIPANT_READ_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_READ);


-- add gallery table and indexes (PRFL-134, PRFL-171)
create table PROFILE_GALLERY_IMAGES_T (
	ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	RESOURCE_MAIN varchar(255) not null,
	RESOURCE_THUMB varchar(255) not null,
	DISPLAY_NAME varchar(255) not null,
	primary key (ID)
);
create index PROFILE_GALLERY_IMAGES_USER_UUID_I on PROFILE_GALLERY_IMAGES_T (USER_UUID);

-- add social networking table (PRFL-252, PRFL-224)
create table PROFILE_SOCIAL_INFO_T (
	USER_UUID varchar(99) not null,
	FACEBOOK_USERNAME varchar(255),
	LINKEDIN_USERNAME varchar(255),
	MYSPACE_USERNAME varchar(255),
	SKYPE_USERNAME varchar(255),
	TWITTER_USERNAME varchar(255),
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

-- add the new email message preference columns, default to 0, (PRFL-152, PRFL-186)
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_NEW bit not null DEFAULT false;
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_REPLY bit not null DEFAULT false;

-- add social networking privacy column (PRFL-285)
alter table PROFILE_PRIVACY_T add SOCIAL_NETWORKING_INFO int not null DEFAULT 0;

-- add the new gallery column (PRFL-171)
alter table PROFILE_PRIVACY_T add MY_PICTURES int not null DEFAULT 0;

-- add the new messages column (PRFL-194)
alter table PROFILE_PRIVACY_T add MESSAGES int not null DEFAULT 0;

-- add the new businessInfo column (PRFL-210)
alter table PROFILE_PRIVACY_T add BUSINESS_INFO int not null DEFAULT 0;

-- add the new staff and student info columns and copy old ACADEMIC_INFO value into them to maintain privacy (PRFL-267)
alter table PROFILE_PRIVACY_T add STAFF_INFO int not null DEFAULT 0;
alter table PROFILE_PRIVACY_T add STUDENT_INFO int not null DEFAULT 0;
update PROFILE_PRIVACY_T set STAFF_INFO = ACADEMIC_INFO;
update PROFILE_PRIVACY_T set STUDENT_INFO = ACADEMIC_INFO;
alter table PROFILE_PRIVACY_T drop ACADEMIC_INFO;

-- add the new useOfficialImage column (PRFL-90)
alter table PROFILE_PREFERENCES_T add USE_OFFICIAL_IMAGE bit not null DEFAULT false;

-- remove search privacy setting (PRFL-293)
alter table PROFILE_PRIVACY_T drop SEARCH;

-- add kudos preference (PRFL-336)
alter table PROFILE_PREFERENCES_T add SHOW_KUDOS bit not null DEFAULT true;

-- add kudos privacy (PRFL-336)
alter table PROFILE_PRIVACY_T add MY_KUDOS int not null DEFAULT 0;

-- add gallery feed preference (PRFL-382)
alter table PROFILE_PREFERENCES_T add SHOW_GALLERY_FEED bit not null DEFAULT true;

-- remove twitter from preferences (PRFL-94)
alter table PROFILE_PREFERENCES_T drop TWITTER_ENABLED;
alter table PROFILE_PREFERENCES_T drop TWITTER_USERNAME;
alter table PROFILE_PREFERENCES_T drop TWITTER_PASSWORD;

-- add external integration table (PRFL-94)
create table PROFILE_EXTERNAL_INTEGRATION_T (
	USER_UUID varchar(99) not null,
	TWITTER_TOKEN varchar(255),
	TWITTER_SECRET varchar(255),
	primary key (USER_UUID)
);
-- Profile2 1.3-1.4 upgrade end
