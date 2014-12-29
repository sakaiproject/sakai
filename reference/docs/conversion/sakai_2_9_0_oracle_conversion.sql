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
alter table SAKAI_SITE add IS_SOFTLY_DELETED char(1) default 0 not null;
update SAKAI_SITE set IS_SOFTLY_DELETED=0 where IS_SOFTLY_DELETED is null;
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

-- SAM-1482:  oracle only
alter table SAM_ITEM_T add (TEMP_CLOB_TEXT clob);
update SAM_ITEM_T SET TEMP_CLOB_TEXT = INSTRUCTION;
alter table SAM_ITEM_T drop column INSTRUCTION;
alter table SAM_ITEM_T rename column TEMP_CLOB_TEXT to INSTRUCTION;

alter table SAM_PUBLISHEDITEM_T add (TEMP_CLOB_TEXT clob);
update SAM_PUBLISHEDITEM_T SET TEMP_CLOB_TEXT = INSTRUCTION;
alter table SAM_PUBLISHEDITEM_T drop column INSTRUCTION;
alter table SAM_PUBLISHEDITEM_T rename column TEMP_CLOB_TEXT to INSTRUCTION;

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


-- SAM-1550
-- Apply the following two queries only if you have SAM-988 in your instance
-- Change the date format from:
-- Wed Sep 14 11:40:53 CDT 2011 (output of Date.toString() in SAM-988)
-- to:
-- 2012-08-23T10:59:34.180-05:00 (ISO8601 format in SAM-1550)
-- Please make the corresponding time zone changes to the queries:
/*
update SAM_SECTIONMETADATA_T
set entry = TO_CHAR(to_timestamp_tz(entry, 'DY MON DD HH24:MI:SS TZD YYYY'),'YYYY-MM-DD')
|| 'T'
|| TO_CHAR(to_timestamp_tz(entry, 'DY MON DD HH24:MI:SS TZD YYYY'),'HH24:MI:SS')
|| '.000'
|| TO_CHAR(to_timestamp_tz(entry, 'DY MON DD HH24:MI:SS TZD YYYY'),'TZH:TZM')
where label='QUESTIONS_RANDOM_DRAW_DATE';

update SAM_PUBLISHEDSECTIONMETADATA_T
set entry = TO_CHAR(to_timestamp_tz(entry, 'DY MON DD HH24:MI:SS TZD YYYY'),'YYYY-MM-DD')
|| 'T'
|| TO_CHAR(to_timestamp_tz(entry, 'DY MON DD HH24:MI:SS TZD YYYY'),'HH24:MI:SS')
|| '.000'
|| TO_CHAR(to_timestamp_tz(entry, 'DY MON DD HH24:MI:SS TZD YYYY'),'TZH:TZM')
where label='QUESTIONS_RANDOM_DRAW_DATE';
*/

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
alter table PROFILE_PRIVACY_T add ONLINE_STATUS number(10,0) default 0;

-- PRFL-720 add missing sequences
create index PROFILE_WI_USER_UUID_I on PROFILE_WALL_ITEMS_T (USER_UUID);
create sequence WALL_ITEMS_S;
create sequence WALL_ITEM_COMMENTS_S;

-- Profile2 v 1.5 conversion END

-- -------------------------------
-- Backfill permissions
-- --------------------------------

-- STAT-275 make Statistics show by default
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'sitestats.admin.view');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'sitestats.view');

-- SAK-20618 make Roleswap enabled by default
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'site.roleswap');

-- SAK-21332 LessonBuilder permissions
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'lessonbuilder.read');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'lessonbuilder.upd');

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','sitestats.view');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','sitestats.view');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','site.roleswap');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','site.roleswap');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','lessonbuilder.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','lessonbuilder.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','lessonbuilder.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','lessonbuilder.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','lessonbuilder.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','lessonbuilder.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','lessonbuilder.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','lessonbuilder.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','lessonbuilder.read');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','lessonbuilder.upd');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','lessonbuilder.upd');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','lessonbuilder.upd');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','lessonbuilder.upd');

-- lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper" OR "!user.template")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper' AND SR.REALM_ID NOT LIKE '!user.template%'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;



