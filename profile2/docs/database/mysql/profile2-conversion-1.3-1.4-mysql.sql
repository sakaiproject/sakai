/* add company profile table and index */
create table PROFILE_COMPANY_PROFILES_T (
	ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	COMPANY_NAME varchar(255) not null,
	COMPANY_DESCRIPTION varchar(255) not null,
	COMPANY_WEB_ADDRESS varchar(255) not null,
	primary key (ID)
);
create index PROFILE_COMPANY_PROFILES_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);

/* add message tables and indexes */
/* TODO */

/* add gallery table and indexes */
create table PROFILE_GALLERY_IMAGES_T (
	ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	RESOURCE_MAIN varchar(255) not null,
	RESOURCE_THUMB varchar(255) not null,
	DISPLAY_NAME varchar(255) not null,
	primary key (ID)
);
create index PROFILE_GALLERY_IMAGES_USER_UUID_I on PROFILE_GALLERY_IMAGES_T (USER_UUID);

/* add official image table and indexes */
/* TODO */

/* add kudos table */
/* TODO */

/* add the new email message preference columns, default to 0, (PRFL-152, PRFL-186) */
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_NEW bit not null DEFAULT false;
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE_REPLY bit not null DEFAULT false;

/* add the new gallery column (PRFL-171) */
alter table PROFILE_PRIVACY_T add MY_PICTURES int not null DEFAULT 0;

/* add the new gallery column (PRFL-194) */
alter table PROFILE_PRIVACY_T add MESSAGES int not null DEFAULT 0;

/* add the new businessInfo column (PRFL-210) */
alter table PROFILE_PRIVACY_T add BUSINESS_INFO int not null DEFAULT 0;

/* add the new useOfficialImage column (PRFL-90) */
alter table PROFILE_PREFERENCES_T add USE_OFFICIAL_IMAGE bit not null DEFAULT false;

