-----------------------------------
--  MSGCNTR-401   -----
--  Add new Property to prevent users from 
--  using Generic Recipients in "To" field (all participants, ect)
-----------------------------------

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'msg.permissions.allowToField.allParticipants');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'msg.permissions.allowToField.groups');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'msg.permissions.allowToField.roles');


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


---------------------------
--  END MSGCNTR-401   -----
---------------------------


--////////////////////////////////////////////////////
--// MSGCNTR-411
--// Post First Option in Forums
--////////////////////////////////////////////////////
-- add column to allow POST_FIRST as template setting
alter table MFR_AREA_T add (POST_FIRST NUMBER(1,0) default 0);
alter table MFR_AREA_T modify (POST_FIRST NUMBER(1,0) not null);

-- add column to allow POST_FIRST to be set at the forum level
alter table MFR_OPEN_FORUM_T add (POST_FIRST NUMBER(1,0) default 0);
alter table MFR_OPEN_FORUM_T modify (POST_FIRST NUMBER(1,0) not null);

-- add column to allow POST_FIRST to be set at the topic level
alter table MFR_TOPIC_T add (POST_FIRST NUMBER(1,0) default 0);
alter table MFR_TOPIC_T modify (POST_FIRST NUMBER(1,0) not null);


-- MSGCNTR-329 - Add BCC option to Messages
alter table MFR_PVT_MSG_USR_T add (BCC NUMBER(1,0) default 0);
alter table MFR_PVT_MSG_USR_T modify (BCC NUMBER(1,0) not null); 
alter table MFR_MESSAGE_T add (RECIPIENTS_AS_TEXT_BCC VARCHAR2(4000));

-- MSGCNTR-503 - Internationalization of message priority
-- Default locale
update mfr_message_t set label='pvt_priority_high' where label='High';
update mfr_message_t set label='pvt_priority_normal' where label='Normal';
update mfr_message_t set label='pvt_priority_low' where label='Low';

-- Locale ar
update mfr_message_t set label='pvt_priority_high' where label='\u0645\u0631\u062A\u0641\u0639';
update mfr_message_t set label='pvt_priority_normal' where label='\u0639\u0627\u062F\u064A';
update mfr_message_t set label='pvt_priority_low' where label='\u0645\u0646\u062E\u0641\u0636';

-- Locale ca
update mfr_message_t set label='pvt_priority_high' where label='Alta';
update mfr_message_t set label='pvt_priority_normal' where label='Normal';
update mfr_message_t set label='pvt_priority_low' where label='Baixa';

-- Locale es
update mfr_message_t set label='pvt_priority_high' where label='Alta';
update mfr_message_t set label='pvt_priority_normal' where label='Normal';
update mfr_message_t set label='pvt_priority_low' where label='Baja';

-- Locale eu
update mfr_message_t set label='pvt_priority_high' where label='Gutxikoa';
update mfr_message_t set label='pvt_priority_normal' where label='Normala';
update mfr_message_t set label='pvt_priority_low' where label='Handikoa';

-- Locale fr_CA
update mfr_message_t set label='pvt_priority_high' where label='\u00C9lev\u00E9e';
update mfr_message_t set label='pvt_priority_normal' where label='Normale';
update mfr_message_t set label='pvt_priority_low' where label='Basse';

-- Locale fr_FR
update mfr_message_t set label='pvt_priority_high' where label='Elev\u00E9e';
update mfr_message_t set label='pvt_priority_normal' where label='Normale';
update mfr_message_t set label='pvt_priority_low' where label='Basse';

-- Locale ja
update mfr_message_t set label='pvt_priority_high' where label='\u9ad8\u3044';
update mfr_message_t set label='pvt_priority_normal' where label='\u666e\u901a';
update mfr_message_t set label='pvt_priority_low' where label='\u4f4e\u3044';

-- Locale nl
update mfr_message_t set label='pvt_priority_high' where label='Hoog';
update mfr_message_t set label='pvt_priority_normal' where label='Normaal';
update mfr_message_t set label='pvt_priority_low' where label='Laag';

-- Locale pt_BR
update mfr_message_t set label='pvt_priority_high' where label='Alta';
update mfr_message_t set label='pvt_priority_normal' where label='Normal';
update mfr_message_t set label='pvt_priority_low' where label='Baixa';

-- Locale pt_PT
update mfr_message_t set label='pvt_priority_high' where label='Alta';
update mfr_message_t set label='pvt_priority_normal' where label='Normal';
update mfr_message_t set label='pvt_priority_low' where label='Baixa';

-- Locale ru
update mfr_message_t set label='pvt_priority_high' where label='\u0412\u044b\u0441\u043e\u043a\u0438\u0439';
update mfr_message_t set label='pvt_priority_normal' where label='\u041e\u0431\u044b\u0447\u043d\u044b\u0439';
update mfr_message_t set label='pvt_priority_low' where label='\u041d\u0438\u0437\u043a\u0438\u0439';

-- Locale sv
update mfr_message_t set label='pvt_priority_high' where label='H\u00F6g';
update mfr_message_t set label='pvt_priority_normal' where label='Normal';
update mfr_message_t set label='pvt_priority_low' where label='L\u00E5g';

-- Locale zh_TW
update mfr_message_t set label='pvt_priority_high' where label='\u9ad8';
update mfr_message_t set label='pvt_priority_normal' where label='\u666e\u901a';
update mfr_message_t set label='pvt_priority_low' where label='\u4f4e';

-- end MSGCNTR-503 --


--////////////////////////////////////////////////////
--// MSGCNTR-438
--// Add Ability to hide specific groups
--////////////////////////////////////////////////////


CREATE TABLE MFR_HIDDEN_GROUPS_T  ( 
    ID                number(20,0) NOT NULL,
    VERSION           number(11,0) NOT NULL,
    a_surrogateKey    number(20,0) NULL,
    GROUP_ID          varchar2(255) NOT NULL,
    PRIMARY KEY(ID)
);

ALTER TABLE MFR_HIDDEN_GROUPS_T
    ADD CONSTRAINT FK1DDE4138A306F94D
    FOREIGN KEY(a_surrogateKey)
    REFERENCES mfr_area_t(ID);

CREATE SEQUENCE MFR_HIDDEN_GROUPS_S;

CREATE INDEX MFR_HIDDEN_GROUPS_PARENT_I
    ON MFR_HIDDEN_GROUPS_T(a_surrogateKey);

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'msg.permissions.viewHidden.groups');

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- end MSGCNTR-438 --



--MSGCNTR-569
alter table MFR_TOPIC_T modify CONTEXT_ID varchar(255);

