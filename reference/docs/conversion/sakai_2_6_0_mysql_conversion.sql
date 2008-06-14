--SAK-12527 Changes to Chat Room options do not work consistently

-- add column timeParam and numberParam 
alter table CHAT2_CHANNEL add (column timeParam int);
alter table CHAT2_CHANNEL add (column numberParam int);

UPDATE CHAT2_CHANNEL
SET numberParam = Case When filterParam = 0 or filterType <> 'SelectByNumber' Then 10 Else filterParam End,
timeParam = Case When filterparam = 0 or filterType <> 'SelectMessagesByTime' Then 3 Else filterParam End;

alter table chat2_channel modify column timeParam int not null;
alter table chat2_channel modify column numberParam int not null;

--SAK-12176 Messages-Send cc to recipients' email address(es)

-- add column sendEmailOut to table MFR_AREA_T
alter table MFR_AREA_T add column (SENDEMAILOUT bit);
update MFR_AREA_T set SENDEMAILOUT=1 where SENDEMAILOUT is NULL;
alter table MFR_AREA_T modify column SENDEMAILOUT bit not null;

--new msg.emailout permission 

INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.emailout');

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
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

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

alter table osp_scaffolding add column generalFeedbackOption tinyint not null DEFAULT '0';
alter table osp_scaffolding add column itemFeedbackOption tinyint not null DEFAULT '0';
update osp_scaffolding set generalFeedbackOption=0;
update osp_scaffolding set itemFeedbackOption=0;

alter table osp_wizard add column generalFeedbackOption tinyint not null DEFAULT '0';
alter table osp_wizard add column itemFeedbackOption tinyint not null DEFAULT '0';
update osp_wizard set generalFeedbackOption=0;
update osp_wizard set itemFeedbackOption=0;

--OSP SAK-11545
alter table osp_wizard add reviewerGroupAccess integer not null default '0';

--SAK-6216 Optional ability to store client hostname (resolved IP) in SAKAI_SESSION
alter table SAKAI_SESSION add column SESSION_HOSTNAME varchar(255);

--SAK-10801 Add CONTEXT field to SAKAI_EVENT
alter table SAKAI_EVENT add column CONTEXT varchar(255);

