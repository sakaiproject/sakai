-- This is the MySQL Sakai 2.2.1 (or later) -> 2.3.1 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.2.1 (or later) to 2.3.1.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------
-- Add new calendar & content permission function names

INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('calendar.revise.any');
INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('calendar.revise.own');
INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('calendar.delete.any');
INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('calendar.delete.own');
INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('content.revise.any');
INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('content.revise.own');
INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('content.delete.any');
INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('content.delete.own');

--
-- Convert and expand calendar permissions: 
--    calendar.revise becomes calendar.revise.own | calendar.revise.any | calendar.delete.own | calendar.delete.any
--
-- Note: mapping revise permission to delete is based on the original (misguided) permissions
--

INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) 
   SELECT SAKAI_REALM_RL_FN.REALM_KEY, SAKAI_REALM_RL_FN.ROLE_KEY, F.FUNCTION_KEY 
   FROM SAKAI_REALM_ROLE, SAKAI_REALM_FUNCTION,SAKAI_REALM_RL_FN,SAKAI_REALM, SAKAI_REALM_FUNCTION F
   WHERE SAKAI_REALM_FUNCTION.FUNCTION_NAME = 'calendar.revise' 
     AND SAKAI_REALM.REALM_KEY = SAKAI_REALM_RL_FN.REALM_KEY 
     AND SAKAI_REALM_ROLE.ROLE_KEY = SAKAI_REALM_RL_FN.ROLE_KEY 
     AND SAKAI_REALM_FUNCTION.FUNCTION_KEY = SAKAI_REALM_RL_FN.FUNCTION_KEY
     AND F.FUNCTION_NAME='calendar.revise.any';

INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) 
   SELECT SAKAI_REALM_RL_FN.REALM_KEY, SAKAI_REALM_RL_FN.ROLE_KEY, F.FUNCTION_KEY 
   FROM SAKAI_REALM_ROLE, SAKAI_REALM_FUNCTION,SAKAI_REALM_RL_FN,SAKAI_REALM, SAKAI_REALM_FUNCTION F
   WHERE SAKAI_REALM_FUNCTION.FUNCTION_NAME = 'calendar.revise' 
     AND SAKAI_REALM.REALM_KEY = SAKAI_REALM_RL_FN.REALM_KEY 
     AND SAKAI_REALM_ROLE.ROLE_KEY = SAKAI_REALM_RL_FN.ROLE_KEY 
     AND SAKAI_REALM_FUNCTION.FUNCTION_KEY = SAKAI_REALM_RL_FN.FUNCTION_KEY
     AND F.FUNCTION_NAME='calendar.revise.own';

INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) 
   SELECT SAKAI_REALM_RL_FN.REALM_KEY, SAKAI_REALM_RL_FN.ROLE_KEY, F.FUNCTION_KEY 
   FROM SAKAI_REALM_ROLE, SAKAI_REALM_FUNCTION,SAKAI_REALM_RL_FN,SAKAI_REALM, SAKAI_REALM_FUNCTION F
   WHERE SAKAI_REALM_FUNCTION.FUNCTION_NAME = 'calendar.revise' 
     AND SAKAI_REALM.REALM_KEY = SAKAI_REALM_RL_FN.REALM_KEY 
     AND SAKAI_REALM_ROLE.ROLE_KEY = SAKAI_REALM_RL_FN.ROLE_KEY 
     AND SAKAI_REALM_FUNCTION.FUNCTION_KEY = SAKAI_REALM_RL_FN.FUNCTION_KEY
     AND F.FUNCTION_NAME='calendar.delete.any';

INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) 
   SELECT SAKAI_REALM_RL_FN.REALM_KEY, SAKAI_REALM_RL_FN.ROLE_KEY, F.FUNCTION_KEY 
   FROM SAKAI_REALM_ROLE, SAKAI_REALM_FUNCTION,SAKAI_REALM_RL_FN,SAKAI_REALM, SAKAI_REALM_FUNCTION F
   WHERE SAKAI_REALM_FUNCTION.FUNCTION_NAME = 'calendar.revise' 
     AND SAKAI_REALM.REALM_KEY = SAKAI_REALM_RL_FN.REALM_KEY 
     AND SAKAI_REALM_ROLE.ROLE_KEY = SAKAI_REALM_RL_FN.ROLE_KEY 
     AND SAKAI_REALM_FUNCTION.FUNCTION_KEY = SAKAI_REALM_RL_FN.FUNCTION_KEY
     AND F.FUNCTION_NAME='calendar.delete.own';

--
-- Convert and expand content permissions: 
--    content.revise becomes content.revise.own | content.revise.any
--    content.delete becomes content.delete.own | content.delete.any
--
  
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) 
   SELECT SAKAI_REALM_RL_FN.REALM_KEY, SAKAI_REALM_RL_FN.ROLE_KEY, F.FUNCTION_KEY 
   FROM SAKAI_REALM_ROLE, SAKAI_REALM_FUNCTION,SAKAI_REALM_RL_FN,SAKAI_REALM, SAKAI_REALM_FUNCTION F
   WHERE SAKAI_REALM_FUNCTION.FUNCTION_NAME = 'content.revise' 
     AND SAKAI_REALM.REALM_KEY = SAKAI_REALM_RL_FN.REALM_KEY 
     AND SAKAI_REALM_ROLE.ROLE_KEY = SAKAI_REALM_RL_FN.ROLE_KEY 
     AND SAKAI_REALM_FUNCTION.FUNCTION_KEY = SAKAI_REALM_RL_FN.FUNCTION_KEY
     AND F.FUNCTION_NAME='content.revise.any';
  
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) 
   SELECT SAKAI_REALM_RL_FN.REALM_KEY, SAKAI_REALM_RL_FN.ROLE_KEY, F.FUNCTION_KEY 
   FROM SAKAI_REALM_ROLE, SAKAI_REALM_FUNCTION,SAKAI_REALM_RL_FN,SAKAI_REALM, SAKAI_REALM_FUNCTION F
   WHERE SAKAI_REALM_FUNCTION.FUNCTION_NAME = 'content.revise' 
     AND SAKAI_REALM.REALM_KEY = SAKAI_REALM_RL_FN.REALM_KEY 
     AND SAKAI_REALM_ROLE.ROLE_KEY = SAKAI_REALM_RL_FN.ROLE_KEY 
     AND SAKAI_REALM_FUNCTION.FUNCTION_KEY = SAKAI_REALM_RL_FN.FUNCTION_KEY
     AND F.FUNCTION_NAME='content.revise.own';
  
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) 
   SELECT SAKAI_REALM_RL_FN.REALM_KEY, SAKAI_REALM_RL_FN.ROLE_KEY, F.FUNCTION_KEY 
   FROM SAKAI_REALM_ROLE, SAKAI_REALM_FUNCTION,SAKAI_REALM_RL_FN,SAKAI_REALM, SAKAI_REALM_FUNCTION F
   WHERE SAKAI_REALM_FUNCTION.FUNCTION_NAME = 'content.delete' 
     AND SAKAI_REALM.REALM_KEY = SAKAI_REALM_RL_FN.REALM_KEY 
     AND SAKAI_REALM_ROLE.ROLE_KEY = SAKAI_REALM_RL_FN.ROLE_KEY 
     AND SAKAI_REALM_FUNCTION.FUNCTION_KEY = SAKAI_REALM_RL_FN.FUNCTION_KEY
     AND F.FUNCTION_NAME='content.delete.any';
  
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) 
   SELECT SAKAI_REALM_RL_FN.REALM_KEY, SAKAI_REALM_RL_FN.ROLE_KEY, F.FUNCTION_KEY 
   FROM SAKAI_REALM_ROLE, SAKAI_REALM_FUNCTION,SAKAI_REALM_RL_FN,SAKAI_REALM, SAKAI_REALM_FUNCTION F
   WHERE SAKAI_REALM_FUNCTION.FUNCTION_NAME = 'content.delete' 
     AND SAKAI_REALM.REALM_KEY = SAKAI_REALM_RL_FN.REALM_KEY 
     AND SAKAI_REALM_ROLE.ROLE_KEY = SAKAI_REALM_RL_FN.ROLE_KEY 
     AND SAKAI_REALM_FUNCTION.FUNCTION_KEY = SAKAI_REALM_RL_FN.FUNCTION_KEY
     AND F.FUNCTION_NAME='content.delete.own';
  
