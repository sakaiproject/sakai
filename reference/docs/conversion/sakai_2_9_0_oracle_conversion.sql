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

