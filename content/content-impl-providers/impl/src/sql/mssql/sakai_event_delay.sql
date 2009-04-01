-- ---------------------------------------------------------------------------
-- SAKAI_EVENT
-- ---------------------------------------------------------------------------
CREATE TABLE SAKAI_EVENT_DELAY
(
	EVENT_DELAY_ID BIGINT IDENTITY PRIMARY KEY,
	EVENT NVARCHAR (32),
	REF NVARCHAR (255),
	USER_ID NVARCHAR (99),
	EVENT_CODE NVARCHAR(1),
	PRIORITY INT
);

CREATE INDEX IE_SAKAI_EVENT_DELAY_RESOURCE ON SAKAI_EVENT_DELAY
(
	REF ASC
);
