-- This is the MySQL Sakai 2.1.2 -> 2.2.0 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.1.2 to 2.2.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------


-- --------------------------------------------------------------------------------------------------------------------------------------
-- User ID / EID backfill
-- We need a record in SAKAI_USER_ID_MAP for each user we have ever seen in Sakai.
-- These records will have the same value in the id and eid fields.
-- The old id was really an eid, and will continue to be the user's eid and id both.
-- We can get the existing users without searching everywhere in the Sakai database by combining
-- 		the users we find in the user table with the users who have been granted any permissions in the system.
-- --------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE SAKAI_USER_ID_MAP
(
       USER_ID             VARCHAR (99) NOT NULL,
       EID                 VARCHAR (255) NOT NULL
);


ALTER TABLE SAKAI_USER_ID_MAP
       ADD  ( PRIMARY KEY (USER_ID) ) ;

CREATE UNIQUE INDEX AK_SAKAI_USER_ID_MAP_EID ON SAKAI_USER_ID_MAP
(
       EID                       ASC
);

INSERT INTO SAKAI_USER_ID_MAP (USER_ID, EID)
SELECT USER_ID, USER_ID FROM SAKAI_USER WHERE USER_ID NOT IN (SELECT USER_ID FROM SAKAI_USER_ID_MAP);

INSERT INTO SAKAI_USER_ID_MAP (USER_ID, EID)
SELECT DISTINCT USER_ID, USER_ID FROM SAKAI_REALM_RL_GR WHERE USER_ID NOT IN (SELECT USER_ID FROM SAKAI_USER_ID_MAP);


-- --------------------------------------------------------------------------------------------------------------------------------------
-- from gradebook/app/business/src/sql/mysql/sakai_gradebook_2.1.x_to_2.2.sql
--
-- Gradebook table changes between Sakai 2.1.* and 2.2.
-- --------------------------------------------------------------------------------------------------------------------------------------

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

alter table GB_GRADING_SCALE_PERCENTS_T add index FKC98BE4675A72817B (GRADING_SCALE_ID), add constraint FKC98BE4675A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T (ID);

-- Add indexes for improved performance and reduced locking.
create index GB_GRADABLE_OBJ_ASN_IDX on GB_GRADABLE_OBJECT_T (OBJECT_TYPE_ID, GRADEBOOK_ID, NAME, REMOVED);
create index GB_GRADE_RECORD_O_T_IDX on GB_GRADE_RECORD_T (OBJECT_TYPE_ID);

-- This may have already been defined via the 2.1.1 upgrade.
create index GB_GRADE_RECORD_STUDENT_ID_IDX on GB_GRADE_RECORD_T (STUDENT_ID);


-- --------------------------------------------------------------------------------------------------------------------------------------
-- for MySQL, increase the field size for the REALM_ID field
-- --------------------------------------------------------------------------------------------------------------------------------------

ALTER TABLE SAKAI_REALM CHANGE REALM_ID REALM_ID VARCHAR (255) NOT NULL;


-- --------------------------------------------------------------------------------------------------------------------------------------
-- make sure admin has the admin site tab (ignore if it already exists)
-- --------------------------------------------------------------------------------------------------------------------------------------

INSERT IGNORE INTO SAKAI_SITE_USER (SITE_ID, USER_ID, PERMISSION) VALUES('!admin', 'admin', -1);


-- --------------------------------------------------------------------------------------------------------------------------------------
-- new default permissions
-- --------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'asn.all.groups');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'calendar.all.groups');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'content.all.groups');
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.all.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.all.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.all.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.all.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.all.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.all.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));


-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new group permissions into existing group realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

-- These are for the group templates, and should already be set in the site templates
-- ADJUST ME: adjust theses for your needs, either with different permissions, or duplicate for other roles than 'access', 'Student', 'maintain', 'Instructor' and 'Teaching Assistant'

INSERT INTO PERMISSIONS_SRC_TEMP values ('access','asn.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','asn.submit');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','calendar.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','content.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','site.visit');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','asn.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','asn.submit');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','calendar.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','content.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','site.visit');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','asn.delete');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','asn.grade');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','asn.new');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','asn.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','asn.revise');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','asn.submit');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','calendar.delete');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','calendar.new');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','calendar.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','calendar.revise');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','content.delete');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','content.new');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','content.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','content.revise');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','site.visit');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','site.visit.unp');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','asn.delete');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','asn.grade');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','asn.new');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','asn.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','asn.revise');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','asn.submit');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','calendar.delete');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','calendar.new');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','calendar.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','calendar.revise');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','content.delete');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','content.new');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','content.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','content.revise');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','site.visit');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','site.visit.unp');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','annc.delete.any');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','asn.delete');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','asn.grade');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','asn.new');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','asn.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','asn.revise');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','asn.submit');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','calendar.delete');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','calendar.new');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','calendar.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','calendar.revise');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','content.delete');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','content.new');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','content.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','content.revise');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','site.visit');

-- lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
FROM PERMISSIONS_SRC_TEMP TMPSRC
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


-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new site permissions into existing site realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a grant of the 'annc.all.groups', we add these others:
CREATE TABLE PERMISSIONS_TEMP (FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP VALUES ((SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME ='asn.all.groups'));
INSERT INTO PERMISSIONS_TEMP VALUES ((SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME ='calendar.all.groups'));
INSERT INTO PERMISSIONS_TEMP VALUES ((SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME ='content.all.groups'));

INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRF.REALM_KEY, SRRF.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    SAKAI_REALM_RL_FN SRRF
    JOIN PERMISSIONS_TEMP TMP
    WHERE SRRF.FUNCTION_KEY = (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION SRF WHERE SRF.FUNCTION_NAME ='annc.all.groups')
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRF.REALM_KEY AND SRRFI.ROLE_KEY=SRRF.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

DROP TABLE PERMISSIONS_TEMP;


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Samigo
-- --------------------------------------------------------------------------------------------------------------------------------------

-- added two columns in SAM_ITEMGRADING_T to support audio recording question type implemented in samigo 2.2, please see JIRA task SAK-1894 for specification

alter table SAM_ITEMGRADING_T add column ATTEMPTSREMAINING integer;
alter table SAM_ITEMGRADING_T add column LASTDURATION varchar(36);

-- new assessment styles

INSERT INTO SAM_TYPE_T (TYPEID ,AUTHORITY ,DOMAIN, KEYWORD,
    DESCRIPTION,
    STATUS, CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (142 , 'stanford.edu' ,'assessment.template.system' ,'System Defined' ,NULL ,1 ,1 ,
    '2006-01-01 12:00:00',1 ,'2005-06-01 12:00:00');

UPDATE SAM_ASSESSMENTBASE_T SET TYPEID=142 WHERE ID=1;
UPDATE SAM_ASSESSMENTBASE_T SET TITLE='Default Assessment Type' WHERE ID=1;

INSERT INTO SAM_ASSESSMENTBASE_T (ID ,ISTEMPLATE ,
    PARENTID ,TITLE ,DESCRIPTION ,COMMENTS,
    ASSESSMENTTEMPLATEID, TYPEID, INSTRUCTORNOTIFICATION,
    TESTEENOTIFICATION , MULTIPARTALLOWED, STATUS,
    CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (NULL,1 ,0 ,'Formative Assessment' , 'System Defined Assessment Type', '', NULL
    ,'142' ,1 ,1 ,1 ,1 ,'admin' ,'2006-06-01 12:00:00' ,'admin' ,'2006-06-01 12:00:00'  )
;     

INSERT INTO SAM_ASSESSEVALUATION_T (ASSESSMENTID,
    EVALUATIONCOMPONENTS, SCORINGTYPE, NUMERICMODELID,
    FIXEDTOTALSCORE, GRADEAVAILABLE, ISSTUDENTIDPUBLIC,
    ANONYMOUSGRADING, AUTOSCORING,TOGRADEBOOK)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
     '' ,1 ,'' , NULL , NULL , NULL ,1 , NULL ,2  )
;
INSERT INTO SAM_ASSESSFEEDBACK_T (ASSESSMENTID,
    FEEDBACKDELIVERY, FEEDBACKAUTHORING, SHOWQUESTIONTEXT, SHOWSTUDENTRESPONSE,
    SHOWCORRECTRESPONSE, SHOWSTUDENTSCORE, SHOWSTUDENTQUESTIONSCORE,
    SHOWQUESTIONLEVELFEEDBACK, SHOWSELECTIONLEVELFEEDBACK,
    SHOWGRADERCOMMENTS, SHOWSTATISTICS, EDITCOMPONENTS)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      1 ,3, 1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1  )
;
INSERT INTO SAM_ASSESSACCESSCONTROL_T (ASSESSMENTID,
    SUBMISSIONSALLOWED, SUBMISSIONSSAVED, ASSESSMENTFORMAT,
    BOOKMARKINGITEM, TIMELIMIT, TIMEDASSESSMENT,
    RETRYALLOWED, LATEHANDLING, STARTDATE, DUEDATE,
    SCOREDATE, FEEDBACKDATE, RETRACTDATE, AUTOSUBMIT,
    ITEMNAVIGATION, ITEMNUMBERING, SUBMISSIONMESSAGE,
    RELEASETO, USERNAME, PASSWORD, FINALPAGEURL,
    UNLIMITEDSUBMISSIONS)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
                      NULL,1 ,1 , NULL , NULL , NULL , NULL ,1 ,
                      NULL, NULL, NULL, NULL, NULL,
                      1 ,2 ,1 ,'' ,'' ,'' ,'' ,'' ,
                      1  )
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'finalPageURL_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'anonymousRelease_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'dueDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'description_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataQuestions_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'bgImage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackComponents_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'retractDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessmentAutoSubmit_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'toGradebook_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayChunking_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'recordedScore_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'authenticatedRelease_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayNumbering_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionMessage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'releaseDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'assessmentAuthor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'passwordRequired_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'author', '')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionModel_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'ipAccessType_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessment_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataAssess_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'bgColor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'testeeIdentity_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'templateInfo_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'itemAccessType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lateHandling_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackAuthoring_isInstructorEditable', 'true')
;

