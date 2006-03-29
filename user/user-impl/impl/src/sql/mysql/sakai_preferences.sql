-----------------------------------------------------------------------------
-- SAKAI_PREFERENCES
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_PREFERENCES
(
    PREFERENCES_ID VARCHAR (99) NOT NULL,
    XML LONGTEXT
);

CREATE UNIQUE INDEX SAKAI_PREFERENCES_INDEX ON SAKAI_PREFERENCES
(
	PREFERENCES_ID
);
