/* add company profile table and index (PRFL-224) */
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
 
/* add message tables and indexes */
/* TODO */

/* add gallery table and indexes (PRFL-134, PRFL-171) */
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

/* add social networking table (PRFL-252, PRFL-224) */
create table PROFILE_SOCIAL_INFO_T (
	USER_UUID varchar2(99) not null,
	FACEBOOK_USERNAME varchar2(255),
	LINKEDIN_USERNAME varchar2(255),
	MYSPACE_USERNAME varchar2(255),
	SKYPE_USERNAME varchar2(255),
	TWITTER_USERNAME varchar2(255),
	primary key (USER_UUID)
);

/* add official image table and indexes */
/* TODO */

/* add kudos table */
/* TODO */

/* add the new email message preference columns, default to 0, (PRFL-152, PRFL-186) */
alter table PROFILE_PREFERENCES_T add (EMAIL_MESSAGE_NEW number(1,0) default 0;
alter table PROFILE_PREFERENCES_T add (EMAIL_MESSAGE_REPLY number(1,0) default 0;

/* add social networking privacy column (PRFL-285) */
alter table PROFILE_PRIVACY_T add SOCIAL_NETWORKING_INFO number(1,0) default 0;

/* add the new gallery column (PRFL-171) */
alter table PROFILE_PRIVACY_T add MY_PICTURES number(1,0) default 0;

/* add the new message column (PRFL-194) */
alter table PROFILE_PRIVACY_T add MESSAGES number(1,0) default 0;

/* add the new businessInfo column (PRFL-210) */
alter table PROFILE_PRIVACY_T add BUSINESS_INFO number(1,0) default 0;

/* add the new staff and student info columns and copy old ACADEMIC_INFO value into them to maintain privacy (PRFL-267) */
alter table PROFILE_PRIVACY_T add STAFF_INFO number(1,0) default 0;
alter table PROFILE_PRIVACY_T add STUDENT_INFO number(1,0) default 0;
update PROFILE_PRIVACY_T set STAFF_INFO = ACADEMIC_INFO;
update PROFILE_PRIVACY_T set STUDENT_INFO = ACADEMIC_INFO;
alter table PROFILE_PRIVACY_T drop ACADEMIC_INFO;

/* add the new useOfficialImage column (PRFL-90) */
alter table PROFILE_PREFERENCES_T add (USE_OFFICIAL_IMAGE number(1,0) default 0;
