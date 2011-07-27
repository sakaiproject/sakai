-- This is the MYSQL Sakai 2.8.0 -> 2.8.1 conversion script
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
-- The conversion for SAK-8005 in the 2.8.0 conversion script did not handle the message_order data in the xml clob
update ANNOUNCEMENT_MESSAGE set MESSAGE_ORDER='1', XML=replace(XML, ' subject=', ' message_order="1" subject=') where MESSAGE_ORDER is null; 

-- KNL-563 correction
-- sakai-2.8.0 conversion script set DEFAULT_VALUE incorrectly to not null.  Set to null to match Hibernate mapping.
alter table SAKAI_MESSAGE_BUNDLE modify DEFAULT_VALUE text null;

-- KNL-725 use a column type that stores the timezone
alter table SAKAI_CLUSTER change UPDATE_TIME UPDATE_TIME timestamp;

-- SAK-20717 mailarchive messages need updating with new field
update MAILARCHIVE_MESSAGE set XML=replace(XML, ' mail-from="', ' message_order="1" mail-from="') where XML not like '% message_order="1" %';

-- SAK-20926 / PRFL-392 fix null status
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN text not null;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB text not null;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN text not null;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB text;