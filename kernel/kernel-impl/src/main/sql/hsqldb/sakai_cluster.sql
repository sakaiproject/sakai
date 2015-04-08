-----------------------------------------------------------------------------
-- SAKAI_CLUSTER
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_CLUSTER
(
	SERVER_ID_INSTANCE VARCHAR (64),
	UPDATE_TIME DATETIME,
	STATUS VARCHAR(8), -- No enums for us here.
	SERVER_ID VARCHAR(64)
	PRIMARY KEY (SERVER_ID_INSTANCE)
);
