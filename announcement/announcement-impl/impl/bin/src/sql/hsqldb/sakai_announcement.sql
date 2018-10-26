-- ---------------------------------------------------------------------------
-- ANNOUNCEMENT_CHANNEL
-- ---------------------------------------------------------------------------

CREATE TABLE ANNOUNCEMENT_CHANNEL
(
    CHANNEL_ID VARCHAR (255) NOT NULL,
	NEXT_ID INT,
    XML LONGVARCHAR,
    CONSTRAINT ANNOUNCEMENT_CHANNEL_INDEX UNIQUE (CHANNEL_ID)
);

-- ---------------------------------------------------------------------------
-- ANNOUNCEMENT_MESSAGE
-- ---------------------------------------------------------------------------

CREATE TABLE ANNOUNCEMENT_MESSAGE (
       CHANNEL_ID           VARCHAR(255) NOT NULL,
       MESSAGE_ID           VARCHAR(36) NOT NULL,
       DRAFT                CHAR(1) NULL,
 --                                  CHECK (DRAFT IN (1, 0)),
       PUBVIEW              CHAR(1) NULL,
 --                                  CHECK (PUBVIEW IN (1, 0)),
       OWNER                VARCHAR (99) NULL,
       MESSAGE_DATE         DATETIME NOT NULL,
       XML                  LONGVARCHAR NULL,
	   MESSAGE_ORDER		INT,
       PRIMARY KEY (CHANNEL_ID, MESSAGE_ID)
);


CREATE INDEX IE_ANNC_MSG_CHANNEL ON ANNOUNCEMENT_MESSAGE
(
       CHANNEL_ID                     ASC
);

CREATE INDEX IE_ANNC_MSG_ATTRIB ON ANNOUNCEMENT_MESSAGE
(
       DRAFT                          ASC,
       PUBVIEW                        ASC,
       OWNER                          ASC,
       MESSAGE_ORDER				  ASC
);

CREATE INDEX IE_ANNC_MSG_DATE ON ANNOUNCEMENT_MESSAGE
(
       MESSAGE_DATE                   ASC
);

CREATE INDEX IE_ANNC_MSG_DATE_DESC ON ANNOUNCEMENT_MESSAGE
(
       MESSAGE_DATE                   DESC
);

CREATE INDEX ANNOUNCEMENT_MESSAGE_CDD ON ANNOUNCEMENT_MESSAGE
(
	CHANNEL_ID,
	MESSAGE_DATE,
    MESSAGE_ORDER,
	DRAFT
);
