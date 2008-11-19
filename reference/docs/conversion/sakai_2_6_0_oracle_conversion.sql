-- This is the Oracle Sakai 2.5.3 -> 2.6.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.5.3 to 2.6.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- The 2.5.0 - 2.5.2 script can be located at https://source.sakaiproject.org/svn/reference/tags/sakai_2-5-2/docs/conversion/sakai_2_5_0-2_5_2_oracle_conversion.sql
-- * Note that there was not a 2.5.1 release due to critical issue identified just prior to release 
-- The 2.5.2 - 2.5.3 script can be located at https://source.sakaiproject.org/svn/reference/tags/sakai_2-5-3/docs/conversion/sakai_2_5_2-2_5_3_oracle_conversion.sql
--
----------------------------------------------------------------------------------------------------------------------------------------

--SAK-12527 Changes to Chat Room options do not work consistently

-- add column timeParam and numberParam 
alter table CHAT2_CHANNEL add timeParam int;
alter table CHAT2_CHANNEL add numberParam int;

UPDATE CHAT2_CHANNEL
SET numberParam = Case When filterParam = 0 or filterType <> 'SelectByNumber' Then 10 Else filterParam End,
timeParam = Case When filterparam = 0 or filterType <> 'SelectMessagesByTime' Then 3 Else filterParam End;

alter table CHAT2_CHANNEL modify (timeParam int not null);
alter table CHAT2_CHANNEL modify (numberParam int not null);

--SAK-12176 Messages-Send cc to recipients' email address(es)

-- add column sendEmailOut to table MFR_AREA_T
alter table MFR_AREA_T add (SENDEMAILOUT NUMBER(1,0));
update MFR_AREA_T set SENDEMAILOUT=1 where SENDEMAILOUT is NULL;
alter table MFR_AREA_T modify (SENDEMAILOUT NUMBER(1,0) not null);

--new msg.emailout permission 

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'msg.emailout');

-- maintain role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.emailout'));

-- Instructor role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.emailout'));

--CIG Coordinator role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.emailout'));

--Program Coordinator role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.emailout'));

--Program Admin role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.emailout'));
----------------------------------------------------------------------------------------------------------------------------------------
-- backfill new msg.emailout permissions into existing realms
----------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR2(99), FUNCTION_NAME VARCHAR2(99));

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.emailout');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.emailout');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.emailout');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Program Coordinator','msg.emailout');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Program Admin','msg.emailout');

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

-- OSP SAK-12016
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.all.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.all.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'));

-- SAK-12777 Create unique constraint on CALENDAR_EVENT EVENT_ID
CREATE UNIQUE INDEX EVENT_INDEX ON CALENDAR_EVENT
(
       EVENT_ID
);

-- SAK-10139 permissions to allow Evaluation tool in My Workspace 

INSERT INTO SAKAI_REALM_RL_FN SELECT DISTINCT SR.REALM_KEY, SRR.ROLE_KEY, SRF.FUNCTION_KEY  from SAKAI_REALM SR, SAKAI_REALM_ROLE SRR, SAKAI_REALM_FUNCTION SRF where SR.REALM_ID like '/site/~%' AND SRR.ROLE_NAME = 'maintain' AND SRF.FUNCTION_NAME ='osp.matrix.evaluate';

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.evaluate'));

--SAK-13406 matrix feedback options

alter table osp_scaffolding add generalFeedbackOption number(10,0) DEFAULT '0' NOT NULL;
alter table osp_scaffolding add itemFeedbackOption number(10,0) DEFAULT '0' NOT NULL;
update osp_scaffolding set generalFeedbackOption=0;
update osp_scaffolding set itemFeedbackOption=0;

alter table osp_wizard add generalFeedbackOption number(10,0)  DEFAULT '0' NOT NULL;
alter table osp_wizard add itemFeedbackOption number(10,0) DEFAULT '0' NOT NULL;
update osp_wizard set generalFeedbackOption=0;
update osp_wizard set itemFeedbackOption=0;


-- SAK-13345
create index ISEARCHBUILDERITEM_STA_ACT on searchbuilderitem (SEARCHSTATE,SEARCHACTION); 
drop index ISEARCHBUILDERITEM_STA; 


