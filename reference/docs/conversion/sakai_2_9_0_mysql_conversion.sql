-- KNL-576 provider_id field is too small for large site with long list of provider id
alter table SAKAI_REALM modify PROVIDER_ID varchar(4000);


-- KNL-705 new soft deletion of sites
alter table SAKAI_SITE add IS_SOFTLY_DELETED char(1) not null DEFAULT 0;
alter table SAKAI_SITE add SOFTLY_DELETED_DATE datetime;

-- KNL-725 use a column type that stores the timezone
alter table SAKAI_CLUSTER change UPDATE_TIME UPDATE_TIME TIMESTAMP;

-- KNL-734 type of session and event date column
alter table SAKAI_SESSION change SESSION_START SESSION_START TIMESTAMP;
alter table SAKAI_SESSION change SESSION_END SESSION_END TIMESTAMP;
alter table SAKAI_EVENT change EVENT_DATE EVENT_DATE TIMESTAMP;


--SAK-19964 Gradebook drop highest and/or lowest or keep highest score for a student
ALTER TABLE GB_CATEGORY_T
ADD COLUMN DROP_HIGHEST int(11) NULL;

Update GB_CATEGORY_T
Set DROP_HIGHEST = 0;


ALTER TABLE GB_CATEGORY_T
ADD COLUMN KEEP_HIGHEST int(11) NULL;

Update GB_CATEGORY_T
Set KEEP_HIGHEST = 0;

--SAK-19731 - Add ability to hide columns in All Grades View for instructors
alter table GB_GRADABLE_OBJECT_T add column (HIDE_IN_ALL_GRADES_TABLE bit default false);
update GB_GRADABLE_OBJECT_T set HIDE_IN_ALL_GRADES_TABLE=0 where HIDE_IN_ALL_GRADES_TABLE is NULL;
