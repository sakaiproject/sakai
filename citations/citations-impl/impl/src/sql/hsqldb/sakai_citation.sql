-----------------------------------------------------------------------------
-- CITATION_COLLECTION
-----------------------------------------------------------------------------

CREATE TABLE CITATION_COLLECTION
(
    COLLECTION_ID VARCHAR (36) NOT NULL,
	PROPERTY_NAME VARCHAR (255),
    PROPERTY_VALUE LONGVARCHAR
---    CONSTRAINT CITATION_COLLECTION_INDEX (COLLECTION_ID)
);

-----------------------------------------------------------------------------
-- CITATION_CITATION
-----------------------------------------------------------------------------

CREATE TABLE CITATION_CITATION
(
    CITATION_ID VARCHAR (36) NOT NULL,
	PROPERTY_NAME VARCHAR (255),
    PROPERTY_VALUE LONGVARCHAR
---    CONSTRAINT CITATION_CITATION_INDEX (CITATION_ID)
);

-----------------------------------------------------------------------------
-- CITATION_SCHEMA
-----------------------------------------------------------------------------

CREATE TABLE CITATION_SCHEMA
(
    SCHEMA_ID VARCHAR (36) NOT NULL,
	PROPERTY_NAME VARCHAR (255),
    PROPERTY_VALUE LONGVARCHAR
---    CONSTRAINT CITATION_SCHEMA_INDEX (SCHEMA_ID)
);

-----------------------------------------------------------------------------
-- CITATION_SCHEMA_FIELD
-----------------------------------------------------------------------------

CREATE TABLE CITATION_SCHEMA_FIELD
(
    SCHEMA_ID VARCHAR (36) NOT NULL,
    FIELD_ID VARCHAR (36) NOT NULL,
	PROPERTY_NAME VARCHAR (255),
    PROPERTY_VALUE LONGVARCHAR
---    CONSTRAINT CITATION_SCHEMA_INDEX (SCHEMA_ID, FIELD_ID)
);

