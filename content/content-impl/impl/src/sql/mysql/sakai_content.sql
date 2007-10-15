-----------------------------------------------------------------------------
-- CONTENT_COLLECTION
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_COLLECTION
(
    COLLECTION_ID VARCHAR (255) NOT NULL,
	IN_COLLECTION VARCHAR (255),
    XML LONGTEXT,
    BINARY_ENTITY BLOB
);

CREATE UNIQUE INDEX CONTENT_COLLECTION_INDEX ON CONTENT_COLLECTION
(
	COLLECTION_ID
);

CREATE INDEX CONTENT_IN_COLLECTION_INDEX ON CONTENT_COLLECTION
(
	IN_COLLECTION
);

INSERT INTO CONTENT_COLLECTION VALUES ('/','',
'<?xml version="1.0" encoding="UTF-8"?>
<collection id="/">
	<properties>
		<property name="CHEF:creator" value="admin"/>
		<property name="CHEF:is-collection" value="true"/>
		<property name="DAV:displayname" value="root"/>
		<property name="CHEF:modifiedby" value="admin"/>
		<property name="DAV:getlastmodified" value="20020401000000000"/>
		<property name="DAV:creationdate" value="20020401000000000"/>
	</properties>
</collection>
', NULL);

INSERT INTO CONTENT_COLLECTION VALUES ('/group/','/',
'<?xml version="1.0" encoding="UTF-8"?>
<collection id="/group/">
	<properties>
		<property name="CHEF:creator" value="admin"/>
		<property name="CHEF:is-collection" value="true"/>
		<property name="DAV:displayname" value="group"/>
		<property name="CHEF:modifiedby" value="admin"/>
		<property name="DAV:getlastmodified" value="20020401000000000"/>
		<property name="DAV:creationdate" value="20020401000000000"/>
	</properties>
</collection>
', NULL);

INSERT INTO CONTENT_COLLECTION VALUES ('/public/','/',
'<?xml version="1.0" encoding="UTF-8"?>
<collection id="/public/">
	<properties>
		<property name="CHEF:creator" value="admin"/>
		<property name="CHEF:is-collection" value="true"/>
		<property name="DAV:displayname" value="public"/>
		<property name="CHEF:modifiedby" value="admin"/>
		<property name="DAV:getlastmodified" value="20020401000000000"/>
		<property name="DAV:creationdate" value="20020401000000000"/>
	</properties>
</collection>
', NULL);

INSERT INTO CONTENT_COLLECTION VALUES ('/attachment/','/',
'<?xml version="1.0" encoding="UTF-8"?>
<collection id="/attachment/">
	<properties>
		<property name="CHEF:creator" value="admin"/>
		<property name="CHEF:is-collection" value="true"/>
		<property name="DAV:displayname" value="attachment"/>
		<property name="CHEF:modifiedby" value="admin"/>
		<property name="DAV:getlastmodified" value="20020401000000000"/>
		<property name="DAV:creationdate" value="20020401000000000"/>
	</properties>
</collection>
', NULL);

INSERT INTO CONTENT_COLLECTION VALUES ('/private/','/',
'<?xml version="1.0" encoding="UTF-8"?>
<collection id="/private/">
	<properties>
		<property name="CHEF:creator" value="admin"/>
		<property name="CHEF:is-collection" value="true"/>
		<property name="DAV:displayname" value="private"/>
		<property name="CHEF:modifiedby" value="admin"/>
		<property name="DAV:getlastmodified" value="20020401000000000"/>
		<property name="DAV:creationdate" value="20020401000000000"/>
	</properties>
</collection>
', NULL);

INSERT INTO CONTENT_COLLECTION VALUES ('/user/', '/',
'<?xml version="1.0" encoding="UTF-8"?>
<collection id="/user/">
	<properties>
		<property name="CHEF:creator" value="admin"/>
		<property name="CHEF:is-collection" value="true"/>
		<property name="DAV:displayname" value="user"/>
		<property name="CHEF:modifiedby" value="admin"/>
		<property name="DAV:getlastmodified" value="20020401000000000"/>
		<property name="DAV:creationdate" value="20020401000000000"/>
	</properties>
</collection>
', NULL);

INSERT INTO CONTENT_COLLECTION VALUES ('/group-user/','/',
'<?xml version="1.0" encoding="UTF-8"?>
	<collection id="/group-user/">
		<properties>
			<property name="CHEF:creator" value="admin"/>
			<property name="CHEF:is-collection" value="true"/>
			<property name="DAV:displayname" value="group-user"/>
			<property name="CHEF:modifiedby" value="admin"/>
			<property name="DAV:getlastmodified" value="20020401000000000"/>
			<property name="DAV:creationdate" value="20020401000000000"/>
		</properties>
	</collection>
', NULL);

-----------------------------------------------------------------------------
-- CONTENT_RESOURCE
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_RESOURCE
(
    RESOURCE_ID VARCHAR (255) NOT NULL,
    RESOURCE_UUID VARCHAR (36),
	IN_COLLECTION VARCHAR (255),
	CONTEXT VARCHAR (99),
	FILE_PATH VARCHAR (128),
	FILE_SIZE BIGINT,
	RESOURCE_TYPE_ID VARCHAR (255),
    XML LONGTEXT,
    BINARY_ENTITY BLOB
    
-- for BLOB body, add BODY BLOB -- and drop the content_resource_body_binary tables -ggolden
);

CREATE UNIQUE INDEX CONTENT_RESOURCE_INDEX ON CONTENT_RESOURCE
(
	RESOURCE_ID
);

CREATE INDEX CONTENT_IN_RESOURCE_INDEX ON CONTENT_RESOURCE
(
	IN_COLLECTION
);

CREATE INDEX CONTENT_RESOURCE_CI ON CONTENT_RESOURCE
(
	CONTEXT
);

CREATE INDEX CONTENT_UUID_RESOURCE_INDEX ON CONTENT_RESOURCE
(
	RESOURCE_UUID
);

CREATE INDEX CONTENT_RESOURCE_RTI ON CONTENT_RESOURCE
(
	RESOURCE_TYPE_ID
);

-----------------------------------------------------------------------------
-- CONTENT_RESOURCE_BODY_BINARY
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_RESOURCE_BODY_BINARY
(
    RESOURCE_ID VARCHAR (255) NOT NULL,
    BODY LONGBLOB
);

CREATE UNIQUE INDEX CONTENT_RESOURCE_BB_INDEX ON CONTENT_RESOURCE_BODY_BINARY
(
	RESOURCE_ID
);

-----------------------------------------------------------------------------
-- CONTENT_DROPBOX_CHANGES
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_DROPBOX_CHANGES
(
    DROPBOX_ID VARCHAR (255) NOT NULL,
    IN_COLLECTION VARCHAR (255),
    LAST_UPDATE VARCHAR (24)
);

CREATE UNIQUE INDEX CONTENT_DROPBOX_CI ON CONTENT_DROPBOX_CHANGES
(
	DROPBOX_ID
);

CREATE INDEX CONTENT_DROPBOX_II ON CONTENT_DROPBOX_CHANGES
(
	IN_COLLECTION
);
