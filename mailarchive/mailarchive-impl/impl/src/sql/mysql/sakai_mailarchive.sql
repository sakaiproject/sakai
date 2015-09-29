-- ---------------------------------------------------------------------------
-- MAILARCHIVE_CHANNEL
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS MAILARCHIVE_CHANNEL
(
    CHANNEL_ID VARCHAR (99) NOT NULL,
    NEXT_ID INT,
    XML LONGTEXT,
    UNIQUE KEY `MAILARCHIVE_CHANNEL_INDEX` (CHANNEL_ID)
);

-- ---------------------------------------------------------------------------
-- MAILARCHIVE_MESSAGE
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS MAILARCHIVE_MESSAGE (
       CHANNEL_ID           VARCHAR (99) NOT NULL,
       MESSAGE_ID           VARCHAR (36) NOT NULL,
       DRAFT                CHAR(1) NULL
                                   CHECK (DRAFT IN (1, 0)),
       PUBVIEW              CHAR(1) NULL
                                   CHECK (PUBVIEW IN (1, 0)),
       OWNER                VARCHAR (99) NULL,
       MESSAGE_DATE         DATETIME NOT NULL,
       XML                  LONGTEXT NULL,
       SUBJECT           VARCHAR (255) NULL,
       BODY              LONGTEXT NULL,
       PRIMARY KEY (CHANNEL_ID, MESSAGE_ID),
       KEY `IE_MAILARC_MSG_ATTRIB` (`DRAFT`,`PUBVIEW`,`OWNER`),
       KEY `IE_MAILARC_MSG_DATE` (`MESSAGE_DATE`),
       KEY `MAILARC_MSG_CDD` (`CHANNEL_ID`,`MESSAGE_DATE`,`DRAFT`),
       KEY `IE_MAILARC_SUBJECT` (`SUBJECT`)
);

INSERT IGNORE INTO MAILARCHIVE_CHANNEL VALUES ('/mailarchive/channel/!site/postmaster', 1, '<?xml version="1.0" encoding="UTF-8"?>
<channel context="!site" id="postmaster" next-message-id="1">
	<properties/>
</channel>
');
