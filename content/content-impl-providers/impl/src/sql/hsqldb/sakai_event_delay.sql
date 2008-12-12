-----------------------------------------------------------------------------
-- SAKAI_EVENT
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_EVENT_DELAY
(
	EVENT_DELAY_ID INT,
	EVENT VARCHAR (32),
	RESOURCE VARCHAR (255),
	USER_ID VARCHAR (99),
	MODIFY VARCHAR (1),
	PRIORITY INT,
	PRIMARY KEY (EVENT_DELAY_ID)
);

CREATE SEQUENCE SAKAI_EVENT_DELAY_SEQ;
