-- Site related tables added in Sakai 2.1
-----------------------------------------------------------------------------
-- SAKAI_SITE_GROUP
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_SITE_GROUP (
       GROUP_ID           VARCHAR (99) NOT NULL,
       SITE_ID              VARCHAR (99) NOT NULL,
       TITLE                VARCHAR (99) NULL,
       DESCRIPTION          LONGVARCHAR NULL,
       PRIMARY KEY (GROUP_ID)
);

ALTER TABLE SAKAI_SITE_GROUP
       ADD FOREIGN KEY (SITE_ID)
                             REFERENCES SAKAI_SITE (SITE_ID);

CREATE INDEX IE_SAKAI_SITE_GRP_SITE ON SAKAI_SITE_GROUP
(
       SITE_ID                       ASC
);

-----------------------------------------------------------------------------
-- SAKAI_SITE_GROUP_PROPERTY
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_SITE_GROUP_PROPERTY (
       SITE_ID              VARCHAR (99) NOT NULL,
       GROUP_ID           VARCHAR (99) NOT NULL,
       NAME                 VARCHAR (99) NOT NULL,
       VALUE                LONGVARCHAR NULL,
       PRIMARY KEY (GROUP_ID, NAME)
);


ALTER TABLE SAKAI_SITE_GROUP_PROPERTY
       ADD  FOREIGN KEY (GROUP_ID)
                             REFERENCES SAKAI_SITE_GROUP (GROUP_ID);

ALTER TABLE SAKAI_SITE_GROUP_PROPERTY
       ADD  FOREIGN KEY (SITE_ID)
                             REFERENCES SAKAI_SITE (SITE_ID);

CREATE INDEX IE_SAKAI_SITE_GRP_PROP_SITE ON SAKAI_SITE_GROUP_PROPERTY
(
       SITE_ID                       ASC
);
