-- KNL-576 provider_id field is too small for large site with long list of provider id
alter table SAKAI_REALM modify PROVIDER_ID varchar2(4000);


-- KNL-705 new soft deletion of sites
-- TODO needs checking for correct syntax - DH
alter table SAKAI_SITE add IS_SOFTLY_DELETED char(1) not null DEFAULT 0;
alter table SAKAI_SITE add SOFTLY_DELETED_DATE datetime;


-- KNL-725 use a datetype with timezone
-- Make sure sakai is stopped when running this.
-- Empty the SAKAI_CLUSTER, Oracle refuses to alter the table with records in it.
DELETE FROM SAKAI_CLUSTER;
-- Change the datatype
ALTER TABLE SAKAI_CLUSTER MODIFY (UPDATE_TIME TIMESTAMP WITH TIME ZONE); 



-- KNL-735 use a datetype with timezone
-- Make sure sakai is stopped when running this.
-- Empty the SAKAI_EVENT & SAKAI_SESSION, Oracle refuses to alter the table with records in it.
DELETE FROM SAKAI_EVENT;
DELETE FROM SAKAI_SESSION;

-- Change the datatype
ALTER TABLE SAKAI_EVENT MODIFY (EVENT_DATE TIMESTAMP WITH TIME ZONE); 
-- Change the datatype
ALTER TABLE SAKAI_SESSION MODIFY (SESSION_START TIMESTAMP WITH TIME ZONE); 
ALTER TABLE SAKAI_SESSION MODIFY (SESSION_END TIMESTAMP WITH TIME ZONE); 


--SAK-19964 Gradebook drop highest and/or lowest or keep highest score for a student
ALTER TABLE GB_CATEGORY_T
ADD COLUMN DROP_HIGHEST number(11,0) NULL;

Update GB_CATEGORY_T
Set DROP_HIGHEST = 0;


ALTER TABLE GB_CATEGORY_T
ADD COLUMN KEEP_HIGHEST number(11,0) NULL;

Update GB_CATEGORY_T
Set KEEP_HIGHEST = 0; 


--SAK-19731 Add ability to hide columns in All Grades View for instructors

alter table GB_GRADABLE_OBJECT_T add column (HIDE_IN_ALL_GRADES_TABLE bit default false);
update GB_GRADABLE_OBJECT_T set HIDE_IN_ALL_GRADES_TABLE=0 where HIDE_IN_ALL_GRADES_TABLE is NULL;