--
-- Delete old functions
--

DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY IN (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME IN ('calendar.revise','calendar.delete','content.revise','content.delete'));

set foreign_key_checks=0;
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME IN ('calendar.revise','calendar.delete','content.revise','content.delete');
set foreign_key_checks=1;

-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------
-- OSP

UPDATE osp_guidance SET securityViewFunction='osp.wizard.operate' WHERE
securityViewFunction='osp.wizard.view';

alter table osp_style add column style_hash varchar(255);

-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-6468 - CM

-- Drop the previously generated tables (these were not used in previous versions of Sakai)

alter table CM_COURSE_SET_CANON_ASSOC_T drop foreign key FKBFCBD9AE47C5DD8C;
alter table CM_COURSE_SET_CANON_ASSOC_T drop foreign key FKBFCBD9AEA12CE4B7;
alter table CM_COURSE_SET_OFFERING_ASSOC_T drop foreign key FK5B9A5CFDF6B32479;
alter table CM_COURSE_SET_OFFERING_ASSOC_T drop foreign key FK5B9A5CFDA12CE4B7;
alter table CM_ENROLLMENT_SET_T drop foreign key FK99479DD1F6B32479;
alter table CM_ENROLLMENT_T drop foreign key FK7A7F878E5E7CD717;
alter table CM_MEMBERSHIP_T drop foreign key FK9FBBBFE0C04582D9;
alter table CM_MEMBER_CONTAINER_T drop foreign key FKD96A9BC6506E999F;
alter table CM_MEMBER_CONTAINER_T drop foreign key FKD96A9BC65E7CD717;
alter table CM_MEMBER_CONTAINER_T drop foreign key FKD96A9BC6F6B32479;
alter table CM_MEMBER_CONTAINER_T drop foreign key FKD96A9BC610198CE7;
alter table CM_MEMBER_CONTAINER_T drop foreign key FKD96A9BC6CE101BF7;
alter table CM_MEMBER_CONTAINER_T drop foreign key FKD96A9BC6988DCAA7;
alter table CM_MEMBER_CONTAINER_T drop foreign key FKD96A9BC6A5FB6F8D;
alter table CM_MEMBER_CONTAINER_T drop foreign key FKD96A9BC6BDA3036C;
alter table CM_OFFICIAL_INSTRUCTORS_T drop foreign key FK470F8ACCDB9C5A23;


drop table if exists CM_ACADEMIC_SESSION_T;
drop table if exists CM_COURSE_SET_CANON_ASSOC_T;
drop table if exists CM_COURSE_SET_OFFERING_ASSOC_T;
drop table if exists CM_CROSS_LISTING_T;
drop table if exists CM_ENROLLMENT_SET_T;
drop table if exists CM_ENROLLMENT_T;

drop table if exists CM_MEMBERSHIP_T;
drop table if exists CM_MEMBER_CONTAINER_T;
drop table if exists CM_OFFICIAL_INSTRUCTORS_T;


-- Create the new CM tables

