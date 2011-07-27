-- SAK-8005/SAK-20560
-- -- The conversion for SAK-8005 in 2.8.0 conversion do not handle the message_order data in the xml clob
-- -- next three statements needed if the xml field is of type Long 
-- -- alter table announcement_message modify xml clob; 
-- -- select 'alter index '||index_name||' rebuild online;' from user_indexes where status = 'INVALID' or status = 'UNUSABLE'; 
-- -- execute all resulting statements from the previous step 
update ANNOUNCEMENT_MESSAGE set MESSAGE_ORDER='1', XML=REPLACE(XML, ' subject=', ' message_order="1" subject=') WHERE MESSAGE_ORDER IS NULL; 

-- KNL-725 use a datetype with timezone
-- Make sure sakai is stopped when running this.
-- Empty the SAKAI_CLUSTER, Oracle refuses to alter the table with records in it.
DELETE FROM SAKAI_CLUSTER;
-- Change the datatype
ALTER TABLE SAKAI_CLUSTER MODIFY (UPDATE_TIME TIMESTAMP WITH TIME ZONE); 

-- SAK-20717 mailarchive messages need updating with new field
UPDATE mailarchive_message SET xml = REPLACE(XML, ' mail-from="', ' message_order="1" mail-from="') WHERE xml NOT LIKE '% message_order="1" %';

-- PRFL-94 remove twitter from preferences 
-- this was part of the Profile2  1.3.x-1.3.9 upgrade so you may have already run it.
alter table PROFILE_PREFERENCES_T drop column TWITTER_ENABLED;
alter table PROFILE_PREFERENCES_T drop column TWITTER_USERNAME;
alter table PROFILE_PREFERENCES_T drop column TWITTER_PASSWORD;

-- PRFL-94 add external integration table
-- this was part of the Profile2 1.3.x-1.3.9 upgrade so you may have already run it. There is a check in place.
create table if not exists PROFILE_EXTERNAL_INTEGRATION_T (
	USER_UUID varchar2(99) not null,
	TWITTER_TOKEN varchar2(255),
	TWITTER_SECRET varchar2(255),
	primary key (USER_UUID)
);
