-----------------------------------------------------------------------------
-- SAKAI_ALIAS
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_ALIAS (
       ALIAS_ID             VARCHAR (99) NOT NULL,
       TARGET               VARCHAR (255) NULL,
       CREATEDBY            VARCHAR (99) NOT NULL,
       MODIFIEDBY           VARCHAR (99) NOT NULL,
       CREATEDON            DATETIME NOT NULL,
       MODIFIEDON           TIMESTAMP NOT NULL
);

ALTER TABLE SAKAI_ALIAS
       ADD  ( PRIMARY KEY (ALIAS_ID) ) ;

-----------------------------------------------------------------------------
-- SAKAI_ALIAS_PROPERTY
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_ALIAS_PROPERTY 
(
       ALIAS_ID             VARCHAR (99) NOT NULL,
       NAME                 VARCHAR (99) NOT NULL,
       VALUE                LONGTEXT NULL
);

ALTER TABLE SAKAI_ALIAS_PROPERTY
       ADD  ( PRIMARY KEY (ALIAS_ID, NAME) ) ;

ALTER TABLE SAKAI_ALIAS_PROPERTY
       ADD  ( FOREIGN KEY (ALIAS_ID)
                             REFERENCES SAKAI_ALIAS (ALIAS_ID) ) ;

-----------------------------------------------------------------------------
-- SAKAI_ALIAS_INDEXES
-----------------------------------------------------------------------------

CREATE INDEX IE_SAKAI_ALIAS_CREATED ON SAKAI_ALIAS
(
       CREATEDBY                      ASC,
       CREATEDON                      ASC
);

CREATE INDEX IE_SAKAI_ALIAS_MODDED ON SAKAI_ALIAS
(
       MODIFIEDBY                     ASC,
       MODIFIEDON                     ASC
);

CREATE INDEX IE_SAKAI_ALIAS_TARGET ON SAKAI_ALIAS
(
       TARGET                         ASC
);
