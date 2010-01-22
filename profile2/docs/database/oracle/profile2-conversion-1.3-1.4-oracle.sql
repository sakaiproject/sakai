/* add message tables and indexes */
/* TODO */

/* add gallery table and indexes */
/* TODO */

/* add official image table and indexes */
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