--OSP SAK-11545
alter table osp_wizard add reviewerGroupAccess number(10, 0) default '0' not null;

--SAK-6216 Optional ability to store client hostname (resolved IP) in SAKAI_SESSION
alter table SAKAI_SESSION add SESSION_HOSTNAME varchar2(255) NULL;

--SAK-10801 Add CONTEXT field to SAKAI_EVENT
alter table SAKAI_EVENT add CONTEXT varchar2(255) NULL;

--SAK-13310 Poll description field too small
alter table POLL_POLL modify POLL_DETAILS VARCHAR2(4000);

-- SAK-14106
alter table SAM_ITEM_T add DISCOUNT float NULL;
alter table SAM_ANSWER_T add DISCOUNT float NULL;
alter table SAM_PUBLISHEDITEM_T add DISCOUNT float NULL;
alter table SAM_PUBLISHEDANSWER_T add DISCOUNT float NULL;

-- SAK-11130 Localization breaks default folders in Messages because of internationalization bug
update MFR_TOPIC_T
set TITLE='pvt_received'
where TITLE in (
	'Received' /* en */, '\u0645\u0633\u062a\u0644\u0645' /* ar */,
	'Rebut' /* ca */, 'Recibidos' /* es */, 'Re\u00e7u' /* fr_CA */,
	'\u53d7\u4fe1\u3057\u307e\u3057\u305f' /* ja */, 'Ontvangen' /* nl */,
	'Recebidas' /* pt_BR */, 'Recebidas' /* pt_PT */,
	'Mottagna' /* sv */) and
    TYPE_UUID = (select uuid from CMN_TYPE_T where KEYWORD = 'privateForums');

update MFR_TOPIC_T
set TITLE='pvt_sent'
where TITLE in (
	'Sent' /* en */, '\u0623\u0631\u0633\u0644' /* ar */,
	'Enviat' /* ca */, 'Enviados' /* es */, 'Envoy\u00e9' /* fr_CA */,
	'\u9001\u4fe1\u3057\u307e\u3057\u305f' /* ja */, 'Verzonden' /* nl */,
	'Enviadas' /* pt_BR  */, 'Enviada' /* pt_PT  */,
	'Skickade' /* sv */) and
    TYPE_UUID = (select uuid from CMN_TYPE_T where KEYWORD = 'privateForums');

update MFR_TOPIC_T
set TITLE='pvt_deleted'
where TITLE in (
	'Deleted' /* en */, '\u062d\u0630\u0641' /* ar */,
	'Suprimit' /* ca */, 'Borrados' /* es */, 'Supprim\u00e9' /* fr_CA */,
	'\u524a\u9664\u3057\u307e\u3057\u305f' /* ja */, 'Verwijderd' /* nl */,
	'Apagadas' /* pt_BR */, 'Eliminadas' /* pt_PT */,
	'Borttagna' /* sv */) and
    TYPE_UUID = (select uuid from CMN_TYPE_T where KEYWORD = 'privateForums');

update MFR_TOPIC_T
set TITLE='pvt_drafts'
where TITLE in (
	'Drafts' /*en */, '\u0645\u0634\u0631\u0648\u0639' /* ar */,
	'Esborrany' /* ca */, 'Preliminar' /* es */, 'Brouillon' /* fr_CA */,
	'\u4e0b\u66f8\u304d' /* ja */, 'Concept' /* nl */,
	'Rascunho' /* pt_BR */, 'Rascunho' /* pt_PT */,
	'Utkast' /* sv */) and
    TYPE_UUID = (select uuid from CMN_TYPE_T where KEYWORD = 'privateForums');

-- SAK-14291
create index SYLLABUS_ATTACH_ID_I on SAKAI_SYLLABUS_ATTACH (syllabusId);
create index SYLLABUS_DATA_SURRO_I on SAKAI_SYLLABUS_DATA (surrogateKey);

