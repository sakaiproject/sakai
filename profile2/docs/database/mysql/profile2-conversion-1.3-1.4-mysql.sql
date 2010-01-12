/* add message tables and indexes */
/* TODO */

/* add gallery table and indexes */
/* TODO */

/* add the new email message preference column, default to 0, (PRFL-152) */
alter table PROFILE_PREFERENCES_T add EMAIL_PRIVATE_MESSAGE bit not null DEFAULT false;

/* add the new gallery column (PRFL-171)*/
alter table PROFILE_PRIVACY_T add MY_PICTURES int not null DEFAULT 0;
