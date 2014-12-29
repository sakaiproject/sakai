-----------------------------------------------------------------------------
-- SAKAI_CLUSTER
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_CLUSTER
(
	SERVER_ID VARCHAR2 (64),
	UPDATE_TIME TIMESTAMP WITH LOCAL TIME ZONE
);

ALTER TABLE SAKAI_CLUSTER ADD ( CONSTRAINT "SAKAI_CLUSTER_PK" PRIMARY KEY ("SERVER_ID") VALIDATE );
