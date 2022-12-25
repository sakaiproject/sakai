-----------------------------------------------------------------------------
-- CONTENT_RESOURCE_BODY_BINARY_DELETE
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_RESOURCE_DELETE
(
    RESOURCE_ID VARCHAR (255) NOT NULL,
    RESOURCE_UUID VARCHAR (36),
	IN_COLLECTION VARCHAR (255),
	CONTEXT VARCHAR (99),
	FILE_PATH VARCHAR (128),
	FILE_SIZE BIGINT,
	RESOURCE_TYPE_ID VARCHAR (255),
	DELETE_DATE DATE,
	DELETE_USERID VARCHAR (36),
    BINARY_ENTITY LONGVARBINARY,
    CONSTRAINT CONTENT_RESOURCE_UUID_DELETE_I UNIQUE (RESOURCE_UUID)
);

CREATE INDEX CONTENT_RESOURCE_DELETE_INDEX ON CONTENT_RESOURCE_DELETE
(
	RESOURCE_ID
);

-----------------------------------------------------------------------------
-- CONTENT_RESOURCE_BODY_BINARY
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_RESOURCE_BB_DELETE
(
    RESOURCE_ID VARCHAR (255) NOT NULL,
    BODY BINARY
);

