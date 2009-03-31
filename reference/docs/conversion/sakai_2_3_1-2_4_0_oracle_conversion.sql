-- This is the Oracle Sakai 2.3.0 (or later) -> 2.4.0 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.3.0 to 2.4.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------

-- OSP conversion (note first 2 alters may be unnecessary for 2.3.x
alter table osp_presentation_template add propertyFormType varchar2(36);
alter table osp_presentation add property_form varchar2(36);
alter table osp_scaffolding add preview number(1,0) DEFAULT 0 not null;
alter table osp_wizard add preview number(1,0) DEFAULT 0 not null;
alter table osp_review add review_item_id varchar2(36);

update osp_list_config set selected_columns = replace(selected_columns, 'name', 'title') where selected_columns like '%name%';
update osp_list_config set selected_columns = replace(selected_columns, 'siteName', 'site.title') where selected_columns like '%siteName%';

-- Updating for a change to the synoptic view for portfolio worksites
update sakai_site_tool_property set name='siteTypeList', value='portfolio,PortfolioAdmin' where value like 'portfolioWorksites';

-- making sure these fields allow nulls
ALTER TABLE osp_scaffolding MODIFY ( readyColor VARCHAR2(7) NULL );
ALTER TABLE osp_scaffolding MODIFY ( pendingColor VARCHAR2(7) NULL );
ALTER TABLE osp_scaffolding MODIFY ( completedColor VARCHAR2(7) NULL );
ALTER TABLE osp_scaffolding MODIFY ( lockedColor VARCHAR2(7) NULL );


-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------

-- SAMIGO conversion
-- SAK-6790 
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

-- For performance
create index SAM_ANSWERFEED_ANSWERID_I on SAM_ANSWERFEEDBACK_T (ANSWERID);
create index SAM_ANSWER_ITEMTEXTID_I on SAM_ANSWER_T (ITEMTEXTID);
create index SAM_ITEMFEED_ITEMID_I on SAM_ITEMFEEDBACK_T (ITEMID);
create index SAM_ITEMMETADATA_ITEMID_I on SAM_ITEMMETADATA_T (ITEMID);
create index SAM_ITEMTEXT_ITEMID_I on SAM_ITEMTEXT_T (ITEMID);
create index SAM_ITEM_SECTIONID_I on SAM_ITEM_T (SECTIONID);
create index SAM_QPOOL_OWNER_I on SAM_QUESTIONPOOL_T (OWNERID);

-- SAK-7093
-- its ok to ignore the drop errors
drop table SAM_STUDENTGRADINGSUMMARY_T cascade constraints;
drop sequence SAM_STUDENTGRADINGSUMMARY_ID_S;
create table SAM_STUDENTGRADINGSUMMARY_T (
STUDENTGRADINGSUMMARYID number(19,0) not null,
PUBLISHEDASSESSMENTID number(19,0) not null,
AGENTID varchar2(255) not null,
NUMBERRETAKE integer,
CREATEDBY varchar2(255) not null,
CREATEDDATE timestamp not null,
LASTMODIFIEDBY varchar2(255) not null,
LASTMODIFIEDDATE timestamp not null,
primary key (STUDENTGRADINGSUMMARYID)
);
create sequence SAM_STUDENTGRADINGSUMMARY_ID_S;
create index SAM_PUBLISHEDASSESSMENT2_I on SAM_STUDENTGRADINGSUMMARY_T (PUBLISHEDASSESSMENTID);

-- SAK-10194
insert into sam_sectionmetadata_t
select sam_sectionmetadata_id_s.nextval,sectionid,'POOLNAME_FOR_RANDOM_DRAW',(select title from sam_questionpool_t where questionpoolid=a.entry)
from sam_sectionmetadata_t a where a.label='POOLID_FOR_RANDOM_DRAW'
and not exists (select null from sam_sectionmetadata_t b where a.sectionid=b.sectionid and label='POOLNAME_FOR_RANDOM_DRAW');


-- --------------------------------------------------------------------------------------------------------------------------------------
-- new roster permissions
-- --------------------------------------------------------------------------------------------------------------------------------------

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

-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new roster permissions into existing realms
-- --------------------------------------------------------------------------------------------------------------------------------------

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


-- --------------------------------------------------------------------------------------------------------------------------------------
-- Site related tables changes needed for 2.4.0 (SAK-7341)
-- --------------------------------------------------------------------------------------------------------------------------------------
ALTER TABLE SAKAI_SITE ADD (CUSTOM_PAGE_ORDERED CHAR(1) DEFAULT '0' CHECK (CUSTOM_PAGE_ORDERED IN (1, 0)));

-- --------------------------------------------------------------------------------------------------------------------------------------
-- Post'em table changes needed for 2.4.0 
-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-8232
ALTER TABLE SAKAI_POSTEM_STUDENT_GRADES MODIFY grade VARCHAR2 (2000);

-- SAK-6948
ALTER TABLE SAKAI_POSTEM_GRADEBOOK MODIFY title VARCHAR2 (255);

-- SAK-8213
ALTER TABLE SAKAI_REALM_ROLE_DESC ADD (PROVIDER_ONLY CHAR(1) NULL);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- Add Moderator functionality to Message Center (SAK-8632)
-- --------------------------------------------------------------------------------------------------------------------------------------
-- add column to allow Moderator as template setting
alter table MFR_AREA_T add (MODERATED NUMBER(1,0));
update MFR_AREA_T set MODERATED=0 where MODERATED is NULL;
alter table MFR_AREA_T modify (MODERATED NUMBER(1,0) not null);

-- change APPROVED column to allow null values to represent pending approvals
alter table MFR_MESSAGE_T modify (APPROVED NUMBER(1,0) null);

-- change MODERATED column in MFR_OPEN_FORUM_T to not null
update MFR_OPEN_FORUM_T set MODERATED=0 where MODERATED is NULL;
alter table MFR_OPEN_FORUM_T modify (MODERATED NUMBER(1,0) not null);

-- change MODERATED column in MFR_TOPIC_T to not null
update MFR_TOPIC_T set MODERATED=0 where MODERATED is NULL;
alter table MFR_TOPIC_T modify (MODERATED NUMBER(1,0) not null);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- New Chat storage and permissions (SAK-8508)
-- --------------------------------------------------------------------------------------------------------------------------------------
-- create new tables
-- This is coming soon as soon as I can generate the ddl for Oracle...

CREATE TABLE CHAT2_CHANNEL ( 
    CHANNEL_ID           	VARCHAR2(99) NOT NULL,
    CONTEXT              	VARCHAR2(36) NOT NULL,
    CREATION_DATE        	TIMESTAMP(6) NULL,
    TITLE                	VARCHAR2(64) NULL,
    DESCRIPTION          	VARCHAR2(255) NULL,
    FILTERTYPE           	VARCHAR2(25) NULL,
    FILTERPARAM          	NUMBER(10,0) NULL,
    CONTEXTDEFAULTCHANNEL	NUMBER(1,0) NULL,
    ENABLE_USER_OVERRIDE	NUMBER(1,0) NULL,
    PRIMARY KEY(CHANNEL_ID)
);

CREATE INDEX CHAT2_CHNL_CNTXT_I ON CHAT2_CHANNEL
(
       CONTEXT
);

CREATE INDEX CHAT2_CHNL_CNTXT_DFLT_I ON CHAT2_CHANNEL
(
       CONTEXT,
       CONTEXTDEFAULTCHANNEL
);

CREATE TABLE CHAT2_MESSAGE ( 
    MESSAGE_ID  	VARCHAR2(99) NOT NULL,
    CHANNEL_ID  	VARCHAR2(99) NULL,
    OWNER       	VARCHAR2(96) NOT NULL,
    MESSAGE_DATE	TIMESTAMP(6) NULL,
    BODY        	CLOB NOT NULL,
    PRIMARY KEY(MESSAGE_ID)
);

ALTER TABLE CHAT2_MESSAGE
    ADD ( FOREIGN KEY(CHANNEL_ID)
	REFERENCES CHAT2_CHANNEL(CHANNEL_ID)
);

CREATE INDEX CHAT2_MSG_CHNL_I ON CHAT2_MESSAGE
(
       CHANNEL_ID
);

CREATE INDEX CHAT2_MSG_CHNL_DATE_I ON CHAT2_MESSAGE
(
       CHANNEL_ID,
       MESSAGE_DATE
);

-- chat conversion prep
alter table CHAT2_CHANNEL add migratedChannelId varchar2(99);
alter table CHAT2_MESSAGE add migratedMessageId varchar2(99);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- New private folder (SAK-8759)
-- --------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO CONTENT_COLLECTION VALUES ('/private/','/',
'<?xml version="1.0" encoding="UTF-8"?>
<collection id="/private/">
	<properties>
		<property name="CHEF:creator" value="admin"/>
		<property name="CHEF:is-collection" value="true"/>
		<property name="DAV:displayname" value="private"/>
		<property name="CHEF:modifiedby" value="admin"/>
		<property name="DAV:getlastmodified" value="20020401000000000"/>
		<property name="DAV:creationdate" value="20020401000000000"/>
	</properties>
</collection>
');

-- --------------------------------------------------------------------------------------------------------------------------------------
-- Gradebook table changes needed for 2.4.0 (SAK-8711)
-- --------------------------------------------------------------------------------------------------------------------------------------
-- Add grade commments.
create table GB_COMMENT_T (
	ID number(19,0) not null,
	VERSION number(10,0) not null,
	GRADER_ID varchar2(255 char) not null,
	STUDENT_ID varchar2(255 char) not null,
	COMMENT_TEXT clob,
	DATE_RECORDED timestamp not null,
	GRADABLE_OBJECT_ID number(19,0) not null,
	primary key (ID),
	unique (STUDENT_ID, GRADABLE_OBJECT_ID));
alter table GB_COMMENT_T 
	add constraint FK7977DFF06F98CFF foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T;
create sequence GB_COMMENT_S;

-- Remove database-caching of calculated course grades.
alter table GB_GRADE_RECORD_T drop column SORT_GRADE;

-- --------------------------------------------------------------------------------------------------------------------------------------
-- CourseManagement Reference Impl table changes needed for 2.4.0
-- --------------------------------------------------------------------------------------------------------------------------------------
create table CM_SEC_CATEGORY_T (CAT_CODE varchar2(255 char) not null, CAT_DESCR varchar2(255 char), primary key (CAT_CODE));
create index CM_ENR_USER on CM_ENROLLMENT_T (USER_ID);
create index CM_MBR_CTR on CM_MEMBERSHIP_T (MEMBER_CONTAINER_ID);
create index CM_MBR_USER on CM_MEMBERSHIP_T (USER_ID);
create index CM_INSTR_IDX on CM_OFFICIAL_INSTRUCTORS_T (INSTRUCTOR_ID);
alter table CM_ACADEMIC_SESSION_T modify (LAST_MODIFIED_DATE date);
alter table CM_ACADEMIC_SESSION_T modify (CREATED_DATE date);
alter table CM_ACADEMIC_SESSION_T modify (START_DATE date);
alter table CM_ACADEMIC_SESSION_T modify (END_DATE date);
alter table CM_CROSS_LISTING_T modify (LAST_MODIFIED_DATE date);
alter table CM_CROSS_LISTING_T modify (CREATED_DATE date);
alter table CM_ENROLLMENT_SET_T modify (LAST_MODIFIED_DATE date);
alter table CM_ENROLLMENT_SET_T modify (CREATED_DATE date);
alter table CM_ENROLLMENT_T modify (LAST_MODIFIED_DATE date);
alter table CM_ENROLLMENT_T modify (CREATED_DATE date);
alter table CM_ENROLLMENT_T add unique (USER_ID, ENROLLMENT_SET);
alter table CM_MEETING_T drop column TIME_OF_DAY;
alter table CM_MEETING_T add (START_TIME date);
alter table CM_MEETING_T add (FINISH_TIME date);
alter table CM_MEETING_T add (MONDAY number(1,0));
alter table CM_MEETING_T add (TUESDAY number(1,0));
alter table CM_MEETING_T add (WEDNESDAY number(1,0));
alter table CM_MEETING_T add (THURSDAY number(1,0));
alter table CM_MEETING_T add (FRIDAY number(1,0));
alter table CM_MEETING_T add (SATURDAY number(1,0));
alter table CM_MEETING_T add (SUNDAY number(1,0));
alter table CM_MEMBERSHIP_T add (STATUS varchar2(255 char));
alter table CM_MEMBERSHIP_T add unique (USER_ID, MEMBER_CONTAINER_ID);
alter table CM_MEMBER_CONTAINER_T modify (LAST_MODIFIED_DATE date);
alter table CM_MEMBER_CONTAINER_T modify (CREATED_DATE date);
alter table CM_MEMBER_CONTAINER_T add (MAXSIZE number(10,0));
alter table CM_MEMBER_CONTAINER_T modify (START_DATE date);
alter table CM_MEMBER_CONTAINER_T modify (END_DATE date);
alter table CM_MEMBER_CONTAINER_T drop constraint FKD96A9BC6D0C1EF35;
alter table CM_MEMBER_CONTAINER_T drop constraint FKD96A9BC66DFDE2;
alter table CM_MEMBER_CONTAINER_T drop column EQUIV_CANON_COURSE_ID;
alter table CM_MEMBER_CONTAINER_T drop column EQUIV_COURSE_OFFERING_ID;
alter table CM_OFFICIAL_INSTRUCTORS_T add unique (ENROLLMENT_SET_ID, INSTRUCTOR_ID);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-7752
-- Add grade comments that were previously stored in Message Center table to the new gradebook table
-- --------------------------------------------------------------------------------------------------------------------------------------
INSERT INTO GB_COMMENT_T
(select GB_COMMENT_S.NEXTVAL, gb_grade_record_t.VERSION, gb_grade_record_t.GRADER_ID, gb_grade_record_t.STUDENT_ID, MFR_MESSAGE_T.GRADECOMMENT, gb_grade_record_t.DATE_RECORDED, GB_GRADABLE_OBJECT_T.ID
    from (select MAX(MFR_MESSAGE_T.MODIFIED) as MSG_MOD, MFR_MESSAGE_T.GRADEASSIGNMENTNAME as ASSGN_NAME, MFR_MESSAGE_T.CREATED_BY as CREATED_BY_STUDENT, MFR_AREA_T.CONTEXT_ID as CONTEXT from MFR_MESSAGE_T 
        join MFR_TOPIC_T on MFR_MESSAGE_T.surrogateKey = MFR_TOPIC_T.ID
    	join MFR_OPEN_FORUM_T on MFR_TOPIC_T.of_surrogateKey = MFR_OPEN_FORUM_T.ID
    	join MFR_AREA_T on MFR_OPEN_FORUM_T.surrogateKey = MFR_AREA_T.ID
    	where MFR_MESSAGE_T.GRADEASSIGNMENTNAME is not null and
            MFR_MESSAGE_T.GRADECOMMENT is not null
            group by MFR_MESSAGE_T.GRADEASSIGNMENTNAME, MFR_MESSAGE_T.CREATED_BY, MFR_AREA_T.CONTEXT_ID)
    join MFR_MESSAGE_T on (MFR_MESSAGE_T.MODIFIED = MSG_MOD and MFR_MESSAGE_T.GRADEASSIGNMENTNAME = ASSGN_NAME and MFR_MESSAGE_T.CREATED_BY = CREATED_BY_STUDENT)
    join GB_GRADEBOOK_T on CONTEXT = GB_GRADEBOOK_T.GRADEBOOK_UID
    join GB_GRADABLE_OBJECT_T on GB_GRADABLE_OBJECT_T.GRADEBOOK_ID = GB_GRADEBOOK_T.ID
    join GB_GRADE_RECORD_T on GB_GRADE_RECORD_T.STUDENT_ID = MFR_MESSAGE_T.CREATED_BY
    left join GB_COMMENT_T
        on (MFR_MESSAGE_T.CREATED_BY = GB_COMMENT_T.STUDENT_ID and GB_GRADABLE_OBJECT_T.ID = GB_COMMENT_T.GRADABLE_OBJECT_ID)
    where 
        GB_COMMENT_T.ID is null and  
        MFR_MESSAGE_T.GRADEASSIGNMENTNAME = GB_GRADABLE_OBJECT_T.NAME and
        MFR_MESSAGE_T.GRADECOMMENT is not null and
        GB_GRADE_RECORD_T.GRADABLE_OBJECT_ID = GB_GRADABLE_OBJECT_T.ID);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-8702 
-- New ScheduledInvocationManager API for jobscheduler
-- --------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE SCHEDULER_DELAYED_INVOCATION (
	INVOCATION_ID VARCHAR2(36) NOT NULL,
	INVOCATION_TIME TIMESTAMP NOT NULL,
	COMPONENT VARCHAR2(2000) NOT NULL,
	CONTEXT VARCHAR2(2000) NULL,
	PRIMARY KEY (INVOCATION_ID)
);

CREATE INDEX SCHEDULER_DI_TIME_INDEX ON SCHEDULER_DELAYED_INVOCATION (INVOCATION_TIME);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-7557
-- New Osp Reports Tables
-- --------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE osp_report_def_xml (
   reportDefId VARCHAR2(36 CHAR) NOT NULL,
   xmlFile CLOB NOT NULL,
   PRIMARY KEY  (reportDefId)
 );

 CREATE TABLE osp_report_xsl (
   	reportXslFileId VARCHAR2(36 CHAR) NOT NULL,
	reportXslFileRef VARCHAR2(255 CHAR),
	reportDefId VARCHAR2(36 CHAR),
	xslFile CLOB NOT NULL,
	PRIMARY KEY (reportXslFileId)
 );

 ALTER TABLE osp_report_xsl add CONSTRAINT FK25C0A259BE381194 FOREIGN KEY (reportDefId)	REFERENCES OSP_REPORT_DEF_XML (reportDefId);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-9029
-- Poll Tool Tables
-- --------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE POLL_POLL (
  POLL_ID number(20,0) NOT NULL,
  POLL_OWNER varchar2(255) NULL,
  POLL_SITE_ID varchar2(255) NULL,
  POLL_DETAILS varchar2(255) NULL,
  POLL_CREATION_DATE timestamp NULL,
  POLL_TEXT clob,
  POLL_VOTE_OPEN timestamp NULL,
  POLL_VOTE_CLOSE timestamp NULL,
  POLL_MIN_OPTIONS number(11,0) NULL,
  POLL_MAX_OPTIONS number(11,0) NULL,
  POLL_DISPLAY_RESULT varchar2(255) NULL,
  POLL_LIMIT_VOTE number(1,0) NULL,
  PRIMARY KEY  (POLL_ID)
);

CREATE SEQUENCE POLL_POLL_ID_SEQ;
CREATE INDEX POLL_POLL_SITE_ID_IDX ON POLL_POLL (POLL_SITE_ID);


CREATE TABLE POLL_OPTION (
  OPTION_ID number(20,0) NOT NULL,
  OPTION_POLL_ID number(20,0) NULL,
  OPTION_TEXT clob,
  PRIMARY KEY  (OPTION_ID)
);

CREATE SEQUENCE POLL_OPTION_ID_SEQ;
CREATE INDEX POLL_OPTION_POLL_ID_IDX ON POLL_OPTION (OPTION_POLL_ID);


CREATE TABLE POLL_VOTE (
  VOTE_ID number(20,0) NOT NULL,
  USER_ID varchar2(255) NULL,
  VOTE_IP varchar2(255) NULL,
  VOTE_DATE timestamp NULL,
  VOTE_POLL_ID number(20,0) NULL,
  VOTE_OPTION number(20,0) NULL,
  VOTE_SUBMISSION_ID varchar2(255) NULL,
  PRIMARY KEY  (VOTE_ID)
);

CREATE SEQUENCE POLL_VOTE_ID_SEQ;
CREATE INDEX POLL_VOTE_POLL_ID_IDX ON POLL_VOTE (VOTE_POLL_ID);
CREATE INDEX POLL_VOTE_USER_ID_IDX ON POLL_VOTE (USER_ID);


-- ---------------------------------------------------------------------------
-- SAK-8892 CONTENT_TYPE_REGISTRY
-- ---------------------------------------------------------------------------

CREATE TABLE CONTENT_TYPE_REGISTRY
(
    CONTEXT_ID VARCHAR (99) NOT NULL,
	RESOURCE_TYPE_ID VARCHAR (255),
    ENABLED VARCHAR (1)
);

CREATE INDEX content_type_registry_idx ON CONTENT_TYPE_REGISTRY 
(
	CONTEXT_ID
);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-9029 new mailtool permissions 
-- --------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'mailtool.admin');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'mailtool.send');