INSERT INTO SAM_ASSESSMENTBASE_T (ID ,ISTEMPLATE ,
    PARENTID ,TITLE ,DESCRIPTION ,COMMENTS,
    ASSESSMENTTEMPLATEID, TYPEID, INSTRUCTORNOTIFICATION,
    TESTEENOTIFICATION , MULTIPARTALLOWED, STATUS,
    CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (NULL,1 ,0 ,'Quiz' , 'System Defined Assessment Type', '', NULL
    ,'142' ,1 ,1 ,1 ,1 ,'admin' ,'2006-06-01 12:00:00' ,'admin' ,'2006-06-01 12:00:00'  )
;     

INSERT INTO SAM_ASSESSEVALUATION_T (ASSESSMENTID,
    EVALUATIONCOMPONENTS, SCORINGTYPE, NUMERICMODELID,
    FIXEDTOTALSCORE, GRADEAVAILABLE, ISSTUDENTIDPUBLIC,
    ANONYMOUSGRADING, AUTOSCORING,TOGRADEBOOK)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
     '' ,1 ,'' , NULL , NULL , NULL ,2 , NULL ,2  )
;
INSERT INTO SAM_ASSESSFEEDBACK_T (ASSESSMENTID,
    FEEDBACKDELIVERY, FEEDBACKAUTHORING, SHOWQUESTIONTEXT, SHOWSTUDENTRESPONSE,
    SHOWCORRECTRESPONSE, SHOWSTUDENTSCORE, SHOWSTUDENTQUESTIONSCORE,
    SHOWQUESTIONLEVELFEEDBACK, SHOWSELECTIONLEVELFEEDBACK,
    SHOWGRADERCOMMENTS, SHOWSTATISTICS, EDITCOMPONENTS)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      2 ,1, 1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1  )
;
INSERT INTO SAM_ASSESSACCESSCONTROL_T (ASSESSMENTID,
    SUBMISSIONSALLOWED, SUBMISSIONSSAVED, ASSESSMENTFORMAT,
    BOOKMARKINGITEM, TIMELIMIT, TIMEDASSESSMENT,
    RETRYALLOWED, LATEHANDLING, STARTDATE, DUEDATE,
    SCOREDATE, FEEDBACKDATE, RETRACTDATE, AUTOSUBMIT,
    ITEMNAVIGATION, ITEMNUMBERING, SUBMISSIONMESSAGE,
    RELEASETO, USERNAME, PASSWORD, FINALPAGEURL,
    UNLIMITEDSUBMISSIONS)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
                      1,1 ,3 , NULL , NULL , NULL , NULL ,2 ,
                      NULL, NULL, NULL, NULL, NULL,
                      1 ,1 ,1 ,'' ,'' ,'' ,'' ,'' ,
                      0  )
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'finalPageURL_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'anonymousRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'dueDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'description_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataQuestions_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'bgImage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackComponents_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'retractDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessmentAutoSubmit_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'toGradebook_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayChunking_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'recordedScore_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'authenticatedRelease_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayNumbering_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionMessage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'releaseDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'assessmentAuthor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'passwordRequired_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'author', '')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionModel_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'ipAccessType_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessment_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataAssess_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'bgColor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'testeeIdentity_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'templateInfo_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'itemAccessType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lateHandling_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackAuthoring_isInstructorEditable', 'true')
;


INSERT INTO SAM_ASSESSMENTBASE_T (ID ,ISTEMPLATE ,
    PARENTID ,TITLE ,DESCRIPTION ,COMMENTS,
    ASSESSMENTTEMPLATEID, TYPEID, INSTRUCTORNOTIFICATION,
    TESTEENOTIFICATION , MULTIPARTALLOWED, STATUS,
    CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (NULL,1 ,0 ,'Problem Set' , 'System Defined Assessment Type', '', NULL
    ,'142' ,1 ,1 ,1 ,1 ,'admin' ,'2006-06-01 12:00:00' ,'admin' ,'2006-06-01 12:00:00'  )
;     

INSERT INTO SAM_ASSESSEVALUATION_T (ASSESSMENTID,
    EVALUATIONCOMPONENTS, SCORINGTYPE, NUMERICMODELID,
    FIXEDTOTALSCORE, GRADEAVAILABLE, ISSTUDENTIDPUBLIC,
    ANONYMOUSGRADING, AUTOSCORING,TOGRADEBOOK)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
     '' ,1 ,'' , NULL , NULL , NULL ,2 , NULL ,2  )
;
INSERT INTO SAM_ASSESSFEEDBACK_T (ASSESSMENTID,
    FEEDBACKDELIVERY, FEEDBACKAUTHORING, SHOWQUESTIONTEXT, SHOWSTUDENTRESPONSE,
    SHOWCORRECTRESPONSE, SHOWSTUDENTSCORE, SHOWSTUDENTQUESTIONSCORE,
    SHOWQUESTIONLEVELFEEDBACK, SHOWSELECTIONLEVELFEEDBACK,
    SHOWGRADERCOMMENTS, SHOWSTATISTICS, EDITCOMPONENTS)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      2 ,1, 1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1  )
;
INSERT INTO SAM_ASSESSACCESSCONTROL_T (ASSESSMENTID,
    SUBMISSIONSALLOWED, SUBMISSIONSSAVED, ASSESSMENTFORMAT,
    BOOKMARKINGITEM, TIMELIMIT, TIMEDASSESSMENT,
    RETRYALLOWED, LATEHANDLING, STARTDATE, DUEDATE,
    SCOREDATE, FEEDBACKDATE, RETRACTDATE, AUTOSUBMIT,
    ITEMNAVIGATION, ITEMNUMBERING, SUBMISSIONMESSAGE,
    RELEASETO, USERNAME, PASSWORD, FINALPAGEURL,
    UNLIMITEDSUBMISSIONS)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
                      NULL,1 ,2 , NULL , NULL , NULL , NULL ,1 ,
                      NULL, NULL, NULL, NULL, NULL,
                      1 ,2 ,1 ,'' ,'' ,'' ,'' ,'' ,
                      1  )
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'finalPageURL_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'anonymousRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'dueDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'description_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataQuestions_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'bgImage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackComponents_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'retractDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessmentAutoSubmit_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'toGradebook_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayChunking_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'recordedScore_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'authenticatedRelease_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayNumbering_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionMessage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'releaseDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'assessmentAuthor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'passwordRequired_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'author', '')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionModel_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'ipAccessType_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessment_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataAssess_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'bgColor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'testeeIdentity_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'templateInfo_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'itemAccessType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lateHandling_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackAuthoring_isInstructorEditable', 'true')
;



INSERT INTO SAM_ASSESSMENTBASE_T (ID ,ISTEMPLATE ,
    PARENTID ,TITLE ,DESCRIPTION ,COMMENTS,
    ASSESSMENTTEMPLATEID, TYPEID, INSTRUCTORNOTIFICATION,
    TESTEENOTIFICATION , MULTIPARTALLOWED, STATUS,
    CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (NULL,1 ,0 ,'Survey' , 'System Defined Assessment Type', '', NULL
    ,'142' ,1 ,1 ,1 ,1 ,'admin' ,'2006-06-01 12:00:00' ,'admin' ,'2006-06-01 12:00:00'  )
;     

INSERT INTO SAM_ASSESSEVALUATION_T (ASSESSMENTID,
    EVALUATIONCOMPONENTS, SCORINGTYPE, NUMERICMODELID,
    FIXEDTOTALSCORE, GRADEAVAILABLE, ISSTUDENTIDPUBLIC,
    ANONYMOUSGRADING, AUTOSCORING,TOGRADEBOOK)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
     '' ,1 ,'' , NULL , NULL , NULL ,1 , NULL ,2  )
