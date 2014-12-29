-- This is the Oracle Sakai 2.1.2 -> 2.2.0 conversion script
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
       USER_ID             VARCHAR2(99) NOT NULL,
       EID                 VARCHAR2(255) NOT NULL
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
-- from gradebook/app/business/src/sql/oracle/sakai_gradebook_2.1.x_to_2.2.sql
--
-- Gradebook table changes between Sakai 2.1.* and 2.2.
-- --------------------------------------------------------------------------------------------------------------------------------------

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


-- --------------------------------------------------------------------------------------------------------------------------------------
-- make sure admin has the admin site tab
-- --------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO SAKAI_SITE_USER (SITE_ID, USER_ID, PERMISSION) VALUES('!admin', 'admin', -1);


-- --------------------------------------------------------------------------------------------------------------------------------------
-- new default permissions
-- --------------------------------------------------------------------------------------------------------------------------------------

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
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'));


-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new group permissions into existing group realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR2(99), FUNCTION_NAME VARCHAR2(99));

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
    JOIN PERMISSIONS_TEMP TMP ON (SRRF.FUNCTION_KEY = TMP.FUNCTION_KEY)
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

alter table SAM_ITEMGRADING_T add (ATTEMPTSREMAINING integer);
alter table SAM_ITEMGRADING_T add (LASTDURATION varchar(36));

-- new assessment styles

INSERT INTO SAM_TYPE_T (TYPEID ,AUTHORITY ,DOMAIN, KEYWORD,
    DESCRIPTION,
    STATUS, CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (142 , 'stanford.edu' ,'assessment.template.system' ,'System Defined' ,NULL ,1 ,1 ,
    SYSDATE,1 ,SYSDATE);

UPDATE SAM_ASSESSMENTBASE_T SET TYPEID=142 WHERE ID=1;
UPDATE SAM_ASSESSMENTBASE_T SET TITLE='Default Assessment Type' WHERE ID=1;

INSERT INTO SAM_ASSESSMENTBASE_T (ID ,ISTEMPLATE ,
    PARENTID ,TITLE ,DESCRIPTION ,COMMENTS,
    ASSESSMENTTEMPLATEID, TYPEID, INSTRUCTORNOTIFICATION,
    TESTEENOTIFICATION , MULTIPARTALLOWED, STATUS,
    CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (sam_assessmentBase_id_s.nextVal,1 ,0 ,'Formative Assessment' , 'System Defined Assessment Type', '', NULL
    ,'142' ,1 ,1 ,1 ,1 ,'admin' ,SYSDATE ,'admin' ,SYSDATE  )
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
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'finalPageURL_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'anonymousRelease_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'dueDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'description_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataQuestions_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'bgImage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackComponents_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'retractDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessmentAutoSubmit_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'toGradebook_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayChunking_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'recordedScore_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'authenticatedRelease_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayNumbering_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionMessage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'releaseDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'assessmentAuthor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'passwordRequired_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'author', '')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionModel_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'ipAccessType_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessment_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataAssess_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'bgColor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'testeeIdentity_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'templateInfo_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'itemAccessType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lateHandling_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackAuthoring_isInstructorEditable', 'true')
;

INSERT INTO SAM_ASSESSMENTBASE_T (ID ,ISTEMPLATE ,
    PARENTID ,TITLE ,DESCRIPTION ,COMMENTS,
    ASSESSMENTTEMPLATEID, TYPEID, INSTRUCTORNOTIFICATION,
    TESTEENOTIFICATION , MULTIPARTALLOWED, STATUS,
    CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (sam_assessmentBase_id_s.nextVal,1 ,0 ,'Quiz' , 'System Defined Assessment Type', '', NULL
    ,'142' ,1 ,1 ,1 ,1 ,'admin' ,SYSDATE ,'admin' ,SYSDATE  )
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
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'finalPageURL_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'anonymousRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'dueDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'description_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataQuestions_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'bgImage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackComponents_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'retractDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessmentAutoSubmit_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'toGradebook_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayChunking_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'recordedScore_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'authenticatedRelease_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayNumbering_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionMessage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'releaseDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'assessmentAuthor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'passwordRequired_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'author', '')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionModel_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'ipAccessType_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessment_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataAssess_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'bgColor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'testeeIdentity_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'templateInfo_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'itemAccessType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lateHandling_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackAuthoring_isInstructorEditable', 'true')
;


