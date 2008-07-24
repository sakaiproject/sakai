-----------------------------------------------------------------------------
-- SAKAI_CLUSTER
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_CLUSTER
(
	SERVER_ID VARCHAR (64) NOT NULL,
	UPDATE_TIME TIMESTAMP
);

ALTER TABLE SAKAI_CLUSTER
       ADD   PRIMARY KEY (SERVER_ID)  ;
