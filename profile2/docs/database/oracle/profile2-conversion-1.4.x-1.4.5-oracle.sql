
/* incorrect default value for messages setting (PRFL-687) */
update PROFILE_PRIVACY_T set MESSAGES=1 where MESSAGES=0;
