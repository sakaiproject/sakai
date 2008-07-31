-----------------------------------------------------------------------------
-- SAKAI_USER_PROPERTY
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_USER_PROPERTY
(
       USER_ID             VARCHAR (99) NOT NULL,
       NAME                 VARCHAR (99) NOT NULL,
       VALUE                CLOB
)
;

ALTER TABLE SAKAI_USER_PROPERTY
       ADD  CONSTRAINT SAKAI_USERPROP_PK PRIMARY KEY (USER_ID, NAME)
;
-----------------------------------------------------------------------------
-- SAKAI_USER
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_USER
(
       USER_ID              VARCHAR (99) NOT NULL,
       EMAIL                VARCHAR (255) ,
       EMAIL_LC             VARCHAR (255) ,
       FIRST_NAME           VARCHAR (255) ,
       LAST_NAME            VARCHAR (255) ,
       TYPE                 VARCHAR (255) ,
       PW                   VARCHAR (255) ,
       CREATEDBY            VARCHAR (99) NOT NULL,
       MODIFIEDBY           VARCHAR (99) NOT NULL,
       CREATEDON            TIMESTAMP NOT NULL,
       MODIFIEDON           TIMESTAMP NOT NULL
)
;
ALTER TABLE SAKAI_USER
       ADD CONSTRAINT SAKAI_USER_PK PRIMARY KEY (USER_ID)
;

ALTER TABLE SAKAI_USER_PROPERTY
       ADD CONSTRAINT SAKAI_USER_FK FOREIGN KEY (USER_ID)
                             REFERENCES SAKAI_USER (USER_ID)
;
CREATE INDEX SAKAI_USER_CRTD ON SAKAI_USER
(
       CREATEDBY                      ASC,
       CREATEDON                      ASC
) ALLOW REVERSE SCANS
;
CREATE INDEX SAKAI_USER_MODDED ON SAKAI_USER
(
       MODIFIEDBY                     ASC,
       MODIFIEDON                     ASC
) ALLOW REVERSE SCANS
;
CREATE INDEX SAKAI_USER_EMAIL ON SAKAI_USER
(
       EMAIL_LC                       ASC
) ALLOW REVERSE SCANS
;
-----------------------------------------------------------------------------
-- SAKAI_USER_ID_MAP
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_USER_ID_MAP
(
       USER_ID             VARCHAR (99) NOT NULL,
       EID                 VARCHAR (255) NOT NULL
) 
;

ALTER TABLE SAKAI_USER_ID_MAP
       ADD CONSTRAINT SAKAI_USER_IMAP_PK PRIMARY KEY (USER_ID)
;

CREATE UNIQUE INDEX SAKAI_UID_MAP_EID ON SAKAI_USER_ID_MAP
(
       EID                       ASC
) ALLOW REVERSE SCANS
;
-- populate with the admin and postmaster users

INSERT INTO SAKAI_USER(USER_ID, EMAIL, EMAIL_LC, FIRST_NAME, LAST_NAME, TYPE, PW, CREATEDBY, MODIFIEDBY, CREATEDON, MODIFIEDON)
VALUES ('admin', '', '', 'Sakai', 'Administrator', '', 'ISMvKXpXpadDiUoOSoAf', 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
;

INSERT INTO SAKAI_USER(USER_ID, EMAIL, EMAIL_LC, FIRST_NAME, LAST_NAME, TYPE, PW, CREATEDBY, MODIFIEDBY, CREATEDON, MODIFIEDON)
VALUES ('postmaster', '', '', 'Sakai', 'Postmaster', '', '', 'postmaster', 'postmaster', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
;
INSERT INTO SAKAI_USER_ID_MAP (USER_ID, EID) VALUES ('admin', 'admin')
;
INSERT INTO SAKAI_USER_ID_MAP (USER_ID, EID) VALUES ('postmaster', 'postmaster')
;
