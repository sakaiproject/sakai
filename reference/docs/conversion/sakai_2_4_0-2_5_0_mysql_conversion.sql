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

--Add categories to the gradebook
create table GB_CATEGORY_T (ID bigint not null auto_increment, VERSION integer not null, GRADEBOOK_ID bigint not null, NAME varchar(255) not null, WEIGHT double precision, DROP_LOWEST integer, REMOVED bit, primary key (ID));
alter table GB_GRADABLE_OBJECT_T add CATEGORY_ID bigint;
alter table GB_GRADEBOOK_T add GRADE_TYPE integer not null;
alter table GB_GRADEBOOK_T add CATEGORY_TYPE integer not null;
alter table GB_CATEGORY_T add index FKCD333737325D7986 (GRADEBOOK_ID), add constraint FKCD333737325D7986 foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T (ID);
alter table GB_GRADABLE_OBJECT_T add index FK759996A7F09DEFAE (CATEGORY_ID), add constraint FK759996A7F09DEFAE foreign key (CATEGORY_ID) references GB_CATEGORY_T (ID);
create index GB_CATEGORY_GB_IDX on GB_CATEGORY_T (GRADEBOOK_ID);
create index GB_GRADABLE_OBJ_CT_IDX on GB_GRADABLE_OBJECT_T (CATEGORY_ID);
update GB_GRADEBOOK_T set GRADE_TYPE = 1, CATEGORY_TYPE = 1;

--Gradebook SAK-10427
alter table GB_GRADABLE_OBJECT_T add column UNGRADED bit;
update GB_GRADABLE_OBJECT_T set UNGRADED = false;

--Gradebook SAK-10571
drop table if exists GB_LETTERGRADE_PERCENT_MAPPING;
drop table if exists GB_LETTERGRADE_MAPPING;
create table GB_LETTERGRADE_MAPPING (LG_MAPPING_ID bigint not null, value double precision, GRADE varchar(255) not null, primary key (LG_MAPPING_ID, GRADE));
create table GB_LETTERGRADE_PERCENT_MAPPING (LGP_MAPPING_ID bigint not null auto_increment, VERSION integer not null, MAPPING_TYPE integer not null, GRADEBOOK_ID bigint, primary key (LGP_MAPPING_ID), unique (MAPPING_TYPE, GRADEBOOK_ID));
alter table GB_LETTERGRADE_MAPPING add index FKC8CDDC5CE7F3A761 (LG_MAPPING_ID), add constraint FKC8CDDC5CE7F3A761 foreign key (LG_MAPPING_ID) references GB_LETTERGRADE_PERCENT_MAPPING (LGP_MAPPING_ID);

insert into GB_LETTERGRADE_PERCENT_MAPPING values (null,  0, 1, null);
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
    GB_PERMISSION_ID bigint AUTO_INCREMENT NOT NULL,
    VERSION     	 integer NOT NULL,
    GRADEBOOK_ID	 bigint NOT NULL,
    USER_ID     	 varchar(99) NOT NULL,
    FUNCTION_NAME  	 varchar(5) NOT NULL,
    CATEGORY_ID 	 bigint NULL,
    GROUP_ID    	 varchar(255) NULL,
    PRIMARY KEY(GB_PERMISSION_ID)
);
