-- This is the MYSQL Sakai 2.9.2 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.9.0/2.9.1 to 2.9.2.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- BLTI-222
ALTER TABLE lti_content ADD pagetitle VARCHAR(255);
ALTER TABLE lti_content MODIFY launch TEXT(1024);
ALTER TABLE lti_content ADD consumerkey VARCHAR(255);
ALTER TABLE lti_content ADD secret VARCHAR(255);
ALTER TABLE lti_content ADD settings TEXT(8096);
ALTER TABLE lti_content ADD placementsecret TEXT(512);
ALTER TABLE lti_content ADD oldplacementsecret TEXT(512);
ALTER TABLE lti_tools ADD allowtitle TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD pagetitle VARCHAR(255);
ALTER TABLE lti_tools ADD allowpagetitle TINYINT DEFAULT '0';
ALTER TABLE lti_tools MODIFY launch TEXT(1024);
ALTER TABLE lti_tools ADD allowlaunch TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD domain VARCHAR(255);
ALTER TABLE lti_tools ADD allowconsumerkey TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD allowsecret TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD allowoutcomes TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD allowroster TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD allowsettings TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD allowlori TINYINT DEFAULT '0';

-- BLTI-208
ALTER TABLE lti_tools MODIFY launch VARCHAR(255) NULL;
ALTER TABLE lti_tools MODIFY consumerkey VARCHAR(255) NULL;
ALTER TABLE lti_tools MODIFY secret VARCHAR(255) NULL;

-- SAK-23452 Roster throws errors in 2.9 after MySQL upgrade 
update PROFILE_PREFERENCES_T set USE_GRAVATAR = false where USE_GRAVATAR is null; 
update PROFILE_PREFERENCES_T set EMAIL_WALL_ITEM_NEW = true where EMAIL_WALL_ITEM_NEW is null; 
update PROFILE_PREFERENCES_T set EMAIL_WORKSITE_NEW = true where EMAIL_WORKSITE_NEW is null; 
update PROFILE_PREFERENCES_T set SHOW_ONLINE_STATUS = true where SHOW_ONLINE_STATUS is null; 
update PROFILE_PRIVACY_T set MY_WALL = 0 where MY_WALL is null; 
update PROFILE_PRIVACY_T set ONLINE_STATUS = 0 where ONLINE_STATUS is null;

