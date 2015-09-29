-- ---------------------------------------------------------------------------
-- ANNOUNCEMENT_CHANNEL
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ANNOUNCEMENT_CHANNEL
(
    CHANNEL_ID VARCHAR (255) NOT NULL,
	NEXT_ID INT,
	XML LONGTEXT,
UNIQUE KEY `ANNOUNCEMENT_CHANNEL_INDEX` (`CHANNEL_ID`)
);


-- ---------------------------------------------------------------------------
-- ANNOUNCEMENT_MESSAGE
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ANNOUNCEMENT_MESSAGE (
       CHANNEL_ID           VARCHAR(255) NOT NULL,
       MESSAGE_ID           VARCHAR(36) NOT NULL,
       DRAFT                CHAR(1) NULL
                                   CHECK (DRAFT IN (1, 0)),
       PUBVIEW              CHAR(1) NULL
                                   CHECK (PUBVIEW IN (1, 0)),
       OWNER                VARCHAR (99) NULL,
	   MESSAGE_DATE         DATETIME NOT NULL,
       XML                  LONGTEXT NULL,
	   MESSAGE_ORDER		INT,
	   PRIMARY KEY (`CHANNEL_ID`,`MESSAGE_ID`),
  KEY `IE_ANNC_MSG_ATTRIB` (`DRAFT`,`PUBVIEW`,`OWNER`,`MESSAGE_ORDER`),
  KEY `IE_ANNC_MSG_DATE` (`MESSAGE_DATE`),
  KEY `ANNOUNCEMENT_MESSAGE_CDD` (`CHANNEL_ID`,`MESSAGE_DATE`,`MESSAGE_ORDER`,`DRAFT`)
);
