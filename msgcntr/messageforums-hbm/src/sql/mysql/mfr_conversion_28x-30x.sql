
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



-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;

-- END MSGCNTR-438 --


--MSGCNTR-569
alter table MFR_TOPIC_T modify CONTEXT_ID varchar(255);

-- SAK-48085 - We don't need to backfill roles in this patcher script since all the roles have been in sakai_realm.sql since 2010

--S2U-29
alter table MFR_PVT_MSG_USR_T add READ_RECEIPT bit(1) DEFAULT null;