-- ADJUST ME: adjust theses for your needs for other roles than 'access', 'Student', 'maintain', 'Instructor' and 'Teaching Assistant'-- maintain role
-- maintain role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'),
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.admin'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'),
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.send'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.admin'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.send'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.admin'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.send'));

-- access role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.send'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.send'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.send'));

-- Instructor role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.admin'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.send'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.admin'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.send'));

-- Teaching Assistant role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.admin'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.send'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.admin'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.send'));

-- Student role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.send'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mailtool.send'));

-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new mailtool permissions into existing realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

-- These are for the site templates
-- ADJUST ME: adjust theses for your needs, either with different permissions,
-- or duplicate for other roles than 'access', 'Student', 'maintain', 'Instructor' and 'Teaching Assistant'

INSERT INTO PERMISSIONS_SRC_TEMP values ('access','mailtool.send');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','mailtool.send');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','mailtool.admin');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','mailtool.send');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','mailtool.admin');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','mailtool.send');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','mailtool.admin');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','mailtool.send');


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

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-8967 new chat permissions 
-- --------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'chat.delete.channel');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'chat.new.channel');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'chat.revise.channel');


-- ADJUST ME: adjust theses for your needs for other roles than 'access', 'Student', 'maintain', 'Instructor' and 'Teaching Assistant'-- maintain role
-- maintain role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'),
 (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'),
 (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
 (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'),
 (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
 (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.channel'));


INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new.channel'));


INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.channel'));


-- access role
-- no new permissions

-- Instructor role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.channel'));


-- Teaching Assistant role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new.channel'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.channel'));

-- Student role
-- no new permissions

-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new chat permissions into existing realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

-- These are for the site templates
-- ADJUST ME: adjust theses for your needs, either with different permissions,
-- or duplicate for other roles than 'access', 'Student', 'maintain', 'Instructor' and 'Teaching Assistant'

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','chat.delete.channel');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','chat.new.channel');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','chat.revise.channel');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','chat.delete.channel');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','chat.new.channel');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','chat.revise.channel');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','chat.delete.channel');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','chat.new.channel');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','chat.revise.channel');


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

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-9327 poll permissions 
-- --------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'poll.add');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'poll.deleteAny');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'poll.deleteOwn');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'poll.editAny');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'poll.editOwn');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'poll.vote');

