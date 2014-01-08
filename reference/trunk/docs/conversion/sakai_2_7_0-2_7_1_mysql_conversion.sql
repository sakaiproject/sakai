-- This is the MYSQL Sakai 2.7.0 -> 2.7.1 conversion script
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

-- 2.7.0 CORRECTIONS

-- SAK-18863 change default value (from '0' to 0)
-- Uncomment the following ALTER TABLE statement if you ran the sakai_2_7_0_mysql_conversion.sql
-- prior to 2.7.x r81038 (10 Aug 2010 17:10:04 PDT).
-- ALTER TABLE osp_presentation ADD isCollab bit DEFAULT 0;

-- SAK-18648 osp_workflow_parent table name is not recognized as upper case OSP_WORKFLOW_PARENT
-- Users have reported case-sensitivity issues; uncomment and run the following conversion script
-- if you ran the sakai_2_7_0_mysql_conversion.sql prior to 2.7.x r81041 (10 Aug 2010 18:15:57 PDT).
-- insert into osp_workflow_parent select s.id, null, null, null, null, null, null from osp_scaffolding s where s.id not in (select wp.id from osp_workflow_parent wp);

-- 2.7.1 CHANGES

-- PRFL-94 remove twitter from preferences
alter table PROFILE_PREFERENCES_T drop TWITTER_ENABLED;
alter table PROFILE_PREFERENCES_T drop TWITTER_USERNAME;
alter table PROFILE_PREFERENCES_T drop TWITTER_PASSWORD;

-- PRFL-94 add external integration table
create table if not exists PROFILE_EXTERNAL_INTEGRATION_T (
	USER_UUID varchar(99) not null,
	TWITTER_TOKEN varchar(255),
	TWITTER_SECRET varchar(255),
	primary key (USER_UUID)
    );
    
-- SAK-5742 create SAKAI_PERSON_T indexes  
create index SAKAI_PERSON_SURNAME_I on SAKAI_PERSON_T (SURNAME);
create index SAKAI_PERSON_ferpaEnabled_I on SAKAI_PERSON_T (ferpaEnabled);
create index SAKAI_PERSON_GIVEN_NAME_I on SAKAI_PERSON_T (GIVEN_NAME);
create index SAKAI_PERSON_UID_I on SAKAI_PERSON_T (UID_C);
