-- Site related tables added in Sakai 2.1
-----------------------------------------------------------------------------
-- SAKAI_SITE_GROUP
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_SITE_GROUP (
       GROUP_ID             VARCHAR2(99) NOT NULL,
       SITE_ID              VARCHAR2(99) NOT NULL,
       TITLE                VARCHAR2(99) NULL,
       DESCRIPTION          CLOB NULL
);

ALTER TABLE SAKAI_SITE_GROUP
       ADD  ( PRIMARY KEY (GROUP_ID) ) ;

ALTER TABLE SAKAI_SITE_GROUP
       ADD  ( FOREIGN KEY (SITE_ID)
                             REFERENCES SAKAI_SITE ) ;

CREATE INDEX IE_SAKAI_SITE_GRP_SITE ON SAKAI_SITE_GROUP
(
       SITE_ID                       ASC
);

-----------------------------------------------------------------------------
-- SAKAI_SITE_GROUP_PROPERTY
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_SITE_GROUP_PROPERTY (
       SITE_ID              VARCHAR2(99) NOT NULL,
       GROUP_ID             VARCHAR2(99) NOT NULL,
       NAME                 VARCHAR2(99) NOT NULL,
       VALUE                CLOB NULL
);

ALTER TABLE SAKAI_SITE_GROUP_PROPERTY
       ADD  ( PRIMARY KEY (GROUP_ID, NAME) ) ;

ALTER TABLE SAKAI_SITE_GROUP_PROPERTY
       ADD  ( FOREIGN KEY (GROUP_ID)
                             REFERENCES SAKAI_SITE_GROUP ) ;

ALTER TABLE SAKAI_SITE_GROUP_PROPERTY
       ADD  ( FOREIGN KEY (SITE_ID)
                             REFERENCES SAKAI_SITE ) ;

CREATE INDEX IE_SAKAI_SITE_GRP_PROP_SITE ON SAKAI_SITE_GROUP_PROPERTY
(
       SITE_ID                       ASC
);
