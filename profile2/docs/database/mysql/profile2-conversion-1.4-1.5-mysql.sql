/* add the gravatar column, default to 0, (PRFL-498) */
alter table PROFILE_PREFERENCES_T add USE_GRAVATAR bit not null DEFAULT false;

/* add the wall privacy setting, default to 0 (PRFL-513) */
alter table PROFILE_PRIVACY_T add MY_WALL int not null DEFAULT 0;