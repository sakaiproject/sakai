-- This is the MySQL Sakai 2.3.0 (or later) -> 2.4.0 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.3.0 to 2.4.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------

-- OSP conversion (note first 2 alters may be unnecessary for 2.3.x
alter table osp_presentation_template add column propertyFormType varchar(36);
alter table osp_presentation add column property_form varchar(36);
alter table osp_scaffolding add column preview tinyint not null DEFAULT '0';
alter table osp_wizard add column preview tinyint not null DEFAULT '0';
alter table osp_review add column review_item_id varchar(36);

update osp_list_config set selected_columns = replace(selected_columns, 'name', 'title') where binary selected_columns like '%name%';
update osp_list_config set selected_columns = replace(selected_columns, 'siteName', 'site.title') where binary selected_columns like '%siteName%';

-- Updating for a change to the synoptic view for portfolio worksites
update SAKAI_SITE_TOOL_PROPERTY set name='siteTypeList', value='portfolio,PortfolioAdmin' where value='portfolioWorksites';

-- making sure these fields allow nulls
ALTER TABLE osp_scaffolding MODIFY readyColor VARCHAR(7) NULL;
ALTER TABLE osp_scaffolding MODIFY pendingColor VARCHAR(7) NULL;
ALTER TABLE osp_scaffolding MODIFY completedColor VARCHAR(7) NULL;
ALTER TABLE osp_scaffolding MODIFY lockedColor VARCHAR(7) NULL;

-- --------------------------------------------------------------------------------------------------------------------------------------

-- --------------------------------------------------------------------------------------------------------------------------------------

-- SAMIGO conversion
-- SAK-6790 
alter table SAM_ASSESSMENTBASE_T MODIFY  CREATEDBY varchar(255) not null,  MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_SECTION_T MODIFY  CREATEDBY varchar(255) not null,  MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_PUBLISHEDASSESSMENT_T MODIFY  CREATEDBY varchar(255) not null,  MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_PUBLISHEDSECTION_T MODIFY  CREATEDBY varchar(255) not null,  MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_ITEM_T MODIFY  ITEMIDSTRING varchar(255), MODIFY  CREATEDBY varchar(255) not null,  MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_ITEMFEEDBACK_T MODIFY TYPEID varchar(255) not null;
alter table SAM_ANSWERFEEDBACK_T MODIFY TYPEID varchar(255);
alter table SAM_ATTACHMENT_T MODIFY CREATEDBY varchar(255) not null, MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_PUBLISHEDITEM_T MODIFY  ITEMIDSTRING varchar(255), MODIFY  CREATEDBY varchar(255) not null,  MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_PUBLISHEDITEMFEEDBACK_T MODIFY TYPEID varchar(255) not null;
alter table SAM_PUBLISHEDANSWERFEEDBACK_T MODIFY TYPEID varchar(255);
alter table SAM_PUBLISHEDATTACHMENT_T  MODIFY CREATEDBY varchar(255) not null, MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_AUTHZDATA_T MODIFY AGENTID varchar(255) not null, MODIFY FUNCTIONID varchar(36) not null, MODIFY QUALIFIERID varchar(36) not null;
alter table SAM_AUTHZDATA_T MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_ASSESSMENTGRADING_T MODIFY AGENTID varchar(255) not null, MODIFY GRADEDBY varchar(255);
alter table SAM_ITEMGRADING_T MODIFY AGENTID varchar(255) not null, MODIFY GRADEDBY varchar(255);
alter table SAM_GRADINGSUMMARY_T MODIFY AGENTID varchar(255) not null;
alter table SAM_MEDIA_T MODIFY CREATEDBY varchar(255), MODIFY LASTMODIFIEDBY varchar(255);
alter table SAM_TYPE_T MODIFY CREATEDBY varchar(255) not null, MODIFY LASTMODIFIEDBY varchar(255) not null;

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
drop table if exists SAM_STUDENTGRADINGSUMMARY_T;
create table SAM_STUDENTGRADINGSUMMARY_T (
STUDENTGRADINGSUMMARYID bigint not null auto_increment,
PUBLISHEDASSESSMENTID bigint not null,
AGENTID varchar(255) not null,
NUMBERRETAKE integer,
CREATEDBY varchar(255) not null,
CREATEDDATE datetime not null,
LASTMODIFIEDBY varchar(255) not null,
LASTMODIFIEDDATE datetime not null,
primary key (STUDENTGRADINGSUMMARYID)
);
create index SAM_PUBLISHEDASSESSMENT2_I on SAM_STUDENTGRADINGSUMMARY_T (PUBLISHEDASSESSMENTID);

-- SAK-10194
insert into SAM_SECTIONMETADATA_T (SECTIONID, LABEL, ENTRY) (
select SECTIONID,'POOLNAME_FOR_RANDOM_DRAW',(select TITLE from SAM_QUESTIONPOOL_T where QUESTIONPOOLID=a.ENTRY)
from SAM_SECTIONMETADATA_T a where a.LABEL='POOLID_FOR_RANDOM_DRAW'
and not exists (select null from SAM_SECTIONMETADATA_T b where a.SECTIONID=b.SECTIONID and LABEL='POOLNAME_FOR_RANDOM_DRAW')
);
-- --------------------------------------------------------------------------------------------------------------------------------------
-- add new default roster permissions
-- --------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'roster.viewall');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'roster.viewofficialid');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'roster.viewhidden');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'roster.viewsection');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'roster.export');