;
INSERT INTO SAM_ASSESSFEEDBACK_T (ASSESSMENTID,
    FEEDBACKDELIVERY, FEEDBACKAUTHORING, SHOWQUESTIONTEXT, SHOWSTUDENTRESPONSE,
    SHOWCORRECTRESPONSE, SHOWSTUDENTSCORE, SHOWSTUDENTQUESTIONSCORE,
    SHOWQUESTIONLEVELFEEDBACK, SHOWSELECTIONLEVELFEEDBACK,
    SHOWGRADERCOMMENTS, SHOWSTATISTICS, EDITCOMPONENTS)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      1 ,3, 0 ,1 ,0 ,0 ,0 ,0 ,0 ,0 ,1 ,1  )
;
INSERT INTO SAM_ASSESSACCESSCONTROL_T (ASSESSMENTID,
    SUBMISSIONSALLOWED, SUBMISSIONSSAVED, ASSESSMENTFORMAT,
    BOOKMARKINGITEM, TIMELIMIT, TIMEDASSESSMENT,
    RETRYALLOWED, LATEHANDLING, STARTDATE, DUEDATE,
    SCOREDATE, FEEDBACKDATE, RETRACTDATE, AUTOSUBMIT,
    ITEMNAVIGATION, ITEMNUMBERING, SUBMISSIONMESSAGE,
    RELEASETO, USERNAME, PASSWORD, FINALPAGEURL,
    UNLIMITEDSUBMISSIONS)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
                      1,1 ,1 , NULL , NULL , NULL , NULL ,1 ,
                      NULL, NULL, NULL, NULL, NULL,
                      1 ,2 ,1 ,'' ,'' ,'' ,'' ,'' ,
                      0  )
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'finalPageURL_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'anonymousRelease_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'dueDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'description_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataQuestions_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'bgImage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackComponents_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'retractDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessmentAutoSubmit_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'toGradebook_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayChunking_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'recordedScore_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'authenticatedRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayNumbering_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionMessage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'releaseDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'assessmentAuthor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'passwordRequired_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'author', '')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionModel_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'ipAccessType_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessment_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataAssess_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'bgColor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'testeeIdentity_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'templateInfo_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'itemAccessType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lateHandling_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackAuthoring_isInstructorEditable', 'false')
;

INSERT INTO SAM_ASSESSMENTBASE_T (ID ,ISTEMPLATE ,
    PARENTID ,TITLE ,DESCRIPTION ,COMMENTS,
    ASSESSMENTTEMPLATEID, TYPEID, INSTRUCTORNOTIFICATION,
    TESTEENOTIFICATION , MULTIPARTALLOWED, STATUS,
    CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (NULL,1 ,0 ,'Test' , 'System Defined Assessment Type', '', NULL
    ,'142' ,1 ,1 ,1 ,1 ,'admin' ,'2006-06-01 12:00:00' ,'admin' ,'2006-06-01 12:00:00'  )
;     

INSERT INTO SAM_ASSESSEVALUATION_T (ASSESSMENTID,
    EVALUATIONCOMPONENTS, SCORINGTYPE, NUMERICMODELID,
    FIXEDTOTALSCORE, GRADEAVAILABLE, ISSTUDENTIDPUBLIC,
    ANONYMOUSGRADING, AUTOSCORING,TOGRADEBOOK)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
     '' ,1 ,'' , NULL , NULL , NULL ,1 , NULL ,2  )
;
INSERT INTO SAM_ASSESSFEEDBACK_T (ASSESSMENTID,
    FEEDBACKDELIVERY, FEEDBACKAUTHORING, SHOWQUESTIONTEXT, SHOWSTUDENTRESPONSE,
    SHOWCORRECTRESPONSE, SHOWSTUDENTSCORE, SHOWSTUDENTQUESTIONSCORE,
    SHOWQUESTIONLEVELFEEDBACK, SHOWSELECTIONLEVELFEEDBACK,
    SHOWGRADERCOMMENTS, SHOWSTATISTICS, EDITCOMPONENTS)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      2 ,1, 1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1  )
;
INSERT INTO SAM_ASSESSACCESSCONTROL_T (ASSESSMENTID,
    SUBMISSIONSALLOWED, SUBMISSIONSSAVED, ASSESSMENTFORMAT,
    BOOKMARKINGITEM, TIMELIMIT, TIMEDASSESSMENT,
    RETRYALLOWED, LATEHANDLING, STARTDATE, DUEDATE,
    SCOREDATE, FEEDBACKDATE, RETRACTDATE, AUTOSUBMIT,
    ITEMNAVIGATION, ITEMNUMBERING, SUBMISSIONMESSAGE,
    RELEASETO, USERNAME, PASSWORD, FINALPAGEURL,
    UNLIMITEDSUBMISSIONS)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
                      1,1 ,1 , NULL , NULL , NULL , NULL ,2 ,
                      NULL, NULL, NULL, NULL, NULL,
                      1 ,1 ,1 ,'' ,'' ,'' ,'' ,'' ,
                      0  )
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'finalPageURL_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'anonymousRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'dueDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'description_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataQuestions_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'bgImage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackComponents_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'retractDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessmentAutoSubmit_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'toGradebook_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayChunking_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'recordedScore_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'authenticatedRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayNumbering_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionMessage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'releaseDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'assessmentAuthor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'passwordRequired_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'author', '')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionModel_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'ipAccessType_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessment_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataAssess_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'bgColor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'testeeIdentity_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'templateInfo_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'itemAccessType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lateHandling_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackAuthoring_isInstructorEditable', 'true')
;


INSERT INTO SAM_ASSESSMENTBASE_T (ID ,ISTEMPLATE ,
    PARENTID ,TITLE ,DESCRIPTION ,COMMENTS,
    ASSESSMENTTEMPLATEID, TYPEID, INSTRUCTORNOTIFICATION,
    TESTEENOTIFICATION , MULTIPARTALLOWED, STATUS,
    CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (NULL,1 ,0 ,'Timed Test' , 'System Defined Assessment Type', '', NULL
    ,'142' ,1 ,1 ,1 ,1 ,'admin' ,'2006-06-01 12:00:00' ,'admin' ,'2006-06-01 12:00:00'  )
;     

INSERT INTO SAM_ASSESSEVALUATION_T (ASSESSMENTID,
    EVALUATIONCOMPONENTS, SCORINGTYPE, NUMERICMODELID,
    FIXEDTOTALSCORE, GRADEAVAILABLE, ISSTUDENTIDPUBLIC,
    ANONYMOUSGRADING, AUTOSCORING,TOGRADEBOOK)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
     '' ,1 ,'' , NULL , NULL , NULL ,1 , NULL ,2  )
;
INSERT INTO SAM_ASSESSFEEDBACK_T (ASSESSMENTID,
    FEEDBACKDELIVERY, FEEDBACKAUTHORING, SHOWQUESTIONTEXT, SHOWSTUDENTRESPONSE,
    SHOWCORRECTRESPONSE, SHOWSTUDENTSCORE, SHOWSTUDENTQUESTIONSCORE,
    SHOWQUESTIONLEVELFEEDBACK, SHOWSELECTIONLEVELFEEDBACK,
    SHOWGRADERCOMMENTS, SHOWSTATISTICS, EDITCOMPONENTS)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      2 ,1, 1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1 ,1  )
;
INSERT INTO SAM_ASSESSACCESSCONTROL_T (ASSESSMENTID,
    SUBMISSIONSALLOWED, SUBMISSIONSSAVED, ASSESSMENTFORMAT,
    BOOKMARKINGITEM, TIMELIMIT, TIMEDASSESSMENT,
    RETRYALLOWED, LATEHANDLING, STARTDATE, DUEDATE,
    SCOREDATE, FEEDBACKDATE, RETRACTDATE, AUTOSUBMIT,
    ITEMNAVIGATION, ITEMNUMBERING, SUBMISSIONMESSAGE,
    RELEASETO, USERNAME, PASSWORD, FINALPAGEURL,
    UNLIMITEDSUBMISSIONS)
    VALUES ((SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
                      1,1 ,1 , NULL , NULL , NULL , NULL ,2 ,
                      NULL, NULL, NULL, NULL, NULL,
                      1 ,1 ,1 ,'' ,'' ,'' ,'' ,'' ,
                      0  )
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'finalPageURL_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'anonymousRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'dueDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'description_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataQuestions_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'bgImage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackComponents_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'retractDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessmentAutoSubmit_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'toGradebook_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayChunking_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'recordedScore_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'authenticatedRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayNumbering_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionMessage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'releaseDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'assessmentAuthor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'passwordRequired_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'author', '')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionModel_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'ipAccessType_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessment_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataAssess_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'bgColor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'testeeIdentity_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'templateInfo_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'itemAccessType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lateHandling_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackAuthoring_isInstructorEditable', 'true')
;


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Search
-- --------------------------------------------------------------------------------------------------------------------------------------

