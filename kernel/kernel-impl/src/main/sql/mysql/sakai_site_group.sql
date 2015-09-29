-- Site related tables added in Sakai 2.1
-----------------------------------------------------------------------------
-- SAKAI_SITE_GROUP
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_SITE_GROUP (
       GROUP_ID             VARCHAR (99) NOT NULL,
       SITE_ID              VARCHAR (99) NOT NULL,
       TITLE                VARCHAR (99) NULL,
       DESCRIPTION          LONGTEXT NULL,
       PRIMARY KEY (`GROUP_ID`),
       KEY `IE_SAKAI_SITE_GRP_SITE` (`SITE_ID`),
       CONSTRAINT `sakai_site_group_ibfk_1` FOREIGN KEY (`SITE_ID`) REFERENCES `SAKAI_SITE` (`SITE_ID`)
);

-----------------------------------------------------------------------------
-- SAKAI_SITE_GROUP_PROPERTY
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_SITE_GROUP_PROPERTY (
       SITE_ID              VARCHAR (99) NOT NULL,
       GROUP_ID             VARCHAR (99) NOT NULL,
       NAME                 VARCHAR (99) NOT NULL,
       VALUE                LONGTEXT NULL,
       PRIMARY KEY (`GROUP_ID`,`NAME`),
       KEY `IE_SAKAI_SITE_GRP_PROP_SITE` (`SITE_ID`),
       CONSTRAINT `sakai_site_group_property_ibfk_1` FOREIGN KEY (`GROUP_ID`) REFERENCES `sakai_site_group` (`GROUP_ID`),
       CONSTRAINT `sakai_site_group_property_ibfk_2` FOREIGN KEY (`SITE_ID`) REFERENCES `sakai_site` (`SITE_ID`)
);
