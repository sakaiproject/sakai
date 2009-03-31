-- ---------------------------------------------------------------------------
-- SAKAI_EVENT
-- ---------------------------------------------------------------------------

CREATE TABLE SAKAI_EVENT_DELAY
(
	EVENT_DELAY_ID BIGINT AUTO_INCREMENT,
	EVENT VARCHAR (32),
	REF VARCHAR (255),
	USER_ID VARCHAR (99),
	MODIFY VARCHAR (5),
	PRIORITY INT,
	PRIMARY KEY (EVENT_DELAY_ID)
);

CREATE INDEX IE_SAKAI_EVENT_DELAY_RESOURCE ON SAKAI_EVENT_DELAY
(
	REF ASC
);
