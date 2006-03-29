-----------------------------------------------------------------------------
-- SAKAI_USER_PROPERTY
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_USER_PROPERTY (
       USER_ID             VARCHAR2(99) NOT NULL,
       NAME                 VARCHAR2(99) NOT NULL,
       VALUE                CLOB NULL
);


ALTER TABLE SAKAI_USER_PROPERTY
       ADD  ( PRIMARY KEY (USER_ID, NAME) ) ;

-----------------------------------------------------------------------------
-- SAKAI_USER
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_USER (
       USER_ID              VARCHAR2(99) NOT NULL,
       EMAIL                VARCHAR2(255) NULL,
       EMAIL_LC             VARCHAR2(255) NULL,
       FIRST_NAME           VARCHAR2(255) NULL,
       LAST_NAME            VARCHAR2(255) NULL,
       TYPE                 VARCHAR2(255) NULL,
       PW                   VARCHAR2(255) NULL,
       CREATEDBY            VARCHAR2(99) NOT NULL,
       MODIFIEDBY           VARCHAR2(99) NOT NULL,
       CREATEDON            TIMESTAMP NOT NULL,
       MODIFIEDON           TIMESTAMP NOT NULL
);

ALTER TABLE SAKAI_USER
       ADD  ( PRIMARY KEY (USER_ID) ) ;


ALTER TABLE SAKAI_USER_PROPERTY
       ADD  ( FOREIGN KEY (USER_ID)
                             REFERENCES SAKAI_USER ) ;

CREATE INDEX IE_SAKAI_USER_CREATED ON SAKAI_USER
(
       CREATEDBY                      ASC,
       CREATEDON                      ASC
);

CREATE INDEX IE_SAKAI_USER_MODDED ON SAKAI_USER
(
       MODIFIEDBY                     ASC,
       MODIFIEDON                     ASC
);

CREATE INDEX IE_SAKAI_USER_EMAIL ON SAKAI_USER
(
       EMAIL_LC                       ASC
);

-- populate with the admin and postmaster users

INSERT INTO SAKAI_USER VALUES ('admin', '', '', 'Sakai', 'Administrator', '', 'ISMvKXpXpadDiUoOSoAf', 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO SAKAI_USER VALUES ('postmaster', '', '', 'Sakai', 'Postmaster', '', '', 'postmaster', 'postmaster', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
