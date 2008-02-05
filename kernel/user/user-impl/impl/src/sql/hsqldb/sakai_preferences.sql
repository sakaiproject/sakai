-----------------------------------------------------------------------------
-- SAKAI_PREFERENCES
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_PREFERENCES
(
    PREFERENCES_ID VARCHAR (99) NOT NULL,
    XML LONGVARCHAR,
    CONSTRAINT SAKAI_PREFERENCES_INDEX UNIQUE (PREFERENCES_ID)
);
