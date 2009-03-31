-- ---------------------------------------------------------------------------
-- SAKAI_PRESENCE
-- ---------------------------------------------------------------------------

CREATE TABLE SAKAI_PRESENCE
(
	SESSION_ID VARCHAR (36),
	LOCATION_ID VARCHAR (255)
);

CREATE INDEX PRESENCE_SESS_IDX ON SAKAI_PRESENCE
(
	SESSION_ID
) ALLOW REVERSE SCANS;

CREATE INDEX PRESENCE_LOC_IDX ON SAKAI_PRESENCE
(
	LOCATION_ID
) ALLOW REVERSE SCANS;
