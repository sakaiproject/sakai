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

CREATE TABLE SAKAI_USER_ID_MAP
(
       USER_ID             VARCHAR (99) NOT NULL,
       EID                 VARCHAR (99) NOT NULL
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
-- make sure admin has the admin site tab (ignore if it already exists)
----------------------------------------------------------------------------------------------------------------------------------------

INSERT IGNORE INTO SAKAI_SITE_USER (SITE_ID, USER_ID, PERMISSION) VALUES('!admin', 'admin', -1);


----------------------------------------------------------------------------------------------------------------------------------------
-- new default permissions
----------------------------------------------------------------------------------------------------------------------------------------

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
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
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


----------------------------------------------------------------------------------------------------------------------------------------
-- backfill new group permissions into existing group realms
----------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

-- These are for the group templates, and should already be set in the site templates
-- ADJUST ME: adjust theses for your needs, either with different permissions, or duplicate for other roles than 'access', 'Student', 'maintain', 'Instructor' and 'Teaching Assistant'

INSERT INTO PERMISSIONS_SRC_TEMP values ('access','asn.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','asn.submit');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','calendar.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','content.read');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','asn.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','asn.submit');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','calendar.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','content.read');

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


----------------------------------------------------------------------------------------------------------------------------------------
-- backfill new site permissions into existing site realms
----------------------------------------------------------------------------------------------------------------------------------------

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


----------------------------------------------------------------------------------------------------------------------------------------
-- Samigo
----------------------------------------------------------------------------------------------------------------------------------------

-- added two columns in SAM_ITEMGRADING_T to support audio recording question type implemented in samigo 2.2, please see JIRA task SAK-1894 for specification

alter table SAM_ITEMGRADING_T add column ATTEMPTSREMAINING integer;
alter table SAM_ITEMGRADING_T add column LASTDURATION varchar(36);


----------------------------------------------------------------------------------------------------------------------------------------
-- Search
----------------------------------------------------------------------------------------------------------------------------------------

drop table if exists search_segments;
drop table if exists searchbuilderitem;
drop table if exists searchwriterlock;
create table search_segments (name_ varchar(254) not null, version_ bigint not null, size_ bigint not null, packet_ longblob, primary key (name_));
create table searchbuilderitem (id varchar(64) not null, version datetime not null, name varchar(255) not null unique, context varchar(255) not null, searchaction integer, searchstate integer, primary key (id));
create table searchwriterlock (id varchar(64) not null, lockkey varchar(64) not null unique, nodename varchar(64), expires datetime not null, primary key (id));
create index isearchbuilderitem_context on searchbuilderitem (context);
create index isearchbuilderitem_name on searchbuilderitem (name);
create index isearchwriterlock_lockkey on searchwriterlock (lockkey);


----------------------------------------------------------------------------------------------------------------------------------------
-- Schedule Summary Tool
----------------------------------------------------------------------------------------------------------------------------------------

-- If you have run the new, provisional, schedule summary tool in its old form, the registration has changed, so run this

UPDATE SAKAI_SITE_TOOL SET REGISTRATION='sakai.summary.calendar' WHERE REGISTRATION='sakai.synoptic.calendar';


----------------------------------------------------------------------------------------------------------------------------------------
-- OSP
----------------------------------------------------------------------------------------------------------------------------------------

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
create table osp_presentation_template (id varchar(36) not null, name varchar(255), description varchar(255), includeHeaderAndFooter bit, includeComments bit, published bit, owner_id varchar(255) not null, renderer varchar(36), markup text, propertyPage varchar(36), documentRoot varchar(255), created datetime not null, modified datetime not null, site_id varchar(36) not null, primary key (id));
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


----------------------------------------------------------------------------------------------------------------------------------------
-- Content / Metaobj
----------------------------------------------------------------------------------------------------------------------------------------

-- These tables have been renamed

RENAME TABLE OSP_REPOSITORY_LOCK TO CONTENT_RESOURCE_LOCK;
RENAME TABLE OSP_STRUCTURED_ARTIFACT_DEF TO METAOBJ_FORM_DEF;
