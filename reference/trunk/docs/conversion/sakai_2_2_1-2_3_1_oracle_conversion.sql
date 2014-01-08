-- This is the Oracle Sakai 2.2.1 (or later) -> 2.3.1 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.2.1 (or later) to 2.3.1.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------
-- Add new calendar & content permission function names
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'calendar.revise.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'calendar.revise.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'calendar.delete.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'calendar.delete.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'content.revise.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'content.revise.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'content.delete.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'content.delete.own');
 
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

DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY IN 
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION 
     WHERE FUNCTION_NAME IN ('calendar.revise','calendar.delete','content.revise','content.delete'));
     
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME IN ('calendar.revise','calendar.delete','content.revise','content.delete');

-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------
-- OSP

UPDATE osp_guidance SET securityViewFunction='osp.wizard.operate' WHERE
securityViewFunction='osp.wizard.view';

alter table osp_style add style_hash varchar2(255);

-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-6468 - CM

-- Drop the previously generated tables (these were not used in previous versions of Sakai)

drop table CM_ACADEMIC_SESSION_T cascade constraints;
drop table CM_COURSE_SET_CANON_ASSOC_T cascade constraints;
drop table CM_COURSE_SET_OFFERING_ASSOC_T cascade constraints;
drop table CM_CROSS_LISTING_T cascade constraints;
drop table CM_ENROLLMENT_SET_T cascade constraints;
drop table CM_ENROLLMENT_T cascade constraints;
drop table CM_MEMBERSHIP_T cascade constraints;
drop table CM_MEMBER_CONTAINER_T cascade constraints;
drop table CM_OFFICIAL_INSTRUCTORS_T cascade constraints;
drop sequence CM_ACADEMIC_SESSION_S;
drop sequence CM_CROSS_LISTING_S;
drop sequence CM_ENROLLMENT_S;
drop sequence CM_ENROLLMENT_SET_S;
drop sequence CM_MEMBERSHIP_S;
drop sequence CM_MEMBER_CONATINER_S;


-- Create the new CM tables

