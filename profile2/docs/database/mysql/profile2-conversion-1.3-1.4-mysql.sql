/* add company profile table and index (PRFL-224) */
create table PROFILE_COMPANY_PROFILES_T (
	ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	COMPANY_NAME varchar(255),
	COMPANY_DESCRIPTION varchar(255),
	COMPANY_WEB_ADDRESS varchar(255),
	primary key (ID)
);
create index PROFILE_COMPANY_PROFILES_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);

/* add message tables and indexes */
/* TODO */

/* add gallery table and indexes (PRFL-134, PRFL-171) */
create table PROFILE_GALLERY_IMAGES_T (
	ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	RESOURCE_MAIN varchar(255) not null,
	RESOURCE_THUMB varchar(255) not null,
	DISPLAY_NAME varchar(255) not null,
	primary key (ID)
);
create index PROFILE_GALLERY_IMAGES_USER_UUID_I on PROFILE_GALLERY_IMAGES_T (USER_UUID);

/* add social networking table (PRFL-252, PRFL-224) */
create table PROFILE_SOCIAL_INFO_T (
	USER_UUID varchar(99) not null,
	FACEBOOK_USERNAME varchar(255),
	LINKEDIN_USERNAME varchar(255),
	MYSPACE_USERNAME varchar(255),
	SKYPE_USERNAME varchar(255),
	TWITTER_USERNAME varchar(255),
	primary key (USER_UUID)
);

/* add official image table and indexes */
/* TODO */

/* add kudos table */
/* TODO */

/* add the new email message preference columns, default to 0, (PRFL-152, PRFL-186) */
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_NEW bit not null DEFAULT false;
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_REPLY bit not null DEFAULT false;

/* add social networking privacy column (PRFL-285) */
alter table PROFILE_PRIVACY_T add SOCIAL_NETWORKING_INFO int not null DEFAULT 0;

/* add the new gallery column (PRFL-171) */
alter table PROFILE_PRIVACY_T add MY_PICTURES int not null DEFAULT 0;

/* add the new messages column (PRFL-194) */
alter table PROFILE_PRIVACY_T add MESSAGES int not null DEFAULT 0;

/* add the new businessInfo column (PRFL-210) */
alter table PROFILE_PRIVACY_T add BUSINESS_INFO int not null DEFAULT 0;

/* add the new staff and student info columns and copy old ACADEMIC_INFO value into them to maintain privacy (PRFL-267) */
alter table PROFILE_PRIVACY_T add STAFF_INFO int not null DEFAULT 0;
alter table PROFILE_PRIVACY_T add STUDENT_INFO int not null DEFAULT 0;
update PROFILE_PRIVACY_T set STAFF_INFO = ACADEMIC_INFO;
update PROFILE_PRIVACY_T set STUDENT_INFO = ACADEMIC_INFO;
alter table PROFILE_PRIVACY_T drop ACADEMIC_INFO;

/* add the new useOfficialImage column (PRFL-90) */
alter table PROFILE_PREFERENCES_T add USE_OFFICIAL_IMAGE bit not null DEFAULT false;

/* remove search privacy setting (PRFL-293) */
alter table PROFILE_PRIVACY_T drop SEARCH;

/* add kudos preference (PRFL-336) */
alter table PROFILE_PREFERENCES_T add SHOW_KUDOS bit not null DEFAULT true;

/* add kudos privacy (PRFL-336) */
alter table PROFILE_PRIVACY_T add MY_KUDOS int not null DEFAULT 0;

/* add gallery feed preference (PRFL-382) */
alter table PROFILE_PREFERENCES_T add SHOW_GALLERY_FEED bit not null DEFAULT true;

/* remove twitter from preferences (PRFL-94) */
alter table PROFILE_PREFERENCES_T drop TWITTER_ENABLED;
alter table PROFILE_PREFERENCES_T drop TWITTER_USERNAME;
alter table PROFILE_PREFERENCES_T drop TWITTER_PASSWORD;

/* add external integration table (PRFL-94) */
create table PROFILE_EXTERNAL_INTEGRATION_T (
	USER_UUID varchar(99) not null,
	TWITTER_TOKEN varchar(255),
	TWITTER_SECRET varchar(255),
	primary key (USER_UUID)
);
