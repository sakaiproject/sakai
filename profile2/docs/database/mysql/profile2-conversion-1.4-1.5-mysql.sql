/* add the gravatar column, default to 0, (PRFL-498) */
alter table PROFILE_PREFERENCES_T add USE_GRAVATAR bit not null DEFAULT false;