create table CM_ACADEMIC_SESSION_T (ACADEMIC_SESSION_ID number(19,0) not null, VERSION number(10,0) not null, LAST_MODIFIED_BY varchar2(255 char), LAST_MODIFIED_DATE timestamp, CREATED_BY varchar2(255 char), CREATED_DATE timestamp, ENTERPRISE_ID varchar2(255 char) not null unique, TITLE varchar2(255 char) not null, DESCRIPTION varchar2(255 char) not null, START_DATE timestamp, END_DATE timestamp, primary key (ACADEMIC_SESSION_ID));
create table CM_COURSE_SET_CANON_ASSOC_T (CANON_COURSE number(19,0) not null, COURSE_SET number(19,0) not null, primary key (COURSE_SET, CANON_COURSE));
create table CM_COURSE_SET_OFFERING_ASSOC_T (COURSE_SET number(19,0) not null, COURSE_OFFERING number(19,0) not null, primary key (COURSE_SET, COURSE_OFFERING));
create table CM_CROSS_LISTING_T (CROSS_LISTING_ID number(19,0) not null, VERSION number(10,0) not null, LAST_MODIFIED_BY varchar2(255 char), LAST_MODIFIED_DATE timestamp, CREATED_BY varchar2(255 char), CREATED_DATE timestamp, primary key (CROSS_LISTING_ID));
create table CM_ENROLLMENT_SET_T (ENROLLMENT_SET_ID number(19,0) not null, VERSION number(10,0) not null, LAST_MODIFIED_BY varchar2(255 char), LAST_MODIFIED_DATE timestamp, CREATED_BY varchar2(255 char), CREATED_DATE timestamp, ENTERPRISE_ID varchar2(255 char) not null unique, TITLE varchar2(255 char) not null, DESCRIPTION varchar2(255 char) not null, CATEGORY varchar2(255 char) not null, DEFAULT_CREDITS varchar2(255 char) not null, COURSE_OFFERING number(19,0), primary key (ENROLLMENT_SET_ID));
create table CM_ENROLLMENT_T (ENROLLMENT_ID number(19,0) not null, VERSION number(10,0) not null, LAST_MODIFIED_BY varchar2(255 char), LAST_MODIFIED_DATE timestamp, CREATED_BY varchar2(255 char), CREATED_DATE timestamp, USER_ID varchar2(255 char) not null, STATUS varchar2(255 char) not null, CREDITS varchar2(255 char) not null, GRADING_SCHEME varchar2(255 char) not null, DROPPED number(1,0), ENROLLMENT_SET number(19,0), primary key (ENROLLMENT_ID));
create table CM_MEETING_T (MEETING_ID number(19,0) not null, LOCATION varchar2(255 char), TIME_OF_DAY varchar2(255 char), NOTES varchar2(255 char), SECTION_ID number(19,0) not null, primary key (MEETING_ID));
create table CM_MEMBERSHIP_T (MEMBER_ID number(19,0) not null, VERSION number(10,0) not null, USER_ID varchar2(255 char) not null, ROLE varchar2(255 char) not null, MEMBER_CONTAINER_ID number(19,0), primary key (MEMBER_ID));
create table CM_MEMBER_CONTAINER_T (MEMBER_CONTAINER_ID number(19,0) not null, CLASS_DISCR varchar2(100 char) not null, VERSION number(10,0) not null, LAST_MODIFIED_BY varchar2(255 char), LAST_MODIFIED_DATE timestamp, CREATED_BY varchar2(255 char), CREATED_DATE timestamp, ENTERPRISE_ID varchar2(100 char) not null, TITLE varchar2(255 char) not null, DESCRIPTION varchar2(255 char) not null, CATEGORY varchar2(255 char), COURSE_OFFERING number(19,0), ENROLLMENT_SET number(19,0), PARENT_SECTION number(19,0), CROSS_LISTING number(19,0), PARENT_COURSE_SET number(19,0), STATUS varchar2(255 char), START_DATE timestamp, END_DATE timestamp, CANONICAL_COURSE number(19,0), ACADEMIC_SESSION number(19,0), EQUIV_CANON_COURSE_ID number(19,0), EQUIV_COURSE_OFFERING_ID number(19,0), primary key (MEMBER_CONTAINER_ID), unique (CLASS_DISCR, ENTERPRISE_ID));
create table CM_OFFICIAL_INSTRUCTORS_T (ENROLLMENT_SET_ID number(19,0) not null, INSTRUCTOR_ID varchar2(255 char));
alter table CM_COURSE_SET_CANON_ASSOC_T add constraint FKBFCBD9AE7F976CD6 foreign key (CANON_COURSE) references CM_MEMBER_CONTAINER_T;
alter table CM_COURSE_SET_CANON_ASSOC_T add constraint FKBFCBD9AE2D306E01 foreign key (COURSE_SET) references CM_MEMBER_CONTAINER_T;
alter table CM_COURSE_SET_OFFERING_ASSOC_T add constraint FK5B9A5CFD26827043 foreign key (COURSE_OFFERING) references CM_MEMBER_CONTAINER_T;
alter table CM_COURSE_SET_OFFERING_ASSOC_T add constraint FK5B9A5CFD2D306E01 foreign key (COURSE_SET) references CM_MEMBER_CONTAINER_T;
create index CM_ENR_SET_CO_IDX on CM_ENROLLMENT_SET_T (COURSE_OFFERING);
alter table CM_ENROLLMENT_SET_T add constraint FK99479DD126827043 foreign key (COURSE_OFFERING) references CM_MEMBER_CONTAINER_T;
create index CM_ENR_ENR_SET_IDX on CM_ENROLLMENT_T (ENROLLMENT_SET);
alter table CM_ENROLLMENT_T add constraint FK7A7F878E456D3EA1 foreign key (ENROLLMENT_SET) references CM_ENROLLMENT_SET_T;
alter table CM_MEETING_T add constraint FKE15DCD9BD0506F16 foreign key (SECTION_ID) references CM_MEMBER_CONTAINER_T;
alter table CM_MEMBERSHIP_T add constraint FK9FBBBFE067131463 foreign key (MEMBER_CONTAINER_ID) references CM_MEMBER_CONTAINER_T;
create index CM_SECTION_PARENT_IDX on CM_MEMBER_CONTAINER_T (PARENT_SECTION);
create index CM_SECTION_ENR_SET_IDX on CM_MEMBER_CONTAINER_T (ENROLLMENT_SET);
create index CM_COURSE_SET_PARENT_IDX on CM_MEMBER_CONTAINER_T (PARENT_COURSE_SET);
create index CM_CO_ACADEMIC_SESS_IDX on CM_MEMBER_CONTAINER_T (ACADEMIC_SESSION);
create index CM_CO_CANON_COURSE_IDX on CM_MEMBER_CONTAINER_T (CANONICAL_COURSE);
create index CM_SECTION_COURSE_IDX on CM_MEMBER_CONTAINER_T (COURSE_OFFERING);
alter table CM_MEMBER_CONTAINER_T add constraint FKD96A9BC6661E50E9 foreign key (ACADEMIC_SESSION) references CM_ACADEMIC_SESSION_T;
alter table CM_MEMBER_CONTAINER_T add constraint FKD96A9BC6456D3EA1 foreign key (ENROLLMENT_SET) references CM_ENROLLMENT_SET_T;
alter table CM_MEMBER_CONTAINER_T add constraint FKD96A9BC626827043 foreign key (COURSE_OFFERING) references CM_MEMBER_CONTAINER_T;
alter table CM_MEMBER_CONTAINER_T add constraint FKD96A9BC63B0306B1 foreign key (PARENT_SECTION) references CM_MEMBER_CONTAINER_T;
alter table CM_MEMBER_CONTAINER_T add constraint FKD96A9BC64F7C8841 foreign key (CROSS_LISTING) references CM_CROSS_LISTING_T;
alter table CM_MEMBER_CONTAINER_T add constraint FKD96A9BC6D05F59F1 foreign key (CANONICAL_COURSE) references CM_MEMBER_CONTAINER_T;
alter table CM_MEMBER_CONTAINER_T add constraint FKD96A9BC6D0C1EF35 foreign key (EQUIV_COURSE_OFFERING_ID) references CM_CROSS_LISTING_T;
alter table CM_MEMBER_CONTAINER_T add constraint FKD96A9BC66DFDE2 foreign key (EQUIV_CANON_COURSE_ID) references CM_CROSS_LISTING_T;
alter table CM_MEMBER_CONTAINER_T add constraint FKD96A9BC649A68CB6 foreign key (PARENT_COURSE_SET) references CM_MEMBER_CONTAINER_T;
alter table CM_OFFICIAL_INSTRUCTORS_T add constraint FK470F8ACCC28CC1AD foreign key (ENROLLMENT_SET_ID) references CM_ENROLLMENT_SET_T;
create sequence CM_ACADEMIC_SESSION_S;
create sequence CM_CROSS_LISTING_S;
create sequence CM_ENROLLMENT_S;
create sequence CM_ENROLLMENT_SET_S;
create sequence CM_MEETING_S;
create sequence CM_MEMBERSHIP_S;
create sequence CM_MEMBER_CONATINER_S;

