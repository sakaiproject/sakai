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

-- KNL-725 use a datetype with timezone
-- Make sure sakai is stopped when running this.
-- Empty the SAKAI_CLUSTER, Oracle refuses to alter the table with records in it..
delete from SAKAI_CLUSTER;
-- Change the datatype
alter table SAKAI_CLUSTER modify (UPDATE_TIME timestamp with time zone);

/* PRFL-392 change row size of image URI columns */
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN varchar2(4000);
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB varchar2(4000);

alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN varchar2(4000); 
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB varchar2(4000);

-- These 3 statements might fail with "FAILURE: ORA-01442: column to be modified to NOT NULL is already NOT NULL"
-- That is acceptable
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN not null;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB not null;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN not null;