drop table if exists search_segments;
drop table if exists searchbuilderitem;
drop table if exists searchwriterlock;
create table search_segments (name_ varchar(254) not null, version_ bigint not null, size_ bigint not null, packet_ longblob, primary key (name_));
create table searchbuilderitem (id varchar(64) not null, version datetime not null, name varchar(255) not null unique, context varchar(255) not null, searchaction integer, searchstate integer, primary key (id));
create table searchwriterlock (id varchar(64) not null, lockkey varchar(64) not null unique, nodename varchar(64), expires datetime not null, primary key (id));
create index isearchbuilderitem_context on searchbuilderitem (context);
create index isearchbuilderitem_name on searchbuilderitem (name);
create index isearchwriterlock_lockkey on searchwriterlock (lockkey);


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Schedule Summary Tool
-- --------------------------------------------------------------------------------------------------------------------------------------

-- If you have run the new, provisional, schedule summary tool in its old form, the registration has changed, so run this

UPDATE SAKAI_SITE_TOOL SET REGISTRATION='sakai.summary.calendar' WHERE REGISTRATION='sakai.synoptic.calendar';


-- --------------------------------------------------------------------------------------------------------------------------------------
-- OSP
-- --------------------------------------------------------------------------------------------------------------------------------------

-- This code will create the OSP tables.  If you are already running OSP, don't do this!

