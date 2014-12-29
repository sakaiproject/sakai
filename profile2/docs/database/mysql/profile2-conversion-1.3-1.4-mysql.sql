/* add company profile table and index (PRFL-224) */
create table PROFILE_COMPANY_PROFILES_T (
	ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	COMPANY_NAME varchar(255),
	COMPANY_DESCRIPTION text,
	COMPANY_WEB_ADDRESS varchar(255),
	primary key (ID)
);
create index PROFILE_CP_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);

/* add private messaging tables and indexes */
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


/* add gallery table and indexes (PRFL-134, PRFL-171) */
create table PROFILE_GALLERY_IMAGES_T (
	ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	RESOURCE_MAIN text not null,
	RESOURCE_THUMB text not null,
	DISPLAY_NAME varchar(255) not null,
	primary key (ID)
);
create index PROFILE_GI_USER_UUID_I on PROFILE_GALLERY_IMAGES_T (USER_UUID);

/* add social networking table (PRFL-252, PRFL-224) */
create table PROFILE_SOCIAL_INFO_T (
	USER_UUID varchar(99) not null,
	FACEBOOK_URL varchar(255),
	LINKEDIN_URL varchar(255),
	MYSPACE_URL varchar(255),
	SKYPE_USERNAME varchar(255),
	TWITTER_URL varchar(255),
	primary key (USER_UUID)
);

/* add official image table */
create table PROFILE_IMAGES_OFFICIAL_T (
	USER_UUID varchar(99) not null,
	URL text not null,
	primary key (USER_UUID)
);

/* add kudos table */
create table PROFILE_KUDOS_T (
	USER_UUID varchar(99) not null,
	SCORE integer not null,
	PERCENTAGE numeric(19,2) not null,
	DATE_ADDED datetime not null,
	primary key (USER_UUID)
);

/* add the new email message preference columns, default to 0, (PRFL-152, PRFL-186) */
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_NEW bit not null default false;
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_REPLY bit not null default false;

/* add social networking privacy column (PRFL-285) */
alter table PROFILE_PRIVACY_T add SOCIAL_NETWORKING_INFO int not null default 0;

/* add the new gallery column (PRFL-171) */
alter table PROFILE_PRIVACY_T add MY_PICTURES int not null default 0;

/* add the new messages column (PRFL-194), default to 1 (PRFL-593) */
alter table PROFILE_PRIVACY_T add MESSAGES int not null default 1;

/* add the new businessInfo column (PRFL-210) */
alter table PROFILE_PRIVACY_T add BUSINESS_INFO int not null default 0;

/* add the new staff and student info columns and copy old ACADEMIC_INFO value into them to maintain privacy (PRFL-267) */
alter table PROFILE_PRIVACY_T add STAFF_INFO int not null default 0;
alter table PROFILE_PRIVACY_T add STUDENT_INFO int not null default 0;
update PROFILE_PRIVACY_T set STAFF_INFO = ACADEMIC_INFO;
update PROFILE_PRIVACY_T set STUDENT_INFO = ACADEMIC_INFO;
alter table PROFILE_PRIVACY_T drop ACADEMIC_INFO;

/* add the new useOfficialImage column (PRFL-90) */
alter table PROFILE_PREFERENCES_T add USE_OFFICIAL_IMAGE bit not null default false;

/* remove search privacy setting (PRFL-293) */
alter table PROFILE_PRIVACY_T drop SEARCH;

/* add kudos preference (PRFL-336) */
alter table PROFILE_PREFERENCES_T add SHOW_KUDOS bit not null default true;

/* add kudos privacy (PRFL-336) */
alter table PROFILE_PRIVACY_T add MY_KUDOS int not null default 0;

/* add gallery feed preference (PRFL-382) */
alter table PROFILE_PREFERENCES_T add SHOW_GALLERY_FEED bit not null default true;

/* adjust size of the profile images resource uri columns (PRFL-392) */
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN text;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB text;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN text;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB text;

/* add indexes to commonly searched columns (PRFL-540) */
create index PROFILE_FRIENDS_CONFIRMED_I on PROFILE_FRIENDS_T (CONFIRMED);
create index PROFILE_STATUS_DATE_ADDED_I on PROFILE_STATUS_T (DATE_ADDED);


