-- KNL-576 provider_id field is too small for large site with long list of provider id
alter table SAKAI_REALM modify PROVIDER_ID varchar2(4000);


-- KNL-705 new soft deletion of sites
-- TODO needs checking for correct syntax - DH
alter table SAKAI_SITE add IS_SOFTLY_DELETED char(1) not null DEFAULT 0;
alter table SAKAI_SITE add SOFTLY_DELETED_DATE datetime;


-- KNL-725 use a datetype with timezone
-- Make sure sakai is stopped when running this.
-- Empty the SAKAI_CLUSTER, Oracle refuses to alter the table with records in it.
DELETE FROM SAKAI_CLUSTER;
-- Change the datatype
ALTER TABLE SAKAI_CLUSTER MODIFY (UPDATE_TIME TIMESTAMP WITH TIME ZONE); 



-- KNL-735 use a datetype with timezone
-- Make sure sakai is stopped when running this.
-- Empty the SAKAI_EVENT & SAKAI_SESSION, Oracle refuses to alter the table with records in it.
DELETE FROM SAKAI_EVENT;
DELETE FROM SAKAI_SESSION;

-- Change the datatype
ALTER TABLE SAKAI_EVENT MODIFY (EVENT_DATE TIMESTAMP WITH TIME ZONE); 
-- Change the datatype
ALTER TABLE SAKAI_SESSION MODIFY (SESSION_START TIMESTAMP WITH TIME ZONE); 
ALTER TABLE SAKAI_SESSION MODIFY (SESSION_END TIMESTAMP WITH TIME ZONE); 