create table CM_ACADEMIC_SESSION_T (ACADEMIC_SESSION_ID bigint not null auto_increment, VERSION integer not null, LAST_MODIFIED_BY varchar(255), LAST_MODIFIED_DATE datetime, CREATED_BY varchar(255), CREATED_DATE datetime, ENTERPRISE_ID varchar(255) not null unique, TITLE varchar(255) not null, DESCRIPTION varchar(255) not null, START_DATE datetime, END_DATE datetime, primary key (ACADEMIC_SESSION_ID));
create table CM_COURSE_SET_CANON_ASSOC_T (CANON_COURSE bigint not null, COURSE_SET bigint not null, primary key (COURSE_SET, CANON_COURSE));
create table CM_COURSE_SET_OFFERING_ASSOC_T (COURSE_SET bigint not null, COURSE_OFFERING bigint not null, primary key (COURSE_SET, COURSE_OFFERING));
create table CM_CROSS_LISTING_T (CROSS_LISTING_ID bigint not null auto_increment, VERSION integer not null, LAST_MODIFIED_BY varchar(255), LAST_MODIFIED_DATE datetime, CREATED_BY varchar(255), CREATED_DATE datetime, primary key (CROSS_LISTING_ID));
create table CM_ENROLLMENT_SET_T (ENROLLMENT_SET_ID bigint not null auto_increment, VERSION integer not null, LAST_MODIFIED_BY varchar(255), LAST_MODIFIED_DATE datetime, CREATED_BY varchar(255), CREATED_DATE datetime, ENTERPRISE_ID varchar(255) not null unique, TITLE varchar(255) not null, DESCRIPTION varchar(255) not null, CATEGORY varchar(255) not null, DEFAULT_CREDITS varchar(255) not null, COURSE_OFFERING bigint, primary key (ENROLLMENT_SET_ID));
create table CM_ENROLLMENT_T (ENROLLMENT_ID bigint not null auto_increment, VERSION integer not null, LAST_MODIFIED_BY varchar(255), LAST_MODIFIED_DATE datetime, CREATED_BY varchar(255), CREATED_DATE datetime, USER_ID varchar(255) not null, STATUS varchar(255) not null, CREDITS varchar(255) not null, GRADING_SCHEME varchar(255) not null, DROPPED bit, ENROLLMENT_SET bigint, primary key (ENROLLMENT_ID));
create table CM_MEETING_T (MEETING_ID bigint not null auto_increment, LOCATION varchar(255), TIME_OF_DAY varchar(255), NOTES varchar(255), SECTION_ID bigint not null, primary key (MEETING_ID));
create table CM_MEMBERSHIP_T (MEMBER_ID bigint not null auto_increment, VERSION integer not null, USER_ID varchar(255) not null, ROLE varchar(255) not null, MEMBER_CONTAINER_ID bigint, primary key (MEMBER_ID));
create table CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID bigint not null auto_increment, CLASS_DISCR varchar(100) not null, VERSION integer not null, LAST_MODIFIED_BY varchar(255), LAST_MODIFIED_DATE datetime, CREATED_BY varchar(255), CREATED_DATE datetime, ENTERPRISE_ID varchar(100) not null, TITLE varchar(255) not null, DESCRIPTION varchar(255) not null, CATEGORY varchar(255), COURSE_OFFERING bigint, ENROLLMENT_SET bigint, PARENT_SECTION bigint, CROSS_LISTING bigint, PARENT_COURSE_SET bigint, STATUS varchar(255), START_DATE datetime, END_DATE datetime, CANONICAL_COURSE bigint, ACADEMIC_SESSION bigint, EQUIV_CANON_COURSE_ID bigint, EQUIV_COURSE_OFFERING_ID bigint, primary key (MEMBER_CONTAINER_ID), unique (CLASS_DISCR, ENTERPRISE_ID));
create table CM_OFFICIAL_INSTRUCTORS_T (ENROLLMENT_SET_ID bigint not null, INSTRUCTOR_ID varchar(255));
alter table CM_COURSE_SET_CANON_ASSOC_T add index FKBFCBD9AE7F976CD6 (CANON_COURSE), add constraint FKBFCBD9AE7F976CD6 foreign key (CANON_COURSE) references CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID);
alter table CM_COURSE_SET_CANON_ASSOC_T add index FKBFCBD9AE2D306E01 (COURSE_SET), add constraint FKBFCBD9AE2D306E01 foreign key (COURSE_SET) references CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID);
alter table CM_COURSE_SET_OFFERING_ASSOC_T add index FK5B9A5CFD26827043 (COURSE_OFFERING), add constraint FK5B9A5CFD26827043 foreign key (COURSE_OFFERING) references CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID);
alter table CM_COURSE_SET_OFFERING_ASSOC_T add index FK5B9A5CFD2D306E01 (COURSE_SET), add constraint FK5B9A5CFD2D306E01 foreign key (COURSE_SET) references CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID);
create index CM_ENR_SET_CO_IDX on CM_ENROLLMENT_SET_T (COURSE_OFFERING);
alter table CM_ENROLLMENT_SET_T add index FK99479DD126827043 (COURSE_OFFERING), add constraint FK99479DD126827043 foreign key (COURSE_OFFERING) references CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID);
create index CM_ENR_ENR_SET_IDX on CM_ENROLLMENT_T (ENROLLMENT_SET);
alter table CM_ENROLLMENT_T add index FK7A7F878E456D3EA1 (ENROLLMENT_SET), add constraint FK7A7F878E456D3EA1 foreign key (ENROLLMENT_SET) references CM_ENROLLMENT_SET_T (ENROLLMENT_SET_ID);
alter table CM_MEETING_T add index FKE15DCD9BD0506F16 (SECTION_ID), add constraint FKE15DCD9BD0506F16 foreign key (SECTION_ID) references CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID);
alter table CM_MEMBERSHIP_T add index FK9FBBBFE067131463 (MEMBER_CONTAINER_ID), add constraint FK9FBBBFE067131463 foreign key (MEMBER_CONTAINER_ID) references CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID);
create index CM_SECTION_PARENT_IDX on CM_MEMBER_CONTAINER_T (PARENT_SECTION);
create index CM_SECTION_ENR_SET_IDX on CM_MEMBER_CONTAINER_T (ENROLLMENT_SET);
create index CM_COURSE_SET_PARENT_IDX on CM_MEMBER_CONTAINER_T (PARENT_COURSE_SET);
create index CM_CO_ACADEMIC_SESS_IDX on CM_MEMBER_CONTAINER_T (ACADEMIC_SESSION);
create index CM_CO_CANON_COURSE_IDX on CM_MEMBER_CONTAINER_T (CANONICAL_COURSE);
create index CM_SECTION_COURSE_IDX on CM_MEMBER_CONTAINER_T (COURSE_OFFERING);
alter table CM_MEMBER_CONTAINER_T add index FKD96A9BC6661E50E9 (ACADEMIC_SESSION), add constraint FKD96A9BC6661E50E9 foreign key (ACADEMIC_SESSION) references CM_ACADEMIC_SESSION_T (ACADEMIC_SESSION_ID);
alter table CM_MEMBER_CONTAINER_T add index FKD96A9BC6456D3EA1 (ENROLLMENT_SET), add constraint FKD96A9BC6456D3EA1 foreign key (ENROLLMENT_SET) references CM_ENROLLMENT_SET_T (ENROLLMENT_SET_ID);
alter table CM_MEMBER_CONTAINER_T add index FKD96A9BC626827043 (COURSE_OFFERING), add constraint FKD96A9BC626827043 foreign key (COURSE_OFFERING) references CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID);
alter table CM_MEMBER_CONTAINER_T add index FKD96A9BC63B0306B1 (PARENT_SECTION), add constraint FKD96A9BC63B0306B1 foreign key (PARENT_SECTION) references CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID);
alter table CM_MEMBER_CONTAINER_T add index FKD96A9BC64F7C8841 (CROSS_LISTING), add constraint FKD96A9BC64F7C8841 foreign key (CROSS_LISTING) references CM_CROSS_LISTING_T (CROSS_LISTING_ID);
alter table CM_MEMBER_CONTAINER_T add index FKD96A9BC6D05F59F1 (CANONICAL_COURSE), add constraint FKD96A9BC6D05F59F1 foreign key (CANONICAL_COURSE) references CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID);
alter table CM_MEMBER_CONTAINER_T add index FKD96A9BC6D0C1EF35 (EQUIV_COURSE_OFFERING_ID), add constraint FKD96A9BC6D0C1EF35 foreign key (EQUIV_COURSE_OFFERING_ID) references CM_CROSS_LISTING_T (CROSS_LISTING_ID);
alter table CM_MEMBER_CONTAINER_T add index FKD96A9BC66DFDE2 (EQUIV_CANON_COURSE_ID), add constraint FKD96A9BC66DFDE2 foreign key (EQUIV_CANON_COURSE_ID) references CM_CROSS_LISTING_T (CROSS_LISTING_ID);
alter table CM_MEMBER_CONTAINER_T add index FKD96A9BC649A68CB6 (PARENT_COURSE_SET), add constraint FKD96A9BC649A68CB6 foreign key (PARENT_COURSE_SET) references CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID);
alter table CM_OFFICIAL_INSTRUCTORS_T add index FK470F8ACCC28CC1AD (ENROLLMENT_SET_ID), add constraint FK470F8ACCC28CC1AD foreign key (ENROLLMENT_SET_ID) references CM_ENROLLMENT_SET_T (ENROLLMENT_SET_ID);

