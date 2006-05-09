-----------------------------------------------------------------------------
-- SAKAI_USER_ID_MAP
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_USER_ID_MAP
(
       USER_ID             VARCHAR2(99) NOT NULL,
       EID                 VARCHAR2(99) NOT NULL
);


ALTER TABLE SAKAI_USER_ID_MAP
       ADD  ( PRIMARY KEY (USER_ID) ) ;

CREATE UNIQUE INDEX AK_SAKAI_USER_ID_MAP_EID ON SAKAI_USER_ID_MAP
(
       EID                       ASC
);

-- populate with the admin and postmaster users

INSERT INTO SAKAI_USER_ID_MAP VALUES ('admin', 'admin');
INSERT INTO SAKAI_USER_ID_MAP VALUES ('postmaster', 'postmaster');
