-----------------------------------------------------------------------------
-- SAKAI_DIGEST
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_DIGEST
(
    DIGEST_ID VARCHAR (99) NOT NULL,
    XML LONGVARCHAR,
    CONSTRAINT SAKAI_DIGEST_INDEX UNIQUE (DIGEST_ID)
);

