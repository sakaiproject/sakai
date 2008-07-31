-----------------------------------------------------------------------------
-- SAKAI_ALIAS
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_ALIAS (
       ALIAS_ID             VARCHAR (99) NOT NULL,
       TARGET               VARCHAR (255) ,
       CREATEDBY            VARCHAR (99) NOT NULL,
       MODIFIEDBY           VARCHAR (99) NOT NULL,
       CREATEDON            TIMESTAMP NOT NULL,
       MODIFIEDON           TIMESTAMP NOT NULL
);

ALTER TABLE SAKAI_ALIAS
       ADD  PRIMARY KEY (ALIAS_ID)  ;

-----------------------------------------------------------------------------
-- SAKAI_ALIAS_PROPERTY
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_ALIAS_PROPERTY
(
       ALIAS_ID             VARCHAR (99) NOT NULL,
       NAME                 VARCHAR (99) NOT NULL,
       VALUE                CLOB
);

ALTER TABLE SAKAI_ALIAS_PROPERTY
       ADD  PRIMARY KEY (ALIAS_ID, NAME)  ;

ALTER TABLE SAKAI_ALIAS_PROPERTY
       ADD  FOREIGN KEY (ALIAS_ID)
                             REFERENCES SAKAI_ALIAS (ALIAS_ID)  ;

-----------------------------------------------------------------------------
-- SAKAI_ALIAS_INDEXES
-----------------------------------------------------------------------------

CREATE INDEX SAKAI_ALIAS_CRTD ON SAKAI_ALIAS
(
       CREATEDBY                      ASC,
       CREATEDON                      ASC
) ALLOW REVERSE SCANS;

CREATE INDEX SAKAI_ALIAS_MOD ON SAKAI_ALIAS
(
       MODIFIEDBY                     ASC,
       MODIFIEDON                     ASC
) ALLOW REVERSE SCANS;

CREATE INDEX SAKAI_ALIAS_TRGT ON SAKAI_ALIAS
(
       TARGET                         ASC
) ALLOW REVERSE SCANS;