-- ADJUST ME: adjust theses for your needs for other roles than 'access', 'Student', 'maintain', 'Instructor' and 'Teaching Assistant'

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
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;

-- --------------------------------------------------------------------------------------------------------------------------------------
-- Site related tables changes needed for 2.4.0 (SAK-7341)
-- --------------------------------------------------------------------------------------------------------------------------------------
ALTER TABLE SAKAI_SITE ADD (CUSTOM_PAGE_ORDERED CHAR(1) DEFAULT '0' CHECK (CUSTOM_PAGE_ORDERED IN (1, 0)));

-- --------------------------------------------------------------------------------------------------------------------------------------
-- Post'em table changes needed for 2.4.0 
-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-8232
ALTER TABLE SAKAI_POSTEM_STUDENT_GRADES MODIFY grade VARCHAR(2000);

-- SAK-6948
ALTER TABLE SAKAI_POSTEM_GRADEBOOK MODIFY title VARCHAR(255) not null;

-- SAK-8213
ALTER TABLE SAKAI_REALM_ROLE_DESC ADD (PROVIDER_ONLY CHAR(1) NULL);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- Add Moderator functionality to Message Center (SAK-8632)
-- --------------------------------------------------------------------------------------------------------------------------------------
-- add column to allow Moderated as template setting
alter table MFR_AREA_T add column (MODERATED bit);
update MFR_AREA_T set MODERATED=0 where MODERATED is NULL;
alter table MFR_AREA_T modify column MODERATED bit not null;

-- change APPROVED column to allow null values to represent pending approvals
alter table MFR_MESSAGE_T modify APPROVED bit null;

-- change MODERATED column in MFR_OPEN_FORUM_T to not null
update MFR_OPEN_FORUM_T set MODERATED=0 where MODERATED is NULL;
alter table MFR_OPEN_FORUM_T modify column MODERATED bit not null;

-- change MODERATED column in MFR_TOPIC_T to not null
update MFR_TOPIC_T set MODERATED=0 where MODERATED is NULL;
alter table MFR_TOPIC_T modify column MODERATED bit not null;


-- --------------------------------------------------------------------------------------------------------------------------------------
-- New Chat storage and permissions (SAK-8508)
-- --------------------------------------------------------------------------------------------------------------------------------------
-- create new tables
CREATE TABLE CHAT2_CHANNEL ( 
    CHANNEL_ID           	varchar(99) NOT NULL,
    CONTEXT              	varchar(36) NOT NULL,
    CREATION_DATE        	datetime NULL,
    title                	varchar(64) NULL,
    description          	varchar(255) NULL,
    filterType           	varchar(25) NULL,
    filterParam          	int(11) NULL,
    contextDefaultChannel	tinyint(1) NULL,
    ENABLE_USER_OVERRIDE	tinyint(1) NULL,
    PRIMARY KEY(CHANNEL_ID)
);

