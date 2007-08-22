-- This is the MySQL Sakai 2.4.0 (or later) -> 2.5.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.4.0 to 2.5.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------

--metaobj conversion
alter TABLE metaobj_form_def add column alternateCreateXslt	varchar(36) NULL;
alter TABLE metaobj_form_def add column alternateViewXslt  	varchar(36) NULL;

--Post'em SAK-8232
ALTER TABLE SAKAI_POSTEM_HEADINGS MODIFY heading VARCHAR(500);


-- Add colums to search to improve performance SAK-9865
alter table searchbuilderitem add itemscope integer;
alter table searchbuilderitem add index isearchbuilderitem_sco(itemscope);


-- SAK-9808: Implement ability to delete threaded messages within Forums
alter table MFR_MESSAGE_T add DELETED bit not null default false;
create index MFR_MESSAGE_DELETED_I on MFR_MESSAGE_T (DELETED);

--Chat SAK-10682
alter table CHAT2_CHANNEL modify CONTEXT VARCHAR(99) NOT NULL;

--Chat SAK-10163
ALTER TABLE CHAT2_CHANNEL ADD COLUMN PLACEMENT_ID varchar(99) NULL;
ALTER TABLE CHAT2_CHANNEL CHANGE contextDefaultChannel placementDefaultChannel tinyint(1) NULL;

update CHAT2_CHANNEL cc, SAKAI_SITE_TOOL st
set cc.PLACEMENT_ID = st.TOOL_ID
where st.REGISTRATION = 'sakai.chat' 
   and cc.placementDefaultChannel = true
   and cc.CONTEXT = st.SITE_ID;


--OSP SAK-10396: Add a default layout to be specified for a portfolio
alter table osp_presentation add column layout_id varchar(36) NULL;

--Profile add dateOfBirth property SAK-8423
alter table SAKAI_PERSON_T add column dateOfBirth date;

-- SAK-8780, SAK-7452 - Add SESSION_ACTIVE flag to explicitly indicate when
-- a session is active rather than relying on SESSION_START and SESSION_END
-- having the same value.
alter table SAKAI_SESSION add column SESSION_ACTIVE tinyint(1);
create index SESSION_ACTIVE_IE on SAKAI_SESSION (SESSION_ACTIVE);
