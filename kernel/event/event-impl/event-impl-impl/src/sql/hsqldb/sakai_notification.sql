-----------------------------------------------------------------------------
-- SAKAI_NOTIFICATION
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_NOTIFICATION
(
    NOTIFICATION_ID VARCHAR (99) NOT NULL,
    XML LONGVARCHAR,
    CONSTRAINT SAKAI_NOTIFICATION_INDEX UNIQUE (NOTIFICATION_ID)
);
