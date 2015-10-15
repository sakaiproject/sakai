-----------------------------------------------------------------------------
-- SAKAI_USER_PROPERTY
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_USER_PROPERTY
(
       USER_ID             VARCHAR (99) NOT NULL,
       NAME                VARCHAR (99) NOT NULL,
       VALUE               LONGTEXT NULL,
       PRIMARY KEY         (`USER_ID`,`NAME`),
       CONSTRAINT `sakai_user_property_ibfk_1` FOREIGN KEY (`USER_ID`) REFERENCES `SAKAI_USER` (`USER_ID`)
);

-----------------------------------------------------------------------------
-- SAKAI_USER
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_USER
(
       USER_ID              VARCHAR (99) NOT NULL,
       EMAIL                VARCHAR (255) NULL,
       EMAIL_LC             VARCHAR (255) NULL,
       FIRST_NAME           VARCHAR (255) NULL,
       LAST_NAME            VARCHAR (255) NULL,
       TYPE                 VARCHAR (255) NULL,
       PW                   VARCHAR (255) NULL,
       CREATEDBY            VARCHAR (99) NOT NULL,
       MODIFIEDBY           VARCHAR (99) NOT NULL,
       CREATEDON            TIMESTAMP NOT NULL,
       MODIFIEDON           DATETIME NOT NULL,
       PRIMARY KEY          (`USER_ID`),
       KEY `IE_SAKAI_USER_CREATED` (`CREATEDBY`,`CREATEDON`),
       KEY `IE_SAKAI_USER_MODDED` (`MODIFIEDBY`,`MODIFIEDON`),
       KEY `IE_SAKAI_USER_EMAIL` (`EMAIL_LC`)
);

-----------------------------------------------------------------------------
-- SAKAI_USER_ID_MAP
-----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SAKAI_USER_ID_MAP
(
       USER_ID             VARCHAR (99) NOT NULL,
       EID                 VARCHAR (255) NOT NULL,
       PRIMARY KEY         (`USER_ID`),
       UNIQUE KEY `AK_SAKAI_USER_ID_MAP_EID` (`EID`)
);

-- populate with the admin and postmaster users

INSERT IGNORE INTO SAKAI_USER VALUES ('admin', '', '', 'Sakai', 'Administrator', '', 'ISMvKXpXpadDiUoOSoAfww==', 'admin', 'admin', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT IGNORE INTO SAKAI_USER VALUES ('postmaster', '', '', 'Sakai', 'Postmaster', '', '', 'postmaster', 'postmaster', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT IGNORE INTO SAKAI_USER_ID_MAP VALUES ('admin', 'admin');
INSERT IGNORE INTO SAKAI_USER_ID_MAP VALUES ('postmaster', 'postmaster');