CREATE INDEX CHAT2_CHNL_CNTXT_I ON CHAT2_CHANNEL
(
       CONTEXT
);

CREATE INDEX CHAT2_CHNL_CNTXT_DFLT_I ON CHAT2_CHANNEL
(
       CONTEXT,
       contextDefaultChannel
);

CREATE TABLE CHAT2_MESSAGE ( 
    MESSAGE_ID  	varchar(99) NOT NULL,
    CHANNEL_ID  	varchar(99) NULL,
    OWNER       	varchar(96) NOT NULL,
    MESSAGE_DATE	datetime NULL,
    BODY        	text NOT NULL,
    PRIMARY KEY(MESSAGE_ID)
);
CREATE INDEX FK720F9882555E0B79
    ON CHAT2_MESSAGE(CHANNEL_ID);
    
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
alter table CHAT2_CHANNEL add column migratedChannelId varchar(99);
alter table CHAT2_MESSAGE add column migratedMessageId varchar(99);

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
	ID bigint not null auto_increment,
	VERSION integer not null,
	GRADER_ID varchar(255) not null,
	STUDENT_ID varchar(255) not null,
	COMMENT_TEXT text,
	DATE_RECORDED datetime not null,
	GRADABLE_OBJECT_ID bigint not null,
	primary key (ID),
	unique (STUDENT_ID, GRADABLE_OBJECT_ID));
alter table GB_COMMENT_T 
	add index FK7977DFF06F98CFF (GRADABLE_OBJECT_ID), 
	add constraint FK7977DFF06F98CFF foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T (ID);

-- Remove database-caching of calculated course grades.
alter table GB_GRADE_RECORD_T drop column SORT_GRADE;


