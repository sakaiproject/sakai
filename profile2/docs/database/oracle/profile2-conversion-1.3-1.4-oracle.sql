/* add message tables and indexes */
/* TODO */

/* add gallery table and indexes */
/* TODO */

/* add the new email message preference columns, default to 0, (PRFL-152, PRFL-186) */
alter table PROFILE_PREFERENCES_T add (EMAIL_MESSAGE_NEW number(1,0) default 0;
alter table PROFILE_PREFERENCES_T add (EMAIL_MESSAGE_REPLY number(1,0) default 0;

/* add the new gallery column (PRFL-171)*/
alter table PROFILE_PRIVACY_T add MY_PICTURES number(1,0) default 0;

/* add the new gallery column (PRFL-194)*/
alter table PROFILE_PRIVACY_T add MESSAGES number(1,0) default 0;