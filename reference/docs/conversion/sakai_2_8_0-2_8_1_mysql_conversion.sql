-- SAK-8005/SAK-20560
-- -- The conversion for SAK-8005 in 2.8.0 conversion do not handle the message_order data in the xml clob
update ANNOUNCEMENT_MESSAGE set MESSAGE_ORDER='1', XML=REPLACE(XML, ' subject=', ' message_order="1" subject=') WHERE MESSAGE_ORDER IS NULL; 

-- KNL-725 use a column type that stores the timezone
alter table SAKAI_CLUSTER change UPDATE_TIME UPDATE_TIME TIMESTAMP;

-- SAK-20717 mailarchive messages need updating with new field
UPDATE mailarchive_message SET xml = REPLACE(XML, ' mail-from="', ' message_order="1" mail-from="') WHERE xml NOT LIKE '% message_order="1" %';

-- SAK-20926 / PRFL-392 fix null status
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN text not null;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB text not null;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN text not null;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB text;