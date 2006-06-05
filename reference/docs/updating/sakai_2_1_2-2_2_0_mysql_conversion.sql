-- This is the MySQL Sakai 2.1.2 -> 2.2.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.1.2 to 2.2.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


----------------------------------------------------------------------------------------------------------------------------------------
-- User ID / EID backfill
-- We need a record in SAKAI_USER_ID_MAP for each user we have ever seen in Sakai.
-- These records will have the same value in the id and eid fields.
-- The old id was really an eid, and will continue to be the user's eid and id both.
-- We can get the existing users without searching everywhere in the Sakai database by combining
-- 		the users we find in the user table with the users who have been granted any permissions in the system.
----------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO SAKAI_USER_ID_MAP (USER_ID, EID)
SELECT USER_ID, USER_ID FROM SAKAI_USER WHERE USER_ID NOT IN (SELECT USER_ID FROM SAKAI_USER_ID_MAP);

INSERT INTO SAKAI_USER_ID_MAP (USER_ID, EID)
SELECT DISTINCT USER_ID, USER_ID FROM SAKAI_REALM_RL_GR WHERE USER_ID NOT IN (SELECT USER_ID FROM SAKAI_USER_ID_MAP);


----------------------------------------------------------------------------------------------------------------------------------------
-- from gradebook/app/business/src/sql/mysql/sakai_gradebook_2.1.x_to_2.2.sql
--
-- Gradebook table changes between Sakai 2.1.* and 2.2.
----------------------------------------------------------------------------------------------------------------------------------------

-- Add grading scale support.
create table GB_PROPERTY_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   NAME varchar(255) not null unique,
   VALUE varchar(255),
   primary key (ID)
);
create table GB_GRADING_SCALE_GRADES_T (
   GRADING_SCALE_ID bigint not null,
   LETTER_GRADE varchar(255),
   GRADE_IDX integer not null,
   primary key (GRADING_SCALE_ID, GRADE_IDX)
);
create table GB_GRADING_SCALE_T (
   ID bigint not null auto_increment,
   OBJECT_TYPE_ID integer not null,
   VERSION integer not null,
   SCALE_UID varchar(255) not null unique,
   NAME varchar(255) not null,
   UNAVAILABLE bit,
   primary key (ID)
);
create table GB_GRADING_SCALE_PERCENTS_T (
   GRADING_SCALE_ID bigint not null,
   PERCENT double precision,
   LETTER_GRADE varchar(255) not null,
   primary key (GRADING_SCALE_ID, LETTER_GRADE)
);
alter table GB_GRADE_MAP_T add column (GB_GRADING_SCALE_T bigint);
alter table GB_GRADING_SCALE_GRADES_T add index FK5D3F0C955A72817B (GRADING_SCALE_ID), add constraint FK5D3F0C955A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T (ID);
alter table GB_GRADE_TO_PERCENT_MAPPING_T add index FKCDEA021164995486 (GRADE_MAP_ID), add constraint FKCDEA021164995486 foreign key (GRADE_MAP_ID) references GB_GRADE_MAP_T (ID);
alter table GB_GRADING_SCALE_PERCENTS_T add index FKC98BE4675A72817B (GRADING_SCALE_ID), add constraint FKC98BE4675A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T (ID);

-- Add indexes for improved performance and reduced locking.
create index GB_GRADABLE_OBJ_ASN_IDX on GB_GRADABLE_OBJECT_T (OBJECT_TYPE_ID, GRADEBOOK_ID, NAME, REMOVED);
create index GB_GRADE_RECORD_O_T_IDX on GB_GRADE_RECORD_T (OBJECT_TYPE_ID);

-- This may have already been defined via the 2.1.1 upgrade.
create index GB_GRADE_RECORD_STUDENT_ID_IDX on GB_GRADE_RECORD_T (STUDENT_ID);


----------------------------------------------------------------------------------------------------------------------------------------
-- for MySQL, increase the field size for the REALM_ID field
----------------------------------------------------------------------------------------------------------------------------------------

ALTER TABLE SAKAI_REALM CHANGE REALM_ID REALM_ID VARCHAR (255) NOT NULL;


----------------------------------------------------------------------------------------------------------------------------------------
-- make sure admin has the admin site tab
----------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO SAKAI_SITE_USER (SITE_ID, USER_ID, PERMISSION) VALUES('!admin', 'admin', -1);

