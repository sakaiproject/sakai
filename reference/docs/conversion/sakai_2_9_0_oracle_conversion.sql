-- This is the Oracle Sakai 2.9.0 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.8.x to 2.9.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement (in most cases table names and fields UPPER CASE; syntax lower case)
-- --------------------------------------------------------------------------------------------------------------------------------------

-- KNL-576 provider_id field is too small for large site with long list of provider id
alter table SAKAI_REALM modify PROVIDER_ID varchar2(4000);

-- KNL-705 new soft deletion of sites
-- TODO needs checking for correct syntax - DH
alter table SAKAI_SITE add IS_SOFTLY_DELETED char(1) default 0 not null;
alter table SAKAI_SITE add SOFTLY_DELETED_DATE timestamp;

-- KNL-725 use a datetype with timezone
-- Make sure sakai is stopped when running this.
-- Empty the SAKAI_CLUSTER, Oracle refuses to alter the table with records in it..
delete from SAKAI_CLUSTER;
-- Change the datatype
alter table SAKAI_CLUSTER modify (UPDATE_TIME timestamp with time zone);

-- KNL-735 use a datetype with timezone
-- Make sure sakai is stopped when running this.
-- Empty the SAKAI_EVENT & SAKAI_SESSION, Oracle refuses to alter the table with records in it.
delete from SAKAI_EVENT;
delete from SAKAI_SESSION;

-- Change the datatype
alter table SAKAI_EVENT MODIFY (EVENT_DATE timestamp with time zone); 
-- Change the datatype
alter table SAKAI_SESSION MODIFY (SESSION_START timestamp with time zone); 
alter table SAKAI_SESSION MODIFY (SESSION_END timestamp with time zone); 

--SAK-19964 Gradebook drop highest and/or lowest or keep highest score for a student
alter table GB_CATEGORY_T add DROP_HIGHEST number(11,0) null;
update GB_CATEGORY_T set DROP_HIGHEST = 0;

alter table GB_CATEGORY_T add KEEP_HIGHEST number(11,0) null;
update GB_CATEGORY_T set KEEP_HIGHEST = 0; 

--SAK-19731 Add ability to hide columns in All Grades View for instructors
alter table GB_GRADABLE_OBJECT_T add (HIDE_IN_ALL_GRADES_TABLE number(1,0) default 0);
update GB_GRADABLE_OBJECT_T set HIDE_IN_ALL_GRADES_TABLE=0 where HIDE_IN_ALL_GRADES_TABLE is null;

-- SAK-20598 change column type to mediumtext (On Oracle we need to copy the column content first though)
alter table SAKAI_PERSON_T add (TMP_NOTES clob);
alter table SAKAI_PERSON_T add (TMP_FAVOURITE_BOOKS clob);
alter table SAKAI_PERSON_T add (TMP_FAVOURITE_TV_SHOWS clob);
alter table SAKAI_PERSON_T add (TMP_FAVOURITE_MOVIES clob);
alter table SAKAI_PERSON_T add (TMP_FAVOURITE_QUOTES clob);
alter table SAKAI_PERSON_T add (TMP_EDUCATION_COURSE clob);
alter table SAKAI_PERSON_T add (TMP_EDUCATION_SUBJECTS clob);
alter table SAKAI_PERSON_T add (TMP_STAFF_PROFILE clob);
alter table SAKAI_PERSON_T add (TMP_UNIVERSITY_PROFILE_URL clob);
alter table SAKAI_PERSON_T add (TMP_ACADEMIC_PROFILE_URL clob);
alter table SAKAI_PERSON_T add (TMP_PUBLICATIONS clob);
alter table SAKAI_PERSON_T add (TMP_BUSINESS_BIOGRAPHY clob);

