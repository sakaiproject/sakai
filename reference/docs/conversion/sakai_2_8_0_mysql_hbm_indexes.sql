-- SAK-20005 
-- Starting up sakai-2.8.0 in order to populate an empty MySQL 5 database (auto.ddl=true) can result 
-- in certain tools relying on Hibernate 3.2.7.ga to generate indexes to fail to do so.

-- Check your database and run this script if indexes are missing.

-- Note: create index statements that have been commented out have been included for review purposes.

-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- KNL-563
create index SMB_SEARCH on SAKAI_MESSAGE_BUNDLE (BASENAME, MODULE_NAME, LOCALE, PROP_NAME); 

-- MSGCNTR-360
create index user_type_context_idx on MFR_PVT_MSG_USR_T (USER_ID(36), TYPE_UUID(36), CONTEXT_ID(36), READ_STATUS);

-- PRFL-224
create index PROFILE_CP_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);
create index PROFILE_M_THREAD_I on PROFILE_MESSAGES_T (MESSAGE_THREAD);
create index PROFILE_M_DATE_POSTED_I on PROFILE_MESSAGES_T (DATE_POSTED);
create index PROFILE_M_FROM_UUID_I on PROFILE_MESSAGES_T (FROM_UUID);
create index PROFILE_M_P_UUID_I on PROFILE_MESSAGE_PARTICIPANTS_T (PARTICIPANT_UUID);
create index PROFILE_M_P_MESSAGE_ID_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_ID);
create index PROFILE_M_P_DELETED_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_DELETED);
create index PROFILE_M_P_READ_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_READ);

-- PRFL-134, PRFL-171
create index PROFILE_GI_USER_UUID_I on PROFILE_GALLERY_IMAGES_T (USER_UUID);

-- PRFL-540
create index PROFILE_FRIENDS_CONFIRMED_I on PROFILE_FRIENDS_T (CONFIRMED);
create index PROFILE_STATUS_DATE_ADDED_I on PROFILE_STATUS_T (DATE_ADDED);

-- SAM-775 if you get an error when running this script you will need to clean the duplicates first. See SAM-775 for details.
-- create unique index ASSESSMENTGRADINGID on SAM_ITEMGRADING_T (ASSESSMENTGRADINGID, PUBLISHEDITEMID, PUBLISHEDITEMTEXTID, AGENTID, PUBLISHEDANSWERID);

-- SAK-20076 missing Sitestats indexes
create index SST_PRESENCE_DATE_IX on SST_PRESENCES (P_DATE);
create index SST_PRESENCE_USER_ID_IX on SST_PRESENCES (USER_ID);
create index SST_PRESENCE_SITE_ID_IX on SST_PRESENCES (SITE_ID);
create index SST_PRESENCE_SUD_ID_IX on SST_PRESENCES (SITE_ID, USER_ID, P_DATE);

-- SHORTURL-27
create index URL_INDEX on URL_RANDOMISED_MAPPINGS_T (URL(200));
create index KEY_INDEX on URL_RANDOMISED_MAPPINGS_T (TINY);
