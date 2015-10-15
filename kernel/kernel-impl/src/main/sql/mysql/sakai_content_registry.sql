-----------------------------------------------------------------------------
-- CONTENT_TYPE_REGISTRY
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS CONTENT_TYPE_REGISTRY
(
    CONTEXT_ID VARCHAR (99) NOT NULL,
	RESOURCE_TYPE_ID VARCHAR (255),
	ENABLED VARCHAR (1),
	KEY `content_type_registry_idx` (`CONTEXT_ID`)
);
