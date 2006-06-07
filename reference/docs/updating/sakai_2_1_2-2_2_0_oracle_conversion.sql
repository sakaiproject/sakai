-- This is the Oracle Sakai 2.1.2 -> 2.2.0 conversion script
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
       USER_ID             VARCHAR2(99) NOT NULL,
       EID                 VARCHAR2(99) NOT NULL
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
-- from gradebook/app/business/src/sql/oracle/sakai_gradebook_2.1.x_to_2.2.sql
--
-- Gradebook table changes between Sakai 2.1.* and 2.2.
----------------------------------------------------------------------------------------------------------------------------------------

-- Add grading scale support.
create table GB_PROPERTY_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   NAME varchar2(255) not null unique,
   VALUE varchar2(255),
   primary key (ID)
);
create table GB_GRADING_SCALE_GRADES_T (
   GRADING_SCALE_ID number(19,0) not null,
   LETTER_GRADE varchar2(255),
   GRADE_IDX number(10,0) not null,
   primary key (GRADING_SCALE_ID, GRADE_IDX)
);
create table GB_GRADING_SCALE_T (
   ID number(19,0) not null,
   OBJECT_TYPE_ID number(10,0) not null,
   VERSION number(10,0) not null,
   SCALE_UID varchar2(255) not null unique,
   NAME varchar2(255) not null,
   UNAVAILABLE number(1,0),
   primary key (ID)
);
create table GB_GRADING_SCALE_PERCENTS_T (
   GRADING_SCALE_ID number(19,0) not null,
   PERCENT double precision,
   LETTER_GRADE varchar2(255) not null,
   primary key (GRADING_SCALE_ID, LETTER_GRADE)
);
alter table GB_GRADE_MAP_T add (GB_GRADING_SCALE_T number(19,0));
alter table GB_GRADING_SCALE_GRADES_T add constraint FK5D3F0C955A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T;
alter table GB_GRADE_MAP_T add constraint FKADE11225108F4490 foreign key (GB_GRADING_SCALE_T) references GB_GRADING_SCALE_T;
alter table GB_GRADING_SCALE_PERCENTS_T add constraint FKC98BE4675A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T;
create sequence GB_PROPERTY_S;
create sequence GB_GRADING_SCALE_S;

-- Add indexes for improved performance and reduced locking.
create index GRADEBOOK_ID on GB_GRADABLE_OBJECT_T (GRADEBOOK_ID);
create index GB_GRADE_MAP_GB_IDX on GB_GRADE_MAP_T (GRADEBOOK_ID);
create index GB_GRADABLE_OBJ_ASN_IDX on GB_GRADABLE_OBJECT_T (OBJECT_TYPE_ID, GRADEBOOK_ID, NAME, REMOVED);
create index GB_GRADE_RECORD_O_T_IDX on GB_GRADE_RECORD_T (OBJECT_TYPE_ID);

-- These two may have already been defined via the 2.1.1 upgrade.
create index GB_GRADE_RECORD_G_O_IDX on GB_GRADE_RECORD_T (GRADABLE_OBJECT_ID);
create index GB_GRADE_RECORD_STUDENT_ID_IDX on GB_GRADE_RECORD_T (STUDENT_ID);


----------------------------------------------------------------------------------------------------------------------------------------
-- make sure admin has the admin site tab
----------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO SAKAI_SITE_USER (SITE_ID, USER_ID, PERMISSION) VALUES('!admin', 'admin', -1);


----------------------------------------------------------------------------------------------------------------------------------------
-- new default permissions
----------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'asn.all.groups');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'calendar.all.groups');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'content.all.groups');
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
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR2(99), FUNCTION_NAME VARCHAR2(99));

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

alter table SAM_ITEMGRADING_T add (ATTEMPTSREMAINING integer);
alter table SAM_ITEMGRADING_T add (LASTDURATION varchar(36));


----------------------------------------------------------------------------------------------------------------------------------------
-- Search
----------------------------------------------------------------------------------------------------------------------------------------

