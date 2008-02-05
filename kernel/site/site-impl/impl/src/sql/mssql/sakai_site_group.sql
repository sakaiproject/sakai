-- Site related tables added in Sakai 2.1
-----------------------------------------------------------------------------
-- SAKAI_SITE_GROUP
-----------------------------------------------------------------------------
CREATE TABLE SAKAI_SITE_GROUP
(
       GROUP_ID             NVARCHAR (99) NOT NULL,
       SITE_ID              NVARCHAR (99) NOT NULL,
       TITLE                NVARCHAR (99) NULL,
       DESCRIPTION          NVARCHAR(MAX) NULL
)
;
sp_tableoption 'SAKAI_SITE_GROUP', 'large value types out of row', 'true'
;
ALTER TABLE SAKAI_SITE_GROUP
       ADD  CONSTRAINT SAKAI_SITE_GROUP_PK PRIMARY KEY (GROUP_ID)

ALTER TABLE SAKAI_SITE_GROUP
       ADD  CONSTRAINT SAKAI_SITE_GROUP_FK1 FOREIGN KEY (SITE_ID)
                             REFERENCES SAKAI_SITE (SITE_ID)

CREATE INDEX IE_SAKAI_SITE_GRP_SITE ON SAKAI_SITE_GROUP
(
       SITE_ID                       ASC
)

-----------------------------------------------------------------------------
-- SAKAI_SITE_GROUP_PROPERTY
-----------------------------------------------------------------------------
CREATE TABLE SAKAI_SITE_GROUP_PROPERTY
(
       SITE_ID              NVARCHAR (99) NOT NULL,
       GROUP_ID             NVARCHAR (99) NOT NULL,
       NAME                 NVARCHAR (99) NOT NULL,
       VALUE                NVARCHAR(MAX) NULL
)
;
sp_tableoption 'SAKAI_SITE_GROUP_PROPERTY', 'large value types out of row', 'true'
;
ALTER TABLE SAKAI_SITE_GROUP_PROPERTY
       ADD  CONSTRAINT SAKAI_SITE_GROUP_PROPERTY_PK PRIMARY KEY (GROUP_ID, NAME)

ALTER TABLE SAKAI_SITE_GROUP_PROPERTY
       ADD  CONSTRAINT SAKAI_SITE_GROUP_PROPERTY_FK1 FOREIGN KEY (GROUP_ID)
                             REFERENCES SAKAI_SITE_GROUP (GROUP_ID)

ALTER TABLE SAKAI_SITE_GROUP_PROPERTY
       ADD  CONSTRAINT SAKAI_SITE_GROUP_PROPERTY_FK2 FOREIGN KEY (SITE_ID)
                             REFERENCES SAKAI_SITE (SITE_ID)

CREATE INDEX IE_SAKAI_SITE_GRP_PROP_SITE ON SAKAI_SITE_GROUP_PROPERTY
(
       SITE_ID                       ASC
)
;