-----------------------------------------------------------------------------
-- SAKAI_LOCKS
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_LOCKS
(
	TABLE_NAME VARCHAR (64),
	RECORD_ID VARCHAR (512),
	LOCK_TIME TIMESTAMP,
	USAGE_SESSION_ID VARCHAR (36)
);

CREATE UNIQUE INDEX SAKAI_LOCKS_INDEX ON SAKAI_LOCKS
(
	TABLE_NAME, RECORD_ID
) ALLOW REVERSE SCANS;
