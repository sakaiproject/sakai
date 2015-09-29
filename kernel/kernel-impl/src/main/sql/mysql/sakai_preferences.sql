-----------------------------------------------------------------------------
-- SAKAI_PREFERENCES
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_PREFERENCES
(
    PREFERENCES_ID VARCHAR (99) NOT NULL,
    XML LONGTEXT,
    UNIQUE KEY `SAKAI_PREFERENCES_INDEX` (`PREFERENCES_ID`)
);