-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAM (SAK-4396)

SET FOREIGN_KEY_CHECKS = 0;
drop table if exists SAM_ATTACHMENT_T;
drop table if exists SAM_PUBLISHEDATTACHMENT_T;
SET FOREIGN_KEY_CHECKS = 1;
create table SAM_ATTACHMENT_T (ATTACHMENTID bigint not null auto_increment, ATTACHMENTTYPE varchar(255) not null, RESOURCEID varchar(255), FILENAME varchar(255), MIMETYPE varchar(80), FILESIZE integer, DESCRIPTION varchar(4000), LOCATION varchar(4000), ISLINK integer, STATUS integer not null, CREATEDBY varchar(36) not null, CREATEDDATE datetime not null, LASTMODIFIEDBY varchar(36) not null, LASTMODIFIEDDATE datetime not null, ASSESSMENTID bigint, SECTIONID bigint, ITEMID bigint, primary key (ATTACHMENTID));
create table SAM_PUBLISHEDATTACHMENT_T (ATTACHMENTID bigint not null auto_increment, ATTACHMENTTYPE varchar(255) not null, RESOURCEID varchar(255), FILENAME varchar(255), MIMETYPE varchar(80), FILESIZE integer, DESCRIPTION varchar(4000), LOCATION varchar(4000), ISLINK integer, STATUS integer not null, CREATEDBY varchar(36) not null, CREATEDDATE datetime not null, LASTMODIFIEDBY varchar(36) not null, LASTMODIFIEDDATE datetime not null, ASSESSMENTID bigint, SECTIONID bigint, ITEMID bigint, primary key (ATTACHMENTID));
alter table SAM_ATTACHMENT_T add index FK99FA8CB8CAC2365B (ASSESSMENTID), add constraint FK99FA8CB8CAC2365B foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T (ID);
alter table SAM_ATTACHMENT_T add index FK99FA8CB83288DBBD (ITEMID), add constraint FK99FA8CB83288DBBD foreign key (ITEMID) references SAM_ITEM_T (ITEMID);
alter table SAM_ATTACHMENT_T add index FK99FA8CB870CE2BD (SECTIONID), add constraint FK99FA8CB870CE2BD foreign key (SECTIONID) references SAM_SECTION_T (SECTIONID);
alter table SAM_PUBLISHEDATTACHMENT_T add index FK270998869482C945 (ASSESSMENTID), add constraint FK270998869482C945 foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T (ID);
alter table SAM_PUBLISHEDATTACHMENT_T add index FK2709988631446627 (ITEMID), add constraint FK2709988631446627 foreign key (ITEMID) references SAM_PUBLISHEDITEM_T (ITEMID);
alter table SAM_PUBLISHEDATTACHMENT_T add index FK27099886895D4813 (SECTIONID), add constraint FK27099886895D4813 foreign key (SECTIONID) references SAM_PUBLISHEDSECTION_T (SECTIONID);

