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

-- SAK-13646
alter table GB_GRADABLE_OBJECT_T add (IS_EXTRA_CREDIT number(1,0), ASSIGNMENT_WEIGHTING double precision);
alter table GB_CATEGORY_T add (IS_EXTRA_CREDIT number(1,0));
alter table GB_GRADE_RECORD_T add (IS_EXCLUDED_FROM_GRADE number(1,0));
