-- This is the Oracle Sakai 2.8.0 -> 2.8.1 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.8.0 to 2.8.1.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- SAK-8005/SAK-20560
-- The conversion for SAK-8005 in 2.8.0 conversion do not handle the message_order data in the xml clob
-- The next three statements are needed if the xml field is of type Long 
-- alter table announcement_message modify xml clob; 
-- select 'alter index '||index_name||' rebuild online;' from user_indexes where status = 'INVALID' or status = 'UNUSABLE'; 
-- execute all resulting statements from the previous step 
update ANNOUNCEMENT_MESSAGE set MESSAGE_ORDER='1', XML=replace(XML, ' subject=', ' message_order="1" subject=') where MESSAGE_ORDER is null; 

-- KNL-563 correction
-- sakai-2.8.0 conversion script set DEFAULT_VALUE incorrectly to not null.  Set to null to match Hibernate mapping.
alter table SAKAI_MESSAGE_BUNDLE modify DEFAULT_VALUE text null;

-- KNL-725 use a datetype with timezone
-- Make sure sakai is stopped when running this.
-- Empty the SAKAI_CLUSTER, Oracle refuses to alter the table with records in it..
delete from SAKAI_CLUSTER;
-- Change the datatype
alter table SAKAI_CLUSTER modify (UPDATE_TIME timestamp with time zone); 

-- SAK-20717 mailarchive messages need updating with new field
update MAILARCHIVE_MESSAGE set XML=replace(XML, ' mail-from="', ' message_order="1" mail-from="') where XML not like '% message_order="1" %';

-- SAK-20926 / PRFL-392 fix null status
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN varchar2(4000) not null;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB varchar2(4000) not null;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN varchar2(4000) not null;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB varchar2(4000);