-- Adding MSGCNTR conversion
-----------------------------------
--  MSGCNTR-401   -----
--  Add new Property to prevent users from 
--  using Generic Recipients in "To" field (all participants, ect)
-----------------------------------

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'msg.permissions.allowToField.allParticipants');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'msg.permissions.allowToField.groups');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'msg.permissions.allowToField.roles');


--msg.permissions.allowToField.allParticipants and groups and roles is false for all users by default
--if you want to turn this feature on for all "student/acces" type roles, then run 
--the following conversion:


INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));



-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new msg.permissions.allowGenericRecipientFields permissions into existing realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.allParticipants');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.groups');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.roles');


-- lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper" OR "!user.template")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper' AND SR.REALM_ID NOT LIKE '!user.template%'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;


---------------------------
--  END MSGCNTR-401   -----
---------------------------


--////////////////////////////////////////////////////
--// MSGCNTR-411
--// Post First Option in Forums
--////////////////////////////////////////////////////
-- add column to allow POST_FIRST as template setting
alter table MFR_AREA_T add (POST_FIRST NUMBER(1,0) default 0);
alter table MFR_AREA_T modify (POST_FIRST NUMBER(1,0) not null);

-- add column to allow POST_FIRST to be set at the forum level
alter table MFR_OPEN_FORUM_T add (POST_FIRST NUMBER(1,0) default 0);
alter table MFR_OPEN_FORUM_T modify (POST_FIRST NUMBER(1,0) not null);

-- add column to allow POST_FIRST to be set at the topic level
alter table MFR_TOPIC_T add (POST_FIRST NUMBER(1,0) default 0);
alter table MFR_TOPIC_T modify (POST_FIRST NUMBER(1,0) not null);


-- MSGCNTR-329 - Add BCC option to Messages
alter table MFR_PVT_MSG_USR_T add (BCC NUMBER(1,0) default 0);
alter table MFR_PVT_MSG_USR_T modify (BCC NUMBER(1,0) not null); 
alter table MFR_MESSAGE_T add (RECIPIENTS_AS_TEXT_BCC VARCHAR2(4000));

-- MSGCNTR-503 - Internationalization of message priority
-- Default locale
update mfr_message_t set label='pvt_priority_high' where label='High';
update mfr_message_t set label='pvt_priority_normal' where label='Normal';
update mfr_message_t set label='pvt_priority_low' where label='Low';

-- Locale ar
update mfr_message_t set label='pvt_priority_high' where label='\u0645\u0631\u062A\u0641\u0639';
update mfr_message_t set label='pvt_priority_normal' where label='\u0639\u0627\u062F\u064A';
update mfr_message_t set label='pvt_priority_low' where label='\u0645\u0646\u062E\u0641\u0636';

-- Locale ca
update mfr_message_t set label='pvt_priority_high' where label='Alta';
update mfr_message_t set label='pvt_priority_normal' where label='Normal';
update mfr_message_t set label='pvt_priority_low' where label='Baixa';

-- Locale es
update mfr_message_t set label='pvt_priority_high' where label='Alta';
update mfr_message_t set label='pvt_priority_normal' where label='Normal';
update mfr_message_t set label='pvt_priority_low' where label='Baja';

-- Locale eu
update mfr_message_t set label='pvt_priority_high' where label='Gutxikoa';
update mfr_message_t set label='pvt_priority_normal' where label='Normala';
update mfr_message_t set label='pvt_priority_low' where label='Handikoa';

-- Locale fr_CA
update mfr_message_t set label='pvt_priority_high' where label='\u00C9lev\u00E9e';
update mfr_message_t set label='pvt_priority_normal' where label='Normale';
update mfr_message_t set label='pvt_priority_low' where label='Basse';

-- Locale fr_FR
update mfr_message_t set label='pvt_priority_high' where label='Elev\u00E9e';
update mfr_message_t set label='pvt_priority_normal' where label='Normale';
update mfr_message_t set label='pvt_priority_low' where label='Basse';