-- --------------------------------------------------------------------------------------------------------------------------------------
-- CourseManagement Reference Impl table changes needed for 2.4.0
-- --------------------------------------------------------------------------------------------------------------------------------------
create table CM_SEC_CATEGORY_T (CAT_CODE varchar(255) not null, CAT_DESCR varchar(255), primary key (CAT_CODE));
create index CM_ENR_USER on CM_ENROLLMENT_T (USER_ID);
create index CM_MBR_CTR on CM_MEMBERSHIP_T (MEMBER_CONTAINER_ID);
create index CM_MBR_USER on CM_MEMBERSHIP_T (USER_ID);
create index CM_INSTR_IDX on CM_OFFICIAL_INSTRUCTORS_T (INSTRUCTOR_ID);
alter table CM_ACADEMIC_SESSION_T change column LAST_MODIFIED_DATE LAST_MODIFIED_DATE date;
alter table CM_ACADEMIC_SESSION_T change column CREATED_DATE CREATED_DATE date;
alter table CM_ACADEMIC_SESSION_T change column START_DATE START_DATE date;
alter table CM_ACADEMIC_SESSION_T change column END_DATE END_DATE date;
alter table CM_CROSS_LISTING_T change column LAST_MODIFIED_DATE LAST_MODIFIED_DATE date;
alter table CM_CROSS_LISTING_T change column CREATED_DATE CREATED_DATE date;
alter table CM_ENROLLMENT_SET_T change column LAST_MODIFIED_DATE LAST_MODIFIED_DATE date;
alter table CM_ENROLLMENT_SET_T change column CREATED_DATE CREATED_DATE date;
alter table CM_ENROLLMENT_T change column LAST_MODIFIED_DATE LAST_MODIFIED_DATE date;
alter table CM_ENROLLMENT_T change column CREATED_DATE CREATED_DATE date;
alter table CM_ENROLLMENT_T add unique (USER_ID, ENROLLMENT_SET);
alter table CM_MEETING_T drop column TIME_OF_DAY;
alter table CM_MEETING_T add column START_TIME time;
alter table CM_MEETING_T add column FINISH_TIME time;
alter table CM_MEETING_T add column MONDAY bit;
alter table CM_MEETING_T add column TUESDAY bit;
alter table CM_MEETING_T add column WEDNESDAY bit;
alter table CM_MEETING_T add column THURSDAY bit;
alter table CM_MEETING_T add column FRIDAY bit;
alter table CM_MEETING_T add column SATURDAY bit;
alter table CM_MEETING_T add column SUNDAY bit;
alter table CM_MEMBERSHIP_T add column STATUS varchar(255);
alter table CM_MEMBERSHIP_T add unique (USER_ID, MEMBER_CONTAINER_ID);
alter table CM_MEMBER_CONTAINER_T change column LAST_MODIFIED_DATE LAST_MODIFIED_DATE date;
alter table CM_MEMBER_CONTAINER_T change column CREATED_DATE CREATED_DATE date;
alter table CM_MEMBER_CONTAINER_T add column MAXSIZE integer;
alter table CM_MEMBER_CONTAINER_T change column START_DATE START_DATE date;
alter table CM_MEMBER_CONTAINER_T change column END_DATE END_DATE date;
alter table CM_MEMBER_CONTAINER_T drop FOREIGN KEY FKD96A9BC6D0C1EF35;
alter table CM_MEMBER_CONTAINER_T drop FOREIGN KEY FKD96A9BC66DFDE2;
alter table CM_MEMBER_CONTAINER_T drop column EQUIV_CANON_COURSE_ID;
alter table CM_MEMBER_CONTAINER_T drop column EQUIV_COURSE_OFFERING_ID;
alter table CM_OFFICIAL_INSTRUCTORS_T add unique (ENROLLMENT_SET_ID, INSTRUCTOR_ID);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-7752
-- Add grade comments that were previously stored in Message Center table to the new gradebook table
-- --------------------------------------------------------------------------------------------------------------------------------------
INSERT INTO GB_COMMENT_T (VERSION, GRADER_ID, STUDENT_ID, COMMENT_TEXT, DATE_RECORDED, GRADABLE_OBJECT_ID)
(select GB_GRADE_RECORD_T.VERSION, GB_GRADE_RECORD_T.GRADER_ID, GB_GRADE_RECORD_T.STUDENT_ID, MFR_MESSAGE_T.GRADECOMMENT, GB_GRADE_RECORD_T.DATE_RECORDED, GB_GRADABLE_OBJECT_T.ID
    from (select MAX(MFR_MESSAGE_T.MODIFIED) as MSG_MOD, MFR_MESSAGE_T.GRADEASSIGNMENTNAME as ASSGN_NAME, MFR_MESSAGE_T.CREATED_BY as CREATED_BY_STUDENT, MFR_AREA_T.CONTEXT_ID as CONTEXT from MFR_MESSAGE_T 
    	join MFR_TOPIC_T on MFR_MESSAGE_T.surrogateKey = MFR_TOPIC_T.ID
    	join MFR_OPEN_FORUM_T on MFR_TOPIC_T.of_surrogateKey = MFR_OPEN_FORUM_T.ID
    	join MFR_AREA_T on MFR_OPEN_FORUM_T.surrogateKey = MFR_AREA_T.ID
    	where MFR_MESSAGE_T.GRADEASSIGNMENTNAME is not null and
            MFR_MESSAGE_T.GRADECOMMENT is not null
            group by MFR_MESSAGE_T.GRADEASSIGNMENTNAME, MFR_MESSAGE_T.CREATED_BY, MFR_AREA_T.CONTEXT_ID) as FILTERED_MSGS
    join MFR_MESSAGE_T on (MFR_MESSAGE_T.MODIFIED = MSG_MOD and MFR_MESSAGE_T.GRADEASSIGNMENTNAME = FILTERED_MSGS.ASSGN_NAME and MFR_MESSAGE_T.CREATED_BY = FILTERED_MSGS.CREATED_BY_STUDENT)
    join GB_GRADEBOOK_T on FILTERED_MSGS.CONTEXT = GB_GRADEBOOK_T.GRADEBOOK_UID
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
	INVOCATION_ID VARCHAR(36) NOT NULL,
	INVOCATION_TIME DATETIME NOT NULL,
	COMPONENT VARCHAR(2000) NOT NULL,
	CONTEXT VARCHAR(2000) NULL,
	PRIMARY KEY (INVOCATION_ID)
);

