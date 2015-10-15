-----------------------------------------------------------------------------
-- SAKAI_ALIAS
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_ALIAS (
       ALIAS_ID             VARCHAR (99) NOT NULL,
       TARGET               VARCHAR (255) NULL,
       CREATEDBY            VARCHAR (99) NOT NULL,
       MODIFIEDBY           VARCHAR (99) NOT NULL,
       CREATEDON            DATETIME NOT NULL,
       MODIFIEDON           TIMESTAMP NOT NULL,
       PRIMARY KEY (`ALIAS_ID`),
       KEY `IE_SAKAI_ALIAS_CREATED` (`CREATEDBY`,`CREATEDON`),
       KEY `IE_SAKAI_ALIAS_MODDED` (`MODIFIEDBY`,`MODIFIEDON`),
       KEY `IE_SAKAI_ALIAS_TARGET` (`TARGET`)
);

-----------------------------------------------------------------------------
-- SAKAI_ALIAS_PROPERTY
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_ALIAS_PROPERTY 
(
       ALIAS_ID             VARCHAR (99) NOT NULL,
       NAME                 VARCHAR (99) NOT NULL,
       VALUE                LONGTEXT NULL,
       PRIMARY KEY (`ALIAS_ID`,`NAME`),
       CONSTRAINT `sakai_alias_property_ibfk_1` FOREIGN KEY (`ALIAS_ID`) REFERENCES `SAKAI_ALIAS` (`ALIAS_ID`)
);
