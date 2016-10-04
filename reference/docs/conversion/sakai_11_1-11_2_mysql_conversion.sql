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

