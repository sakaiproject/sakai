-- This is the MYSQL Sakai 2.7.1 -> 2.7.2 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.7.1 to 2.7.2.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- KNL-725 use a column type that stores the timezone
alter table SAKAI_CLUSTER change UPDATE_TIME UPDATE_TIME timestamp;

/* PRFL-392 change row size of image URI columns */
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN text not null;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB text not null;

alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN text not null;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB text;

-- SAK-19448
alter table EMAIL_TEMPLATE_ITEM modify HTMLMESSAGE LONGTEXT;