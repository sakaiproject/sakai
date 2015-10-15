-- ---------------------------------------------------------------------------
-- ASSIGNMENT_ASSIGNMENT
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ASSIGNMENT_ASSIGNMENT
(
    ASSIGNMENT_ID VARCHAR (99) NOT NULL,
	CONTEXT VARCHAR (99),
	XML LONGTEXT,
	UNIQUE KEY `ASSIGNMENT_ASSIGNMENT_INDEX` (`ASSIGNMENT_ID`),
	KEY `ASSIGNMENT_ASSIGNMENT_CONTEXT` (`CONTEXT`)
);


-- ---------------------------------------------------------------------------
-- ASSIGNMENT_CONTENT
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ASSIGNMENT_CONTENT
(
    CONTENT_ID VARCHAR (99) NOT NULL,
	CONTEXT VARCHAR (99),
	XML LONGTEXT,
	UNIQUE KEY `ASSIGNMENT_CONTENT_INDEX` (`CONTENT_ID`),
	KEY `ASSIGNMENT_CONTENT_CONTEXT` (`CONTEXT`)
);


-- ---------------------------------------------------------------------------
-- ASSIGNMENT_SUBMISSION
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ASSIGNMENT_SUBMISSION
(
    SUBMISSION_ID VARCHAR (99) NOT NULL,
	CONTEXT VARCHAR (99) NOT NULL,
	SUBMITTER_ID VARCHAR(99) NOT NULL,
	SUBMIT_TIME VARCHAR(99),
	SUBMITTED VARCHAR(6),
	GRADED VARCHAR(6),
	XML LONGTEXT,
	UNIQUE KEY `ASSIGNMENT_SUBMISSION_INDEX` (`SUBMISSION_ID`),
	UNIQUE KEY `ASSIGNMENT_SUBMISSION_SUBMITTER_INDEX` (`CONTEXT`,`SUBMITTER_ID`),
	KEY `ASSIGNMENT_SUBMISSION_CONTEXT` (`CONTEXT`)
);
