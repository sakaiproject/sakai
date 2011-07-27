-- This is the Oracle Sakai 2.7.1 -> 2.7.2 conversion script
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

/* PRFL-392 change row size of image URI columns */
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN varchar2(4000) not null;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB varchar2(4000) not null;

alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN varchar2(4000) not null;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB varchar2(4000);
