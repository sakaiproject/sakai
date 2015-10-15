-----------------------------------------------------------------------------
-- SAKAI_LOCKS
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_LOCKS
(
	TABLE_NAME VARCHAR (64),
	RECORD_ID VARCHAR (512),
	LOCK_TIME DATETIME,
	USAGE_SESSION_ID VARCHAR (36),
	UNIQUE KEY `SAKAI_LOCKS_INDEX` (`TABLE_NAME`,`RECORD_ID`(128))
);
