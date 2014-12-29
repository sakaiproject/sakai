/* add company profile table and index (PRFL-224) */
create table PROFILE_COMPANY_PROFILES_T (
	ID number(19,0) not null,
	USER_UUID varchar2(99) not null,
	COMPANY_NAME varchar2(255),
	COMPANY_DESCRIPTION varchar2(4000),
	COMPANY_WEB_ADDRESS varchar2(255),
	primary key (ID)
);
create sequence COMPANY_PROFILES_S;
create index PROFILE_CP_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);
 
/* add message tables and indexes */
create table PROFILE_MESSAGES_T (
	ID varchar2(36) not null,
	FROM_UUID varchar2(99) not null,
	MESSAGE_BODY varchar2(4000) not null,
	MESSAGE_THREAD varchar2(36) not null,
	DATE_POSTED timestamp(6) not null,
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
create index PROFILE_M_THREAD_I on PROFILE_MESSAGES_T (MESSAGE_THREAD);
create index PROFILE_M_DATE_POSTED_I on PROFILE_MESSAGES_T (DATE_POSTED);
create index PROFILE_M_FROM_UUID_I on PROFILE_MESSAGES_T (FROM_UUID);
create index PROFILE_M_P_UUID_I on PROFILE_MESSAGE_PARTICIPANTS_T (PARTICIPANT_UUID);
create index PROFILE_M_P_MESSAGE_ID_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_ID);
create index PROFILE_M_P_DELETED_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_DELETED);
create index PROFILE_M_P_READ_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_READ);

/* add gallery table and indexes (PRFL-134, PRFL-171) */
create table PROFILE_GALLERY_IMAGES_T (
	ID number(19,0) not null,
	USER_UUID varchar2(99) not null,
	RESOURCE_MAIN varchar2(4000) not null,
	RESOURCE_THUMB varchar2(4000) not null,
	DISPLAY_NAME varchar2(255) not null,
	primary key (ID)
);
create sequence GALLERY_IMAGES_S;
create index PROFILE_GI_USER_UUID_I on PROFILE_GALLERY_IMAGES_T (USER_UUID);

/* add social networking table (PRFL-252, PRFL-224) */
create table PROFILE_SOCIAL_INFO_T (
	USER_UUID varchar2(99) not null,
	FACEBOOK_URL varchar2(255),
	LINKEDIN_URL varchar2(255),
	MYSPACE_URL varchar2(255),
	SKYPE_USERNAME varchar2(255),
	TWITTER_URL varchar2(255),
	primary key (USER_UUID)
);

/* add official image table */
create table PROFILE_IMAGES_OFFICIAL_T (
	USER_UUID varchar2(99) not null,
	URL varchar2(4000) not null,
	primary key (USER_UUID)
);

/* add kudos table */
create table PROFILE_KUDOS_T (
	USER_UUID varchar2(99) not null,
	SCORE number(10,0) not null,
	PERCENTAGE number(19,2) not null,
	DATE_ADDED timestamp(6) not null,
	primary key (USER_UUID)
);

/* add the new email message preference columns, default to 0, (PRFL-152, PRFL-186) */
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_NEW number(1,0) default 0 not null;
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_REPLY number(1,0) default 0 not null;

/* add social networking privacy column (PRFL-285) */
alter table PROFILE_PRIVACY_T add SOCIAL_NETWORKING_INFO number(10,0) default 0 not null;

/* add the new gallery column (PRFL-171) */
alter table PROFILE_PRIVACY_T add MY_PICTURES number(10,0) default 0 not null;

/* add the new message column (PRFL-194), default to 1 (PRFL-593)  */
alter table PROFILE_PRIVACY_T add MESSAGES number(10,0) default 1 not null;

/* add the new businessInfo column (PRFL-210) */
alter table PROFILE_PRIVACY_T add BUSINESS_INFO number(10,0) default 0 not null;

/* add the new staff and student info columns and copy old ACADEMIC_INFO value into them to maintain privacy (PRFL-267) */
alter table PROFILE_PRIVACY_T add STAFF_INFO number(10,0) default 0 not null;
alter table PROFILE_PRIVACY_T add STUDENT_INFO number(10,0) default 0 not null;
update PROFILE_PRIVACY_T set STAFF_INFO = ACADEMIC_INFO;
update PROFILE_PRIVACY_T set STUDENT_INFO = ACADEMIC_INFO;
alter table PROFILE_PRIVACY_T drop column ACADEMIC_INFO;

/* add the new useOfficialImage column (PRFL-90) */
alter table PROFILE_PREFERENCES_T add USE_OFFICIAL_IMAGE number(1,0) default 0 not null;

/* remove search privacy setting (PRFL-293) */
alter table PROFILE_PRIVACY_T drop column SEARCH;

/* add kudos preference (PRFL-336) */
alter table PROFILE_PREFERENCES_T add SHOW_KUDOS number(1,0) default 1 not null;

/* add kudos privacy (PRFL-336) */
alter table PROFILE_PRIVACY_T add MY_KUDOS number(10,0) default 0 not null;

/* add gallery feed preference (PRFL-382) */
alter table PROFILE_PREFERENCES_T add SHOW_GALLERY_FEED number(1,0) default 1 not null;

/* adjust size of the profile images resource uri columns (PRFL-392) */
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN varchar2(4000);
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB varchar2(4000);
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN varchar2(4000);
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB varchar2(4000);

/* add indexes to commonly searched columns (PRFL-540) */
create index PROFILE_FRIENDS_CONFIRMED_I on PROFILE_FRIENDS_T (CONFIRMED);
create index PROFILE_STATUS_DATE_ADDED_I on PROFILE_STATUS_T (DATE_ADDED);

