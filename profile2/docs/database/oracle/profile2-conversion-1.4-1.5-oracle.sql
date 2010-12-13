/* add the gravatar column, default to 0, (PRFL-498) */
alter table PROFILE_PREFERENCES_T add USE_GRAVATAR number(1,0) default 0;

/* add the wall privacy setting, default to 0 (PRFL-513) */
alter table PROFILE_PRIVACY_T add MY_WALL number(1,0) default 0;