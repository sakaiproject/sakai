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