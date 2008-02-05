-----------------------------------------------------------------------------
-- SAKAI_DIGEST
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_DIGEST
(
    DIGEST_ID VARCHAR (99) NOT NULL,
    XML LONGTEXT
);

CREATE UNIQUE INDEX SAKAI_DIGEST_INDEX ON SAKAI_DIGEST
(
	DIGEST_ID
);
