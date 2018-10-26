-----------------------------------------------------------------------------
-- SAKAI_CLUSTER
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_CLUSTER
(
	SERVER_ID_INSTANCE VARCHAR (64),
	UPDATE_TIME TIMESTAMP,
	STATUS ENUM('STARTING', 'RUNNING', 'CLOSING', 'STOPPING'),
	SERVER_ID VARCHAR (64)
);

ALTER TABLE SAKAI_CLUSTER
       ADD  ( PRIMARY KEY (SERVER_ID_INSTANCE) ) ;