INSERT INTO SAM_ASSESSMENTBASE_T (ID ,ISTEMPLATE ,
    PARENTID ,TITLE ,DESCRIPTION ,COMMENTS,
    ASSESSMENTTEMPLATEID, TYPEID, INSTRUCTORNOTIFICATION,
    TESTEENOTIFICATION , MULTIPARTALLOWED, STATUS,
    CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (sam_assessmentBase_id_s.nextVal,1 ,0 ,'Problem Set' , 'System Defined Assessment Type', '', NULL
    ,'142' ,1 ,1 ,1 ,1 ,'admin' ,SYSDATE ,'admin' ,SYSDATE  )
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
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'finalPageURL_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'anonymousRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'dueDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'description_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataQuestions_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'bgImage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackComponents_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'retractDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessmentAutoSubmit_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'toGradebook_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayChunking_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'recordedScore_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'authenticatedRelease_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayNumbering_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionMessage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'releaseDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'assessmentAuthor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'passwordRequired_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'author', '')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionModel_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'ipAccessType_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessment_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataAssess_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'bgColor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'testeeIdentity_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'templateInfo_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'itemAccessType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lateHandling_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackAuthoring_isInstructorEditable', 'true')
;



INSERT INTO SAM_ASSESSMENTBASE_T (ID ,ISTEMPLATE ,
    PARENTID ,TITLE ,DESCRIPTION ,COMMENTS,
    ASSESSMENTTEMPLATEID, TYPEID, INSTRUCTORNOTIFICATION,
    TESTEENOTIFICATION , MULTIPARTALLOWED, STATUS,
    CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (sam_assessmentBase_id_s.nextVal,1 ,0 ,'Survey' , 'System Defined Assessment Type', '', NULL
    ,'142' ,1 ,1 ,1 ,1 ,'admin' ,SYSDATE ,'admin' ,SYSDATE  )
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
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'finalPageURL_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'anonymousRelease_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'dueDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'description_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataQuestions_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'bgImage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackComponents_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'retractDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessmentAutoSubmit_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'toGradebook_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayChunking_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'recordedScore_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'authenticatedRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayNumbering_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionMessage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'releaseDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'assessmentAuthor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'passwordRequired_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'author', '')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionModel_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'ipAccessType_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessment_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataAssess_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'bgColor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'testeeIdentity_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'templateInfo_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'itemAccessType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lateHandling_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackAuthoring_isInstructorEditable', 'false')
;

INSERT INTO SAM_ASSESSMENTBASE_T (ID ,ISTEMPLATE ,
    PARENTID ,TITLE ,DESCRIPTION ,COMMENTS,
    ASSESSMENTTEMPLATEID, TYPEID, INSTRUCTORNOTIFICATION,
    TESTEENOTIFICATION , MULTIPARTALLOWED, STATUS,
    CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (sam_assessmentBase_id_s.nextVal,1 ,0 ,'Test' , 'System Defined Assessment Type', '', NULL
    ,'142' ,1 ,1 ,1 ,1 ,'admin' ,SYSDATE ,'admin' ,SYSDATE  )
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
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'finalPageURL_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'anonymousRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'dueDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'description_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataQuestions_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'bgImage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackComponents_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'retractDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessmentAutoSubmit_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'toGradebook_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayChunking_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'recordedScore_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'authenticatedRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayNumbering_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionMessage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'releaseDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'assessmentAuthor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'passwordRequired_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'author', '')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionModel_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'ipAccessType_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessment_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataAssess_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'bgColor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'testeeIdentity_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'templateInfo_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'itemAccessType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lateHandling_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackAuthoring_isInstructorEditable', 'true')
;