-- ADJUST ME: adjust theses for your needs for other roles than 'access', 'Student', 'maintain', 'Instructor' and 'Teaching Assistant'-- maintain role
-- maintain role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'),
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.add'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'),
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.deleteAny'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'),
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.deleteOwn'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'),
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.editAny'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'),
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.editOwn'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'),
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.vote'));



INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.add'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.deleteAny'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.deleteOwn'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.editAny'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.editOwn'));



INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.vote'));



INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.add'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.deleteAny'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.deleteOwn'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.editAny'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.editOwn'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.vote'));

-- access role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.vote'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.vote'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.vote'));


-- Instructor role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.add'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.deleteAny'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.deleteOwn'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.editAny'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.editOwn'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.vote'));


INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.add'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.deleteAny'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.deleteOwn'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.editAny'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.editOwn'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.vote'));


-- Teaching Assistant role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.vote'));


INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.vote'));

-- Student role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.vote'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), 
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), 
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'poll.vote'));

-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new Poll permissions into existing realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

-- These are for the site templates
-- ADJUST ME: adjust theses for your needs, either with different permissions,
-- or duplicate for other roles than 'access', 'Student', 'maintain', 'Instructor' and 'Teaching Assistant'

INSERT INTO PERMISSIONS_SRC_TEMP values ('access','poll.vote');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','poll.vote');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','poll.add');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','poll.vote');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','poll.deleteAny');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','poll.deleteOwn');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','poll.editOwn');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','poll.editAny');


INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','poll.add');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','poll.vote');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','poll.deleteAny');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','poll.deleteOwn');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','poll.editOwn');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','poll.editAny');


INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','poll.vote');


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

-- ---------------------------------------------------------------------------
-- INITIALIZE TABLES FOR CITATIONS HELPER
-- ---------------------------------------------------------------------------

-- create CITATION_COLLECTION table
CREATE TABLE CITATION_COLLECTION ( COLLECTION_ID VARCHAR (36) NOT NULL, PROPERTY_NAME VARCHAR (255), PROPERTY_VALUE CLOB );

-- create CITATION_CITATION table
CREATE TABLE CITATION_CITATION ( CITATION_ID VARCHAR (36) NOT NULL, PROPERTY_NAME VARCHAR (255), PROPERTY_VALUE CLOB );

-- create CITATION_SCHEMA table
CREATE TABLE CITATION_SCHEMA ( SCHEMA_ID VARCHAR (36) NOT NULL, PROPERTY_NAME VARCHAR (255), PROPERTY_VALUE CLOB );

--  create CITATION_SCHEMA_FIELD table
CREATE TABLE CITATION_SCHEMA_FIELD ( SCHEMA_ID VARCHAR (36) NOT NULL, FIELD_ID VARCHAR (36) NOT NULL, PROPERTY_NAME VARCHAR (255), PROPERTY_VALUE CLOB );