-- Locale ja
update mfr_message_t set label='pvt_priority_high' where label='\u9ad8\u3044';
update mfr_message_t set label='pvt_priority_normal' where label='\u666e\u901a';
update mfr_message_t set label='pvt_priority_low' where label='\u4f4e\u3044';

-- Locale nl
update mfr_message_t set label='pvt_priority_high' where label='Hoog';
update mfr_message_t set label='pvt_priority_normal' where label='Normaal';
update mfr_message_t set label='pvt_priority_low' where label='Laag';

-- Locale pt_BR
update mfr_message_t set label='pvt_priority_high' where label='Alta';
update mfr_message_t set label='pvt_priority_normal' where label='Normal';
update mfr_message_t set label='pvt_priority_low' where label='Baixa';

-- Locale pt_PT
update mfr_message_t set label='pvt_priority_high' where label='Alta';
update mfr_message_t set label='pvt_priority_normal' where label='Normal';
update mfr_message_t set label='pvt_priority_low' where label='Baixa';

-- Locale ru
update mfr_message_t set label='pvt_priority_high' where label='\u0412\u044b\u0441\u043e\u043a\u0438\u0439';
update mfr_message_t set label='pvt_priority_normal' where label='\u041e\u0431\u044b\u0447\u043d\u044b\u0439';
update mfr_message_t set label='pvt_priority_low' where label='\u041d\u0438\u0437\u043a\u0438\u0439';

-- Locale sv
update mfr_message_t set label='pvt_priority_high' where label='H\u00F6g';
update mfr_message_t set label='pvt_priority_normal' where label='Normal';
update mfr_message_t set label='pvt_priority_low' where label='L\u00E5g';

-- Locale zh_TW
update mfr_message_t set label='pvt_priority_high' where label='\u9ad8';
update mfr_message_t set label='pvt_priority_normal' where label='\u666e\u901a';
update mfr_message_t set label='pvt_priority_low' where label='\u4f4e';

-- end MSGCNTR-503 --


--////////////////////////////////////////////////////
--// MSGCNTR-438
--// Add Ability to hide specific groups
--////////////////////////////////////////////////////


CREATE TABLE MFR_HIDDEN_GROUPS_T  ( 
    ID                number(20,0) NOT NULL,
    VERSION           number(11,0) NOT NULL,
    a_surrogateKey    number(20,0) NULL,
    GROUP_ID          varchar2(255) NOT NULL,
    PRIMARY KEY(ID)
);

ALTER TABLE MFR_HIDDEN_GROUPS_T
    ADD CONSTRAINT FK1DDE4138A306F94D
    FOREIGN KEY(a_surrogateKey)
    REFERENCES mfr_area_t(ID);

CREATE SEQUENCE MFR_HIDDEN_GROUPS_S;

CREATE INDEX MFR_HIDDEN_GROUPS_PARENT_I
    ON MFR_HIDDEN_GROUPS_T(a_surrogateKey);

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'msg.permissions.viewHidden.groups');

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new permission into existing realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.viewHidden.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.viewHidden.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.viewHidden.groups');


-- lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper" OR "!user.template")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper' AND SR.REALM_ID NOT LIKE '!user.template%'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;

-- end MSGCNTR-438 --

--MSGCNTR-569
alter table MFR_TOPIC_T modify CONTEXT_ID varchar(255);


-- END MSGCNTR conversion

