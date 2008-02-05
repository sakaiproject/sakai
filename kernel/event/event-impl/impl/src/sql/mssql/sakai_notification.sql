-- SAKAI_NOTIFICATION
-----------------------------------------------------------------------------
CREATE TABLE SAKAI_NOTIFICATION
(
    NOTIFICATION_ID VARCHAR (99) NOT NULL,
    XML NVARCHAR(MAX)
)

CREATE UNIQUE INDEX SAKAI_NOTIFICATION_INDEX ON SAKAI_NOTIFICATION
(
	NOTIFICATION_ID
)
;
sp_tableoption 'SAKAI_NOTIFICATION', 'large value types out of row', 'true'
;