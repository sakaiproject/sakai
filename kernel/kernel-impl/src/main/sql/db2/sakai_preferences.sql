-----------------------------------------------------------------------------
-- SAKAI_PREFERENCES
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_PREFERENCES
(
    PREFERENCES_ID VARCHAR (99) NOT NULL,
    XML CLOB
);

CREATE UNIQUE INDEX SAKAI_PREF_INDEX ON SAKAI_PREFERENCES
(
	PREFERENCES_ID
) ALLOW REVERSE SCANS;