-- Samigo
-- SAK-8432
create index SAM_AG_AGENTID_I on SAM_ASSESSMENTGRADING_T (AGENTID);
--SAK-14430
ALTER TABLE SAM_ASSESSACCESSCONTROL_T ADD MARKFORREVIEW number(1,0) NULL;
ALTER TABLE SAM_PUBLISHEDACCESSCONTROL_T ADD MARKFORREVIEW number(1,0) NULL;
-- SAK-14472
INSERT INTO SAM_TYPE_T ("TYPEID" ,"AUTHORITY", "DOMAIN", "KEYWORD", "DESCRIPTION", "STATUS", "CREATEDBY", "CREATEDDATE", "LASTMODIFIEDBY", "LASTMODIFIEDDATE")
    VALUES (12 , 'stanford.edu', 'assessment.item', 'Multiple Correct Single Selection', NULL, 1, 1, SYSDATE, 1, SYSDATE);
-- SAK-14474
update sam_assessaccesscontrol_t set autosubmit = 0;
update sam_publishedaccesscontrol_t set autosubmit = 0;
alter table SAM_ASSESSMENTGRADING_T add ISAUTOSUBMITTED number(1, 0) default '0' null;
-- SAK-14430 
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY) VALUES
	(sam_assessMetaData_id_s.nextVal, 1, 'markForReview_isInstructorEditable', 'true');
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY) VALUES
	(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment' AND TYPEID='142' AND ISTEMPLATE=1), 'markForReview_isInstructorEditable', 'true');
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY) VALUES
	(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz' AND TYPEID='142' AND ISTEMPLATE=1), 'markForReview_isInstructorEditable', 'true');
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY) VALUES
	(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set' AND TYPEID='142' AND ISTEMPLATE=1), 'markForReview_isInstructorEditable', 'true');
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY) VALUES
	(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test' AND TYPEID='142' AND ISTEMPLATE=1), 'markForReview_isInstructorEditable', 'true');
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY) VALUES
	(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test' AND TYPEID='142' AND ISTEMPLATE=1), 'markForReview_isInstructorEditable', 'true');
update SAM_ASSESSACCESSCONTROL_T set MARKFORREVIEW = 1 where ASSESSMENTID = (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment' AND TYPEID='142' AND ISTEMPLATE=1);


-- SAK-13646
alter table GB_GRADABLE_OBJECT_T add (IS_EXTRA_CREDIT number(1,0), ASSIGNMENT_WEIGHTING double precision);
alter table GB_CATEGORY_T add (IS_EXTRA_CREDIT number(1,0));
alter table GB_GRADE_RECORD_T add (IS_EXCLUDED_FROM_GRADE number(1,0));

-- SAK-12883, SAK-12582 - Allow control over which academic sessions are
-- considered current; support more than one current academic session
alter table CM_ACADEMIC_SESSION_T add IS_CURRENT number(1,0) default 0 not null;

-- WARNING: This simply emulates the old runtime behavior. It is strongly
-- recommended that you decide which terms should be treated as current
-- and edit this script accordingly!
update CM_ACADEMIC_SESSION_T set IS_CURRENT=1 where SYSDATE >= START_DATE and SYSDATE <= END_DATE;


--Tables for email template service (new tool - SAK-14573)
    create table EMAIL_TEMPLATE_ITEM (
        ID number(19,0) not null,
        LAST_MODIFIED date not null,
        OWNER varchar2(255) not null,
        SUBJECT clob not null,
        MESSAGE clob not null,
        TEMPLATE_KEY varchar2(255) not null,
        TEMPLATE_LOCALE varchar2(255),
        defaultType varchar2(255),
        primary key (ID)
    );

    create index email_templ_owner on EMAIL_TEMPLATE_ITEM (OWNER);

    create index email_templ_key on EMAIL_TEMPLATE_ITEM (TEMPLATE_KEY);

    create sequence hibernate_sequence;

----------------------------------------------------------------------------------------------------------------------------------------
-- SAK-7924 - add and backfill new site.roleswap permissions into existing realms and templates
----------------------------------------------------------------------------------------------------------------------------------------

------ View Site in a Different Role Backfill --------
-- SAK-7924 -- Adding the new site.roleswap permission as well as backfilling where appropriate
-- roles that can be switched to are defined in sakai.properties with the studentview.roles property

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'site.roleswap');

-- Add the permission to the templates - this is which roles have permission to access the functionality to switch roles
-- course sites
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'),
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'),
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.roleswap'));

-- maintain/access sites
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'),
(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.roleswap'));

----------------------------------------------------------------------------------------------------------------------------------------
-- backfill script
----------------------------------------------------------------------------------------------------------------------------------------

-- course sites

CREATE TABLE PERMISSIONS_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));
CREATE TABLE PERMISSIONS_TEMP2 (REALM_KEY INTEGER, ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);

