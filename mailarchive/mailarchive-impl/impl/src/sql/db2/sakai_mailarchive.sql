-- ---------------------------------------------------------------------------
-- MAILARCHIVE_CHANNEL
-- ---------------------------------------------------------------------------

CREATE TABLE MAILARCHIVE_CHANNEL
(
    CHANNEL_ID VARCHAR (99) NOT NULL,
	NEXT_ID INT,
    XML CLOB
);

CREATE UNIQUE INDEX MAILARC_CHAN ON MAILARCHIVE_CHANNEL
(
	CHANNEL_ID
) ALLOW REVERSE SCANS;

-- ---------------------------------------------------------------------------
-- MAILARCHIVE_MESSAGE
-- ---------------------------------------------------------------------------

CREATE TABLE MAILARCHIVE_MESSAGE (
       CHANNEL_ID           VARCHAR (99) NOT NULL,
       MESSAGE_ID           VARCHAR (36) NOT NULL,
       DRAFT                CHAR(1) ,
                                   CHECK (DRAFT IN ('1', '0')),
       PUBVIEW              CHAR(1) ,
                                   CHECK (PUBVIEW IN ('1', '0')),
       OWNER                VARCHAR (99),
       MESSAGE_DATE         TIMESTAMP NOT NULL,
       XML                  CLOB
) ORGANIZE BY DIMENSIONS (DRAFT, PUBVIEW);

ALTER TABLE MAILARCHIVE_MESSAGE
       ADD  PRIMARY KEY (CHANNEL_ID, MESSAGE_ID)  ;

CREATE INDEX MAILARC_MSG_OWN ON MAILARCHIVE_MESSAGE
(
       OWNER                          ASC
) ALLOW REVERSE SCANS;

CREATE INDEX MAILARC_MSG_DATE ON MAILARCHIVE_MESSAGE
(
       MESSAGE_DATE                   ASC
) ALLOW REVERSE SCANS;


CREATE INDEX MAILARC_MSG_CDD ON MAILARCHIVE_MESSAGE
(
	CHANNEL_ID,
	MESSAGE_DATE
) ALLOW REVERSE SCANS;

INSERT INTO MAILARCHIVE_CHANNEL (CHANNEL_ID, NEXT_ID, XML) VALUES ('/mailarchive/channel/!site/postmaster', 1, '<?xml version="1.0" encoding="UTF-8"?>
<channel context="!site" id="postmaster" next-message-id="1">
	<properties/>
</channel>
');
