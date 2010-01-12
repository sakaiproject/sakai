/* add message tables and indexes */
/* TODO */

/* add gallery table and indexes */
/* TODO */


/* add the new email message preference column, default to 0, (PRFL-152) */
alter table PROFILE_PREFERENCES_T add (EMAIL_PRIVATE_MESSAGE number(1,0) default 0;

/* add the new gallery column (PRFL-171)*/
alter table PROFILE_PRIVACY_T add MY_PICTURES number(1,0) default 0;