CREATE INDEX SCHEDULER_DI_TIME_INDEX ON SCHEDULER_DELAYED_INVOCATION (INVOCATION_TIME);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-7557
-- New Osp Reports Tables
-- --------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE osp_report_def_xml (
  reportDefId varchar(36) NOT NULL,
  xmlFile longblob NOT NULL,
  PRIMARY KEY  (reportDefId)
);

CREATE TABLE osp_report_xsl (
  reportXslFileId varchar(36) NOT NULL default '',
  reportXslFileRef varchar(255) default NULL,
  reportDefId varchar(36) default NULL,
  xslFile longblob NOT NULL,
  PRIMARY KEY  (reportXslFileId),
  KEY FK25C0A259BE381194 (reportDefId),
  CONSTRAINT FK25C0A259BE381194 FOREIGN KEY (reportDefId) REFERENCES osp_report_def_xml (reportDefId)
);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- SAK-9029
-- Poll Tool Tables
-- --------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE `POLL_POLL` (
  `POLL_ID` bigint(20) NOT NULL auto_increment,
  `POLL_OWNER` varchar(255) default NULL,
  `POLL_SITE_ID` varchar(255) default NULL,
  `POLL_DETAILS` varchar(255) default NULL,
  `POLL_CREATION_DATE` datetime default NULL,
  `POLL_TEXT` text,
  `POLL_VOTE_OPEN` datetime default NULL,
  `POLL_VOTE_CLOSE` datetime default NULL,
  `POLL_MIN_OPTIONS` int(11) default NULL,
  `POLL_MAX_OPTIONS` int(11) default NULL,
  `POLL_DISPLAY_RESULT` varchar(255) default NULL,
  `POLL_LIMIT_VOTE` tinyint(1) default NULL,
  PRIMARY KEY  (`POLL_ID`),
  KEY `POLL_POLL_SITE_ID_IDX` (`POLL_SITE_ID`)
);


CREATE TABLE `POLL_OPTION` (
  `OPTION_ID` bigint(20) NOT NULL auto_increment,
  `OPTION_POLL_ID` bigint(20) default NULL,
  `OPTION_TEXT` text,
  PRIMARY KEY  (`OPTION_ID`),
  KEY `POLL_OPTION_POLL_ID_IDX` (`OPTION_POLL_ID`)
);

CREATE TABLE `POLL_VOTE` (
  `VOTE_ID` bigint(20) NOT NULL auto_increment,
  `USER_ID` varchar(255) default NULL,
  `VOTE_IP` varchar(255) default NULL,
  `VOTE_DATE` datetime default NULL,
  `VOTE_POLL_ID` bigint(20) default NULL,
  `VOTE_OPTION` bigint(20) default NULL,
  `VOTE_SUBMISSION_ID` varchar(255) default NULL,
  PRIMARY KEY  (`VOTE_ID`),
  KEY `POLL_VOTE_POLL_ID_IDX` (`VOTE_POLL_ID`),
  KEY `POLL_VOTE_USER_ID_IDX` (`USER_ID`)
);
 
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

INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'mailtool.admin');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'mailtool.send');


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

INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'chat.delete.channel');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'chat.new.channel');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'chat.revise.channel');


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

INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'poll.add');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'poll.deleteAny');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'poll.deleteOwn');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'poll.editAny');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'poll.editOwn');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'poll.vote');

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
CREATE TABLE CITATION_COLLECTION ( COLLECTION_ID VARCHAR (36) NOT NULL, PROPERTY_NAME VARCHAR (255), PROPERTY_VALUE LONGTEXT );

-- create CITATION_CITATION table
CREATE TABLE CITATION_CITATION ( CITATION_ID VARCHAR (36) NOT NULL, PROPERTY_NAME VARCHAR (255), PROPERTY_VALUE LONGTEXT );

-- create CITATION_SCHEMA table
CREATE TABLE CITATION_SCHEMA ( SCHEMA_ID VARCHAR (36) NOT NULL, PROPERTY_NAME VARCHAR (255), PROPERTY_VALUE LONGTEXT );

--  create CITATION_SCHEMA_FIELD table
CREATE TABLE CITATION_SCHEMA_FIELD ( SCHEMA_ID VARCHAR (36) NOT NULL, FIELD_ID VARCHAR (36) NOT NULL, PROPERTY_NAME VARCHAR (255), PROPERTY_VALUE LONGTEXT );

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

