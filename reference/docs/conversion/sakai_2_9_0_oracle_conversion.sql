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
