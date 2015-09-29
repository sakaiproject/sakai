-----------------------------------------------------------------------------
-- SAKAI_NOTIFICATION
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_NOTIFICATION
(
    NOTIFICATION_ID VARCHAR (99) NOT NULL,
    XML LONGTEXT,
    UNIQUE KEY `SAKAI_NOTIFICATION_INDEX` (`NOTIFICATION_ID`)
);