INSERT INTO SAM_ASSESSMENTBASE_T (ID ,ISTEMPLATE ,
    PARENTID ,TITLE ,DESCRIPTION ,COMMENTS,
    ASSESSMENTTEMPLATEID, TYPEID, INSTRUCTORNOTIFICATION,
    TESTEENOTIFICATION , MULTIPARTALLOWED, STATUS,
    CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (sam_assessmentBase_id_s.nextVal,1 ,0 ,'Timed Test' , 'System Defined Assessment Type', '', NULL
    ,'142' ,1 ,1 ,1 ,1 ,'admin' ,SYSDATE ,'admin' ,SYSDATE  )
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
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'finalPageURL_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'anonymousRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'dueDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'description_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataQuestions_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
     'bgImage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackComponents_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'retractDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessmentAutoSubmit_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'toGradebook_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayChunking_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'recordedScore_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'authenticatedRelease_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayNumbering_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionMessage_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'releaseDate_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'assessmentAuthor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'passwordRequired_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'author', '')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'submissionModel_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'ipAccessType_isInstructorEditable', 'false')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'timedAssessment_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'metadataAssess_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'bgColor_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'testeeIdentity_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'templateInfo_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'itemAccessType_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'lateHandling_isInstructorEditable', 'true')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'feedbackAuthoring_isInstructorEditable', 'true')
;


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Search
-- --------------------------------------------------------------------------------------------------------------------------------------

drop table search_segments cascade constraints;
drop table searchbuilderitem cascade constraints;
drop table searchwriterlock cascade constraints;
create table search_segments (name_ varchar2(254 char) not null, version_ number(19,0) not null, size_ number(19,0) not null, packet_ blob, primary key (name_));
create table searchbuilderitem (id varchar2(64 char) not null, version timestamp not null, name varchar2(255 char) not null unique, context varchar2(255 char) not null, searchaction number(10,0), searchstate number(10,0), primary key (id));
create table searchwriterlock (id varchar2(64 char) not null, lockkey varchar2(64 char) not null unique, nodename varchar2(64 char), expires timestamp not null, primary key (id));
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
create table osp_presentation_template (id varchar2(36 char) not null, name varchar2(255 char), description varchar2(255 char), includeHeaderAndFooter number(1,0), published number(1,0), owner_id varchar2(255 char) not null, renderer varchar2(36 char), markup varchar2(4000 char), propertyPage varchar2(36 char), documentRoot varchar2(255 char), created timestamp not null, modified timestamp not null, site_id varchar2(36 char) not null, primary key (id));
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


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Content / Metaobj
-- --------------------------------------------------------------------------------------------------------------------------------------

-- These tables have been renamed

RENAME osp_repository_lock TO content_resource_lock;
RENAME osp_structured_artifact_def TO metaobj_form_def;


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Message Center
-- --------------------------------------------------------------------------------------------------------------------------------------

-- Note, this is complet DDL, since MessageCenter is new to Sakai for 2.2.  If you have an older version of MessageCenter running, DO NOT RUN THIS!

