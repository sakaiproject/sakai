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
   and cc.CONTEXT = st.SITE_ID);

--OSP SAK-10396: Add a default layout to be specified for a portfolio
alter table osp_presentation add layout_id varchar2(36) NULL;

--Profile add dateOfBirth property SAK-8423
alter table SAKAI_PERSON_T add (dateOfBirth date);

-- SAK-8780, SAK-7452 - Add SESSION_ACTIVE flag to explicitly indicate when
-- a session is active rather than relying on SESSION_START and SESSION_END
-- having the same value.
alter table SAKAI_SESSION add column SESSION_ACTIVE number(1,0);
create index SESSION_ACTIVE_IE on SAKAI_SESSION (SESSION_ACTIVE);

--Add categories to gradebook
create table GB_CATEGORY_T (ID number(19,0) not null, VERSION number(10,0) not null, GRADEBOOK_ID number(19,0) not null, NAME varchar2(255 char) not null, WEIGHT double precision, DROP_LOWEST number(10,0), REMOVED number(1,0), primary key (ID));
alter table GB_GRADABLE_OBJECT_T add CATEGORY_ID number(19,0);
alter table GB_GRADEBOOK_T add GRADE_TYPE number(10,0);
alter table GB_GRADEBOOK_T add CATEGORY_TYPE number(10,0);
alter table GB_CATEGORY_T add constraint FKCD333737325D7986 foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T;
alter table GB_GRADABLE_OBJECT_T add constraint FK759996A7F09DEFAE foreign key (CATEGORY_ID) references GB_CATEGORY_T;
create sequence GB_CATEGORY_S;
create index GB_CATEGORY_GB_IDX on GB_CATEGORY_T (GRADEBOOK_ID);
create index GB_GRADABLE_OBJ_CT_IDX on GB_GRADABLE_OBJECT_T (CATEGORY_ID);
update GB_GRADEBOOK_T set GRADE_TYPE = 1, CATEGORY_TYPE = 1;
alter table GB_GRADEBOOK_T modify ( GRADE_TYPE number(10,0) not null, CATEGORY_TYPE number(10,0) not null );

--Gradebook SAK-10427
alter table GB_GRADABLE_OBJECT_T add (UNGRADED number(1,0));
update GB_GRADABLE_OBJECT_T set UNGRADED = 0;

--Gradebook SAK-10571
create table GB_LETTERGRADE_MAPPING (LG_MAPPING_ID number(19,0) not null, VALUE double precision, GRADE varchar2(255 char) not null, primary key (LG_MAPPING_ID, GRADE));
create table GB_LETTERGRADE_PERCENT_MAPPING (LGP_MAPPING_ID number(19,0) not null, VERSION number(10,0) not null, MAPPING_TYPE number(10,0) not null, GRADEBOOK_ID number(19,0), primary key (LGP_MAPPING_ID), unique (MAPPING_TYPE, GRADEBOOK_ID));
alter table GB_LETTERGRADE_MAPPING add constraint FKC8CDDC5CE7F3A761 foreign key (LG_MAPPING_ID) references GB_LETTERGRADE_PERCENT_MAPPING (LGP_MAPPING_ID);
create sequence GB_LETTER_MAPPING_S;

insert into GB_LETTERGRADE_PERCENT_MAPPING values (GB_LETTER_MAPPING_S.NEXTVAL, 0, 1, null);
insert into GB_LETTERGRADE_MAPPING values (1, 100.0, 'A+');
insert into GB_LETTERGRADE_MAPPING values (1, 95.0, 'A');
insert into GB_LETTERGRADE_MAPPING values (1, 90.0, 'A-');
insert into GB_LETTERGRADE_MAPPING values (1, 87.0, 'B+');
insert into GB_LETTERGRADE_MAPPING values (1, 83.0, 'B');
insert into GB_LETTERGRADE_MAPPING values (1, 80.0, 'B-');
insert into GB_LETTERGRADE_MAPPING values (1, 77.0, 'C+');
insert into GB_LETTERGRADE_MAPPING values (1, 73.0, 'C');
insert into GB_LETTERGRADE_MAPPING values (1, 70.0, 'C-');
insert into GB_LETTERGRADE_MAPPING values (1, 67.0, 'D+');
insert into GB_LETTERGRADE_MAPPING values (1, 63.0, 'D');
insert into GB_LETTERGRADE_MAPPING values (1, 60.0, 'D-');
insert into GB_LETTERGRADE_MAPPING values (1, 0.0, 'F');

--Gradebook SAK-10835
CREATE TABLE GB_PERMISSION_T ( 
    GB_PERMISSION_ID number(19,0) not null,
    VERSION     	 number(10,0) not null,
    GRADEBOOK_ID	 number(19,0) not null,
    USER_ID     	 varchar2(99) not null,
    FUNCTION_NAME 	 varchar2(5) not null,
    CATEGORY_ID 	 number(19,0) null,
    GROUP_ID    	 varchar2(255) null,
    PRIMARY KEY(GB_PERMISSION_ID)
);

create sequence GB_PERMISSION_S;

