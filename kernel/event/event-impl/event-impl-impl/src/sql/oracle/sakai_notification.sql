-----------------------------------------------------------------------------
-- SAKAI_NOTIFICATION
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_NOTIFICATION
(
    NOTIFICATION_ID VARCHAR2 (99) NOT NULL,
    XML LONG
);

CREATE UNIQUE INDEX SAKAI_NOTIFICATION_INDEX ON SAKAI_NOTIFICATION
(
	NOTIFICATION_ID
);