update SAKAI_PERSON_T set TMP_NOTES = NOTES;
update SAKAI_PERSON_T set TMP_FAVOURITE_BOOKS = FAVOURITE_BOOKS;
update SAKAI_PERSON_T set TMP_FAVOURITE_TV_SHOWS = FAVOURITE_TV_SHOWS;
update SAKAI_PERSON_T set TMP_FAVOURITE_MOVIES = FAVOURITE_MOVIES;
update SAKAI_PERSON_T set TMP_FAVOURITE_QUOTES = FAVOURITE_QUOTES;
update SAKAI_PERSON_T set TMP_EDUCATION_COURSE = EDUCATION_COURSE;
update SAKAI_PERSON_T set TMP_EDUCATION_SUBJECTS = EDUCATION_SUBJECTS;
update SAKAI_PERSON_T set TMP_STAFF_PROFILE = STAFF_PROFILE;
update SAKAI_PERSON_T set TMP_UNIVERSITY_PROFILE_URL = UNIVERSITY_PROFILE_URL;
update SAKAI_PERSON_T set TMP_ACADEMIC_PROFILE_URL = ACADEMIC_PROFILE_URL;
update SAKAI_PERSON_T set TMP_PUBLICATIONS = PUBLICATIONS;
update SAKAI_PERSON_T set TMP_BUSINESS_BIOGRAPHY = BUSINESS_BIOGRAPHY;

alter table SAKAI_PERSON_T drop column NOTES;
alter table SAKAI_PERSON_T drop column FAVOURITE_BOOKS;
alter table SAKAI_PERSON_T drop column FAVOURITE_TV_SHOWS;
alter table SAKAI_PERSON_T drop column FAVOURITE_MOVIES;
alter table SAKAI_PERSON_T drop column FAVOURITE_QUOTES;
alter table SAKAI_PERSON_T drop column EDUCATION_COURSE;
alter table SAKAI_PERSON_T drop column EDUCATION_SUBJECTS;
alter table SAKAI_PERSON_T drop column STAFF_PROFILE;
alter table SAKAI_PERSON_T drop column UNIVERSITY_PROFILE_URL;
alter table SAKAI_PERSON_T drop column ACADEMIC_PROFILE_URL;
alter table SAKAI_PERSON_T drop column PUBLICATIONS;
alter table SAKAI_PERSON_T drop column BUSINESS_BIOGRAPHY;

alter table SAKAI_PERSON_T rename column TMP_NOTES to NOTES;
alter table SAKAI_PERSON_T rename column TMP_FAVOURITE_BOOKS to FAVOURITE_BOOKS;
alter table SAKAI_PERSON_T rename column TMP_FAVOURITE_TV_SHOWS to FAVOURITE_TV_SHOWS;
alter table SAKAI_PERSON_T rename column TMP_FAVOURITE_MOVIES to FAVOURITE_MOVIES;
alter table SAKAI_PERSON_T rename column TMP_FAVOURITE_QUOTES to FAVOURITE_QUOTES;
alter table SAKAI_PERSON_T rename column TMP_EDUCATION_COURSE to EDUCATION_COURSE;
alter table SAKAI_PERSON_T rename column TMP_EDUCATION_SUBJECTS to EDUCATION_SUBJECTS;
alter table SAKAI_PERSON_T rename column TMP_STAFF_PROFILE to STAFF_PROFILE;
alter table SAKAI_PERSON_T rename column TMP_UNIVERSITY_PROFILE_URL to UNIVERSITY_PROFILE_URL;
alter table SAKAI_PERSON_T rename column TMP_ACADEMIC_PROFILE_URL to ACADEMIC_PROFILE_URL;
alter table SAKAI_PERSON_T rename column TMP_PUBLICATIONS to PUBLICATIONS;
alter table SAKAI_PERSON_T rename column TMP_BUSINESS_BIOGRAPHY to BUSINESS_BIOGRAPHY;
-- end SAK-20598

-- SAM-1008:  oracle only
alter table SAM_ANSWER_T add (TEMP_CLOB_TEXT clob);
update SAM_ANSWER_T SET TEMP_CLOB_TEXT = TEXT;
alter table SAM_ANSWER_T drop column TEXT;
alter table SAM_ANSWER_T rename column TEMP_CLOB_TEXT to TEXT;

