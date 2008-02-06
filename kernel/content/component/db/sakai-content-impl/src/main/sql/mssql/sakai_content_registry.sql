-----------------------------------------------------------------------------
-- CONTENT_TYPE_REGISTRY
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_TYPE_REGISTRY
(
    CONTEXT_ID NVARCHAR (99) NOT NULL,
    RESOURCE_TYPE_ID NVARCHAR (255),
    ENABLED NVARCHAR (1)
);

CREATE INDEX content_type_registry_idx ON CONTENT_TYPE_REGISTRY
(
	CONTEXT_ID
);
