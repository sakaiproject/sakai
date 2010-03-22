/* add company profile table and index */
create table PROFILE_COMPANY_PROFILES_T (
	ID number(19,0) not null,
	USER_UUID varchar2(99) not null,
	COMPANY_NAME varchar2(255) not null,
	COMPANY_DESCRIPTION varchar2(255) not null,
	COMPANY_WEB_ADDRESS varchar2(255) not null,
	primary key (ID)
);
create sequence COMPANY_PROFILES_S;
create index PROFILE_COMPANY_PROFILES_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);
 
/* add message tables and indexes */
/* TODO */

/* add gallery table and indexes */
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

/* add official image table and indexes */
/* TODO */

/* add kudos table */
/* TODO */

/* add the new email message preference columns, default to 0, (PRFL-152, PRFL-186) */
alter table PROFILE_PREFERENCES_T add (EMAIL_MESSAGE_NEW number(1,0) default 0;
alter table PROFILE_PREFERENCES_T add (EMAIL_MESSAGE_REPLY number(1,0) default 0;

/* add the new gallery column (PRFL-171) */
alter table PROFILE_PRIVACY_T add MY_PICTURES number(1,0) default 0;

/* add the new gallery column (PRFL-194) */
alter table PROFILE_PRIVACY_T add MESSAGES number(1,0) default 0;

/* add the new businessInfo column (PRFL-210) */
alter table PROFILE_PRIVACY_T add BUSINESS_INFO number(1,0) default 0;

/* add the new useOfficialImage column (PRFL-90) */
alter table PROFILE_PREFERENCES_T add (USE_OFFICIAL_IMAGE number(1,0) default 0;
