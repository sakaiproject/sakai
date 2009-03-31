-- ---------------------------------------------------------------------------
-- SAKAI_PRESENCE
-- ---------------------------------------------------------------------------

CREATE TABLE SAKAI_PRESENCE
(
	SESSION_ID VARCHAR2 (36),
	LOCATION_ID VARCHAR2 (255)
);

CREATE INDEX SAKAI_PRESENCE_SESSION_INDEX ON SAKAI_PRESENCE
(
	SESSION_ID
);

CREATE INDEX SAKAI_PRESENCE_LOCATION_INDEX ON SAKAI_PRESENCE
(
	LOCATION_ID
);