-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAM (SAK-4396)

drop table SAM_ATTACHMENT_T cascade constraints;
drop table SAM_PUBLISHEDATTACHMENT_T cascade constraints;
drop sequence SAM_ATTACHMENT_ID_S;
drop sequence SAM_PUBLISHEDATTACHMENT_ID_S;
create table SAM_ATTACHMENT_T (ATTACHMENTID number(19,0) not null, ATTACHMENTTYPE varchar2(255 char) not null, RESOURCEID varchar(255), FILENAME varchar(255), MIMETYPE varchar(80), FILESIZE integer, DESCRIPTION varchar(4000), LOCATION varchar(4000), ISLINK integer, STATUS integer not null, CREATEDBY varchar(36) not null, CREATEDDATE timestamp not null, LASTMODIFIEDBY varchar(36) not null, LASTMODIFIEDDATE timestamp not null, ASSESSMENTID number(19,0), SECTIONID number(19,0), ITEMID number(19,0), primary key (ATTACHMENTID));
create table SAM_PUBLISHEDATTACHMENT_T (ATTACHMENTID number(19,0) not null, ATTACHMENTTYPE varchar2(255 char) not null, RESOURCEID varchar(255), FILENAME varchar(255), MIMETYPE varchar(80), FILESIZE integer, DESCRIPTION varchar(4000), LOCATION varchar(4000), ISLINK integer, STATUS integer not null, CREATEDBY varchar(36) not null, CREATEDDATE timestamp not null, LASTMODIFIEDBY varchar(36) not null, LASTMODIFIEDDATE timestamp not null, ASSESSMENTID number(19,0), SECTIONID number(19,0), ITEMID number(19,0), primary key (ATTACHMENTID));
alter table SAM_ATTACHMENT_T add constraint FK99FA8CB8CAC2365B foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
alter table SAM_ATTACHMENT_T add constraint FK99FA8CB83288DBBD foreign key (ITEMID) references SAM_ITEM_T;
alter table SAM_ATTACHMENT_T add constraint FK99FA8CB870CE2BD foreign key (SECTIONID) references SAM_SECTION_T;
alter table SAM_PUBLISHEDATTACHMENT_T add constraint FK270998869482C945 foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_PUBLISHEDATTACHMENT_T add constraint FK2709988631446627 foreign key (ITEMID) references SAM_PUBLISHEDITEM_T;
alter table SAM_PUBLISHEDATTACHMENT_T add constraint FK27099886895D4813 foreign key (SECTIONID) references SAM_PUBLISHEDSECTION_T;
create sequence SAM_ATTACHMENT_ID_S;
create sequence SAM_PUBLISHEDATTACHMENT_ID_S;

