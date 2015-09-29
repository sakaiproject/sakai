-- ---------------------------------------------------------------------------
-- SAKAI_PRESENCE
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_PRESENCE
(
	SESSION_ID VARCHAR (36),
	LOCATION_ID VARCHAR (255),
	KEY `SAKAI_PRESENCE_SESSION_INDEX` (`SESSION_ID`),
	KEY `SAKAI_PRESENCE_LOCATION_INDEX` (`LOCATION_ID`)
);