-- Start Lesson Builder table creation (if no auto.ddl)

    create table lesson_builder_comments (
        id number(19,0) not null,
        itemId number(19,0) not null,
        pageId number(19,0) not null,
        timePosted date not null,
        author varchar2(36) not null,
        commenttext clob,
        UUID varchar2(36) not null,
        html number(1,0) not null,
        points double precision,
        primary key (id)
    );

    create table lesson_builder_groups (
        id number(19,0) not null,
        itemId varchar2(255) not null,
        groupId varchar2(255) not null,
        groups varchar2(500),
        primary key (id)
    );

    create table lesson_builder_items (
        id number(19,0) not null,
        pageId number(19,0) not null,
        sequence number(10,0) not null,
        type number(10,0) not null,
        sakaiId varchar2(250),
        name varchar2(100),
        html clob,
        description varchar2(500),
        height varchar2(8),
        width varchar2(8),
        alt varchar2(500),
        nextPage number(1,0),
        format varchar2(255),
        required number(1,0),
        alternate number(1,0),
        prerequisite number(1,0),
        subrequirement number(1,0),
        requirementText varchar2(20),
        sameWindow number(1,0),
        groups varchar2(500),
        anonymous number(1,0),
        showComments number(1,0),
        forcedCommentsAnonymous number(1,0),
        gradebookId varchar2(35),
        gradebookPoints number(10,0),
        gradebookTitle varchar2(200),
        altGradebook varchar2(35),
        altPoints number(10,0),
        altGradebookTitle varchar2(200),
        primary key (id)
    );

    create table lesson_builder_log (
        id number(19,0) not null,
        lastViewed date not null,
        itemId number(19,0) not null,
        userId varchar2(255) not null,
        firstViewed date not null,
        complete number(1,0) not null,
        dummy number(1,0) not null,
        path varchar2(255),
        toolId varchar2(250),
        studentPageId number(19,0),
        primary key (id)
    );

    create table lesson_builder_pages (
        pageId number(19,0) not null,
        toolId varchar2(250) not null,
        siteId varchar2(250) not null,
        title varchar2(100) not null,
        parent number(19,0),
        topParent number(19,0),
        hidden number(1,0),
        releaseDate date,
        gradebookPoints double precision,
        owner varchar2(36),
        groupOwned number(1,0),
        cssSheet varchar2(250),
        primary key (pageId)
    );

    create table lesson_builder_student_pages (
        id number(19,0) not null,
        lastUpdated date not null,
        itemId number(19,0) not null,
        pageId number(19,0) not null,
        title varchar2(100) not null,
        owner varchar2(36) not null,
        groupOwned number(1,0) not null,
        commentsSection number(19,0),
        lastCommentChange date,
        deleted number(1,0),
        points double precision,
        primary key (id)
    );

    create sequence hibernate_sequence;

-- End Lesson Builder table creation

-- Start Lesson Builder Index creation

create index lb_comments_itemid_author on lesson_builder_comments(itemId, author);
create index lb_student_pages_pageId on lesson_builder_student_pages(pageId);
create index lb_student_pages_itemId on lesson_builder_student_pages(itemId);
create index lb_log_index on lesson_builder_log(userId,itemId, studentPageId);
create index lb_student_pages_index on lesson_builder_student_pages(itemId, owner, deleted);
create index lb_comments_uuid on lesson_builder_comments(UUID);
create index lb_comments_author on lesson_builder_comments(pageId, author);
create index lb_log_index3 on lesson_builder_log(itemId);
create index lb_log_index2 on lesson_builder_log(userId,toolId);
create index lb_groups_itemid on lesson_builder_groups(itemId);
create index lb_pages_toolid on lesson_builder_pages(toolId, parent);
create index lb_items_pageid on lesson_builder_items(pageId);
create index lb_items_sakaiid on lesson_builder_items(sakaiId);

-- End Lesson Builder Index creation

-- SAK-21754
INSERT INTO SAKAI_SITE_PROPERTY VALUES ('!error', 'display-users-present', 'false');

-- PRFL-612 add avatar image url column to uploaded and external image records
alter table PROFILE_IMAGES_T add RESOURCE_AVATAR varchar2(4000);
alter table PROFILE_IMAGES_EXTERNAL_T add URL_AVATAR varchar2(4000);

