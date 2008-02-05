-----------------------------------------------------------------------------
-- SAKAI_PREFERENCES
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_PREFERENCES
(
    PREFERENCES_ID VARCHAR2 (99) NOT NULL,
    XML LONG
);

CREATE UNIQUE INDEX SAKAI_PREFERENCES_INDEX ON SAKAI_PREFERENCES
(
	PREFERENCES_ID
);
