--
-- SAK-31766 - Conversion for instances of Sakai that existed prior to 2.4 (all other instances should already be this column length)
--

ALTER TABLE GB_PERMISSION_T MODIFY (FUNCTION_NAME VARCHAR2(255));
ALTER TABLE GB_PERMISSION_T MODIFY (USER_ID VARCHAR2(255));
