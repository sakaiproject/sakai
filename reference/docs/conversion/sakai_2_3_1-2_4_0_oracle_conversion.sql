-- This is the Oracle Sakai 2.3.0 (or later) -> 2.4.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.3.0 to 2.4.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------------------------------------------------

-- OSP conversion
alter table osp_presentation_template add propertyFormType varchar2(36);
alter table osp_presentation add property_form varchar2(36);
alter table osp_scaffolding add preview number(1,0);
alter table osp_wizard add preview number(1,0);
alter table osp_review add review_item_id varchar2(36);

update osp_list_config set selected_columns = replace(selected_columns, 'name', 'title') where selected_columns like '%name%';
update osp_list_config set selected_columns = replace(selected_columns, 'siteName', 'site.title') where selected_columns like '%siteName%';

--Updating for a change to the synoptic view for portfolio worksites
update sakai_site_tool_property set name='siteTypeList', value='portfolio,PortfolioAdmin' where value='portfolioWorksites';


-- Samigo SAK-6790 conversion
alter table SAM_ASSESSMENTBASE_T MODIFY (CREATEDBY varchar(255) , LASTMODIFIEDBY varchar(255));
alter table SAM_SECTION_T MODIFY (CREATEDBY varchar(255), LASTMODIFIEDBY varchar(255));
alter table SAM_PUBLISHEDASSESSMENT_T MODIFY(CREATEDBY varchar(255), LASTMODIFIEDBY varchar(255));
alter table SAM_PUBLISHEDSECTION_T MODIFY(CREATEDBY varchar(255), LASTMODIFIEDBY varchar(255));
alter table SAM_ITEM_T MODIFY(ITEMIDSTRING varchar(255),  CREATEDBY varchar(255), LASTMODIFIEDBY varchar(255));
alter table SAM_ITEMFEEDBACK_T MODIFY(TYPEID varchar(255));
alter table SAM_ANSWERFEEDBACK_T MODIFY(TYPEID varchar(255));
alter table SAM_ATTACHMENT_T MODIFY(CREATEDBY varchar(255) , LASTMODIFIEDBY varchar(255));
alter table SAM_PUBLISHEDITEM_T MODIFY(ITEMIDSTRING varchar(255),  CREATEDBY varchar(255), LASTMODIFIEDBY varchar(255));
alter table SAM_PUBLISHEDITEMFEEDBACK_T MODIFY(TYPEID varchar(255));
alter table SAM_PUBLISHEDANSWERFEEDBACK_T MODIFY(TYPEID varchar(255));
alter table SAM_PUBLISHEDATTACHMENT_T  MODIFY(CREATEDBY varchar(255) , LASTMODIFIEDBY varchar(255));
alter table SAM_AUTHZDATA_T MODIFY(AGENTID varchar(255), LASTMODIFIEDBY varchar(255));
alter table SAM_ASSESSMENTGRADING_T MODIFY(AGENTID varchar(255) , GRADEDBY varchar(255));
alter table SAM_ITEMGRADING_T MODIFY(AGENTID varchar(255) , GRADEDBY varchar(255));
alter table SAM_GRADINGSUMMARY_T MODIFY(AGENTID varchar(255));
alter table SAM_MEDIA_T MODIFY(CREATEDBY varchar(255), LASTMODIFIEDBY varchar(255));
alter table SAM_TYPE_T MODIFY(CREATEDBY varchar(255) , LASTMODIFIEDBY varchar(255));

----------------------------------------------------------------------------------------------------------------------------------------
-- new roster permissions
----------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'roster.viewall');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'roster.viewofficialid');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'roster.viewhidden');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'roster.viewsection');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'roster.export');

-- ADJUST ME: adjust theses for your needs for other roles than 'access', 'Student', 'maintain', 'Instructor' and 'Teaching Assistant'-- maintain role
-- maintain role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewall'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.export'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewall'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.export'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewall'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.export'));

-- access role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsection'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsection'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsection'));

-- Instructor role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewall'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewhidden'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewofficialid'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.export'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewall'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewhidden'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewofficialid'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.export'));

-- Teaching Assistant role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsection'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewhidden'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewofficialid'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.export'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsection'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewhidden'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewofficialid'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.export'));

-- Student role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsection'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsection'));

----------------------------------------------------------------------------------------------------------------------------------------
-- backfill new roster permissions into existing realms
----------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

-- These are for the site templates
-- ADJUST ME: adjust theses for your needs, either with different permissions, or duplicate for other roles than 'access', 'Student', 'maintain', 'Instructor' and 'Teaching Assistant'

INSERT INTO PERMISSIONS_SRC_TEMP values ('access','roster.viewsection');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','roster.viewsection');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','roster.viewall');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','roster.export');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','roster.viewall');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','roster.viewofficialid');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','roster.viewhidden');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','roster.export');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','roster.viewsection');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','roster.viewofficialid');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','roster.viewhidden');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','roster.export');

-- lookup the role and function numbers
create table PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
insert into PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
select SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
join SAKAI_REALM_ROLE SRR on (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
join SAKAI_REALM_FUNCTION SRF on (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper")
insert into SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
from
    (select distinct SRRF.REALM_KEY, SRRF.ROLE_KEY from SAKAI_REALM_RL_FN SRRF) SRRFD
    join PERMISSIONS_TEMP TMP on (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    join SAKAI_REALM SR on (SRRFD.REALM_KEY = SR.REALM_KEY)
    where SR.REALM_ID != '!site.helper'
    and not exists (
        select 1
            from SAKAI_REALM_RL_FN SRRFI
            where SRRFI.REALM_KEY=SRRFD.REALM_KEY and SRRFI.ROLE_KEY=SRRFD.ROLE_KEY and  SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables
drop table PERMISSIONS_TEMP;
drop table PERMISSIONS_SRC_TEMP;


----------------------------------------------------------------------------------------------------------------------------------------
-- Site related tables changes needed for 2.4.0 (SAK-7341)
----------------------------------------------------------------------------------------------------------------------------------------
ALTER TABLE SAKAI_SITE ADD (CUSTOM_PAGE_ORDERED CHAR(1) DEFAULT '0' CHECK (CUSTOM_PAGE_ORDERED IN (1, 0)));

----------------------------------------------------------------------------------------------------------------------------------------
-- Post'em table changes needed for 2.4.0 
----------------------------------------------------------------------------------------------------------------------------------------
-- SAK-8232
ALTER TABLE SAKAI_POSTEM_STUDENT_GRADES MODIFY grade VARCHAR2 (2000);

-- SAK-6948
ALTER TABLE SAKAI_POSTEM_GRADEBOOK MODIFY title VARCHAR2 (255);

-- SAK-8213
ALTER TABLE SAKAI_REALM_ROLE_DESC ADD (PROVIDER_ONLY CHAR(1) NULL);