create table osp_authz_simple (id varchar(36) not null, qualifier_id varchar(255) not null, agent_id varchar(255) not null, function_name varchar(255) not null, primary key (id));
create table osp_completed_wiz_category (id varchar(36) not null, completed_wizard_id varchar(36), category_id varchar(36), expanded bit, seq_num integer, parent_category_id varchar(36), primary key (id));
create table osp_completed_wizard (id varchar(36) not null, owner_id varchar(255) not null, created datetime not null, lastVisited datetime not null, status varchar(255), wizard_id varchar(36), root_category varchar(36) unique, primary key (id));
create table osp_completed_wizard_page (id varchar(36) not null, completed_category_id varchar(36), wizard_page_def_id varchar(36), wizard_page_id varchar(36) unique, seq_num integer, created datetime not null, lastVisited datetime, primary key (id));
create table osp_guidance (id varchar(36) not null, description varchar(255), site_id varchar(36) not null, securityQualifier varchar(255), securityViewFunction varchar(255) not null, securityEditFunction varchar(255) not null, primary key (id));
create table osp_guidance_item (id varchar(36) not null, type varchar(255), text text, guidance_id varchar(36) not null, primary key (id));
create table osp_guidance_item_file (id varchar(36) not null, baseReference varchar(255), fullReference varchar(255), item_id varchar(36) not null, primary key (id));
create table osp_help_glossary (id varchar(36) not null, worksite_id varchar(255), term varchar(255) not null, description varchar(255) not null, primary key (id));
create table osp_help_glossary_desc (id varchar(36) not null, entry_id varchar(255), long_description text, primary key (id));
create table osp_list_config (id varchar(36) not null, owner_id varchar(255) not null, tool_id varchar(36), title varchar(255), height integer, numRows integer, selected_columns varchar(255) not null, primary key (id));
create table osp_matrix (id varchar(36) not null, owner varchar(255) not null, scaffolding_id varchar(36) not null, primary key (id));
create table osp_matrix_cell (id varchar(36) not null, matrix_id varchar(36) not null, wizard_page_id varchar(36) unique, scaffolding_cell_id varchar(36), primary key (id));
create table osp_matrix_label (id varchar(36) not null, type char(1) not null, description varchar(255), color varchar(7), textColor varchar(7), primary key (id));
create table osp_pres_itemdef_mimetype (item_def_id varchar(36) not null, primaryMimeType varchar(36), secondaryMimeType varchar(36));
create table osp_presentation (id varchar(36) not null, owner_id varchar(255) not null, template_id varchar(36) not null, name varchar(255), description varchar(255), isDefault bit, isPublic bit, presentationType varchar(255) not null, expiresOn datetime, created datetime not null, modified datetime not null, allowComments bit, site_id varchar(36) not null, properties blob, style_id varchar(36), advanced_navigation bit, tool_id varchar(36), primary key (id));
create table osp_presentation_comment (id varchar(36) not null, title varchar(255) not null, commentText text, creator_id varchar(255) not null, presentation_id varchar(36) not null, visibility tinyint not null, created datetime not null, primary key (id));
create table osp_presentation_item (presentation_id varchar(36) not null, artifact_id varchar(36) not null, item_definition_id varchar(36) not null, primary key (presentation_id, artifact_id, item_definition_id));
create table osp_presentation_item_def (id varchar(36) not null, name varchar(255), title varchar(255), description varchar(255), allowMultiple bit, type varchar(255), external_type varchar(255), sequence_no integer, template_id varchar(36) not null, primary key (id));
create table osp_presentation_item_property (id varchar(36) not null, presentation_page_item_id varchar(36) not null, property_key varchar(255) not null, property_value varchar(255), primary key (id));
create table osp_presentation_layout (id varchar(36) not null, name varchar(255) not null, description varchar(255), globalState integer not null, owner_id varchar(255) not null, created datetime not null, modified datetime not null, xhtml_file_id varchar(36) not null, preview_image_id varchar(36), tool_id varchar(36), site_id varchar(36), primary key (id));
create table osp_presentation_log (id varchar(36) not null, viewer_id varchar(255) not null, presentation_id varchar(36) not null, view_date datetime, primary key (id));
create table osp_presentation_page (id varchar(36) not null, title varchar(255), description varchar(255), keywords varchar(255), presentation_id varchar(36) not null, layout_id varchar(36) not null, style_id varchar(36), seq_num integer, created datetime not null, modified datetime not null, primary key (id));
create table osp_presentation_page_item (id varchar(36) not null, presentation_page_region_id varchar(36) not null, type varchar(255), value longtext, seq_num integer not null, primary key (id));
create table osp_presentation_page_region (id varchar(36) not null, presentation_page_id varchar(36) not null, region_id varchar(255) not null, type varchar(255), help_text varchar(255), primary key (id));
create table osp_presentation_template (id varchar(36) not null, name varchar(255), description varchar(255), includeHeaderAndFooter bit, published bit, owner_id varchar(255) not null, renderer varchar(36), markup text, propertyPage varchar(36), documentRoot varchar(255), created datetime not null, modified datetime not null, site_id varchar(36) not null, primary key (id));
create table osp_reports (reportId varchar(36) not null, reportDefIdMark varchar(255), userId varchar(255), title varchar(255), keywords varchar(255), description varchar(255), isLive bit, creationDate datetime, type varchar(255), display bit, primary key (reportId));
create table osp_reports_params (paramId varchar(36) not null, reportId varchar(36), reportDefParamIdMark varchar(255), value varchar(255), primary key (paramId));
create table osp_reports_results (resultId varchar(36) not null, reportId varchar(36), userId varchar(255), title varchar(255), keywords varchar(255), description varchar(255), creationDate datetime, xml text, primary key (resultId));
create table osp_review (id varchar(36) not null, review_content_id varchar(36), site_id varchar(36) not null, parent_id varchar(36), review_device_id varchar(36), review_type integer not null, primary key (id));
create table osp_scaffolding (id varchar(36) not null, ownerId varchar(255) not null, title varchar(255), description text, worksiteId varchar(255), published bit, publishedBy varchar(255), publishedDate datetime, columnLabel varchar(255) not null, rowLabel varchar(255) not null, readyColor varchar(7) not null, pendingColor varchar(7) not null, completedColor varchar(7) not null, lockedColor varchar(7) not null, workflowOption integer not null, exposed_page_id varchar(36), style_id varchar(36), primary key (id));
create table osp_scaffolding_cell (id varchar(36) not null, rootcriterion_id varchar(36), level_id varchar(36), scaffolding_id varchar(36) not null, wiz_page_def_id varchar(36) unique, primary key (id));
create table osp_scaffolding_cell_form_defs (wiz_page_def_id varchar(36) not null, form_def_id varchar(255), seq_num integer not null, primary key (wiz_page_def_id, seq_num));
create table osp_scaffolding_criteria (scaffolding_id varchar(36) not null, elt varchar(36) not null, seq_num integer not null, primary key (scaffolding_id, seq_num));
create table osp_scaffolding_levels (scaffolding_id varchar(36) not null, elt varchar(36) not null, seq_num integer not null, primary key (scaffolding_id, seq_num));
create table osp_site_tool (id varchar(40) not null, site_id varchar(36), tool_id varchar(36), listener_id varchar(255), primary key (id));
create table osp_style (id varchar(36) not null, name varchar(255), description varchar(255), globalState integer not null, owner_id varchar(255) not null, style_file_id varchar(36), site_id varchar(36), created datetime not null, modified datetime not null, primary key (id));
create table osp_template_file_ref (id varchar(36) not null, file_id varchar(36), file_type_id varchar(36), usage_desc varchar(255), template_id varchar(36) not null, primary key (id));
create table osp_wiz_page_attachment (id varchar(36) not null, artifactId varchar(36), page_id varchar(36) not null, primary key (id));
create table osp_wiz_page_form (id varchar(36) not null, artifactId varchar(36), page_id varchar(36) not null, formType varchar(36), primary key (id));
create table osp_wizard (id varchar(36) not null, owner_id varchar(255) not null, name varchar(255), description text, keywords text, created datetime not null, modified datetime not null, site_id varchar(36) not null, guidance_id varchar(36), published bit, wizard_type varchar(255), style_id varchar(36), exposed_page_id varchar(36), root_category varchar(36) unique, seq_num integer, primary key (id));
create table osp_wizard_category (id varchar(36) not null, name varchar(255), description varchar(255), keywords varchar(255), created datetime not null, modified datetime not null, wizard_id varchar(36), parent_category_id varchar(36), seq_num integer, primary key (id));
create table osp_wizard_page (id varchar(36) not null, owner varchar(255) not null, status varchar(255), wiz_page_def_id varchar(36), modified datetime, primary key (id));
create table osp_wizard_page_def (id varchar(36) not null, initialStatus varchar(255), name varchar(255), description text, site_id varchar(255), guidance_id varchar(255), style_id varchar(36), primary key (id));
create table osp_wizard_page_sequence (id varchar(36) not null, seq_num integer, category_id varchar(36) not null, wiz_page_def_id varchar(36) unique, primary key (id));
create table osp_workflow (id varchar(36) not null, title varchar(255), parent_id varchar(36) not null, primary key (id));
create table osp_workflow_item (id varchar(36) not null, actionType integer not null, action_object_id varchar(255) not null, action_value varchar(255) not null, workflow_id varchar(36) not null, primary key (id));
create table osp_workflow_parent (id varchar(36) not null, reflection_device_id varchar(36), reflection_device_type varchar(255), evaluation_device_id varchar(36), evaluation_device_type varchar(255), review_device_id varchar(36), review_device_type varchar(255), primary key (id));
alter table osp_completed_wiz_category add index FK4EC54F7C6EA23D5D (category_id), add constraint FK4EC54F7C6EA23D5D foreign key (category_id) references osp_wizard_category (id);
alter table osp_completed_wiz_category add index FK4EC54F7C21B27839 (completed_wizard_id), add constraint FK4EC54F7C21B27839 foreign key (completed_wizard_id) references osp_completed_wizard (id);
alter table osp_completed_wiz_category add index FK4EC54F7CF992DFC3 (parent_category_id), add constraint FK4EC54F7CF992DFC3 foreign key (parent_category_id) references osp_completed_wiz_category (id);
alter table osp_completed_wizard add index FKABC9DEB2D4C797 (root_category), add constraint FKABC9DEB2D4C797 foreign key (root_category) references osp_completed_wiz_category (id);
alter table osp_completed_wizard add index FKABC9DEB2D62513B2 (wizard_id), add constraint FKABC9DEB2D62513B2 foreign key (wizard_id) references osp_wizard (id);
alter table osp_completed_wizard_page add index FK52DE9BFCE4E7E6D3 (wizard_page_id), add constraint FK52DE9BFCE4E7E6D3 foreign key (wizard_page_id) references osp_wizard_page (id);
alter table osp_completed_wizard_page add index FK52DE9BFC2E24C4 (wizard_page_def_id), add constraint FK52DE9BFC2E24C4 foreign key (wizard_page_def_id) references osp_wizard_page_sequence (id);
alter table osp_completed_wizard_page add index FK52DE9BFC473463E4 (completed_category_id), add constraint FK52DE9BFC473463E4 foreign key (completed_category_id) references osp_completed_wiz_category (id);
alter table osp_guidance_item add index FK605DDBA737209105 (guidance_id), add constraint FK605DDBA737209105 foreign key (guidance_id) references osp_guidance (id);
alter table osp_guidance_item_file add index FK29770314DB93091D (item_id), add constraint FK29770314DB93091D foreign key (item_id) references osp_guidance_item (id);
alter table osp_matrix add index FK5A172054A6286438 (scaffolding_id), add constraint FK5A172054A6286438 foreign key (scaffolding_id) references osp_scaffolding (id);
alter table osp_matrix_cell add index FK8C1D366DE4E7E6D3 (wizard_page_id), add constraint FK8C1D366DE4E7E6D3 foreign key (wizard_page_id) references osp_wizard_page (id);
alter table osp_matrix_cell add index FK8C1D366D2D955C (matrix_id), add constraint FK8C1D366D2D955C foreign key (matrix_id) references osp_matrix (id);
alter table osp_matrix_cell add index FK8C1D366DCD99D2B1 (scaffolding_cell_id), add constraint FK8C1D366DCD99D2B1 foreign key (scaffolding_cell_id) references osp_scaffolding_cell (id);
create index IDX_MATRIX_LABEL on osp_matrix_label (type);
alter table osp_pres_itemdef_mimetype add index FK9EA59837650346CA (item_def_id), add constraint FK9EA59837650346CA foreign key (item_def_id) references osp_presentation_item_def (id);
alter table osp_presentation add index FKA9028D6DFAEA67E8 (style_id), add constraint FKA9028D6DFAEA67E8 foreign key (style_id) references osp_style (id);
alter table osp_presentation add index FKA9028D6D6FE1417D (template_id), add constraint FKA9028D6D6FE1417D foreign key (template_id) references osp_presentation_template (id);
alter table osp_presentation_comment add index FK1E7E658D7658ED43 (presentation_id), add constraint FK1E7E658D7658ED43 foreign key (presentation_id) references osp_presentation (id);
alter table osp_presentation_item add index FK2FA02A59165E3E4 (item_definition_id), add constraint FK2FA02A59165E3E4 foreign key (item_definition_id) references osp_presentation_item_def (id);
alter table osp_presentation_item add index FK2FA02A57658ED43 (presentation_id), add constraint FK2FA02A57658ED43 foreign key (presentation_id) references osp_presentation (id);
alter table osp_presentation_item_def add index FK1B6ADB6B6FE1417D (template_id), add constraint FK1B6ADB6B6FE1417D foreign key (template_id) references osp_presentation_template (id);
alter table osp_presentation_item_property add index FK86B1362FA9B15561 (presentation_page_item_id), add constraint FK86B1362FA9B15561 foreign key (presentation_page_item_id) references osp_presentation_page_item (id);
alter table osp_presentation_log add index FK2120E1727658ED43 (presentation_id), add constraint FK2120E1727658ED43 foreign key (presentation_id) references osp_presentation (id);
alter table osp_presentation_page add index FK2FCEA217658ED43 (presentation_id), add constraint FK2FCEA217658ED43 foreign key (presentation_id) references osp_presentation (id);
alter table osp_presentation_page add index FK2FCEA21FAEA67E8 (style_id), add constraint FK2FCEA21FAEA67E8 foreign key (style_id) references osp_style (id);
alter table osp_presentation_page add index FK2FCEA21533F283D (layout_id), add constraint FK2FCEA21533F283D foreign key (layout_id) references osp_presentation_layout (id);
alter table osp_presentation_page_item add index FK6417671954DB801 (presentation_page_region_id), add constraint FK6417671954DB801 foreign key (presentation_page_region_id) references osp_presentation_page_region (id);
alter table osp_presentation_page_region add index FK8A46C2D215C572B8 (presentation_page_id), add constraint FK8A46C2D215C572B8 foreign key (presentation_page_id) references osp_presentation_page (id);
alter table osp_reports_params add index FK231D4599C8A69327 (reportId), add constraint FK231D4599C8A69327 foreign key (reportId) references osp_reports (reportId);
alter table osp_reports_results add index FKB1427243C8A69327 (reportId), add constraint FKB1427243C8A69327 foreign key (reportId) references osp_reports (reportId);
alter table osp_scaffolding add index FK65135779FAEA67E8 (style_id), add constraint FK65135779FAEA67E8 foreign key (style_id) references osp_style (id);
alter table osp_scaffolding_cell add index FK184EAE68A6286438 (scaffolding_id), add constraint FK184EAE68A6286438 foreign key (scaffolding_id) references osp_scaffolding (id);
alter table osp_scaffolding_cell add index FK184EAE689FECDBB8 (level_id), add constraint FK184EAE689FECDBB8 foreign key (level_id) references osp_matrix_label (id);
alter table osp_scaffolding_cell add index FK184EAE68754F20BD (wiz_page_def_id), add constraint FK184EAE68754F20BD foreign key (wiz_page_def_id) references osp_wizard_page_def (id);
alter table osp_scaffolding_cell add index FK184EAE6870EDF97A (rootcriterion_id), add constraint FK184EAE6870EDF97A foreign key (rootcriterion_id) references osp_matrix_label (id);
alter table osp_scaffolding_cell_form_defs add index FK904DCA92754F20BD (wiz_page_def_id), add constraint FK904DCA92754F20BD foreign key (wiz_page_def_id) references osp_wizard_page_def (id);
alter table osp_scaffolding_criteria add index FK8634116518C870CC (elt), add constraint FK8634116518C870CC foreign key (elt) references osp_matrix_label (id);
alter table osp_scaffolding_criteria add index FK86341165A6286438 (scaffolding_id), add constraint FK86341165A6286438 foreign key (scaffolding_id) references osp_scaffolding (id);
alter table osp_scaffolding_levels add index FK4EBCD0F51EFC6CAF (elt), add constraint FK4EBCD0F51EFC6CAF foreign key (elt) references osp_matrix_label (id);
alter table osp_scaffolding_levels add index FK4EBCD0F5A6286438 (scaffolding_id), add constraint FK4EBCD0F5A6286438 foreign key (scaffolding_id) references osp_scaffolding (id);
alter table osp_template_file_ref add index FK4B70FB026FE1417D (template_id), add constraint FK4B70FB026FE1417D foreign key (template_id) references osp_presentation_template (id);
alter table osp_wiz_page_attachment add index FK2257FCC9BDC195A7 (page_id), add constraint FK2257FCC9BDC195A7 foreign key (page_id) references osp_wizard_page (id);
alter table osp_wiz_page_form add index FK4725E4EABDC195A7 (page_id), add constraint FK4725E4EABDC195A7 foreign key (page_id) references osp_wizard_page (id);
alter table osp_wizard add index FK6B9ACDFEE831DD1C (root_category), add constraint FK6B9ACDFEE831DD1C foreign key (root_category) references osp_wizard_category (id);
alter table osp_wizard add index FK6B9ACDFEFAEA67E8 (style_id), add constraint FK6B9ACDFEFAEA67E8 foreign key (style_id) references osp_style (id);
alter table osp_wizard add index FK6B9ACDFEC73F84BD (id), add constraint FK6B9ACDFEC73F84BD foreign key (id) references osp_workflow_parent (id);
alter table osp_wizard_category add index FK3A81FE1FD62513B2 (wizard_id), add constraint FK3A81FE1FD62513B2 foreign key (wizard_id) references osp_wizard (id);
alter table osp_wizard_category add index FK3A81FE1FE0EFF548 (parent_category_id), add constraint FK3A81FE1FE0EFF548 foreign key (parent_category_id) references osp_wizard_category (id);
alter table osp_wizard_page add index FK4CFB5C30754F20BD (wiz_page_def_id), add constraint FK4CFB5C30754F20BD foreign key (wiz_page_def_id) references osp_wizard_page_def (id);
alter table osp_wizard_page_def add index FK6ABE7776FAEA67E8 (style_id), add constraint FK6ABE7776FAEA67E8 foreign key (style_id) references osp_style (id);
alter table osp_wizard_page_def add index FK6ABE7776C73F84BD (id), add constraint FK6ABE7776C73F84BD foreign key (id) references osp_workflow_parent (id);
alter table osp_wizard_page_sequence add index FKA5A702F06EA23D5D (category_id), add constraint FKA5A702F06EA23D5D foreign key (category_id) references osp_wizard_category (id);
alter table osp_wizard_page_sequence add index FKA5A702F0754F20BD (wiz_page_def_id), add constraint FKA5A702F0754F20BD foreign key (wiz_page_def_id) references osp_wizard_page_def (id);
alter table osp_workflow add index FK2065879242A62872 (parent_id), add constraint FK2065879242A62872 foreign key (parent_id) references osp_workflow_parent (id);
alter table osp_workflow_item add index FKB38697A091A4BC5E (workflow_id), add constraint FKB38697A091A4BC5E foreign key (workflow_id) references osp_workflow (id);


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Content / Metaobj
-- --------------------------------------------------------------------------------------------------------------------------------------

