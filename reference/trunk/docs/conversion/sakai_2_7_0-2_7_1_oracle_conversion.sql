-- This is the Oracle Sakai 2.7.0 -> 2.7.1 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.7.0 to 2.7.1.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- PRFL-94 remove twitter from preferences
-- NOTE: users will need to re-add their Twitter details to Profile2
alter table PROFILE_PREFERENCES_T drop column TWITTER_ENABLED;
alter table PROFILE_PREFERENCES_T drop column TWITTER_USERNAME;
alter table PROFILE_PREFERENCES_T drop column TWITTER_PASSWORD;

-- PRFL-94 add external integration table
-- NOTE: users will need to re-add their Twitter details to Profile2
create table PROFILE_EXTERNAL_INTEGRATION_T (
	USER_UUID varchar2(99) not null,
	TWITTER_TOKEN varchar2(255),
	TWITTER_SECRET varchar2(255),
	primary key (USER_UUID)
);
-- SAK-5742 create SAKAI_PERSON_T indexes  
create index SAKAI_PERSON_SURNAME_I on SAKAI_PERSON_T (SURNAME);
create index SAKAI_PERSON_ferpaEnabled_I on SAKAI_PERSON_T (ferpaEnabled);
create index SAKAI_PERSON_GIVEN_NAME_I on SAKAI_PERSON_T (GIVEN_NAME);
create index SAKAI_PERSON_UID_I on SAKAI_PERSON_T (UID_C);