-- more SAM

INSERT INTO SAM_TYPE_T (TYPEID ,AUTHORITY ,DOMAIN , KEYWORD,
    DESCRIPTION,
    STATUS, CREATEDBY, CREATEDDATE, LASTMODIFIEDBY,
    LASTMODIFIEDDATE )
    VALUES (11 , 'stanford.edu' , 'assessment.item' ,'Numeric Response' ,NULL ,1 ,1 ,
    '2005-01-01 12:00:00',1 ,'2005-01-01 12:00:00');

-- more more SAM
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY)
  VALUES(NULL, 1, 'releaseTo', 'SITE_MEMBERS')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
   ENTRY)
   VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
    AND TYPEID='142' AND ISTEMPLATE=1),
     'releaseTo', 'SITE_MEMBERS')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
   ENTRY)
   VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
    AND TYPEID='142' AND ISTEMPLATE=1),
     'releaseTo', 'SITE_MEMBERS')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
   ENTRY)
   VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
    AND TYPEID='142' AND ISTEMPLATE=1),
     'releaseTo', 'SITE_MEMBERS')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
   ENTRY)
   VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
    AND TYPEID='142' AND ISTEMPLATE=1),
     'releaseTo', 'SITE_MEMBERS')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
   ENTRY)
   VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
    AND TYPEID='142' AND ISTEMPLATE=1),
     'releaseTo', 'SITE_MEMBERS')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
   ENTRY)
   VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
    AND TYPEID='142' AND ISTEMPLATE=1),
     'releaseTo', 'SITE_MEMBERS')
