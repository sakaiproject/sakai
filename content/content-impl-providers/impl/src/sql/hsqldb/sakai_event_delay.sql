-- ---------------------------------------------------------------------------
-- SAKAI_EVENT
-- ---------------------------------------------------------------------------

CREATE TABLE SAKAI_EVENT_DELAY
(
	EVENT_DELAY_ID INT,
	EVENT VARCHAR (32),
	REF VARCHAR (255),
	USER_ID VARCHAR (99),
	EVENT_CODE VARCHAR (1),
	PRIORITY SMALLINT,
	PRIMARY KEY (EVENT_DELAY_ID)
);

CREATE SEQUENCE SAKAI_EVENT_DELAY_SEQ;
