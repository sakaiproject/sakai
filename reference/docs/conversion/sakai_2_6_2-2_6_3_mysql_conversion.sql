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