-- BLTI-156
CREATE TABLE lti_mapping (
    id INTEGER,
    matchpattern VARCHAR2(255) NOT NULL,
    launch VARCHAR2(255) NOT NULL,
    note VARCHAR2(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

create SEQUENCE lti_mapping_id_sequence INCREMENT BY 1 START WITH 1;

CREATE TABLE lti_content (
    id INTEGER,
    tool_id INTEGER,
    SITE_ID VARCHAR2(99),
    title VARCHAR2(255) NOT NULL,
    frameheight INTEGER,
    newpage NUMBER(1) DEFAULT '0',
    debug NUMBER(1) DEFAULT '0',
    custom VARCHAR2(1024),
    launch VARCHAR2(255),
    xmlimport CLOB,
    placement VARCHAR2(256),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

create SEQUENCE lti_content_id_sequence INCREMENT BY 1 START WITH 1;

CREATE TABLE lti_tools (
    id INTEGER,
    SITE_ID VARCHAR2(99),
    title VARCHAR2(255) NOT NULL,
    description CLOB,
    status NUMBER(1) DEFAULT '0',
    visible NUMBER(1) DEFAULT '0',
    launch VARCHAR2(255) NOT NULL,
    consumerkey VARCHAR2(255) NOT NULL,
    secret VARCHAR2(255) NOT NULL,
    frameheight INTEGER,
    allowframeheight NUMBER(1) DEFAULT '0',
    sendname NUMBER(1) DEFAULT '0',
    sendemailaddr NUMBER(1) DEFAULT '0',
    newpage NUMBER(1) DEFAULT '0',
    debug NUMBER(1) DEFAULT '0',
    custom VARCHAR2(1024),
    allowcustom NUMBER(1) DEFAULT '0',
    xmlimport CLOB,
    splash CLOB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

create SEQUENCE lti_tools_id_sequence INCREMENT BY 1 START WITH 1;

-- end BLTI-156

-- STAT-315
CREATE TABLE SST_SERVERSTATS ( 
ID NUMBER(19) NOT NULL, 
ACTIVITY_DATE DATE NOT NULL, 
EVENT_ID VARCHAR2(32 CHAR) NOT NULL, 
ACTIVITY_COUNT	NUMBER(19) NOT NULL, 
PRIMARY KEY(ID) 
); 

CREATE TABLE SST_USERSTATS ( 
ID NUMBER(19) NOT NULL, 
LOGIN_DATE DATE NOT NULL, 
USER_ID VARCHAR2(99 CHAR) NOT NULL, 
LOGIN_COUNT	NUMBER(19) NOT NULL, 
PRIMARY KEY(ID) 
); 

CREATE SEQUENCE SST_SERVERSTATS_ID; 

CREATE SEQUENCE SST_USERSTATS_ID; 
-- end STAT-315

-- SAK-21739 Enforce uniqueness on template key + locale
-- The following lines will remove duplicates from your database by filtering first on date and then on max id (tie break).
CREATE TABLE EMAIL_TEMPLATE_ITEM_TEMP (ID INTEGER);
INSERT INTO EMAIL_TEMPLATE_ITEM_TEMP SELECT ID from EMAIL_TEMPLATE_ITEM 
  where LAST_MODIFIED not in (select MAX(LAST_MODIFIED) from EMAIL_TEMPLATE_ITEM GROUP BY template_key, template_locale);
DELETE FROM EMAIL_TEMPLATE_ITEM WHERE ID IN (SELECT ID FROM EMAIL_TEMPLATE_ITEM_TEMP);
DELETE FROM EMAIL_TEMPLATE_ITEM_TEMP;
INSERT INTO EMAIL_TEMPLATE_ITEM_TEMP SELECT MAX(ID) FROM EMAIL_TEMPLATE_ITEM GROUP BY UPPER(TEMPLATE_KEY), UPPER(TEMPLATE_LOCALE);
DELETE FROM EMAIL_TEMPLATE_ITEM WHERE ID NOT IN (SELECT ID FROM EMAIL_TEMPLATE_ITEM_TEMP);
DROP TABLE EMAIL_TEMPLATE_ITEM_TEMP;

alter table EMAIL_TEMPLATE_ITEM add constraint EMAIL_TI_KEY_LOCALE_UNIQUE unique (TEMPLATE_KEY,TEMPLATE_LOCALE);
-- end of SAK-21739

-- SAK-22223 don't use null as a template key
update EMAIL_TEMPLATE_ITEM set TEMPLATE_LOCALE = 'default' where TEMPLATE_LOCALE is null or TEMPLATE_LOCALE = '';
-- end of SAK-22223 

-- SAM-1216 Oracle and hibernate and forcing the blob
alter table SAM_MEDIA_T modify (MEDIA blob); 
-- end SAM-1216
