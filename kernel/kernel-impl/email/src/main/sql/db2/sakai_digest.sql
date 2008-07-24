-----------------------------------------------------------------------------
-- SAKAI_DIGEST
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_DIGEST
(
    DIGEST_ID VARCHAR (99) NOT NULL,
    XML CLOB
)
;

CREATE UNIQUE INDEX DIGEST_INDEX ON SAKAI_DIGEST
(
	DIGEST_ID
)
;