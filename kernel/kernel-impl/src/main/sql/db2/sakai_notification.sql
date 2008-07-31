-----------------------------------------------------------------------------
-- SAKAI_NOTIFICATION
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_NOTIFICATION
(
    NOTIFICATION_ID VARCHAR (99) NOT NULL,
    XML CLOB
);

CREATE UNIQUE INDEX NOTIFICATION_INDEX ON SAKAI_NOTIFICATION
(
	NOTIFICATION_ID
) ALLOW REVERSE SCANS;
