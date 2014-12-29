-- This is the Oracle Sakai 2.8.1 -> 2.8.2 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.8.1 to 2.8.2.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------


-- PRFL-687 incorrect default value for messages setting
update PROFILE_PRIVACY_T set MESSAGES=1 where MESSAGES=0;