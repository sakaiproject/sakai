-----------------------------------------------------------------------------
-- SAKAI_EVENT
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_EVENT
(
	EVENT_ID NUMBER,
	EVENT_DATE TIMESTAMP WITH TIME ZONE,
	EVENT VARCHAR2 (32),
	REF VARCHAR2 (255),
	CONTEXT VARCHAR2 (255),
	SESSION_ID VARCHAR2 (163),
	EVENT_CODE VARCHAR2 (1)
);

CREATE UNIQUE INDEX SAKAI_EVENT_INDEX ON SAKAI_EVENT
(
	EVENT_ID
);

CREATE SEQUENCE SAKAI_EVENT_SEQ;
