-- This is the Oracle Sakai 2.8.2 -> 2.8.3 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.8.2 to 2.8.3.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- POLL-172
-- Hibernate error in 2.8.2 Oracle after running conversion scripts still present (polls)

update poll_poll set poll_is_public = 0 where poll_is_public is null;