create table MFR_LABEL_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(36) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(36) not null,
   KEY_C varchar2(255) not null,
   VALUE_C varchar2(255) not null,
   df_surrogateKey number(19,0),
   df_index_col number(10,0),
   dt_surrogateKey number(19,0),
   dt_index_col number(10,0),
   primary key (ID)
);
create table MFR_UNREAD_STATUS_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   TOPIC_C number(19,0) not null,
   MESSAGE_C number(19,0) not null,
   USER_C varchar2(255) not null,
   READ_C number(1,0) not null,
   primary key (ID)
);
create table MFR_AP_ACCESSORS_T (
   apaSurrogateKey number(19,0) not null,
   userSurrogateKey number(19,0) not null,
   accessors_index_col number(10,0) not null,
   primary key (apaSurrogateKey, accessors_index_col)
);
create table MFR_AP_MODERATORS_T (
   apmSurrogateKey number(19,0) not null,
   userSurrogateKey number(19,0) not null,
   moderators_index_col number(10,0) not null,
   primary key (apmSurrogateKey, moderators_index_col)
);
create table MFR_TOPIC_T (
   ID number(19,0) not null,
   TOPIC_DTYPE varchar2(2) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(36) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(36) not null,
   DEFAULTASSIGNNAME varchar2(255),
   TITLE varchar2(255) not null,
   SHORT_DESCRIPTION varchar2(255),
   EXTENDED_DESCRIPTION clob,
   MUTABLE number(1,0) not null,
   SORT_INDEX number(10,0) not null,
   TYPE_UUID varchar2(36) not null,
   of_surrogateKey number(19,0),
   pf_surrogateKey number(19,0),
   USER_ID varchar2(255),
   CONTEXT_ID varchar2(36),
   pt_surrogateKey number(19,0),
   LOCKED number(1,0),
   DRAFT number(1,0),
   CONFIDENTIAL_RESPONSES number(1,0),
   MUST_RESPOND_BEFORE_READING number(1,0),
   HOUR_BEFORE_RESPONSES_VISIBLE number(10,0),
   MODERATED number(1,0),
   GRADEBOOK varchar2(255),
   GRADEBOOK_ASSIGNMENT varchar2(255),
   primary key (ID)
);
create table MFR_MESSAGE_PERMISSIONS_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   ROLE_C varchar2(255) not null,
   READ_C number(1,0) not null,
   REVISE_ANY number(1,0) not null,
   REVISE_OWN number(1,0) not null,
   DELETE_ANY number(1,0) not null,
   DELETE_OWN number(1,0) not null,
   READ_DRAFTS number(1,0) not null,
   MARK_AS_READ number(1,0) not null,
   DEFAULT_VALUE number(1,0) not null,
   areaSurrogateKey number(19,0),
   forumSurrogateKey number(19,0),
   topicSurrogateKey number(19,0),
   primary key (ID)
);
create table MFR_PERMISSION_LEVEL_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(255) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(255) not null,
   NAME varchar2(50) not null,
   TYPE_UUID varchar2(36) not null,
   CHANGE_SETTINGS number(1,0) not null,
   DELETE_ANY number(1,0) not null,
   DELETE_OWN number(1,0) not null,
   MARK_AS_READ number(1,0) not null,
   MOVE_POSTING number(1,0) not null,
   NEW_FORUM number(1,0) not null,
   NEW_RESPONSE number(1,0) not null,
   NEW_RESPONSE_TO_RESPONSE number(1,0) not null,
   NEW_TOPIC number(1,0) not null,
   POST_TO_GRADEBOOK number(1,0) not null,
   X_READ number(1,0) not null,
   REVISE_ANY number(1,0) not null,
   REVISE_OWN number(1,0) not null,
   MODERATE_POSTINGS number(1,0) not null,
   primary key (ID)
);
create table MFR_AP_CONTRIBUTORS_T (
   apcSurrogateKey number(19,0) not null,
   userSurrogateKey number(19,0) not null,
   contributors_index_col number(10,0) not null,
   primary key (apcSurrogateKey, contributors_index_col)
);
create table MFR_MESSAGE_T (
   ID number(19,0) not null,
   MESSAGE_DTYPE varchar2(2) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(36) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(36) not null,
   TITLE varchar2(255) not null,
   BODY clob,
   AUTHOR varchar2(255) not null,
   HAS_ATTACHMENTS number(1,0) not null,
   GRADECOMMENT clob,
   GRADEASSIGNMENTNAME varchar2(255),
   LABEL varchar2(255),
   IN_REPLY_TO number(19,0),
   GRADEBOOK varchar2(255),
   GRADEBOOK_ASSIGNMENT varchar2(255),
   TYPE_UUID varchar2(36) not null,
   APPROVED number(1,0) not null,
   DRAFT number(1,0) not null,
   surrogateKey number(19,0),
   EXTERNAL_EMAIL number(1,0),
   EXTERNAL_EMAIL_ADDRESS varchar2(255),
   RECIPIENTS_AS_TEXT clob,
   primary key (ID)
);
create table MFR_AREA_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(36) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(36) not null,
   CONTEXT_ID varchar2(255) not null,
   NAME varchar2(255) not null,
   HIDDEN number(1,0) not null,
   TYPE_UUID varchar2(36) not null,
   ENABLED number(1,0) not null,
   LOCKED number(1,0) not null,
   primary key (ID)
);
create table MFR_PVT_MSG_USR_T (
   messageSurrogateKey number(19,0) not null,
   USER_ID varchar2(255) not null,
   TYPE_UUID varchar2(255) not null,
   CONTEXT_ID varchar2(255) not null,
   READ_STATUS number(1,0) not null,
   user_index_col number(10,0) not null,
   primary key (messageSurrogateKey, user_index_col)
);
create table MFR_ATTACHMENT_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(255) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(255) not null,
   ATTACHMENT_ID varchar2(255) not null,
   ATTACHMENT_URL varchar2(255) not null,
   ATTACHMENT_NAME varchar2(255) not null,
   ATTACHMENT_SIZE varchar2(255) not null,
   ATTACHMENT_TYPE varchar2(255) not null,
   m_surrogateKey number(19,0),
   of_surrogateKey number(19,0),
   pf_surrogateKey number(19,0),
   t_surrogateKey number(19,0),
   of_urrogateKey number(19,0),
   primary key (ID)
);
create table MFR_ACTOR_PERMISSIONS_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   primary key (ID)
);
create table MFR_MEMBERSHIP_ITEM_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(255) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(255) not null,
   NAME varchar2(255) not null,
   TYPE number(10,0) not null,
   PERMISSION_LEVEL_NAME varchar2(255) not null,
   PERMISSION_LEVEL number(19,0) unique,
   a_surrogateKey number(19,0),
   of_surrogateKey number(19,0),
   t_surrogateKey number(19,0),
   primary key (ID)
);
create table MFR_CONTROL_PERMISSIONS_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   ROLE varchar2(255) not null,
   NEW_FORUM number(1,0) not null,
   POST_TO_GRADEBOOK number(1,0) not null,
   NEW_TOPIC number(1,0) not null,
   NEW_RESPONSE number(1,0) not null,
   RESPONSE_TO_RESPONSE number(1,0) not null,
   MOVE_POSTINGS number(1,0) not null,
   CHANGE_SETTINGS number(1,0) not null,
   DEFAULT_VALUE number(1,0) not null,
   areaSurrogateKey number(19,0),
   forumSurrogateKey number(19,0),
   topicSurrogateKey number(19,0),
   primary key (ID)
);
create table MFR_PRIVATE_FORUM_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(36) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(36) not null,
   TITLE varchar2(255) not null,
   SHORT_DESCRIPTION varchar2(255),
   EXTENDED_DESCRIPTION clob,
   TYPE_UUID varchar2(36) not null,
   SORT_INDEX number(10,0) not null,
   OWNER varchar2(255) not null,
   AUTO_FORWARD number(1,0),
   AUTO_FORWARD_EMAIL varchar2(255),
   PREVIEW_PANE_ENABLED number(1,0),
   surrogateKey number(19,0),
   primary key (ID)
);
create table MFR_OPEN_FORUM_T (
   ID number(19,0) not null,
   FORUM_DTYPE varchar2(2) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(36) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(36) not null,
   DEFAULTASSIGNNAME varchar2(255),
   TITLE varchar2(255) not null,
   SHORT_DESCRIPTION varchar2(255),
   EXTENDED_DESCRIPTION clob,
   TYPE_UUID varchar2(36) not null,
   SORT_INDEX number(10,0) not null,
   LOCKED number(1,0) not null,
   DRAFT number(1,0),
   surrogateKey number(19,0),
   MODERATED number(1,0),
   primary key (ID)
);
create table MFR_MESSAGE_FORUMS_USER_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   USER_ID varchar2(255) not null,
   TYPE_UUID varchar2(36) not null,
   primary key (ID)
);
create table MFR_DATE_RESTRICTIONS_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   VISIBLE date not null,
   VISIBLE_POST_ON_SCHEDULE number(1,0) not null,
   POSTING_ALLOWED date not null,
   PSTNG_ALLWD_PST_ON_SCHD number(1,0) not null,
   READ_ONLY date not null,
   READ_ONLY_POST_ON_SCHEDULE number(1,0) not null,
   HIDDEN date not null,
   HIDDEN_POST_ON_SCHEDULE number(1,0) not null,
   primary key (ID)
);
create index MFR_LABEL_PARENT_I2 on MFR_LABEL_T (dt_surrogateKey);
create index MFR_LABEL_PARENT_I1 on MFR_LABEL_T (df_surrogateKey);
alter table MFR_LABEL_T add constraint FKC6611543EA902104 foreign key (df_surrogateKey) references MFR_OPEN_FORUM_T;
alter table MFR_LABEL_T add constraint FKC661154344B127B6 foreign key (dt_surrogateKey) references MFR_TOPIC_T;
create index MFR_UNREAD_STATUS_I1 on MFR_UNREAD_STATUS_T (TOPIC_C, MESSAGE_C, USER_C, READ_C);
alter table MFR_AP_ACCESSORS_T add constraint FKC8532ED796792399 foreign key (apaSurrogateKey) references MFR_ACTOR_PERMISSIONS_T;
alter table MFR_AP_ACCESSORS_T add constraint FKC8532ED721BCC7D2 foreign key (userSurrogateKey) references MFR_MESSAGE_FORUMS_USER_T;
alter table MFR_AP_MODERATORS_T add constraint FK75B43C0D21BCC7D2 foreign key (userSurrogateKey) references MFR_MESSAGE_FORUMS_USER_T;
alter table MFR_AP_MODERATORS_T add constraint FK75B43C0DC49D71A5 foreign key (apmSurrogateKey) references MFR_ACTOR_PERMISSIONS_T;
create index MFR_TOPIC_PARENT_I1 on MFR_TOPIC_T (of_surrogateKey);
create index MRF_TOPIC_DTYPE_I on MFR_TOPIC_T (TOPIC_DTYPE);
create index MFR_TOPIC_PRI_PARENT_I on MFR_TOPIC_T (pt_surrogateKey);
create index MFR_PT_CONTEXT_I on MFR_TOPIC_T (CONTEXT_ID);
create index MFR_TOPIC_PARENT_I2 on MFR_TOPIC_T (pf_surrogateKey);
create index MFR_TOPIC_CREATED_I on MFR_TOPIC_T (CREATED);
alter table MFR_TOPIC_T add constraint FK863DC0BEC6FDB1CF foreign key (of_surrogateKey) references MFR_OPEN_FORUM_T;
alter table MFR_TOPIC_T add constraint FK863DC0BE7AFA22C2 foreign key (pt_surrogateKey) references MFR_TOPIC_T;
alter table MFR_TOPIC_T add constraint FK863DC0BE20D91C10 foreign key (pf_surrogateKey) references MFR_PRIVATE_FORUM_T;
create index MFR_MP_PARENT_FORUM_I on MFR_MESSAGE_PERMISSIONS_T (forumSurrogateKey);
create index MFR_MP_PARENT_AREA_I on MFR_MESSAGE_PERMISSIONS_T (areaSurrogateKey);
create index MFR_MP_PARENT_TOPIC_I on MFR_MESSAGE_PERMISSIONS_T (topicSurrogateKey);
alter table MFR_MESSAGE_PERMISSIONS_T add constraint FK750F9AFB17721828 foreign key (forumSurrogateKey) references MFR_OPEN_FORUM_T;
alter table MFR_MESSAGE_PERMISSIONS_T add constraint FK750F9AFB51C89994 foreign key (areaSurrogateKey) references MFR_AREA_T;
alter table MFR_MESSAGE_PERMISSIONS_T add constraint FK750F9AFBE581B336 foreign key (topicSurrogateKey) references MFR_TOPIC_T;
alter table MFR_AP_CONTRIBUTORS_T add constraint FKA221A1F7737F309B foreign key (apcSurrogateKey) references MFR_ACTOR_PERMISSIONS_T;
alter table MFR_AP_CONTRIBUTORS_T add constraint FKA221A1F721BCC7D2 foreign key (userSurrogateKey) references MFR_MESSAGE_FORUMS_USER_T;
create index MFR_MESSAGE_LABEL_I on MFR_MESSAGE_T (LABEL);
create index MFR_MESSAGE_HAS_ATTACHMENTS_I on MFR_MESSAGE_T (HAS_ATTACHMENTS);
create index MFR_MESSAGE_CREATED_I on MFR_MESSAGE_T (CREATED);
create index MFR_MESSAGE_AUTHOR_I on MFR_MESSAGE_T (AUTHOR);
create index MFR_MESSAGE_DTYPE_I on MFR_MESSAGE_T (MESSAGE_DTYPE);
create index MFR_MESSAGE_TITLE_I on MFR_MESSAGE_T (TITLE);
create index MFR_MESSAGE_PARENT_TOPIC_I on MFR_MESSAGE_T (surrogateKey);
alter table MFR_MESSAGE_T add constraint FK80C1A316FE0789EA foreign key (IN_REPLY_TO) references MFR_MESSAGE_T;
alter table MFR_MESSAGE_T add constraint FK80C1A3164FDCE067 foreign key (surrogateKey) references MFR_TOPIC_T;
create index MFR_AREA_CONTEXT_I on MFR_AREA_T (CONTEXT_ID);
create index MFR_AREA_TYPE_I on MFR_AREA_T (TYPE_UUID);
create index MFR_PVT_MSG_USR_I1 on MFR_PVT_MSG_USR_T (USER_ID, TYPE_UUID, CONTEXT_ID, READ_STATUS);
alter table MFR_PVT_MSG_USR_T add constraint FKC4DE0E14FA8620E foreign key (messageSurrogateKey) references MFR_MESSAGE_T;
create index MFR_ATTACHMENT_PARENT_I4 on MFR_ATTACHMENT_T (t_surrogateKey);
create index MFR_ATTACHMENT_PARENT_I on MFR_ATTACHMENT_T (m_surrogateKey);
create index MFR_ATTACHMENT_PARENT_I3 on MFR_ATTACHMENT_T (pf_surrogateKey);
create index MFR_ATTACHMENT_PARENT_I2 on MFR_ATTACHMENT_T (of_surrogateKey);
alter table MFR_ATTACHMENT_T add constraint FK7B2D5CDE2AFBA652 foreign key (t_surrogateKey) references MFR_TOPIC_T;
alter table MFR_ATTACHMENT_T add constraint FK7B2D5CDEC6FDB1CF foreign key (of_surrogateKey) references MFR_OPEN_FORUM_T;
alter table MFR_ATTACHMENT_T add constraint FK7B2D5CDE20D91C10 foreign key (pf_surrogateKey) references MFR_PRIVATE_FORUM_T;
alter table MFR_ATTACHMENT_T add constraint FK7B2D5CDEFDEB22F9 foreign key (m_surrogateKey) references MFR_MESSAGE_T;
alter table MFR_ATTACHMENT_T add constraint FK7B2D5CDEAD5AF852 foreign key (of_urrogateKey) references MFR_OPEN_FORUM_T;
alter table MFR_MEMBERSHIP_ITEM_T add constraint FKE03761CB6785AF85 foreign key (a_surrogateKey) references MFR_AREA_T;
alter table MFR_MEMBERSHIP_ITEM_T add constraint FKE03761CBC6FDB1CF foreign key (of_surrogateKey) references MFR_OPEN_FORUM_T;
alter table MFR_MEMBERSHIP_ITEM_T add constraint FKE03761CB2AFBA652 foreign key (t_surrogateKey) references MFR_TOPIC_T;
alter table MFR_MEMBERSHIP_ITEM_T add constraint FKE03761CB925CE0F4 foreign key (PERMISSION_LEVEL) references MFR_PERMISSION_LEVEL_T;
create index MFR_CP_PARENT_FORUM_I on MFR_CONTROL_PERMISSIONS_T (forumSurrogateKey);
create index MFR_CP_PARENT_TOPIC_I on MFR_CONTROL_PERMISSIONS_T (topicSurrogateKey);
create index MFR_CP_PARENT_AREA_I on MFR_CONTROL_PERMISSIONS_T (areaSurrogateKey);
alter table MFR_CONTROL_PERMISSIONS_T add constraint FKA07CF1D1E581B336 foreign key (topicSurrogateKey) references MFR_TOPIC_T;
alter table MFR_CONTROL_PERMISSIONS_T add constraint FKA07CF1D151C89994 foreign key (areaSurrogateKey) references MFR_AREA_T;
alter table MFR_CONTROL_PERMISSIONS_T add constraint FKA07CF1D117721828 foreign key (forumSurrogateKey) references MFR_OPEN_FORUM_T;
create index MFR_PRIVATE_FORUM_CREATED_I on MFR_PRIVATE_FORUM_T (CREATED);
create index MFR_PRIVATE_FORUM_OWNER_I on MFR_PRIVATE_FORUM_T (OWNER);
create index MFR_PF_PARENT_BASEFORUM_I on MFR_PRIVATE_FORUM_T (surrogateKey);
alter table MFR_PRIVATE_FORUM_T add constraint FKA9EE57544FDCE067 foreign key (surrogateKey) references MFR_AREA_T;
create index MFR_OPEN_FORUM_DTYPE_I on MFR_OPEN_FORUM_T (FORUM_DTYPE);
create index MFR_OPEN_FORUM_TYPE_I on MFR_OPEN_FORUM_T (TYPE_UUID);
create index MFR_OF_PARENT_BASEFORUM_I on MFR_OPEN_FORUM_T (surrogateKey);
alter table MFR_OPEN_FORUM_T add constraint FKC17608474FDCE067 foreign key (surrogateKey) references MFR_AREA_T;
create sequence MFR_PERMISSION_LEVEL_S;
create sequence MFR_MESSAGE_S;
create sequence MFR_AREA_S;
create sequence MFR_PRIVATE_FORUM_S;
create sequence MFR_TOPIC_S;
create sequence MFR_MESSAGE_PERMISSIONS_S;
create sequence MFR_LABEL_S;
create sequence MFR_DATE_RESTRICTIONS_S;
create sequence MFR_CONTROL_PERMISSIONS_S;
create sequence MFR_UNREAD_STATUS_S;
create sequence MFR_ACTOR_PERMISSIONS_S;
create sequence MFR_MESSAGE_FORUMS_USER_S;
create sequence MFR_ATTACHMENT_S;
create sequence MFR_MEMBERSHIP_ITEM_S;
create sequence MFR_OPEN_FORUM_S;


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Postem
-- --------------------------------------------------------------------------------------------------------------------------------------

