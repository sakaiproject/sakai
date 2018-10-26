-----------------------------------------------------------------------------
-- SAKAI_DIGEST
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_DIGEST
(
    DIGEST_ID VARCHAR2 (99) NOT NULL,
    XML LONG
);

CREATE UNIQUE INDEX SAKAI_DIGEST_INDEX ON SAKAI_DIGEST
(
	DIGEST_ID
);
