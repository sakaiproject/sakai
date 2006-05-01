-----------------------------------------------------------------------------
-- CONTENT_COLLECTION
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_COLLECTION
(
    COLLECTION_ID VARCHAR (255) NOT NULL,
	IN_COLLECTION VARCHAR (255),
	ACCESS_MODE VARCHAR(12), 
	RELEASE_DATE DATETIME, 
	RETRACT_DATE DATETIME, 	
	SORT_ORDER INT,
    XML LONGVARCHAR,
    CONSTRAINT CONTENT_COLLECTION_INDEX UNIQUE (COLLECTION_ID)
 );

CREATE INDEX CONTENT_IN_COLLECTION_INDEX ON CONTENT_COLLECTION
(
	IN_COLLECTION
);

INSERT INTO CONTENT_COLLECTION VALUES ('/',
'','site',null,null,0,
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
');
UPDATE CONTENT_COLLECTION SET RELEASE_DATE='1970-01-01 00:00:00.000' WHERE COLLECTION_ID = '/';
UPDATE CONTENT_COLLECTION SET RETRACT_DATE='9999-12-31 23:59:59.999' WHERE COLLECTION_ID = '/';

INSERT INTO CONTENT_COLLECTION VALUES ('/group/',
'/','site',null,null,0,
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
');
UPDATE CONTENT_COLLECTION SET RELEASE_DATE='1970-01-01 00:00:00.000' WHERE COLLECTION_ID = '/group/';
UPDATE CONTENT_COLLECTION SET RETRACT_DATE='9999-12-31 23:59:59.999' WHERE COLLECTION_ID = '/group/';

INSERT INTO CONTENT_COLLECTION VALUES ('/public/',
'/','site',null,null,0,
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
');
UPDATE CONTENT_COLLECTION SET RELEASE_DATE='1970-01-01 00:00:00.000' WHERE COLLECTION_ID = '/public/';
UPDATE CONTENT_COLLECTION SET RETRACT_DATE='9999-12-31 23:59:59.999' WHERE COLLECTION_ID = '/public/';

INSERT INTO CONTENT_COLLECTION VALUES ('/attachment/',
'/','site',null,null,0,
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
');
UPDATE CONTENT_COLLECTION SET RELEASE_DATE='1970-01-01 00:00:00.000' WHERE COLLECTION_ID = '/attachment/';
UPDATE CONTENT_COLLECTION SET RETRACT_DATE='9999-12-31 23:59:59.999' WHERE COLLECTION_ID = '/attachment/';

INSERT INTO CONTENT_COLLECTION VALUES ('/user/',
'/','site',null,null,0,
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
');
UPDATE CONTENT_COLLECTION SET RELEASE_DATE='1970-01-01 00:00:00.000' WHERE COLLECTION_ID = '/user/';
UPDATE CONTENT_COLLECTION SET RETRACT_DATE='9999-12-31 23:59:59.999' WHERE COLLECTION_ID = '/user/';

INSERT INTO CONTENT_COLLECTION VALUES ('/group-user/',
'/','site',null,null,0,
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
');
UPDATE CONTENT_COLLECTION SET RELEASE_DATE='1970-01-01 00:00:00.000' WHERE COLLECTION_ID = '/group-user/';
UPDATE CONTENT_COLLECTION SET RETRACT_DATE='9999-12-31 23:59:59.999' WHERE COLLECTION_ID = '/group-user/';

-----------------------------------------------------------------------------
-- CONTENT_RESOURCE
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_RESOURCE
(
    RESOURCE_ID VARCHAR (255) NOT NULL,
    RESOURCE_UUID VARCHAR (36),
	IN_COLLECTION VARCHAR (255),
	FILE_PATH VARCHAR (128),
	ACCESS_MODE VARCHAR (12),
	RELEASE_DATE DATETIME,
	RETRACT_DATE DATETIME,
	SORT_ORDER INT,
    XML LONGVARCHAR,
    CONSTRAINT CONTENT_RESOURCE_INDEX UNIQUE (RESOURCE_ID)
-- for BINARY body, add BODY BINARY -- and drop the content_resource_body_binary tables -ggolden
);

CREATE INDEX CONTENT_IN_RESOURCE_INDEX ON CONTENT_RESOURCE
(
	IN_COLLECTION
);

CREATE INDEX CONTENT_UUID_RESOURCE_INDEX ON CONTENT_RESOURCE
(
	RESOURCE_UUID
);

-----------------------------------------------------------------------------
-- CONTENT_RESOURCE_BODY_BINARY
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_RESOURCE_BODY_BINARY
(
    RESOURCE_ID VARCHAR (255) NOT NULL,
    BODY BINARY,
    CONSTRAINT CONTENT_RESOURCE_BB_INDEX UNIQUE (RESOURCE_ID)
);

-----------------------------------------------------------------------------
-- CONTENT_ENTITY_GROUPS
-----------------------------------------------------------------------------

CREATE TABLE CONTENT_ENTITY_GROUPS
(
    ENTITY_ID VARCHAR (255) NOT NULL,
    GROUP_ID VARCHAR (255) NOT NULL
);