drop table search_segments cascade constraints;
drop table searchbuilderitem cascade constraints;
drop table searchwriterlock cascade constraints;
create table search_segments (name_ varchar2(254 char) not null, version_ number(19,0) not null, size_ number(19,0) not null, packet_ blob, primary key (name_));
create table searchbuilderitem (id varchar2(64 char) not null, version timestamp not null, name varchar2(255 char) not null unique, context varchar2(255 char) not null, searchaction number(10,0), searchstate number(10,0), primary key (id));
create table searchwriterlock (id varchar2(64 char) not null, lockkey varchar2(64 char) not null unique, nodename varchar2(64 char), expires timestamp not null, primary key (id));
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

create table osp_authz_simple (id varchar2(36 char) not null, qualifier_id varchar2(255 char) not null, agent_id varchar2(255 char) not null, function_name varchar2(255 char) not null, primary key (id));
create table osp_completed_wiz_category (id varchar2(36 char) not null, completed_wizard_id varchar2(36 char), category_id varchar2(36 char), expanded number(1,0), seq_num number(10,0), parent_category_id varchar2(36 char), primary key (id));
create table osp_completed_wizard (id varchar2(36 char) not null, owner_id varchar2(255 char) not null, created timestamp not null, lastVisited timestamp not null, status varchar2(255 char), wizard_id varchar2(36 char), root_category varchar2(36 char) unique, primary key (id));
create table osp_completed_wizard_page (id varchar2(36 char) not null, completed_category_id varchar2(36 char), wizard_page_def_id varchar2(36 char), wizard_page_id varchar2(36 char) unique, seq_num number(10,0), created timestamp not null, lastVisited timestamp, primary key (id));
create table osp_guidance (id varchar2(36 char) not null, description varchar2(255 char), site_id varchar2(36 char) not null, securityQualifier varchar2(255 char), securityViewFunction varchar2(255 char) not null, securityEditFunction varchar2(255 char) not null, primary key (id));
create table osp_guidance_item (id varchar2(36 char) not null, type varchar2(255 char), text clob, guidance_id varchar2(36 char) not null, primary key (id));
create table osp_guidance_item_file (id varchar2(36 char) not null, baseReference varchar2(255 char), fullReference varchar2(255 char), item_id varchar2(36 char) not null, primary key (id));
create table osp_help_glossary (id varchar2(36 char) not null, worksite_id varchar2(255 char), term varchar2(255 char) not null, description varchar2(255 char) not null, primary key (id));
create table osp_help_glossary_desc (id varchar2(36 char) not null, entry_id varchar2(255 char), long_description clob, primary key (id));
create table osp_list_config (id varchar2(36 char) not null, owner_id varchar2(255 char) not null, tool_id varchar2(36 char), title varchar2(255 char), height number(10,0), numRows number(10,0), selected_columns varchar2(255 char) not null, primary key (id));
create table osp_matrix (id varchar2(36 char) not null, owner varchar2(255 char) not null, scaffolding_id varchar2(36 char) not null, primary key (id));
create table osp_matrix_cell (id varchar2(36 char) not null, matrix_id varchar2(36 char) not null, wizard_page_id varchar2(36 char) unique, scaffolding_cell_id varchar2(36 char), primary key (id));
create table osp_matrix_label (id varchar2(36 char) not null, type char(1 char) not null, description varchar2(255 char), color varchar2(7 char), textColor varchar2(7 char), primary key (id));
create table osp_pres_itemdef_mimetype (item_def_id varchar2(36 char) not null, primaryMimeType varchar2(36 char), secondaryMimeType varchar2(36 char));
create table osp_presentation (id varchar2(36 char) not null, owner_id varchar2(255 char) not null, template_id varchar2(36 char) not null, name varchar2(255 char), description varchar2(255 char), isDefault number(1,0), isPublic number(1,0), presentationType varchar2(255 char) not null, expiresOn timestamp, created timestamp not null, modified timestamp not null, allowComments number(1,0), site_id varchar2(36 char) not null, properties blob, style_id varchar2(36 char), advanced_navigation number(1,0), tool_id varchar2(36 char), primary key (id));
create table osp_presentation_comment (id varchar2(36 char) not null, title varchar2(255 char) not null, commentText varchar2(1024 char), creator_id varchar2(255 char) not null, presentation_id varchar2(36 char) not null, visibility number(3,0) not null, created timestamp not null, primary key (id));
create table osp_presentation_item (presentation_id varchar2(36 char) not null, artifact_id varchar2(36 char) not null, item_definition_id varchar2(36 char) not null, primary key (presentation_id, artifact_id, item_definition_id));
create table osp_presentation_item_def (id varchar2(36 char) not null, name varchar2(255 char), title varchar2(255 char), description varchar2(255 char), allowMultiple number(1,0), type varchar2(255 char), external_type varchar2(255 char), sequence_no number(10,0), template_id varchar2(36 char) not null, primary key (id));
create table osp_presentation_item_property (id varchar2(36 char) not null, presentation_page_item_id varchar2(36 char) not null, property_key varchar2(255 char) not null, property_value varchar2(255 char), primary key (id));
create table osp_presentation_layout (id varchar2(36 char) not null, name varchar2(255 char) not null, description varchar2(255 char), globalState number(10,0) not null, owner_id varchar2(255 char) not null, created timestamp not null, modified timestamp not null, xhtml_file_id varchar2(36 char) not null, preview_image_id varchar2(36 char), tool_id varchar2(36 char), site_id varchar2(36 char), primary key (id));
create table osp_presentation_log (id varchar2(36 char) not null, viewer_id varchar2(255 char) not null, presentation_id varchar2(36 char) not null, view_date timestamp, primary key (id));
create table osp_presentation_page (id varchar2(36 char) not null, title varchar2(255 char), description varchar2(255 char), keywords varchar2(255 char), presentation_id varchar2(36 char) not null, layout_id varchar2(36 char) not null, style_id varchar2(36 char), seq_num number(10,0), created timestamp not null, modified timestamp not null, primary key (id));
create table osp_presentation_page_item (id varchar2(36 char) not null, presentation_page_region_id varchar2(36 char) not null, type varchar2(255 char), value long, seq_num number(10,0) not null, primary key (id));
create table osp_presentation_page_region (id varchar2(36 char) not null, presentation_page_id varchar2(36 char) not null, region_id varchar2(255 char) not null, type varchar2(255 char), help_text varchar2(255 char), primary key (id));
create table osp_presentation_template (id varchar2(36 char) not null, name varchar2(255 char), description varchar2(255 char), includeHeaderAndFooter number(1,0), includeComments number(1,0), published number(1,0), owner_id varchar2(255 char) not null, renderer varchar2(36 char), markup varchar2(4000 char), propertyPage varchar2(36 char), documentRoot varchar2(255 char), created timestamp not null, modified timestamp not null, site_id varchar2(36 char) not null, primary key (id));
create table osp_reports (reportId varchar2(36 char) not null, reportDefIdMark varchar2(255 char), userId varchar2(255 char), title varchar2(255 char), keywords varchar2(255 char), description varchar2(255 char), isLive number(1,0), creationDate timestamp, type varchar2(255 char), display number(1,0), primary key (reportId));
create table osp_reports_params (paramId varchar2(36 char) not null, reportId varchar2(36 char), reportDefParamIdMark varchar2(255 char), value varchar2(255 char), primary key (paramId));
create table osp_reports_results (resultId varchar2(36 char) not null, reportId varchar2(36 char), userId varchar2(255 char), title varchar2(255 char), keywords varchar2(255 char), description varchar2(255 char), creationDate timestamp, xml clob, primary key (resultId));
create table osp_review (id varchar2(36 char) not null, review_content_id varchar2(36 char), site_id varchar2(36 char) not null, parent_id varchar2(36 char), review_device_id varchar2(36 char), review_type number(10,0) not null, primary key (id));
create table osp_scaffolding (id varchar2(36 char) not null, ownerId varchar2(255 char) not null, title varchar2(255 char), description clob, worksiteId varchar2(255 char), published number(1,0), publishedBy varchar2(255 char), publishedDate timestamp, columnLabel varchar2(255 char) not null, rowLabel varchar2(255 char) not null, readyColor varchar2(7 char) not null, pendingColor varchar2(7 char) not null, completedColor varchar2(7 char) not null, lockedColor varchar2(7 char) not null, workflowOption number(10,0) not null, exposed_page_id varchar2(36 char), style_id varchar2(36 char), primary key (id));
create table osp_scaffolding_cell (id varchar2(36 char) not null, rootcriterion_id varchar2(36 char), level_id varchar2(36 char), scaffolding_id varchar2(36 char) not null, wiz_page_def_id varchar2(36 char) unique, primary key (id));
create table osp_scaffolding_cell_form_defs (wiz_page_def_id varchar2(36 char) not null, form_def_id varchar2(255 char), seq_num number(10,0) not null, primary key (wiz_page_def_id, seq_num));
create table osp_scaffolding_criteria (scaffolding_id varchar2(36 char) not null, elt varchar2(36 char) not null, seq_num number(10,0) not null, primary key (scaffolding_id, seq_num));
create table osp_scaffolding_levels (scaffolding_id varchar2(36 char) not null, elt varchar2(36 char) not null, seq_num number(10,0) not null, primary key (scaffolding_id, seq_num));
create table osp_site_tool (id varchar2(40 char) not null, site_id varchar2(36 char), tool_id varchar2(36 char), listener_id varchar2(255 char), primary key (id));
create table osp_style (id varchar2(36 char) not null, name varchar2(255 char), description varchar2(255 char), globalState number(10,0) not null, owner_id varchar2(255 char) not null, style_file_id varchar2(36 char), site_id varchar2(36 char), created timestamp not null, modified timestamp not null, primary key (id));
create table osp_template_file_ref (id varchar2(36 char) not null, file_id varchar2(36 char), file_type_id varchar2(36 char), usage_desc varchar2(255 char), template_id varchar2(36 char) not null, primary key (id));
create table osp_wiz_page_attachment (id varchar2(36 char) not null, artifactId varchar2(36 char), page_id varchar2(36 char) not null, primary key (id));
create table osp_wiz_page_form (id varchar2(36 char) not null, artifactId varchar2(36 char), page_id varchar2(36 char) not null, formType varchar2(36 char), primary key (id));
create table osp_wizard (id varchar2(36 char) not null, owner_id varchar2(255 char) not null, name varchar2(255 char), description varchar2(1024 char), keywords varchar2(1024 char), created timestamp not null, modified timestamp not null, site_id varchar2(36 char) not null, guidance_id varchar2(36 char), published number(1,0), wizard_type varchar2(255 char), style_id varchar2(36 char), exposed_page_id varchar2(36 char), root_category varchar2(36 char) unique, seq_num number(10,0), primary key (id));
create table osp_wizard_category (id varchar2(36 char) not null, name varchar2(255 char), description varchar2(255 char), keywords varchar2(255 char), created timestamp not null, modified timestamp not null, wizard_id varchar2(36 char), parent_category_id varchar2(36 char), seq_num number(10,0), primary key (id));
create table osp_wizard_page (id varchar2(36 char) not null, owner varchar2(255 char) not null, status varchar2(255 char), wiz_page_def_id varchar2(36 char), modified timestamp, primary key (id));
create table osp_wizard_page_def (id varchar2(36 char) not null, initialStatus varchar2(255 char), name varchar2(255 char), description clob, site_id varchar2(255 char), guidance_id varchar2(255 char), style_id varchar2(36 char), primary key (id));
create table osp_wizard_page_sequence (id varchar2(36 char) not null, seq_num number(10,0), category_id varchar2(36 char) not null, wiz_page_def_id varchar2(36 char) unique, primary key (id));
create table osp_workflow (id varchar2(36 char) not null, title varchar2(255 char), parent_id varchar2(36 char) not null, primary key (id));
create table osp_workflow_item (id varchar2(36 char) not null, actionType number(10,0) not null, action_object_id varchar2(255 char) not null, action_value varchar2(255 char) not null, workflow_id varchar2(36 char) not null, primary key (id));
create table osp_workflow_parent (id varchar2(36 char) not null, reflection_device_id varchar2(36 char), reflection_device_type varchar2(255 char), evaluation_device_id varchar2(36 char), evaluation_device_type varchar2(255 char), review_device_id varchar2(36 char), review_device_type varchar2(255 char), primary key (id));
alter table osp_completed_wiz_category add constraint FK4EC54F7C6EA23D5D foreign key (category_id) references osp_wizard_category;
alter table osp_completed_wiz_category add constraint FK4EC54F7C21B27839 foreign key (completed_wizard_id) references osp_completed_wizard;
alter table osp_completed_wiz_category add constraint FK4EC54F7CF992DFC3 foreign key (parent_category_id) references osp_completed_wiz_category;
alter table osp_completed_wizard add constraint FKABC9DEB2D4C797 foreign key (root_category) references osp_completed_wiz_category;
alter table osp_completed_wizard add constraint FKABC9DEB2D62513B2 foreign key (wizard_id) references osp_wizard;
alter table osp_completed_wizard_page add constraint FK52DE9BFCE4E7E6D3 foreign key (wizard_page_id) references osp_wizard_page;
alter table osp_completed_wizard_page add constraint FK52DE9BFC2E24C4 foreign key (wizard_page_def_id) references osp_wizard_page_sequence;
alter table osp_completed_wizard_page add constraint FK52DE9BFC473463E4 foreign key (completed_category_id) references osp_completed_wiz_category;
alter table osp_guidance_item add constraint FK605DDBA737209105 foreign key (guidance_id) references osp_guidance;
alter table osp_guidance_item_file add constraint FK29770314DB93091D foreign key (item_id) references osp_guidance_item;
alter table osp_matrix add constraint FK5A172054A6286438 foreign key (scaffolding_id) references osp_scaffolding;
alter table osp_matrix_cell add constraint FK8C1D366DE4E7E6D3 foreign key (wizard_page_id) references osp_wizard_page;
alter table osp_matrix_cell add constraint FK8C1D366D2D955C foreign key (matrix_id) references osp_matrix;
alter table osp_matrix_cell add constraint FK8C1D366DCD99D2B1 foreign key (scaffolding_cell_id) references osp_scaffolding_cell;
create index IDX_MATRIX_LABEL on osp_matrix_label (type);
alter table osp_pres_itemdef_mimetype add constraint FK9EA59837650346CA foreign key (item_def_id) references osp_presentation_item_def;
alter table osp_presentation add constraint FKA9028D6DFAEA67E8 foreign key (style_id) references osp_style;
alter table osp_presentation add constraint FKA9028D6D6FE1417D foreign key (template_id) references osp_presentation_template;
alter table osp_presentation_comment add constraint FK1E7E658D7658ED43 foreign key (presentation_id) references osp_presentation;
alter table osp_presentation_item add constraint FK2FA02A59165E3E4 foreign key (item_definition_id) references osp_presentation_item_def;
alter table osp_presentation_item add constraint FK2FA02A57658ED43 foreign key (presentation_id) references osp_presentation;
alter table osp_presentation_item_def add constraint FK1B6ADB6B6FE1417D foreign key (template_id) references osp_presentation_template;
alter table osp_presentation_item_property add constraint FK86B1362FA9B15561 foreign key (presentation_page_item_id) references osp_presentation_page_item;
alter table osp_presentation_log add constraint FK2120E1727658ED43 foreign key (presentation_id) references osp_presentation;
alter table osp_presentation_page add constraint FK2FCEA217658ED43 foreign key (presentation_id) references osp_presentation;
alter table osp_presentation_page add constraint FK2FCEA21FAEA67E8 foreign key (style_id) references osp_style;
alter table osp_presentation_page add constraint FK2FCEA21533F283D foreign key (layout_id) references osp_presentation_layout;
alter table osp_presentation_page_item add constraint FK6417671954DB801 foreign key (presentation_page_region_id) references osp_presentation_page_region;
alter table osp_presentation_page_region add constraint FK8A46C2D215C572B8 foreign key (presentation_page_id) references osp_presentation_page;
alter table osp_reports_params add constraint FK231D4599C8A69327 foreign key (reportId) references osp_reports;
alter table osp_reports_results add constraint FKB1427243C8A69327 foreign key (reportId) references osp_reports;
alter table osp_scaffolding add constraint FK65135779FAEA67E8 foreign key (style_id) references osp_style;
alter table osp_scaffolding_cell add constraint FK184EAE68A6286438 foreign key (scaffolding_id) references osp_scaffolding;
alter table osp_scaffolding_cell add constraint FK184EAE689FECDBB8 foreign key (level_id) references osp_matrix_label;
alter table osp_scaffolding_cell add constraint FK184EAE68754F20BD foreign key (wiz_page_def_id) references osp_wizard_page_def;
alter table osp_scaffolding_cell add constraint FK184EAE6870EDF97A foreign key (rootcriterion_id) references osp_matrix_label;
alter table osp_scaffolding_cell_form_defs add constraint FK904DCA92754F20BD foreign key (wiz_page_def_id) references osp_wizard_page_def;
alter table osp_scaffolding_criteria add constraint FK8634116518C870CC foreign key (elt) references osp_matrix_label;
alter table osp_scaffolding_criteria add constraint FK86341165A6286438 foreign key (scaffolding_id) references osp_scaffolding;
alter table osp_scaffolding_levels add constraint FK4EBCD0F51EFC6CAF foreign key (elt) references osp_matrix_label;
alter table osp_scaffolding_levels add constraint FK4EBCD0F5A6286438 foreign key (scaffolding_id) references osp_scaffolding;
alter table osp_template_file_ref add constraint FK4B70FB026FE1417D foreign key (template_id) references osp_presentation_template;
alter table osp_wiz_page_attachment add constraint FK2257FCC9BDC195A7 foreign key (page_id) references osp_wizard_page;
alter table osp_wiz_page_form add constraint FK4725E4EABDC195A7 foreign key (page_id) references osp_wizard_page;
alter table osp_wizard add constraint FK6B9ACDFEE831DD1C foreign key (root_category) references osp_wizard_category;
alter table osp_wizard add constraint FK6B9ACDFEFAEA67E8 foreign key (style_id) references osp_style;
alter table osp_wizard add constraint FK6B9ACDFEC73F84BD foreign key (id) references osp_workflow_parent;
alter table osp_wizard_category add constraint FK3A81FE1FD62513B2 foreign key (wizard_id) references osp_wizard;
alter table osp_wizard_category add constraint FK3A81FE1FE0EFF548 foreign key (parent_category_id) references osp_wizard_category;
alter table osp_wizard_page add constraint FK4CFB5C30754F20BD foreign key (wiz_page_def_id) references osp_wizard_page_def;
alter table osp_wizard_page_def add constraint FK6ABE7776FAEA67E8 foreign key (style_id) references osp_style;
alter table osp_wizard_page_def add constraint FK6ABE7776C73F84BD foreign key (id) references osp_workflow_parent;
alter table osp_wizard_page_sequence add constraint FKA5A702F06EA23D5D foreign key (category_id) references osp_wizard_category;
alter table osp_wizard_page_sequence add constraint FKA5A702F0754F20BD foreign key (wiz_page_def_id) references osp_wizard_page_def;
alter table osp_workflow add constraint FK2065879242A62872 foreign key (parent_id) references osp_workflow_parent;
alter table osp_workflow_item add constraint FKB38697A091A4BC5E foreign key (workflow_id) references osp_workflow;


----------------------------------------------------------------------------------------------------------------------------------------
-- Content / Metaobj
----------------------------------------------------------------------------------------------------------------------------------------

-- These tables have been renamed

RENAME TABLE OSP_REPOSITORY_LOCK TO CONTENT_RESOURCE_LOCK;
RENAME TABLE OSP_STRUCTURED_ARTIFACT_DEF TO METAOBJ_FORM_DEF;
