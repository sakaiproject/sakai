-----------------------------------
--  MSGCNTR-401   -----
--  Add new Property to prevent users from 
--  using Generic Recipients in "To" field (all participants, ect)
-----------------------------------

INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.allowToField.allParticipants');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.allowToField.groups');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.allowToField.roles');


--msg.permissions.allowToField.allParticipants and groups and roles is false for all users by default
--if you want to turn this feature on for all "student/acces" type roles, then run 
--the following conversion:


INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.allParticipants'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));



-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new msg.permissions.allowGenericRecipientFields permissions into existing realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.allParticipants');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.allParticipants');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.groups');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.roles');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.roles');


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


---------------------------
--  END MSGCNTR-401   -----
---------------------------


--////////////////////////////////////////////////////
--// MSGCNTR-411
--// Post First Option in Forums
--////////////////////////////////////////////////////
-- add column to allow postFirst as template setting
alter table MFR_AREA_T add column (POST_FIRST bit);
update MFR_AREA_T set POST_FIRST =0 where POST_FIRST is NULL;
alter table MFR_AREA_T modify column POST_FIRST bit not null;

-- add column to allow POST_FIRST to be set at the forum level
alter table MFR_OPEN_FORUM_T add column (POST_FIRST bit);
update MFR_OPEN_FORUM_T set POST_FIRST =0 where POST_FIRST is NULL;
alter table MFR_OPEN_FORUM_T modify column POST_FIRST bit not null;

-- add column to allow POST_FIRST to be set at the topic level
alter table MFR_TOPIC_T add column (POST_FIRST bit);
update MFR_TOPIC_T set POST_FIRST =0 where POST_FIRST is NULL;
alter table MFR_TOPIC_T modify column POST_FIRST bit not null;


-- MSGCNTR-329 - Add BCC option to Messages
alter table MFR_PVT_MSG_USR_T add column (BCC bit);
update MFR_PVT_MSG_USR_T set BCC=0 where BCC is NULL;
alter table MFR_PVT_MSG_USR_T modify column BCC bit not null; 
alter table MFR_MESSAGE_T add column RECIPIENTS_AS_TEXT_BCC TEXT;

-- MSGCNTR-503 - Internationalization of message priority
-- Default locale
update MFR_MESSAGE_T set label='pvt_priority_high' where label='High';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Low';

-- Locale ar
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='\u0645\u0631\u062A\u0641\u0639';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='\u0639\u0627\u062F\u064A';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='\u0645\u0646\u062E\u0641\u0636';

-- Locale ca
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Alta';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Baixa';

-- Locale es
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Alta';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Baja';

-- Locale eu
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Gutxikoa';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normala';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Handikoa';

-- Locale fr_CA
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='\u00C9lev\u00E9e';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normale';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Basse';

-- Locale fr_FR
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Elev\u00E9e';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normale';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Basse';

-- Locale ja
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='\u9ad8\u3044';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='\u666e\u901a';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='\u4f4e\u3044';

-- Locale nl
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Hoog';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normaal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Laag';

-- Locale pt_BR
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Alta';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Baixa';

-- Locale pt_PT
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='Alta';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='Baixa';

-- Locale ru
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='\u0412\u044b\u0441\u043e\u043a\u0438\u0439';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='\u041e\u0431\u044b\u0447\u043d\u044b\u0439';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='\u041d\u0438\u0437\u043a\u0438\u0439';

-- Locale sv
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='H\u00F6g';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='Normal';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='L\u00E5g';

-- Locale zh_TW
update  MFR_MESSAGE_T set label='pvt_priority_high' where label='\u9ad8';
update  MFR_MESSAGE_T set label='pvt_priority_normal' where label='\u666e\u901a';
update  MFR_MESSAGE_T set label='pvt_priority_low' where label='\u4f4e';

-- END MSGCNTR-503 --


--////////////////////////////////////////////////////
--// MSGCNTR-438
--// Add Ability to hide specific groups
--////////////////////////////////////////////////////

CREATE TABLE MFR_HIDDEN_GROUPS_T  ( 
    ID                bigint(20) AUTO_INCREMENT NOT NULL,
    VERSION           int(11) NOT NULL,
    a_surrogateKey    bigint(20) NULL,
    GROUP_ID          varchar(255) NOT NULL,
    PRIMARY KEY(ID)
);

ALTER TABLE MFR_HIDDEN_GROUPS_T
    ADD CONSTRAINT FK1DDE4138A306F94D
    FOREIGN KEY(a_surrogateKey)
    REFERENCES MFR_AREA_T(ID)
    ON DELETE RESTRICT 
    ON UPDATE RESTRICT;

CREATE INDEX MFR_HIDDEN_GROUPS_PARENT_I
    ON MFR_HIDDEN_GROUPS_T(a_surrogateKey);


INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.viewHidden.groups');

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new permission into existing realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.viewHidden.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.viewHidden.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.viewHidden.groups');


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

-- END MSGCNTR-438 --


--MSGCNTR-569
alter table MFR_TOPIC_T modify CONTEXT_ID varchar(255);

