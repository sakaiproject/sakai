-- This is the Oracle Sakai 2.6.x -> 2.7.0 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.6.x to 2.7.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------

-- SAK-16610 introduced a new osp presentation review permission
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.presentation.review');

-- SAK-16686/KNL-241 Support exceptions to dynamic page localization
INSERT INTO SAKAI_SITE_PAGE_PROPERTY VALUES ('~admin','~admin-400','sitePage.customTitle','true');

-- SAK-16832
ALTER TABLE SAM_PUBLISHEDASSESSMENT_T ADD LASTNEEDRESUBMITDATE DATE NULL;

-- SAK-16880 collaborative portfolio editing
ALTER TABLE osp_presentation ADD isCollab number(1,0) DEFAULT 0 NOT NULL;

-- SAK-17447
alter table EMAIL_TEMPLATE_ITEM add HTMLMESSAGE clob; 

-- SAK-16984 new column in sakai-Person
alter TABLE SAKAI_PERSON_T add NORMALIZEDMOBILE varchar2(255) NULL;

-- SAK-15165 new fields for SakaiPerson
alter table SAKAI_PERSON_T add FAVOURITE_BOOKS varchar2(4000); 
alter table SAKAI_PERSON_T add FAVOURITE_TV_SHOWS varchar2(4000); 
alter table SAKAI_PERSON_T add FAVOURITE_MOVIES varchar2(4000); 
alter table SAKAI_PERSON_T add FAVOURITE_QUOTES varchar2(4000); 
alter table SAKAI_PERSON_T add EDUCATION_COURSE varchar2(4000); 
alter table SAKAI_PERSON_T add EDUCATION_SUBJECTS varchar2(4000);

