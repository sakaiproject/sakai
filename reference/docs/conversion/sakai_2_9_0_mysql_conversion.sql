-- This is the MYSQL Sakai 2.9.0 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.8.x to 2.9.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- KNL-576 provider_id field is too small for large site with long list of provider id
alter table SAKAI_REALM modify PROVIDER_ID varchar(4000);

-- KNL-705 new soft deletion of sites
alter table SAKAI_SITE add IS_SOFTLY_DELETED char(1) not null default 0;
alter table SAKAI_SITE add SOFTLY_DELETED_DATE datetime;

-- KNL-725 use a column type that stores the timezone
alter table SAKAI_CLUSTER change UPDATE_TIME UPDATE_TIME timestamp;

-- KNL-734 type of session and event date column
alter table SAKAI_SESSION change SESSION_START SESSION_START timestamp;
alter table SAKAI_SESSION change SESSION_END SESSION_END timestamp;
alter table SAKAI_EVENT change EVENT_DATE EVENT_DATE timestamp;

--SAK-19964 Gradebook drop highest and/or lowest or keep highest score for a student
alter table GB_CATEGORY_T add column DROP_HIGHEST int(11) null;
update GB_CATEGORY_T set DROP_HIGHEST = 0;

alter table GB_CATEGORY_T add column KEEP_HIGHEST int(11) null;
update GB_CATEGORY_T set KEEP_HIGHEST = 0;

--SAK-19731 - Add ability to hide columns in All Grades View for instructors
alter table GB_GRADABLE_OBJECT_T add column (HIDE_IN_ALL_GRADES_TABLE bit default false);
update GB_GRADABLE_OBJECT_T set HIDE_IN_ALL_GRADES_TABLE = 0 where HIDE_IN_ALL_GRADES_TABLE is null;

-- SAK-20598 change column type to mediumtext
alter table SAKAI_PERSON_T change NOTES NOTES mediumtext null;
alter table SAKAI_PERSON_T change FAVOURITE_BOOKS FAVOURITE_BOOKS mediumtext null;
alter table SAKAI_PERSON_T change FAVOURITE_TV_SHOWS FAVOURITE_TV_SHOWS mediumtext null;
alter table SAKAI_PERSON_T change FAVOURITE_MOVIES FAVOURITE_MOVIES mediumtext null;
alter table SAKAI_PERSON_T change FAVOURITE_QUOTES FAVOURITE_QUOTES mediumtext null;
alter table SAKAI_PERSON_T change EDUCATION_COURSE EDUCATION_COURSE mediumtext null;
alter table SAKAI_PERSON_T change EDUCATION_SUBJECTS EDUCATION_SUBJECTS mediumtext null;
alter table SAKAI_PERSON_T change STAFF_PROFILE STAFF_PROFILE mediumtext null;
alter table SAKAI_PERSON_T change UNIVERSITY_PROFILE_URL UNIVERSITY_PROFILE_URL mediumtext null;
alter table SAKAI_PERSON_T change ACADEMIC_PROFILE_URL ACADEMIC_PROFILE_URL mediumtext null;
alter table SAKAI_PERSON_T change PUBLICATIONS PUBLICATIONS mediumtext null;
alter table SAKAI_PERSON_T change BUSINESS_BIOGRAPHY BUSINESS_BIOGRAPHY mediumtext null;
-- end SAK-20598

-- SAM-1255 	
Update SAM_ASSESSEVALUATION_T 
Set ANONYMOUSGRADING = 2
WHERE ASSESSMENTID = (Select ID from SAM_ASSESSMENTBASE_T where TITLE='Default Assessment Type' AND TYPEID='142' AND ISTEMPLATE=1)	


INSERT INTO SAM_TYPE_T (TYPEID,AUTHORITY,DOMAIN, KEYWORD, 
	DESCRIPTION,  
	STATUS, CREATEDBY, CREATEDDATE, LASTMODIFIEDBY, 
	LASTMODIFIEDDATE ) 
	VALUES (13 , 'stanford.edu' ,'assessment.item' ,'Matrix Choices Survey' ,NULL ,1 ,1 ,
	'2010-01-01 12:00:00',1 ,'2010-01-01 12:00:00');
	
-- SAM-1255 	
Update SAM_ASSESSEVALUATION_T 
Set ANONYMOUSGRADING = 2
WHERE ASSESSMENTID = (Select ID from SAM_ASSESSMENTBASE_T where TITLE='Default Assessment Type' AND TYPEID='142' AND ISTEMPLATE=1)	

-- SAM-1205
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, 1, 'lockedBrowser_isInstructorEditable', 'true')
;

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lockedBrowser_isInstructorEditable', 'true')
;

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lockedBrowser_isInstructorEditable', 'true')
;

-- Profile2 v 1.5 conversion START

-- PRFL-498 add the gravatar column, default to 0
alter table PROFILE_PREFERENCES_T add USE_GRAVATAR bit not null DEFAULT false;

-- PRFL-528 add the wall email notification column, default to 1
alter table PROFILE_PREFERENCES_T add EMAIL_WALL_ITEM_NEW bit not null DEFAULT true;

-- PRFL-388 add the worksite email notification column, default to 1
alter table PROFILE_PREFERENCES_T add EMAIL_WORKSITE_NEW bit not null DEFAULT true;

-- PRFL-513 add the wall privacy setting, default to 0
alter table PROFILE_PRIVACY_T add MY_WALL int not null DEFAULT 0;

-- PRFL-518 add profile wall items table
create table PROFILE_WALL_ITEMS_T (
	WALL_ITEM_ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	CREATOR_UUID varchar(99) not null,
	WALL_ITEM_TYPE integer not null,
	WALL_ITEM_TEXT text not null,
	WALL_ITEM_DATE datetime not null,
	primary key (WALL_ITEM_ID)
);

create table PROFILE_WALL_ITEM_COMMENTS_T (
	WALL_ITEM_COMMENT_ID bigint not null auto_increment,
	WALL_ITEM_ID bigint not null,
	CREATOR_UUID varchar(99) not null,
	WALL_ITEM_COMMENT_TEXT text not null,
	WALL_ITEM_COMMENT_DATE datetime not null,
	primary key (WALL_ITEM_COMMENT_ID)
);

alter table PROFILE_WALL_ITEM_COMMENTS_T 
	add index FK32185F67BEE209 (WALL_ITEM_ID), 
	add constraint FK32185F67BEE209 
	foreign key (WALL_ITEM_ID) 
	references PROFILE_WALL_ITEMS_T (WALL_ITEM_ID);
	
-- PRFL-350 add the show online status column, default to 1
alter table PROFILE_PREFERENCES_T add SHOW_ONLINE_STATUS bit not null DEFAULT true;
alter table PROFILE_PRIVACY_T add ONLINE_STATUS int not null DEFAULT 0;

-- Profile2 v 1.5 conversion END

-- -------------------------------
-- Backfill permissions
-- --------------------------------

-- STAT-275 make Statistics show by default
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'sitestats.admin.view');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'sitestats.view');

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','sitestats.view');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','sitestats.view');

-- lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;
