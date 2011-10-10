-- This is the Oracle Sakai 2.8.0 -> 2.8.1 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.8.0 to 2.8.1.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- SAK-8005/SAK-20560
-- The conversion for SAK-8005 in 2.8.0 conversion do not handle the message_order data in the xml clob
-- The next three statements are needed if the xml field is of type Long 
-- alter table announcement_message modify xml clob; 
-- select 'alter index '||index_name||' rebuild online;' from user_indexes where status = 'INVALID' or status = 'UNUSABLE'; 
-- execute all resulting statements from the previous step 
update ANNOUNCEMENT_MESSAGE set MESSAGE_ORDER='1', XML=replace(XML, ' subject=', ' message_order="1" subject=') where MESSAGE_ORDER is null; 

-- KNL-563 correction
-- sakai-2.8.0 conversion script set DEFAULT_VALUE incorrectly to not null.  Set to null to match Hibernate mapping.
alter table SAKAI_MESSAGE_BUNDLE modify DEFAULT_VALUE null;

-- KNL-725 use a datetype with timezone
-- Make sure sakai is stopped when running this.
-- Empty the SAKAI_CLUSTER, Oracle refuses to alter the table with records in it..
delete from SAKAI_CLUSTER;
-- Change the datatype
alter table SAKAI_CLUSTER modify (UPDATE_TIME timestamp with time zone); 

-- SAK-20717 mailarchive messages need updating with new field
update MAILARCHIVE_MESSAGE set XML=replace(XML, ' mail-from="', ' message_order="1" mail-from="') where XML not like '% message_order="1" %';

-- SAK-20926 / PRFL-392 fix null status
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN varchar2(4000);
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB varchar2(4000);
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN varchar2(4000);
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB varchar2(4000);

-- These 3 statements might fail with "FAILURE: ORA-01442: column to be modified to NOT NULL is already NOT NULL"
-- That is acceptable

alter table PROFILE_IMAGES_T modify RESOURCE_MAIN not null;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB  not null;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN not null;

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
