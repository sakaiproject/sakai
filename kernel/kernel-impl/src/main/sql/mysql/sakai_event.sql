-----------------------------------------------------------------------------
-- SAKAI_EVENT
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_EVENT
(
	EVENT_ID BIGINT AUTO_INCREMENT,
	EVENT_DATE TIMESTAMP,
	EVENT VARCHAR (32),
	REF VARCHAR (255),
	CONTEXT VARCHAR (255),
	SESSION_ID VARCHAR (163),
	EVENT_CODE VARCHAR (1),
	PRIMARY KEY (EVENT_ID),
	KEY `IE_SAKAI_EVENT_SESSION_ID` (`SESSION_ID`)
);