-- provide default values for citation schemas 
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','creator','sakai:hasOrder','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','creator','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','creator','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','creator','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','creator','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','creator','sakai:ris_identifier','A1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','creator');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','title','sakai:hasOrder','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','title','sakai:required','true');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','title','sakai:minCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','title','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','title','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','title','sakai:ris_identifier','T1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','title');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','year','sakai:hasOrder','2');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','year','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','year','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','year','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','year','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','year');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','date','sakai:hasOrder','3');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','date','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','date','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','date','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','date','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','date','sakai:ris_identifier','Y1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','date');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','publisher','sakai:hasOrder','4');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','publisher','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','publisher','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','publisher','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','publisher','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','publisher','sakai:ris_identifier','PB');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','publisher');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','publicationLocation','sakai:hasOrder','5');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','publicationLocation','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','publicationLocation','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','publicationLocation','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','publicationLocation','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','publicationLocation','sakai:ris_identifier','CY');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','publicationLocation');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','volume','sakai:hasOrder','6');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','volume','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','volume','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','volume','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','volume','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','volume','sakai:ris_identifier','VL');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','volume');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','issue','sakai:hasOrder','7');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','issue','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','issue','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','issue','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','issue','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','issue','sakai:ris_identifier','IS');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','issue');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','pages','sakai:hasOrder','8');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','pages','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','pages','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','pages','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','pages','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','pages','sakai:ris_identifier','SP');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','pages');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','startPage','sakai:hasOrder','9');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','startPage','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','startPage','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','startPage','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','startPage','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','startPage','sakai:ris_identifier','SP');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','startPage');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','endPage','sakai:hasOrder','10');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','endPage','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','endPage','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','endPage','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','endPage','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','endPage','sakai:ris_identifier','EP');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','endPage');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','edition','sakai:hasOrder','11');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','edition','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','edition','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','edition','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','edition','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','edition','sakai:ris_identifier','VL');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','edition');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','editor','sakai:hasOrder','12');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','editor','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','editor','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','editor','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','editor','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','editor','sakai:ris_identifier','A3');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','editor');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sourceTitle','sakai:hasOrder','13');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sourceTitle','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sourceTitle','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sourceTitle','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sourceTitle','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sourceTitle','sakai:ris_identifier','T3');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','sourceTitle');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','Language','sakai:hasOrder','14');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','Language','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','Language','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','Language','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','Language','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','Language');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','abstract','sakai:hasOrder','15');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','abstract','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','abstract','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','abstract','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','abstract','sakai:valueType','longtext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','abstract','sakai:ris_identifier','N2');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','abstract');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','note','sakai:hasOrder','16');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','note','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','note','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','note','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','note','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','note','sakai:ris_identifier','N1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','note');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','isnIdentifier','sakai:hasOrder','17');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','isnIdentifier','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','isnIdentifier','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','isnIdentifier','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','isnIdentifier','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','isnIdentifier','sakai:ris_identifier','SN');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','isnIdentifier');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','subject','sakai:hasOrder','18');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','subject','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','subject','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','subject','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','subject','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','subject','sakai:ris_identifier','KW');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','subject');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','locIdentifier','sakai:hasOrder','19');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','locIdentifier','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','locIdentifier','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','locIdentifier','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','locIdentifier','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','locIdentifier','sakai:ris_identifier','M1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','locIdentifier');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','dateRetrieved','sakai:hasOrder','20');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','dateRetrieved','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','dateRetrieved','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','dateRetrieved','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','dateRetrieved','sakai:valueType','date');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','dateRetrieved');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','openURL','sakai:hasOrder','21');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','openURL','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','openURL','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','openURL','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','openURL','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','openURL');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','doi','sakai:hasOrder','22');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','doi','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','doi','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','doi','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','doi','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','doi');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','rights','sakai:hasOrder','23');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','rights','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','rights','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','rights','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','rights','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('unknown','sakai:hasField','rights');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','creator','sakai:hasOrder','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','creator','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','creator','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','creator','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','creator','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','creator','sakai:ris_identifier','A1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','creator');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','title','sakai:hasOrder','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','title','sakai:required','true');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','title','sakai:minCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','title','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','title','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','title','sakai:ris_identifier','T1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','title');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sourceTitle','sakai:hasOrder','2');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sourceTitle','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sourceTitle','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sourceTitle','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sourceTitle','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sourceTitle','sakai:ris_identifier','JF');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','sourceTitle');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','year','sakai:hasOrder','3');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','year','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','year','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','year','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','year','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','year');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','date','sakai:hasOrder','4');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','date','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','date','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','date','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','date','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','date','sakai:ris_identifier','Y1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','date');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','volume','sakai:hasOrder','5');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','volume','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','volume','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','volume','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','volume','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','volume','sakai:ris_identifier','VL');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','volume');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','issue','sakai:hasOrder','6');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','issue','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','issue','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','issue','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','issue','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','issue','sakai:ris_identifier','IS');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','issue');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','pages','sakai:hasOrder','7');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','pages','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','pages','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','pages','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','pages','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','pages');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','startPage','sakai:hasOrder','8');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','startPage','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','startPage','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','startPage','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','startPage','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','startPage','sakai:ris_identifier','SP');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','startPage');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','endPage','sakai:hasOrder','9');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','endPage','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','endPage','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','endPage','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','endPage','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','endPage','sakai:ris_identifier','EP');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','endPage');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','abstract','sakai:hasOrder','10');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','abstract','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','abstract','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','abstract','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','abstract','sakai:valueType','longtext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','abstract','sakai:ris_identifier','N2');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','abstract');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','note','sakai:hasOrder','11');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','note','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','note','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','note','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','note','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','note','sakai:ris_identifier','N1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','note');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','isnIdentifier','sakai:hasOrder','12');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','isnIdentifier','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','isnIdentifier','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','isnIdentifier','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','isnIdentifier','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','isnIdentifier','sakai:ris_identifier','SN');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','isnIdentifier');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','subject','sakai:hasOrder','13');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','subject','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','subject','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','subject','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','subject','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','subject','sakai:ris_identifier','KW');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','subject');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','Language','sakai:hasOrder','14');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','Language','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','Language','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','Language','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','Language','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','Language');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','locIdentifier','sakai:hasOrder','15');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','locIdentifier','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','locIdentifier','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','locIdentifier','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','locIdentifier','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','locIdentifier','sakai:ris_identifier','M1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','locIdentifier');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','dateRetrieved','sakai:hasOrder','16');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','dateRetrieved','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','dateRetrieved','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','dateRetrieved','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','dateRetrieved','sakai:valueType','date');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','dateRetrieved');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','openURL','sakai:hasOrder','17');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','openURL','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','openURL','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','openURL','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','openURL','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','openURL');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','doi','sakai:hasOrder','18');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','doi','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','doi','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','doi','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','doi','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','doi');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','rights','sakai:hasOrder','19');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','rights','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','rights','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','rights','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','rights','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('article','sakai:hasField','rights');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','creator','sakai:hasOrder','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','creator','sakai:required','true');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','creator','sakai:minCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','creator','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','creator','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','creator','sakai:ris_identifier','A1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','creator');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','title','sakai:hasOrder','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','title','sakai:required','true');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','title','sakai:minCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','title','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','title','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','title','sakai:ris_identifier','BT');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','title');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','year','sakai:hasOrder','2');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','year','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','year','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','year','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','year','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','year');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','date','sakai:hasOrder','3');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','date','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','date','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','date','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','date','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','date','sakai:ris_identifier','Y1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','date');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','publisher','sakai:hasOrder','4');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','publisher','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','publisher','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','publisher','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','publisher','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','publisher','sakai:ris_identifier','PB');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','publisher');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','publicationLocation','sakai:hasOrder','5');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','publicationLocation','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','publicationLocation','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','publicationLocation','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','publicationLocation','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','publicationLocation','sakai:ris_identifier','CY');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','publicationLocation');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','edition','sakai:hasOrder','6');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','edition','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','edition','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','edition','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','edition','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','edition','sakai:ris_identifier','VL');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','edition');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','editor','sakai:hasOrder','7');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','editor','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','editor','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','editor','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','editor','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','editor','sakai:ris_identifier','A3');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','editor');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sourceTitle','sakai:hasOrder','8');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sourceTitle','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sourceTitle','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sourceTitle','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sourceTitle','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sourceTitle','sakai:ris_identifier','T3');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','sourceTitle');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','abstract','sakai:hasOrder','9');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','abstract','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','abstract','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','abstract','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','abstract','sakai:valueType','longtext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','abstract','sakai:ris_identifier','N2');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','abstract');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','note','sakai:hasOrder','10');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','note','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','note','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','note','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','note','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','note','sakai:ris_identifier','N1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','note');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','isnIdentifier','sakai:hasOrder','11');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','isnIdentifier','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','isnIdentifier','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','isnIdentifier','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','isnIdentifier','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','isnIdentifier','sakai:ris_identifier','SN');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','isnIdentifier');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','subject','sakai:hasOrder','12');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','subject','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','subject','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','subject','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','subject','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','subject','sakai:ris_identifier','KW');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','subject');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','Language','sakai:hasOrder','13');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','Language','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','Language','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','Language','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','Language','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','Language');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','locIdentifier','sakai:hasOrder','14');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','locIdentifier','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','locIdentifier','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','locIdentifier','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','locIdentifier','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','locIdentifier','sakai:ris_identifier','M1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','locIdentifier');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','dateRetrieved','sakai:hasOrder','15');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','dateRetrieved','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','dateRetrieved','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','dateRetrieved','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','dateRetrieved','sakai:valueType','date');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','dateRetrieved');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','openURL','sakai:hasOrder','16');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','openURL','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','openURL','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','openURL','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','openURL','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','openURL');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','doi','sakai:hasOrder','17');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','doi','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','doi','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','doi','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','doi','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','doi');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','rights','sakai:hasOrder','18');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','rights','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','rights','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','rights','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','rights','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('book','sakai:hasField','rights');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','creator','sakai:hasOrder','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','creator','sakai:required','true');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','creator','sakai:minCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','creator','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','creator','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','creator','sakai:ris_identifier','A1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','creator');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','title','sakai:hasOrder','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','title','sakai:required','true');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','title','sakai:minCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','title','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','title','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','title','sakai:ris_identifier','CT');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','title');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','year','sakai:hasOrder','2');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','year','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','year','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','year','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','year','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','year');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','date','sakai:hasOrder','3');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','date','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','date','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','date','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','date','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','date','sakai:ris_identifier','Y1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','date');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','publisher','sakai:hasOrder','4');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','publisher','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','publisher','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','publisher','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','publisher','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','publisher','sakai:ris_identifier','PB');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','publisher');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','publicationLocation','sakai:hasOrder','5');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','publicationLocation','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','publicationLocation','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','publicationLocation','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','publicationLocation','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','publicationLocation','sakai:ris_identifier','CY');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','publicationLocation');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','edition','sakai:hasOrder','6');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','edition','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','edition','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','edition','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','edition','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','edition','sakai:ris_identifier','VL');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','edition');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','editor','sakai:hasOrder','7');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','editor','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','editor','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','editor','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','editor','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','editor','sakai:ris_identifier','ED');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','editor');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sourceTitle','sakai:hasOrder','8');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sourceTitle','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sourceTitle','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sourceTitle','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sourceTitle','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sourceTitle','sakai:ris_identifier','BT');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','sourceTitle');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','pages','sakai:hasOrder','9');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','pages','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','pages','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','pages','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','pages','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','pages','sakai:ris_identifier','SP');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','pages');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','startPage','sakai:hasOrder','10');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','startPage','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','startPage','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','startPage','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','startPage','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','startPage','sakai:ris_identifier','SP');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','startPage');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','endPage','sakai:hasOrder','11');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','endPage','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','endPage','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','endPage','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','endPage','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','endPage','sakai:ris_identifier','EP');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','endPage');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','abstract','sakai:hasOrder','12');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','abstract','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','abstract','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','abstract','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','abstract','sakai:valueType','longtext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','abstract','sakai:ris_identifier','N2');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','abstract');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','note','sakai:hasOrder','13');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','note','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','note','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','note','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','note','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','note','sakai:ris_identifier','N1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','note');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','isnIdentifier','sakai:hasOrder','14');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','isnIdentifier','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','isnIdentifier','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','isnIdentifier','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','isnIdentifier','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','isnIdentifier','sakai:ris_identifier','SN');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','isnIdentifier');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','subject','sakai:hasOrder','15');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','subject','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','subject','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','subject','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','subject','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','subject','sakai:ris_identifier','KW');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','subject');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','Language','sakai:hasOrder','16');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','Language','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','Language','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','Language','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','Language','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','Language');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','locIdentifier','sakai:hasOrder','17');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','locIdentifier','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','locIdentifier','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','locIdentifier','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','locIdentifier','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','locIdentifier','sakai:ris_identifier','M1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','locIdentifier');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','dateRetrieved','sakai:hasOrder','18');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','dateRetrieved','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','dateRetrieved','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','dateRetrieved','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','dateRetrieved','sakai:valueType','date');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','dateRetrieved');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','openURL','sakai:hasOrder','19');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','openURL','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','openURL','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','openURL','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','openURL','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','openURL');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','doi','sakai:hasOrder','20');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','doi','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','doi','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','doi','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','doi','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','doi');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','rights','sakai:hasOrder','21');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','rights','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','rights','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','rights','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','rights','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('chapter','sakai:hasField','rights');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','creator','sakai:hasOrder','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','creator','sakai:required','true');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','creator','sakai:minCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','creator','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','creator','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','creator','sakai:ris_identifier','A1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','creator');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','title','sakai:hasOrder','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','title','sakai:required','true');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','title','sakai:minCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','title','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','title','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','title','sakai:ris_identifier','T1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','title');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','year','sakai:hasOrder','2');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','year','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','year','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','year','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','year','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','year');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','date','sakai:hasOrder','3');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','date','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','date','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','date','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','date','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','date','sakai:ris_identifier','Y1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','date');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','publisher','sakai:hasOrder','4');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','publisher','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','publisher','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','publisher','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','publisher','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','publisher','sakai:ris_identifier','PB');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','publisher');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','publicationLocation','sakai:hasOrder','5');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','publicationLocation','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','publicationLocation','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','publicationLocation','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','publicationLocation','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','publicationLocation','sakai:ris_identifier','CY');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','publicationLocation');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','editor','sakai:hasOrder','6');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','editor','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','editor','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','editor','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','editor','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','editor','sakai:ris_identifier','A3');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','editor');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','edition','sakai:hasOrder','7');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','edition','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','edition','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','edition','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','edition','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','edition','sakai:ris_identifier','VL');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','edition');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sourceTitle','sakai:hasOrder','8');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sourceTitle','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sourceTitle','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sourceTitle','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sourceTitle','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sourceTitle','sakai:ris_identifier','T3');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','sourceTitle');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','pages','sakai:hasOrder','9');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','pages','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','pages','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','pages','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','pages','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','pages','sakai:ris_identifier','SP');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','pages');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','abstract','sakai:hasOrder','10');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','abstract','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','abstract','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','abstract','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','abstract','sakai:valueType','longtext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','abstract','sakai:ris_identifier','N2');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','abstract');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','isnIdentifier','sakai:hasOrder','11');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','isnIdentifier','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','isnIdentifier','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','isnIdentifier','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','isnIdentifier','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','isnIdentifier','sakai:ris_identifier','SN');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','isnIdentifier');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','note','sakai:hasOrder','12');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','note','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','note','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','note','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','note','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','note','sakai:ris_identifier','N1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','note');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','subject','sakai:hasOrder','13');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','subject','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','subject','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','subject','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','subject','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','subject','sakai:ris_identifier','KW');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','subject');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','Language','sakai:hasOrder','14');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','Language','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','Language','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','Language','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','Language','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','Language');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','locIdentifier','sakai:hasOrder','15');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','locIdentifier','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','locIdentifier','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','locIdentifier','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','locIdentifier','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','locIdentifier','sakai:ris_identifier','M1');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','locIdentifier');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','dateRetrieved','sakai:hasOrder','16');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','dateRetrieved','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','dateRetrieved','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','dateRetrieved','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','dateRetrieved','sakai:valueType','date');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','dateRetrieved');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','openURL','sakai:hasOrder','17');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','openURL','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','openURL','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','openURL','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','openURL','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','openURL');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','doi','sakai:hasOrder','18');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','doi','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','doi','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','doi','sakai:maxCardinality','1');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','doi','sakai:valueType','number');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','doi');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','rights','sakai:hasOrder','19');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','rights','sakai:required','false');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','rights','sakai:minCardinality','0');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','rights','sakai:maxCardinality','2147483647');
INSERT INTO CITATION_SCHEMA_FIELD (SCHEMA_ID, FIELD_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','rights','sakai:valueType','shorttext');
INSERT INTO CITATION_SCHEMA (SCHEMA_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES('report','sakai:hasField','rights');
-- ---------------------------------------------------------------------------

-- ----------------------------------------------------------------------
-- - SAK-9436 Missing indexes in rwiki 
-- ----------------------------------------------------------------------
-- - its ok to ignore the drop errors, 
drop index rwikiproperties_name;
drop index  irwikicurrentcontent_rwi;
drop index  irwikihistorycontent_rwi;
drop index  irwikipagepresence_sid;
drop index  irwikihistory_name;
drop index  irwikihistory_realm;
drop index  irwikihistory_ref;
drop index  irwikihistoryobj_rwid;
drop index  irwikiobject_name;
drop index  irwikiobject_realm;
drop index  irwikiobject_ref;

drop index  irwikipr_userid;
drop index  irwikipm_sessionid;
drop index  irwikipm_user;
drop index  irwikipm_pagespace;
drop index  irwikipm_pagename;
drop index  irwikipt_user;
drop index  irwikipt_pagespace;
drop index  irwikipt_pavename;

create index irwikiproperties_name on rwikiproperties (name);
create index irwikicurrentcontent_rwi on  rwikicurrentcontent (rwikiid);
create index irwikihistorycontent_rwi on  rwikihistorycontent (rwikiid); 
create index irwikipagepresence_sid on  rwikipagepresence (sessionid);
create index irwikihistory_name on  rwikihistory (name);
create index irwikihistory_realm on  rwikihistory (realm);
create index irwikihistory_ref on  rwikihistory (referenced);
create index irwikihistoryobj_rwid on  rwikihistory (rwikiobjectid);
create index irwikiobject_name on  rwikiobject (name);
create index irwikiobject_realm on  rwikiobject (realm);
create index irwikiobject_ref on  rwikiobject (referenced);

create index irwikipr_userid on  rwikipreference (userid);
create index irwikipm_sessionid on  rwikipagemessage (sessionid);
create index irwikipm_user on  rwikipagemessage (userid);
create index irwikipm_pagespace on  rwikipagemessage (pagespace);
create index irwikipm_pagename on  rwikipagemessage (pagename);
create index irwikipt_user on  rwikipagetrigger (userid);
create index irwikipt_pagespace on  rwikipagetrigger (pagespace);
create index irwikipt_pavename on  rwikipagetrigger (pagename);

-- ----------------------------------------------------------------------
-- SAK-9439 Missing indexes in search
-- ----------------------------------------------------------------------
drop index   isearchbuilderitem_name;
drop index   isearchbuilderitem_ctx;
drop index   isearchbuilderitem_act;
drop index   isearchbuilderitem_sta;
drop index   isearchwriterlock_lk;


create index isearchbuilderitem_name on  searchbuilderitem (name);
create index isearchbuilderitem_ctx on  searchbuilderitem (context);
create index isearchbuilderitem_act on  searchbuilderitem (searchaction);
create index isearchbuilderitem_sta on  searchbuilderitem (searchstate);
create index isearchwriterlock_lk on  searchwriterlock (lockkey);

