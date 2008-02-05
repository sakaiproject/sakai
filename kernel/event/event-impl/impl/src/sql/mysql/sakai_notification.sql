-----------------------------------------------------------------------------
-- SAKAI_NOTIFICATION
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_NOTIFICATION
(
    NOTIFICATION_ID VARCHAR (99) NOT NULL,
    XML LONGTEXT
);

CREATE UNIQUE INDEX SAKAI_NOTIFICATION_INDEX ON SAKAI_NOTIFICATION
(
	NOTIFICATION_ID
);
