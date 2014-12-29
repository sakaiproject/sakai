-- ---------------------------------------------------------------------------
-- MAILARCHIVE_CHANNEL
-- ---------------------------------------------------------------------------

CREATE TABLE MAILARCHIVE_CHANNEL
(
    CHANNEL_ID VARCHAR (99) NOT NULL,
	NEXT_ID INT,
    XML LONGVARCHAR,
    CONSTRAINT MAILARCHIVE_CHANNEL_INDEX UNIQUE (CHANNEL_ID)
);

-- ---------------------------------------------------------------------------
-- MAILARCHIVE_MESSAGE
-- ---------------------------------------------------------------------------

CREATE TABLE MAILARCHIVE_MESSAGE (
       CHANNEL_ID           VARCHAR (99) NOT NULL,
       MESSAGE_ID           VARCHAR (36) NOT NULL,
       DRAFT                CHAR(1) NULL,
 --                                  CHECK (DRAFT IN (1, 0)),
       PUBVIEW              CHAR(1) NULL,
--                                   CHECK (PUBVIEW IN (1, 0)),
       OWNER                VARCHAR (99) NULL,
       MESSAGE_DATE         DATETIME NOT NULL,
       XML                  LONGVARCHAR NULL,
       PRIMARY KEY (CHANNEL_ID, MESSAGE_ID)
);

CREATE INDEX IE_MAILARC_MSG_CHAN ON MAILARCHIVE_MESSAGE
(
       CHANNEL_ID                     ASC
);

CREATE INDEX IE_MAILARC_MSG_ATTRIB ON MAILARCHIVE_MESSAGE
(
       DRAFT                          ASC,
       PUBVIEW                        ASC,
       OWNER                          ASC
);

CREATE INDEX IE_MAILARC_MSG_DATE ON MAILARCHIVE_MESSAGE
(
       MESSAGE_DATE                   ASC
);

CREATE INDEX IE_MAILARC_MSG_DATE_DESC ON MAILARCHIVE_MESSAGE
(
       MESSAGE_DATE                   DESC
);

CREATE INDEX MAILARC_MSG_CDD ON MAILARCHIVE_MESSAGE
(
	CHANNEL_ID,
	MESSAGE_DATE,
	DRAFT
);

INSERT INTO MAILARCHIVE_CHANNEL VALUES ('/mailarchive/channel/!site/postmaster', 1, '<?xml version="1.0" encoding="UTF-8"?>
<channel context="!site" id="postmaster" next-message-id="1">
	<properties/>
</channel>
');
