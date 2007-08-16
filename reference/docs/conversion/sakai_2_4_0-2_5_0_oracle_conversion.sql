-- This is the Oracle Sakai 2.4.0 (or later) -> 2.5.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.4.0 to 2.5.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------

--metaobj conversion
alter TABLE metaobj_form_def add alternateCreateXslt varchar2(36) NULL;
alter TABLE metaobj_form_def add alternateViewXslt varchar2(36) NULL;

--Post'em SAK-8232
ALTER TABLE SAKAI_POSTEM_HEADINGS MODIFY heading VARCHAR2 (500);


-- Add colums to search to improve performance SAK-9865
alter table searchbuilderitem add itemscope integer;
alter table searchbuilderitem add index isearchbuilderitem_sco(itemscope);

-- SAK-9808: Implement ability to delete threaded messages within Forums
alter table MFR_MESSAGE_T add DELETED number(1, 0) default '0' not null;
create index MFR_MESSAGE_DELETED_I on MFR_MESSAGE_T (DELETED);

--Chat SAK-10682
alter table CHAT2_CHANNEL modify (CONTEXT VARCHAR2(99));

--Chat SAK-10163
ALTER TABLE CHAT2_CHANNEL ADD PLACEMENT_ID varchar2(99) NULL;
ALTER TABLE CHAT2_CHANNEL RENAME COLUMN contextDefaultChannel TO placementDefaultChannel;

update CHAT2_CHANNEL cc
set cc.PLACEMENT_ID = (select st.TOOL_ID from SAKAI_SITE_TOOL st where st.REGISTRATION = 'sakai.chat' 
   and cc.placementDefaultChannel = 1
   and cc.CONTEXT = st.SITE_ID )
where EXISTS 
(select st.TOOL_ID from SAKAI_SITE_TOOL st where st.REGISTRATION = 'sakai.chat' 
   and cc.placementDefaultChannel = 1
   and cc.CONTEXT = st.SITE_ID)

--OSP SAK-10396: Add a default layout to be specified for a portfolio
alter table osp_presentation add layout_id varchar2(36) NULL;

--Profile add dateOfBirth property SAK-8423
alter table SAKAI_PERSON_T add (dateOfBirth date);
