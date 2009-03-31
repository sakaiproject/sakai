-- ---------------------------------------------------------------------------
-- ANNOUNCEMENT_CHANNEL
-- ---------------------------------------------------------------------------

CREATE TABLE ANNOUNCEMENT_CHANNEL
(
    CHANNEL_ID VARCHAR (99) NOT NULL,
	NEXT_ID INT,
    XML CLOB
);

CREATE UNIQUE INDEX ANNOUNCE_CHAN_IDX ON ANNOUNCEMENT_CHANNEL
(
	CHANNEL_ID
) ALLOW REVERSE SCANS;

-- ---------------------------------------------------------------------------
-- ANNOUNCEMENT_MESSAGE
-- ---------------------------------------------------------------------------

CREATE TABLE ANNOUNCEMENT_MESSAGE (
       CHANNEL_ID           VARCHAR(99) NOT NULL,
       MESSAGE_ID           VARCHAR(36) NOT NULL,
       DRAFT                CHAR(1), 
                                   CHECK (DRAFT IN ('1', '0')),
       PUBVIEW              CHAR(1), 
                                   CHECK (PUBVIEW IN ('1', '0')),
       OWNER                VARCHAR (99),
       MESSAGE_DATE         TIMESTAMP NOT NULL,
       XML                  CLOB
) ORGANIZE BY DIMENSIONS (DRAFT, PUBVIEW);


ALTER TABLE ANNOUNCEMENT_MESSAGE
       ADD  PRIMARY KEY (CHANNEL_ID, MESSAGE_ID)  ;

CREATE INDEX IE_ANNC_MSG_ATTRIB ON ANNOUNCEMENT_MESSAGE
(
       OWNER                          ASC
) ALLOW REVERSE SCANS;

CREATE INDEX ANNOUNCE_MSGDATE ON ANNOUNCEMENT_MESSAGE
(
       MESSAGE_DATE                   ASC
) ALLOW REVERSE SCANS;
