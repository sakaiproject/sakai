-- This is the MYSQL Sakai 2.6.2 -> 2.6.3 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.6.2 to 2.6.3.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- SAK-8421 add indexes to improve performance of Forums statistics view.
create index MFR_UNREAD_MESSAGE_C_ID on MFR_UNREAD_STATUS_T (MESSAGE_C);
create index MFR_UNREAD_TOPIC_C_ID on MFR_UNREAD_STATUS_T (TOPIC_C);
create index MFR_UNREAD_USER_C_ID on MFR_UNREAD_STATUS_T (USER_C);
create index MFR_UNREAD_READ_C_ID on MFR_UNREAD_STATUS_T (READ_C);

-- SAK-14482 update Mercury site to use new assignments tool
-- The 'mercury' site has the old sakai.assignment tool which is not available in 2.5
-- (and thus has no icon and clicking on it has no effect). This replaces it with the sakai.assignment.grades tool 
-- as well as patches the !worksite which suffers from the same problem.

-- NOTE: while provided in the sakai_2_5_0-2_5_x_mysql_conversion005_SAK-14482.sql on 12 Jan 2009 
-- this change was not included in a 2.5 rollup conversion script until sakai_2_5_5-2_5_6_mysql_conversion.sql
-- and was only added to the 2.6.x branch on 2 Feb 2010 as sakai_2_6_0-2_6_x_mysql_conversion005_SAK-14482.sql 
-- AFTER sakai-2.6.2 was released on 29 Jan 2010.  There is a chance then that deployers may have missed this update
-- and we are including it for the sake of consistency.  Running the two update statements against an already 
-- updated SAKAI_SITE_TOOL table will affect 0 rows and do no harm.

-- update Mercury site
UPDATE SAKAI_SITE_TOOL SET REGISTRATION='sakai.assignment.grades' WHERE REGISTRATION='sakai.assignment' AND SITE_ID='mercury';

-- update !worksite site
UPDATE SAKAI_SITE_TOOL SET REGISTRATION='sakai.assignment.grades' WHERE REGISTRATION='sakai.assignment' AND SITE_ID='!worksite';

-- SAK-5742 create SAKAI_PERSON_T indexes  
create index SAKAI_PERSON_SURNAME_I on SAKAI_PERSON_T (SURNAME);
create index SAKAI_PERSON_ferpaEnabled_I on SAKAI_PERSON_T (ferpaEnabled);
create index SAKAI_PERSON_GIVEN_NAME_I on SAKAI_PERSON_T (GIVEN_NAME);
create index SAKAI_PERSON_UID_I on SAKAI_PERSON_T (UID_C);