alter table SAM_PUBLISHEDANSWER_T add (TEMP_CLOB_TEXT clob);
update SAM_PUBLISHEDANSWER_T SET TEMP_CLOB_TEXT = TEXT;
alter table SAM_PUBLISHEDANSWER_T drop column TEXT;
alter table SAM_PUBLISHEDANSWER_T rename column TEMP_CLOB_TEXT to TEXT;

INSERT INTO SAM_TYPE_T ("TYPEID" ,"AUTHORITY" ,"DOMAIN" ,"KEYWORD",
    "DESCRIPTION" ,
    "STATUS" ,"CREATEDBY" ,"CREATEDDATE" ,"LASTMODIFIEDBY" ,
    "LASTMODIFIEDDATE" )
    VALUES (13 , 'stanford.edu' ,'assessment.item' ,'Matrix Choices Survey' ,NULL ,1 ,1 ,
    SYSDATE ,1 ,SYSDATE);   
    
-- SAM-1255    
Update SAM_ASSESSEVALUATION_T
Set ANONYMOUSGRADING = 2
WHERE ASSESSMENTID = (Select ID from SAM_ASSESSMENTBASE_T where TITLE='Default Assessment Type' AND TYPEID='142' AND ISTEMPLATE=1);

-- SAM-1205
INSERT INTO SAM_ASSESSMETADATA_T ("ASSESSMENTMETADATAID", "ASSESSMENTID","LABEL",
    "ENTRY")
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Default Assessment Type' AND TYPEID='142' AND ISTEMPLATE=1), 'lockedBrowser_isInstructorEditable', 'true');
    
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lockedBrowser_isInstructorEditable', 'true')
;

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lockedBrowser_isInstructorEditable', 'true')
;  

alter table GB_GRADEBOOK_T
add (
	DO_SHOW_STATISTICS_CHART number(1,0)
);

-- Profile2 v 1.5 conversion START

-- PRFL-498 add the gravatar column, default to 0, 
alter table PROFILE_PREFERENCES_T add USE_GRAVATAR number(1,0) default 0;

-- PRFL-528 add the wall email notification column, default to 1
alter table PROFILE_PREFERENCES_T add EMAIL_WALL_ITEM_NEW number(1,0) default 1;

-- PRFL-388 add the worksite email notification column, default to 1
alter table PROFILE_PREFERENCES_T add EMAIL_WORKSITE_NEW number(1,0) default 1;

-- PRFL-513 add the wall privacy setting, default to 0
alter table PROFILE_PRIVACY_T add MY_WALL number(1,0) default 0;

-- PRFL-518 add profile wall items table
create table PROFILE_WALL_ITEMS_T (
	WALL_ITEM_ID number(19,0) not null,
	USER_UUID varchar2(99) not null,
	CREATOR_UUID varchar2(99) not null,
	WALL_ITEM_TYPE number(10,0) not null,
	WALL_ITEM_TEXT varchar2(4000) not null,
	WALL_ITEM_DATE date not null,
	primary key (WALL_ITEM_ID)
);

create table PROFILE_WALL_ITEM_COMMENTS_T (
	WALL_ITEM_COMMENT_ID number(19,0) not null,
	WALL_ITEM_ID number(19,0) not null,
	CREATOR_UUID varchar2(99) not null,
	WALL_ITEM_COMMENT_TEXT varchar2(4000) not null,
	WALL_ITEM_COMMENT_DATE date not null,
	primary key (WALL_ITEM_COMMENT_ID)
);

alter table PROFILE_WALL_ITEM_COMMENTS_T 
	add constraint FK32185F67BEE209 
	foreign key (WALL_ITEM_ID) 
	references PROFILE_WALL_ITEMS_T;
	
-- PRFL-350 add the show online status column, default to 1
alter table PROFILE_PREFERENCES_T add SHOW_ONLINE_STATUS number(1,0) default 1;
alter table PROFILE_PRIVACY_T add SHOW_ONLINE_STATUS number(1,0) default 0;

-- Profile2 v 1.5 conversion END

-- -------------------------------
-- Backfill permissions
-- --------------------------------

-- STAT-275 make Statistics show by default
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'sitestats.admin.view');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'sitestats.view');

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