-- more SAM

INSERT INTO SAM_TYPE_T ("TYPEID" ,"AUTHORITY" ,"DOMAIN" ,"KEYWORD",
    "DESCRIPTION" ,
    "STATUS" ,"CREATEDBY" ,"CREATEDDATE" ,"LASTMODIFIEDBY" ,
    "LASTMODIFIEDDATE" )
    VALUES (11 , 'stanford.edu' , 'assessment.item' ,'Numeric Response' ,NULL ,1 ,1 ,
    SYSDATE ,1 ,SYSDATE);

-- more SAM
INSERT INTO SAM_ASSESSMETADATA_T ("ASSESSMENTMETADATAID",
"ASSESSMENTID","LABEL", "ENTRY")
  VALUES(sam_assessMetaData_id_s.nextVal, 1, 'releaseTo', 'SITE_MEMBERS')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
   ENTRY)
   VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
    AND TYPEID='142' AND ISTEMPLATE=1),
     'releaseTo', 'SITE_MEMBERS')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
   ENTRY)
   VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
    AND TYPEID='142' AND ISTEMPLATE=1),
     'releaseTo', 'SITE_MEMBERS')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
   ENTRY)
   VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
    AND TYPEID='142' AND ISTEMPLATE=1),
     'releaseTo', 'SITE_MEMBERS')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
   ENTRY)
   VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
    AND TYPEID='142' AND ISTEMPLATE=1),
     'releaseTo', 'SITE_MEMBERS')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
   ENTRY)
   VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
    AND TYPEID='142' AND ISTEMPLATE=1),
     'releaseTo', 'SITE_MEMBERS')
;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
   ENTRY)
   VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
    AND TYPEID='142' AND ISTEMPLATE=1),
     'releaseTo', 'SITE_MEMBERS')
;

-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------
-- syllabus

ALTER TABLE sakai_syllabus_data RENAME COLUMN position TO position_c;

-- --------------------------------------------------------------------------------------------------------------------------------------
-- privacy manager

create table SAKAI_PRIVACY_RECORD (id number(19,0) not null, lockId number(10,0) not null, contextId varchar2(255 char) not null, recordType varchar2(255 char) not null, userId varchar2(255 char) not null, viewable number(1,0) not null, primary key (id), unique (contextId, recordType, userId));
create sequence PrivacyRecordImpl_SEQ;

-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------
-- events

ALTER TABLE SAKAI_EVENT MODIFY SESSION_ID VARCHAR2 (163);

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
    ID          	NUMBER(19,0) NOT NULL,
    VERSION     	NUMBER(10,0) NOT NULL,
    CREATOR     	VARCHAR2(255) NOT NULL,
    NAME        	VARCHAR2(255) NOT NULL,
    CONTENT     	CLOB NOT NULL,
    DATE_CREATED	DATE NOT NULL,
    GRADEBOOK_ID	NUMBER(19,0) NOT NULL,
    PRIMARY KEY(ID)
);

create sequence GB_SPREADSHEET_S;

alter table GB_GRADABLE_OBJECT_T add (RELEASED NUMBER(1,0));
update GB_GRADABLE_OBJECT_T set RELEASED=1 where RELEASED is NULL;

-- --------------------------------------------------------------------------------------------------------------------------------------