-- These tables have been renamed

RENAME TABLE osp_repository_lock TO content_resource_lock;
RENAME TABLE osp_structured_artifact_def TO metaobj_form_def;


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Rwiki
-- --------------------------------------------------------------------------------------------------------------------------------------

ALTER TABLE rwikicurrentcontent CHANGE content content MEDIUMTEXT;
ALTER TABLE rwikihistorycontent CHANGE content content MEDIUMTEXT;


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Message Center
-- --------------------------------------------------------------------------------------------------------------------------------------

-- Note, this is complet DDL, since MessageCenter is new to Sakai for 2.2.  If you have an older version of MessageCenter running, DO NOT RUN THIS!
create table MFR_LABEL_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(36) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(36) not null,
   KEY_C varchar(255) not null,
   VALUE_C varchar(255) not null,
   df_surrogateKey bigint,
   df_index_col integer,
   dt_surrogateKey bigint,
   dt_index_col integer,
   primary key (ID)
);
create table MFR_UNREAD_STATUS_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   TOPIC_C bigint not null,
   MESSAGE_C bigint not null,
   USER_C varchar(255) not null,
   READ_C bit not null,
   primary key (ID)
);
create table MFR_AP_ACCESSORS_T (
   apaSurrogateKey bigint not null,
   userSurrogateKey bigint not null,
   accessors_index_col integer not null,
   primary key (apaSurrogateKey, accessors_index_col)
);
create table MFR_AP_MODERATORS_T (
   apmSurrogateKey bigint not null,
   userSurrogateKey bigint not null,
   moderators_index_col integer not null,
   primary key (apmSurrogateKey, moderators_index_col)
);
create table MFR_TOPIC_T (
   ID bigint not null auto_increment,
   TOPIC_DTYPE varchar(2) not null,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(36) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(36) not null,
   DEFAULTASSIGNNAME varchar(255),
   TITLE varchar(255) not null,
   SHORT_DESCRIPTION varchar(255),
   EXTENDED_DESCRIPTION text,
   MUTABLE bit not null,
   SORT_INDEX integer not null,
   TYPE_UUID varchar(36) not null,
   of_surrogateKey bigint,
   pf_surrogateKey bigint,
   USER_ID varchar(255),
   CONTEXT_ID varchar(36),
   pt_surrogateKey bigint,
   LOCKED bit,
   DRAFT bit,
   CONFIDENTIAL_RESPONSES bit,
   MUST_RESPOND_BEFORE_READING bit,
   HOUR_BEFORE_RESPONSES_VISIBLE integer,
   MODERATED bit,
   GRADEBOOK varchar(255),
   GRADEBOOK_ASSIGNMENT varchar(255),
   primary key (ID)
);
create table MFR_MESSAGE_PERMISSIONS_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   ROLE_C varchar(255) not null,
   READ_C bit not null,
   REVISE_ANY bit not null,
   REVISE_OWN bit not null,
   DELETE_ANY bit not null,
   DELETE_OWN bit not null,
   READ_DRAFTS bit not null,
   MARK_AS_READ bit not null,
   DEFAULT_VALUE bit not null,
   areaSurrogateKey bigint,
   forumSurrogateKey bigint,
   topicSurrogateKey bigint,
   primary key (ID)
);
create table MFR_PERMISSION_LEVEL_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(255) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(255) not null,
   NAME varchar(50) not null,
   TYPE_UUID varchar(36) not null,
   CHANGE_SETTINGS bit not null,
   DELETE_ANY bit not null,
   DELETE_OWN bit not null,
   MARK_AS_READ bit not null,
   MOVE_POSTING bit not null,
   NEW_FORUM bit not null,
   NEW_RESPONSE bit not null,
   NEW_RESPONSE_TO_RESPONSE bit not null,
   NEW_TOPIC bit not null,
   POST_TO_GRADEBOOK bit not null,
   X_READ bit not null,
   REVISE_ANY bit not null,
   REVISE_OWN bit not null,
   MODERATE_POSTINGS bit not null,
   primary key (ID)
);
create table MFR_AP_CONTRIBUTORS_T (
   apcSurrogateKey bigint not null,
   userSurrogateKey bigint not null,
   contributors_index_col integer not null,
   primary key (apcSurrogateKey, contributors_index_col)
);
create table MFR_MESSAGE_T (
   ID bigint not null auto_increment,
   MESSAGE_DTYPE varchar(2) not null,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(36) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(36) not null,
   TITLE varchar(255) not null,
   BODY text,
   AUTHOR varchar(255) not null,
   HAS_ATTACHMENTS bit not null,
   GRADECOMMENT text,
   GRADEASSIGNMENTNAME varchar(255),
   LABEL varchar(255),
   IN_REPLY_TO bigint,
   GRADEBOOK varchar(255),
   GRADEBOOK_ASSIGNMENT varchar(255),
   TYPE_UUID varchar(36) not null,
   APPROVED bit not null,
   DRAFT bit not null,
   surrogateKey bigint,
   EXTERNAL_EMAIL bit,
   EXTERNAL_EMAIL_ADDRESS varchar(255),
   RECIPIENTS_AS_TEXT text,
   primary key (ID)
);
create table MFR_AREA_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(36) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(36) not null,
   CONTEXT_ID varchar(255) not null,
   NAME varchar(255) not null,
   HIDDEN bit not null,
   TYPE_UUID varchar(36) not null,
   ENABLED bit not null,
   LOCKED bit not null,
   primary key (ID)
);
create table MFR_PVT_MSG_USR_T (
   messageSurrogateKey bigint not null,
   USER_ID varchar(255) not null,
   TYPE_UUID varchar(255) not null,
   CONTEXT_ID varchar(255) not null,
   READ_STATUS bit not null,
   user_index_col integer not null,
   primary key (messageSurrogateKey, user_index_col)
);
create table MFR_ATTACHMENT_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(255) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(255) not null,
   ATTACHMENT_ID varchar(255) not null,
   ATTACHMENT_URL varchar(255) not null,
   ATTACHMENT_NAME varchar(255) not null,
   ATTACHMENT_SIZE varchar(255) not null,
   ATTACHMENT_TYPE varchar(255) not null,
   m_surrogateKey bigint,
   of_surrogateKey bigint,
   pf_surrogateKey bigint,
   t_surrogateKey bigint,
   of_urrogateKey bigint,
   primary key (ID)
);
create table MFR_ACTOR_PERMISSIONS_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   primary key (ID)
);
create table MFR_MEMBERSHIP_ITEM_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(255) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(255) not null,
   NAME varchar(255) not null,
   TYPE integer not null,
   PERMISSION_LEVEL_NAME varchar(255) not null,
   PERMISSION_LEVEL bigint unique,
   a_surrogateKey bigint,
   of_surrogateKey bigint,
   t_surrogateKey bigint,
   primary key (ID)
);
create table MFR_CONTROL_PERMISSIONS_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   ROLE varchar(255) not null,
   NEW_FORUM bit not null,
   POST_TO_GRADEBOOK bit not null,
   NEW_TOPIC bit not null,
   NEW_RESPONSE bit not null,
   RESPONSE_TO_RESPONSE bit not null,
   MOVE_POSTINGS bit not null,
   CHANGE_SETTINGS bit not null,
   DEFAULT_VALUE bit not null,
   areaSurrogateKey bigint,
   forumSurrogateKey bigint,
   topicSurrogateKey bigint,
   primary key (ID)
);
create table MFR_PRIVATE_FORUM_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(36) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(36) not null,
   TITLE varchar(255) not null,
   SHORT_DESCRIPTION varchar(255),
   EXTENDED_DESCRIPTION text,
   TYPE_UUID varchar(36) not null,
   SORT_INDEX integer not null,
   OWNER varchar(255) not null,
   AUTO_FORWARD bit,
   AUTO_FORWARD_EMAIL varchar(255),
   PREVIEW_PANE_ENABLED bit,
   surrogateKey bigint,
   primary key (ID)
);
create table MFR_OPEN_FORUM_T (
   ID bigint not null auto_increment,
   FORUM_DTYPE varchar(2) not null,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(36) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(36) not null,
   DEFAULTASSIGNNAME varchar(255),
   TITLE varchar(255) not null,
   SHORT_DESCRIPTION varchar(255),
   EXTENDED_DESCRIPTION text,
   TYPE_UUID varchar(36) not null,
   SORT_INDEX integer not null,
   LOCKED bit not null,
   DRAFT bit,
   surrogateKey bigint,
   MODERATED bit,
   primary key (ID)
);
create table MFR_MESSAGE_FORUMS_USER_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   USER_ID varchar(255) not null,
   TYPE_UUID varchar(36) not null,
   primary key (ID)
);
create table MFR_DATE_RESTRICTIONS_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   VISIBLE datetime not null,
   VISIBLE_POST_ON_SCHEDULE bit not null,
   POSTING_ALLOWED datetime not null,
   PSTNG_ALLWD_PST_ON_SCHD bit not null,
   READ_ONLY datetime not null,
   READ_ONLY_POST_ON_SCHEDULE bit not null,
   HIDDEN datetime not null,
   HIDDEN_POST_ON_SCHEDULE bit not null,
   primary key (ID)
);
create index MFR_LABEL_PARENT_I2 on MFR_LABEL_T (dt_surrogateKey);
create index MFR_LABEL_PARENT_I1 on MFR_LABEL_T (df_surrogateKey);
alter table MFR_LABEL_T add index FKC6611543EA902104 (df_surrogateKey), add constraint FKC6611543EA902104 foreign key (df_surrogateKey) references MFR_OPEN_FORUM_T (ID);
alter table MFR_LABEL_T add index FKC661154344B127B6 (dt_surrogateKey), add constraint FKC661154344B127B6 foreign key (dt_surrogateKey) references MFR_TOPIC_T (ID);
create index MFR_UNREAD_STATUS_I1 on MFR_UNREAD_STATUS_T (TOPIC_C, MESSAGE_C, USER_C, READ_C);
alter table MFR_AP_ACCESSORS_T add index FKC8532ED796792399 (apaSurrogateKey), add constraint FKC8532ED796792399 foreign key (apaSurrogateKey) references MFR_ACTOR_PERMISSIONS_T (ID);
alter table MFR_AP_ACCESSORS_T add index FKC8532ED721BCC7D2 (userSurrogateKey), add constraint FKC8532ED721BCC7D2 foreign key (userSurrogateKey) references MFR_MESSAGE_FORUMS_USER_T (ID);
alter table MFR_AP_MODERATORS_T add index FK75B43C0D21BCC7D2 (userSurrogateKey), add constraint FK75B43C0D21BCC7D2 foreign key (userSurrogateKey) references MFR_MESSAGE_FORUMS_USER_T (ID);
alter table MFR_AP_MODERATORS_T add index FK75B43C0DC49D71A5 (apmSurrogateKey), add constraint FK75B43C0DC49D71A5 foreign key (apmSurrogateKey) references MFR_ACTOR_PERMISSIONS_T (ID);
create index MFR_TOPIC_PARENT_I1 on MFR_TOPIC_T (of_surrogateKey);
create index MRF_TOPIC_DTYPE_I on MFR_TOPIC_T (TOPIC_DTYPE);
create index MFR_TOPIC_PRI_PARENT_I on MFR_TOPIC_T (pt_surrogateKey);
create index MFR_PT_CONTEXT_I on MFR_TOPIC_T (CONTEXT_ID);
create index MFR_TOPIC_PARENT_I2 on MFR_TOPIC_T (pf_surrogateKey);
create index MFR_TOPIC_CREATED_I on MFR_TOPIC_T (CREATED);
alter table MFR_TOPIC_T add index FK863DC0BEC6FDB1CF (of_surrogateKey), add constraint FK863DC0BEC6FDB1CF foreign key (of_surrogateKey) references MFR_OPEN_FORUM_T (ID);
alter table MFR_TOPIC_T add index FK863DC0BE7AFA22C2 (pt_surrogateKey), add constraint FK863DC0BE7AFA22C2 foreign key (pt_surrogateKey) references MFR_TOPIC_T (ID);
alter table MFR_TOPIC_T add index FK863DC0BE20D91C10 (pf_surrogateKey), add constraint FK863DC0BE20D91C10 foreign key (pf_surrogateKey) references MFR_PRIVATE_FORUM_T (ID);
create index MFR_MP_PARENT_FORUM_I on MFR_MESSAGE_PERMISSIONS_T (forumSurrogateKey);
create index MFR_MP_PARENT_AREA_I on MFR_MESSAGE_PERMISSIONS_T (areaSurrogateKey);
create index MFR_MP_PARENT_TOPIC_I on MFR_MESSAGE_PERMISSIONS_T (topicSurrogateKey);
alter table MFR_MESSAGE_PERMISSIONS_T add index FK750F9AFB17721828 (forumSurrogateKey), add constraint FK750F9AFB17721828 foreign key (forumSurrogateKey) references MFR_OPEN_FORUM_T (ID);
alter table MFR_MESSAGE_PERMISSIONS_T add index FK750F9AFB51C89994 (areaSurrogateKey), add constraint FK750F9AFB51C89994 foreign key (areaSurrogateKey) references MFR_AREA_T (ID);
alter table MFR_MESSAGE_PERMISSIONS_T add index FK750F9AFBE581B336 (topicSurrogateKey), add constraint FK750F9AFBE581B336 foreign key (topicSurrogateKey) references MFR_TOPIC_T (ID);
alter table MFR_AP_CONTRIBUTORS_T add index FKA221A1F7737F309B (apcSurrogateKey), add constraint FKA221A1F7737F309B foreign key (apcSurrogateKey) references MFR_ACTOR_PERMISSIONS_T (ID);
alter table MFR_AP_CONTRIBUTORS_T add index FKA221A1F721BCC7D2 (userSurrogateKey), add constraint FKA221A1F721BCC7D2 foreign key (userSurrogateKey) references MFR_MESSAGE_FORUMS_USER_T (ID);
create index MFR_MESSAGE_LABEL_I on MFR_MESSAGE_T (LABEL);
create index MFR_MESSAGE_HAS_ATTACHMENTS_I on MFR_MESSAGE_T (HAS_ATTACHMENTS);
create index MFR_MESSAGE_CREATED_I on MFR_MESSAGE_T (CREATED);
create index MFR_MESSAGE_AUTHOR_I on MFR_MESSAGE_T (AUTHOR);
create index MFR_MESSAGE_DTYPE_I on MFR_MESSAGE_T (MESSAGE_DTYPE);
create index MFR_MESSAGE_TITLE_I on MFR_MESSAGE_T (TITLE);
create index MFR_MESSAGE_PARENT_TOPIC_I on MFR_MESSAGE_T (surrogateKey);
alter table MFR_MESSAGE_T add index FK80C1A316FE0789EA (IN_REPLY_TO), add constraint FK80C1A316FE0789EA foreign key (IN_REPLY_TO) references MFR_MESSAGE_T (ID);
alter table MFR_MESSAGE_T add index FK80C1A3164FDCE067 (surrogateKey), add constraint FK80C1A3164FDCE067 foreign key (surrogateKey) references MFR_TOPIC_T (ID);
create index MFR_AREA_CONTEXT_I on MFR_AREA_T (CONTEXT_ID);
create index MFR_AREA_TYPE_I on MFR_AREA_T (TYPE_UUID);
create index MFR_PVT_MSG_USR_I1 on MFR_PVT_MSG_USR_T (USER_ID);
create index MFR_PVT_MSG_USR_I2 on MFR_PVT_MSG_USR_T (TYPE_UUID);
create index MFR_PVT_MSG_USR_I3 on MFR_PVT_MSG_USR_T (CONTEXT_ID);
create index MFR_PVT_MSG_USR_I4 on MFR_PVT_MSG_USR_T (READ_STATUS);
alter table MFR_PVT_MSG_USR_T add index FKC4DE0E14FA8620E (messageSurrogateKey), add constraint FKC4DE0E14FA8620E foreign key (messageSurrogateKey) references MFR_MESSAGE_T (ID);
create index MFR_ATTACHMENT_PARENT_I4 on MFR_ATTACHMENT_T (t_surrogateKey);
create index MFR_ATTACHMENT_PARENT_I on MFR_ATTACHMENT_T (m_surrogateKey);
create index MFR_ATTACHMENT_PARENT_I3 on MFR_ATTACHMENT_T (pf_surrogateKey);
create index MFR_ATTACHMENT_PARENT_I2 on MFR_ATTACHMENT_T (of_surrogateKey);
alter table MFR_ATTACHMENT_T add index FK7B2D5CDE2AFBA652 (t_surrogateKey), add constraint FK7B2D5CDE2AFBA652 foreign key (t_surrogateKey) references MFR_TOPIC_T (ID);
alter table MFR_ATTACHMENT_T add index FK7B2D5CDEC6FDB1CF (of_surrogateKey), add constraint FK7B2D5CDEC6FDB1CF foreign key (of_surrogateKey) references MFR_OPEN_FORUM_T (ID);
alter table MFR_ATTACHMENT_T add index FK7B2D5CDE20D91C10 (pf_surrogateKey), add constraint FK7B2D5CDE20D91C10 foreign key (pf_surrogateKey) references MFR_PRIVATE_FORUM_T (ID);
alter table MFR_ATTACHMENT_T add index FK7B2D5CDEFDEB22F9 (m_surrogateKey), add constraint FK7B2D5CDEFDEB22F9 foreign key (m_surrogateKey) references MFR_MESSAGE_T (ID);
alter table MFR_ATTACHMENT_T add index FK7B2D5CDEAD5AF852 (of_urrogateKey), add constraint FK7B2D5CDEAD5AF852 foreign key (of_urrogateKey) references MFR_OPEN_FORUM_T (ID);
alter table MFR_MEMBERSHIP_ITEM_T add index FKE03761CB6785AF85 (a_surrogateKey), add constraint FKE03761CB6785AF85 foreign key (a_surrogateKey) references MFR_AREA_T (ID);
alter table MFR_MEMBERSHIP_ITEM_T add index FKE03761CBC6FDB1CF (of_surrogateKey), add constraint FKE03761CBC6FDB1CF foreign key (of_surrogateKey) references MFR_OPEN_FORUM_T (ID);
alter table MFR_MEMBERSHIP_ITEM_T add index FKE03761CB2AFBA652 (t_surrogateKey), add constraint FKE03761CB2AFBA652 foreign key (t_surrogateKey) references MFR_TOPIC_T (ID);
alter table MFR_MEMBERSHIP_ITEM_T add index FKE03761CB925CE0F4 (PERMISSION_LEVEL), add constraint FKE03761CB925CE0F4 foreign key (PERMISSION_LEVEL) references MFR_PERMISSION_LEVEL_T (ID);
create index MFR_CP_PARENT_FORUM_I on MFR_CONTROL_PERMISSIONS_T (forumSurrogateKey);
create index MFR_CP_PARENT_TOPIC_I on MFR_CONTROL_PERMISSIONS_T (topicSurrogateKey);
create index MFR_CP_PARENT_AREA_I on MFR_CONTROL_PERMISSIONS_T (areaSurrogateKey);
alter table MFR_CONTROL_PERMISSIONS_T add index FKA07CF1D1E581B336 (topicSurrogateKey), add constraint FKA07CF1D1E581B336 foreign key (topicSurrogateKey) references MFR_TOPIC_T (ID);
alter table MFR_CONTROL_PERMISSIONS_T add index FKA07CF1D151C89994 (areaSurrogateKey), add constraint FKA07CF1D151C89994 foreign key (areaSurrogateKey) references MFR_AREA_T (ID);
alter table MFR_CONTROL_PERMISSIONS_T add index FKA07CF1D117721828 (forumSurrogateKey), add constraint FKA07CF1D117721828 foreign key (forumSurrogateKey) references MFR_OPEN_FORUM_T (ID);
create index MFR_PRIVATE_FORUM_CREATED_I on MFR_PRIVATE_FORUM_T (CREATED);
create index MFR_PRIVATE_FORUM_OWNER_I on MFR_PRIVATE_FORUM_T (OWNER);
create index MFR_PF_PARENT_BASEFORUM_I on MFR_PRIVATE_FORUM_T (surrogateKey);
alter table MFR_PRIVATE_FORUM_T add index FKA9EE57544FDCE067 (surrogateKey), add constraint FKA9EE57544FDCE067 foreign key (surrogateKey) references MFR_AREA_T (ID);
create index MFR_OPEN_FORUM_DTYPE_I on MFR_OPEN_FORUM_T (FORUM_DTYPE);
create index MFR_OPEN_FORUM_TYPE_I on MFR_OPEN_FORUM_T (TYPE_UUID);
create index MFR_OF_PARENT_BASEFORUM_I on MFR_OPEN_FORUM_T (surrogateKey);
alter table MFR_OPEN_FORUM_T add index FKC17608474FDCE067 (surrogateKey), add constraint FKC17608474FDCE067 foreign key (surrogateKey) references MFR_AREA_T (ID);


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Postem
-- --------------------------------------------------------------------------------------------------------------------------------------

