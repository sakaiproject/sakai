-- This is the Oracle Sakai 2.2.0 -> 2.2.1 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.2.0 to 2.2.1.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------

-- OSP-1607
-- http://bugs.osportfolio.org/jira/browse/OSP-1607
-- Increasing the size of fields that hold a site id to 99.  Some were not specifying a length and would result in a length of 255.  I'm leaving those alone for now.

alter table osp_guidance modify site_id varchar2(99);
alter table osp_review modify site_id varchar2(99);
alter table osp_style modify site_id varchar2(99);
alter table osp_site_tool modify site_id varchar2(99);
alter table osp_presentation_template modify site_id varchar2(99);
alter table osp_presentation modify site_id varchar2(99);
alter table osp_presentation_layout modify site_id varchar2(99);
alter table osp_wizard modify site_id varchar2(99);


-- SAK-5595
-- http://bugs.sakaiproject.org/jira/browse/SAK-5595
-- Conversion script error: missing column DURATION in SAM_MEDIA_T

alter table SAM_MEDIA_T add (DURATION varchar(36)); 

-- OSP-1289
-- http://bugs.osportfolio.org/jira/browse/OSP-1289
-- Need to add delete to the default metaobj permissions
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'metaobj.delete');
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'));

-- SAK-5564
-- http://bugs.sakaiproject.org/jira/browse/SAK-5564
--  exception thrown when trying to update

alter table SAM_ITEMGRADING_T modify (SUBMITTEDDATE date null);

