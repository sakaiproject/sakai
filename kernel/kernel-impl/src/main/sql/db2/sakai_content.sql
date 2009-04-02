-----------------------------------------------------------------------------
-- CONTENT_COLLECTION
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_COLLECTION
(
    COLLECTION_ID VARCHAR (255) NOT NULL,
	IN_COLLECTION VARCHAR (255),
    XML CLOB,
    BINARY_ENTITY BLOB
);

CREATE UNIQUE INDEX CONT_COLL_IDX ON CONTENT_COLLECTION
(
	COLLECTION_ID
) ALLOW REVERSE SCANS;

CREATE INDEX CONT_IN_COLL_IDX ON CONTENT_COLLECTION
(
	IN_COLLECTION
) ALLOW REVERSE SCANS;

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
',NULL);

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
',NULL);

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
',NULL);

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
',NULL);

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
',NULL);

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
',NULL);

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
	 RESOURCE_TYPE_ID VARCHAR(255),
    XML CLOB,
    BINARY_ENTITY BLOB
-- for BLOB body, add BODY BLOB -- and drop the content_resource_body_binary tables -ggolden
);

CREATE UNIQUE INDEX CONT_RSRC_IDX ON CONTENT_RESOURCE
(
	RESOURCE_ID
) ALLOW REVERSE SCANS;

CREATE INDEX CONT_IN_RSRC_IDX ON CONTENT_RESOURCE
(
	IN_COLLECTION
) ALLOW REVERSE SCANS;

CREATE INDEX CONT_RSRC_CI_IDX ON CONTENT_RESOURCE
(
	CONTEXT
) ALLOW REVERSE SCANS;

CREATE INDEX CONT_UUID_RSRC_IDX ON CONTENT_RESOURCE
(
	RESOURCE_UUID
) ALLOW REVERSE SCANS;

CREATE INDEX CONT_RSC_RTI_IDX ON CONTENT_RESOURCE
(
	RESOURCE_TYPE_ID
) ALLOW REVERSE SCANS;

-----------------------------------------------------------------------------
-- CONTENT_RESOURCE_BODY_BINARY
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_RESOURCE_BODY_BINARY
(
    RESOURCE_ID VARCHAR (255) NOT NULL,
    BODY BLOB{2G) NOT LOGGED
);

CREATE UNIQUE INDEX CONT_RSRC_BB_IDX ON CONTENT_RESOURCE_BODY_BINARY
(
	RESOURCE_ID
) ALLOW REVERSE SCANS;

