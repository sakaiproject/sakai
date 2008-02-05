-----------------------------------------------------------------------------
-- SAKAI_CLUSTER
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_CLUSTER
(
	SERVER_ID NVARCHAR (64) NOT NULL,
	UPDATE_TIME DATETIME
)

ALTER TABLE SAKAI_CLUSTER
       ADD  CONSTRAINT SAKAI_CLUSTER_PK PRIMARY KEY (SERVER_ID)
;