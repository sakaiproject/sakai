/* add message table and indexes */


/* add the new email message preference column, default to 0, (PRFL-152) */
alter table PROFILE_PREFERENCES_T add EMAIL_MESSAGE bit not null DEFAULT false;