;

-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------
-- syllabus

alter table SAKAI_SYLLABUS_DATA change position position_c integer not null;

-- --------------------------------------------------------------------------------------------------------------------------------------
-- privacy manager
create table SAKAI_PRIVACY_RECORD (id bigint not null auto_increment, lockId integer not null, contextId varchar(255) not null, recordType varchar(255) not null, userId varchar(255) not null, viewable bit not null, primary key (id), unique (contextId, recordType, userId)) default charset=latin1;

-- --------------------------------------------------------------------------------------------------------------------------------------


-- --------------------------------------------------------------------------------------------------------------------------------------
-- events

ALTER TABLE SAKAI_EVENT CHANGE SESSION_ID SESSION_ID VARCHAR (163);

-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------
-- rwiki (SAK-5674)
 
 UPDATE rwikiobject r , SAKAI_SITE s
     SET r.name = replace(r.name, concat('/site/',lower(s.site_id)), concat('/site/', s.site_id)),
     r.referenced = replace(r.referenced, concat('/site/',lower(s.site_id)), concat('/site/', s.site_id)),
     r.realm = replace(r.realm,  concat('/site/',lower(s.site_id)), concat('/site/', s.site_id))
     WHERE r.name LIKE concat('/site/',concat(s.site_id, '/%'));
 
 UPDATE rwikihistory r , SAKAI_SITE s
     SET r.name = replace(r.name, concat('/site/',lower(s.site_id)), concat('/site/', s.site_id)),
     r.referenced = replace(r.referenced, concat('/site/',lower(s.site_id)), concat('/site/', s.site_id)),
     r.realm = replace(r.realm,  concat('/site/',lower(s.site_id)), concat('/site/', s.site_id))
     WHERE r.name LIKE concat('/site/',concat(s.site_id, '/%'));

-- --------------------------------------------------------------------------------------------------------------------------------------

-- SAK-6780 added SQL update scripts to add new tables and alter existing tables to support selective release and spreadsheet upload

-- Gradebook table changes between Sakai 2.2.* and 2.3.

-- Add spreadsheet upload support.
create table GB_SPREADSHEET_T (
  ID bigint(20) NOT NULL auto_increment,
  VERSION int(11) NOT NULL,
  CREATOR varchar(255) NOT NULL,
  NAME varchar(255) NOT NULL,
  CONTENT text NOT NULL,
  DATE_CREATED datetime NOT NULL,
  GRADEBOOK_ID bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`) 
);


-- Add selective release support

alter table GB_GRADABLE_OBJECT_T add column (RELEASED bit);
update GB_GRADABLE_OBJECT_T set RELEASED=1 where RELEASED is NULL;
-- --------------------------------------------------------------------------------------------------------------------------------------