-- ---------------------------------------------------------------------------
-- SAK-9398 -- Larger field to prevent Data truncation in CONTENT column
-- ---------------------------------------------------------------------------
ALTER TABLE GB_SPREADSHEET_T MODIFY COLUMN CONTENT MEDIUMTEXT; 



-- ----------------------------------------------------------------------
-- - SAK-9436 Missing indexes in rwiki 
-- ----------------------------------------------------------------------
-- - its ok to ignore the drop errors, 
alter table rwikiproperties drop index irwikiproperties_name;
alter table rwikicurrentcontent drop index irwikicurrentcontent_rwi;
alter table rwikihistorycontent drop index irwikihistorycontent_rwi;
alter table rwikipagepresence drop index irwikipagepresence_sid;
alter table rwikihistory drop index irwikihistory_name;
alter table rwikihistory drop index irwikihistory_realm;
alter table rwikihistory drop index irwikihistory_ref;
alter table rwikihistory drop index irwikihistoryobj_rwid;
alter table rwikiobject drop index irwikiobject_name;
alter table rwikiobject drop index irwikiobject_realm;
alter table rwikiobject drop index irwikiobject_ref;

alter table rwikipreference drop index irwikipr_userid;
alter table rwikipagemessage drop index irwikipm_sessionid;
alter table rwikipagemessage drop index irwikipm_user;
alter table rwikipagemessage drop index irwikipm_pagespace;
alter table rwikipagemessage drop index irwikipm_pagename;
alter table rwikipagetrigger drop index irwikipt_user;
alter table rwikipagetrigger drop index irwikipt_pagespace;
alter table rwikipagetrigger drop index irwikipt_pavename;

alter table rwikiproperties add index irwikiproperties_name (name);
alter table rwikicurrentcontent add index irwikicurrentcontent_rwi (rwikiid);
alter table rwikihistorycontent add index irwikihistorycontent_rwi (rwikiid); 
alter table rwikipagepresence add index irwikipagepresence_sid (sessionid);
alter table rwikihistory add index irwikihistory_name (name);
alter table rwikihistory add index irwikihistory_realm (realm);
alter table rwikihistory add index irwikihistory_ref (referenced(255));
alter table rwikihistory add index irwikihistoryobj_rwid (rwikiobjectid);
alter table rwikiobject add index irwikiobject_name (name);
alter table rwikiobject add index irwikiobject_realm (realm);
alter table rwikiobject add index irwikiobject_ref (referenced(255));

alter table rwikipreference add index irwikipr_userid (userid);
alter table rwikipagemessage add index irwikipm_sessionid (sessionid);
alter table rwikipagemessage add index irwikipm_user (userid);
alter table rwikipagemessage add index irwikipm_pagespace (pagespace);
alter table rwikipagemessage add index irwikipm_pagename (pagename);
alter table rwikipagetrigger add index irwikipt_user (userid);
alter table rwikipagetrigger add index irwikipt_pagespace (pagespace);
alter table rwikipagetrigger add index irwikipt_pavename (pagename);

-- ----------------------------------------------------------------------
-- SAK-9439 Missing indexes in search
-- ----------------------------------------------------------------------
alter table searchbuilderitem drop index isearchbuilderitem_name;
alter table searchbuilderitem drop index isearchbuilderitem_ctx;
alter table searchbuilderitem drop index isearchbuilderitem_act;
alter table searchbuilderitem drop index isearchbuilderitem_sta;
alter table searchwriterlock drop index isearchwriterlock_lk;


alter table searchbuilderitem add index isearchbuilderitem_name (name);
alter table searchbuilderitem add index isearchbuilderitem_ctx (context);
alter table searchbuilderitem add index isearchbuilderitem_act (searchaction);
alter table searchbuilderitem add index isearchbuilderitem_sta (searchstate);
alter table searchwriterlock add index isearchwriterlock_lk (lockkey);

-- ----------------------------------------------------------------------
-- SAK-8447 Increase syllabus date column size in mysql
-- ----------------------------------------------------------------------
ALTER TABLE SAKAI_SYLLABUS_DATA MODIFY COLUMN asset MEDIUMTEXT; 
