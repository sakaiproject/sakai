-----------------------------------------------------------------------------
-- SAKAI_DIGEST
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_DIGEST
(
    DIGEST_ID VARCHAR (99) NOT NULL,
    XML LONGTEXT,
    UNIQUE KEY `SAKAI_DIGEST_INDEX` (`DIGEST_ID`)
);
