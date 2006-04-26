-----------------------------------------------------------------------------
-- ASSIGNMENT_ASSIGNMENT
-----------------------------------------------------------------------------

CREATE TABLE ASSIGNMENT_ASSIGNMENT
(
    ASSIGNMENT_ID VARCHAR (99) NOT NULL,
	CONTEXT VARCHAR (99),
    XML LONGVARCHAR,
    CONSTRAINT ASSIGNMENT_ASSIGNMENT_INDEX UNIQUE (ASSIGNMENT_ID)
);

CREATE INDEX ASSIGNMENT_ASSIGNMENT_CONTEXT ON ASSIGNMENT_ASSIGNMENT
(
	CONTEXT
);

-----------------------------------------------------------------------------
-- ASSIGNMENT_CONTENT
-----------------------------------------------------------------------------

CREATE TABLE ASSIGNMENT_CONTENT
(
    CONTENT_ID VARCHAR (99) NOT NULL,
	CONTEXT VARCHAR (99),
    XML LONGVARCHAR,
    CONSTRAINT ASSIGNMENT_CONTENT_INDEX UNIQUE (CONTENT_ID)
);

CREATE INDEX ASSIGNMENT_CONTENT_CONTEXT ON ASSIGNMENT_CONTENT
(
	CONTEXT
);

-----------------------------------------------------------------------------
-- ASSIGNMENT_SUBMISSION
-----------------------------------------------------------------------------

CREATE TABLE ASSIGNMENT_SUBMISSION
(
    SUBMISSION_ID VARCHAR (99) NOT NULL,
	CONTEXT VARCHAR (99),
    XML LONGVARCHAR,
    CONSTRAINT ASSIGNMENT_SUBMISSION_INDEX UNIQUE (SUBMISSION_ID)
);

CREATE INDEX ASSIGNMENT_SUBMISSION_CONTEXT ON ASSIGNMENT_SUBMISSION
(
	CONTEXT
);
