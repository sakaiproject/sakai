-----------------------------------------------------------------------------
-- SAKAI_PREFERENCES
-----------------------------------------------------------------------------
CREATE TABLE SAKAI_PREFERENCES
(
    PREFERENCES_ID NVARCHAR (99) NOT NULL,
    XML NVARCHAR(MAX)
)
;
sp_tableoption 'SAKAI_PREFERENCES', 'large value types out of row', 'true'
;

CREATE UNIQUE INDEX SAKAI_PREFERENCES_INDEX ON SAKAI_PREFERENCES
(
	PREFERENCES_ID
)
;