-- Note, this is complete DDL for Postem, since Postem is new to Sakai for 2.2.  If you are running an older version of Postem, DO NOT RUN THIS!

create table SAKAI_POSTEM_HEADINGS (
   gradebook_id number(19,0) not null,
   heading varchar2(255) not null,
   location number(10,0) not null,
   primary key (gradebook_id, location)
);
create table SAKAI_POSTEM_STUDENT (
   id number(19,0) not null,
   lockId number(10,0) not null,
   username varchar2(36) not null,
   last_checked date,
   surrogate_key number(19,0),
   primary key (id)
);
create table SAKAI_POSTEM_STUDENT_GRADES (
   student_id number(19,0) not null,
   grade varchar2(255),
   location number(10,0) not null,
   primary key (student_id, location)
);
create table SAKAI_POSTEM_GRADEBOOK (
   id number(19,0) not null,
   lockId number(10,0) not null,
   title varchar2(36) not null,
   context varchar2(36) not null,
   creator varchar2(36) not null,
   created date not null,
   last_updater varchar2(36) not null,
   last_updated date not null,
   released number(1,0) not null,
   stats number(1,0) not null,
   template varchar2(255),
   primary key (id),
   unique (title, context)
);
alter table SAKAI_POSTEM_HEADINGS add constraint FKF54C1C2EE091F27A foreign key (gradebook_id) references SAKAI_POSTEM_GRADEBOOK;
create index POSTEM_STUDENT_USERNAME_I on SAKAI_POSTEM_STUDENT (username);
alter table SAKAI_POSTEM_STUDENT add constraint FK4FBA80FEABC85878 foreign key (surrogate_key) references SAKAI_POSTEM_GRADEBOOK;
alter table SAKAI_POSTEM_STUDENT_GRADES add constraint FK321A31DDC276819F foreign key (student_id) references SAKAI_POSTEM_STUDENT;
create index POSTEM_GB_CONTEXT_I on SAKAI_POSTEM_GRADEBOOK (context);
create index POSTEM_GB_TITLE_I on SAKAI_POSTEM_GRADEBOOK (title);
create sequence POSTEM_GRADEBOOK_S;
create sequence POSTEM_STUDGRADES_S;


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Increase the field size for the SESSION_IP field in the SAKAI_SESSION table
-- --------------------------------------------------------------------------------------------------------------------------------------

ALTER TABLE SAKAI_SESSION MODIFY SESSION_IP VARCHAR2 (128);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- Increase the field size for the NOTES field in the SAKAI_PERSON_T table
-- --------------------------------------------------------------------------------------------------------------------------------------

ALTER TABLE SAKAI_PERSON_T MODIFY (NOTES varchar2(4000));