-- Note, this is complete DDL for Postem, since Postem is new to Sakai for 2.2.  If you are running an older version of Postem, DO NOT RUN THIS!

create table SAKAI_POSTEM_HEADINGS (
   gradebook_id bigint not null,
   heading varchar(255) not null,
   location integer not null,
   primary key (gradebook_id, location)
);
create table SAKAI_POSTEM_STUDENT (
   id bigint not null auto_increment,
   lockId integer not null,
   username varchar(36) not null,
   last_checked datetime,
   surrogate_key bigint,
   primary key (id)
);
create table SAKAI_POSTEM_STUDENT_GRADES (
   student_id bigint not null,
   grade varchar(255),
   location integer not null,
   primary key (student_id, location)
);
create table SAKAI_POSTEM_GRADEBOOK (
   id bigint not null auto_increment,
   lockId integer not null,
   title varchar(36) not null,
   context varchar(36) not null,
   creator varchar(36) not null,
   created datetime not null,
   last_updater varchar(36) not null,
   last_updated datetime not null,
   released bit not null,
   stats bit not null,
   template varchar(255),
   primary key (id),
   unique (title, context)
);
alter table SAKAI_POSTEM_HEADINGS add index FKF54C1C2EE091F27A (gradebook_id), add constraint FKF54C1C2EE091F27A foreign key (gradebook_id) references SAKAI_POSTEM_GRADEBOOK (id);
create index POSTEM_STUDENT_USERNAME_I on SAKAI_POSTEM_STUDENT (username);
alter table SAKAI_POSTEM_STUDENT add index FK4FBA80FEABC85878 (surrogate_key), add constraint FK4FBA80FEABC85878 foreign key (surrogate_key) references SAKAI_POSTEM_GRADEBOOK (id);
alter table SAKAI_POSTEM_STUDENT_GRADES add index FK321A31DDC276819F (student_id), add constraint FK321A31DDC276819F foreign key (student_id) references SAKAI_POSTEM_STUDENT (id);
create index POSTEM_GB_CONTEXT_I on SAKAI_POSTEM_GRADEBOOK (context);
create index POSTEM_GB_TITLE_I on SAKAI_POSTEM_GRADEBOOK (title);


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Increase the field size for the SESSION_IP field in the SAKAI_SESSION table
-- --------------------------------------------------------------------------------------------------------------------------------------

ALTER TABLE SAKAI_SESSION CHANGE SESSION_IP SESSION_IP VARCHAR (128);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- Increase the field size for the NOTES field in the SAKAI_PERSON_T table
-- --------------------------------------------------------------------------------------------------------------------------------------

ALTER TABLE SAKAI_PERSON_T CHANGE NOTES NOTES varchar(4000);
