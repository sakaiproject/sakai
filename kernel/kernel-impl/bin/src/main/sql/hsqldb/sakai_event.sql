-----------------------------------------------------------------------------
-- SAKAI_EVENT
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_EVENT
(
	EVENT_ID INT,
	EVENT_DATE DATE,
	EVENT VARCHAR (32),
	REF VARCHAR (255),
	CONTEXT VARCHAR (255),
	SESSION_ID VARCHAR (163),
	EVENT_CODE VARCHAR (1),
	PRIMARY KEY (EVENT_ID)
);

CREATE SEQUENCE SAKAI_EVENT_SEQ;
