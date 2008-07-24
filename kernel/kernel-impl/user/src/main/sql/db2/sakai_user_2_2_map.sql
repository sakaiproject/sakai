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
       ADD CONSTRAINT SAKAI_USERID_MAPPK PRIMARY KEY (USER_ID)
;
 
CREATE UNIQUE INDEX SAKAI_USERIDMAPEID ON SAKAI_USER_ID_MAP
(
       EID                       ASC
) ALLOW REVERSE SCANS
;

INSERT INTO SAKAI_USER_ID_MAP VALUES ('admin', 'admin')
;
INSERT INTO SAKAI_USER_ID_MAP VALUES ('postmaster', 'postmaster')
;
