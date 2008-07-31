-----------------------------------------------------------------------------
-- SAKAI_EVENT
-----------------------------------------------------------------------------
CREATE TABLE SAKAI_EVENT
(
	EVENT_ID BIGINT IDENTITY,
	EVENT_DATE DATETIME,
	EVENT NVARCHAR (32),
	REF NVARCHAR (255),
	CONTEXT NVARCHAR (255),
   SESSION_ID NVARCHAR (163),
	EVENT_CODE NVARCHAR (1),
	PRIMARY KEY (EVENT_ID)
);

CREATE INDEX IE_SAKAI_EVENT_SESSION_ID ON SAKAI_EVENT
(
	SESSION_ID	ASC
);

