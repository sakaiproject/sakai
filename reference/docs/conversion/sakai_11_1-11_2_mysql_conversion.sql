--
-- SAK-31766 - Conversion for instances of Sakai that existed prior to 2.4 (all other instances should already be this column length)
--

ALTER TABLE GB_PERMISSION_T MODIFY FUNCTION_NAME VARCHAR(255);
ALTER TABLE GB_PERMISSION_T MODIFY USER_ID VARCHAR(255);

-- #3258 Drop this unused column
ALTER TABLE gb_grade_record_t DROP COLUMN user_entered_grade;

-- SAM-3040 slow query observed
ALTER TABLE SAM_ASSESSMETADATA_T MODIFY COLUMN `LABEL` varchar(99), ADD INDEX `SAM_METADATA_IDX` (`LABEL`, `ENTRY`) ;
ALTER TABLE SAM_PUBLISHEDMETADATA_T MODIFY COLUMN `LABEL` varchar(99), ADD INDEX `SAM_PUBMETADATA_IDX` (`LABEL`, `ENTRY`) ;
-- END SAM-3040

-- SAK-31276 remove unncecessary keys because there is a composite key that handles this
DROP INDEX SST_PRESENCE_SITE_ID_IX ON SST_PRESENCES;
DROP INDEX SST_EVENTS_USER_ID_IX ON SST_EVENTS;
-- END SAK-31276

-- SAK-31905
-- Uncomment this if you're using a standard Sakai GroupProvider/CourseManagement
-- If you're using a custom implementation you need to check what separator is used between provider IDs and if you
-- are using anything other than "," this uncomment this and update to use your separator
-- UPDATE SAKAI_REALM SET PROVIDER_ID = REPLACE(PROVIDER_ID, ',', '+') where PROVIDER_ID like '%,%';

-- You do want to run this part of it which fixes any older role IDs to use the correct separator.
UPDATE SAKAI_SITE_GROUP_PROPERTY SET value = REPLACE(value, '+', ',')
  WHERE name = 'group_prop_role_providerid' AND value LIKE '%+%';
-- END SAK-31905