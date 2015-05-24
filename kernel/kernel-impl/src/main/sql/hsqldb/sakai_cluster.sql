-----------------------------------------------------------------------------
-- SAKAI_CLUSTER
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_CLUSTER
(
	SERVER_ID_INSTANCE VARCHAR (64),
	UPDATE_TIME DATETIME,
-- We can't use an enum here because HSQL doesn't support it
	STATUS VARCHAR(8),
	SERVER_ID VARCHAR(64),
	PRIMARY KEY (SERVER_ID_INSTANCE)
);
