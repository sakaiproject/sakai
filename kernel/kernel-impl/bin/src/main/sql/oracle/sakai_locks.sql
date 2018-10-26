-----------------------------------------------------------------------------
-- SAKAI_LOCKS
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_LOCKS
(
	TABLE_NAME VARCHAR2 (64),
	RECORD_ID VARCHAR2 (512),
	LOCK_TIME DATE,
	USAGE_SESSION_ID VARCHAR2 (36)
);

CREATE UNIQUE INDEX SAKAI_LOCKS_INDEX ON SAKAI_LOCKS
(
	TABLE_NAME, RECORD_ID
);
