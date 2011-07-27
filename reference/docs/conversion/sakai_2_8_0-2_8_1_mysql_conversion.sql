-- SAK-8005/SAK-20560
-- -- The conversion for SAK-8005 in 2.8.0 conversion do not handle the message_order data in the xml clob
update ANNOUNCEMENT_MESSAGE set MESSAGE_ORDER='1', XML=REPLACE(XML, ' subject=', ' message_order="1" subject=') WHERE MESSAGE_ORDER IS NULL; 

-- KNL-725 use a column type that stores the timezone
alter table SAKAI_CLUSTER change UPDATE_TIME UPDATE_TIME TIMESTAMP;

-- SAK-20717 mailarchive messages need updating with new field
UPDATE mailarchive_message SET xml = REPLACE(XML, ' mail-from="', ' message_order="1" mail-from="') WHERE xml NOT LIKE '% message_order="1" %';

-- PRFL-94 remove twitter from preferences 
-- this was part of the Profile2  1.3.x-1.3.9 upgrade so you may have already run it.
alter table PROFILE_PREFERENCES_T drop TWITTER_ENABLED;
alter table PROFILE_PREFERENCES_T drop TWITTER_USERNAME;
alter table PROFILE_PREFERENCES_T drop TWITTER_PASSWORD;

-- PRFL-94 add external integration table
-- this was part of the Profile2 1.3.x-1.3.9 upgrade so you may have already run it. There is a check in place.
create table if not exists PROFILE_EXTERNAL_INTEGRATION_T (
	USER_UUID varchar(99) not null,
	TWITTER_TOKEN varchar(255),
	TWITTER_SECRET varchar(255),
	primary key (USER_UUID)
);