INSERT INTO PERMISSIONS_TEMP values ('Instructor','site.roleswap');

INSERT INTO PERMISSIONS_TEMP2 (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select distinct SAKAI_REALM.REALM_KEY,
SAKAI_REALM_ROLE.ROLE_KEY, SAKAI_REALM_FUNCTION.FUNCTION_KEY
from SAKAI_REALM, SAKAI_REALM_ROLE, PERMISSIONS_TEMP,
SAKAI_REALM_FUNCTION, SAKAI_SITE
where SAKAI_REALM_ROLE.ROLE_NAME = PERMISSIONS_TEMP.ROLE_NAME
AND SAKAI_REALM_FUNCTION.FUNCTION_NAME =
PERMISSIONS_TEMP.FUNCTION_NAME
AND (substr(SAKAI_REALM.REALM_ID, 7,
length(SAKAI_REALM.REALM_ID)) = SAKAI_SITE.SITE_ID)
AND SAKAI_SITE.TYPE='course';

insert into SAKAI_REALM_RL_FN SELECT * FROM PERMISSIONS_TEMP2
tmp WHERE
not exists (
select 1
from SAKAI_REALM_RL_FN SRRFI
where SRRFI.REALM_KEY=tmp.REALM_KEY and SRRFI.ROLE_KEY=tmp.ROLE_KEY and
SRRFI.FUNCTION_KEY=tmp.FUNCTION_KEY
);

DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_TEMP2;

-- project sites

CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','site.roleswap');

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

--- Tables added for SAK-12912:Add optional ability to prompt for questions during site creation

    create table SSQ_ANSWER (
        ID varchar2(99) not null,
        ANSWER varchar2(255),
        ANSWER_STRING varchar2(255),
        FILL_IN_BLANK number(1,0),
        ORDER_NUM number(10,0),
        QUESTION_ID number(19,0),
        primary key (ID)
    );

    create table SSQ_QUESTION (
        ID varchar2(99) not null,
        QUESTION varchar2(255),
        REQUIRED number(1,0),
        MULTIPLE_ANSWERS number(1,0),
        ORDER_NUM number(10,0),
        IS_CURRENT varchar2(255),
        SITETYPE_ID number(19,0),
        primary key (ID)
    );

    comment on table SSQ_QUESTION is
        'This table stores site setup questions';

    create table SSQ_SITETYPE_QUESTIONS (
        ID varchar2(99) not null,
        SITE_TYPE varchar2(255),
        INSTRUCTION varchar2(255),
        URL varchar2(255),
        URL_LABEL varchar2(255),
        URL_Target varchar2(255),
        primary key (ID)
    );

    create table SSQ_USER_ANSWER (
        ID varchar2(99) not null,
        SITE_ID varchar2(255),
        USER_ID varchar2(255),
        ANSWER_STRING varchar2(255),
        ANSWER_ID varchar2(255),
        QUESTION_ID varchar2(255),
        primary key (ID)
    );

    create index SSQ_ANSWER_QUESTION_I on SSQ_ANSWER (QUESTION_ID);

    alter table SSQ_ANSWER 
        add constraint FK390C0DCC6B21AFB4 
        foreign key (QUESTION_ID) 
        references SSQ_QUESTION;

    create index SSQ_QUESTION_SITETYPE_I on SSQ_QUESTION (SITETYPE_ID);

    alter table SSQ_QUESTION 
        add constraint FKFE88BA7443AD4C69 
        foreign key (SITETYPE_ID) 
        references SSQ_SITETYPE_QUESTIONS;

    create sequence hibernate_sequence;
