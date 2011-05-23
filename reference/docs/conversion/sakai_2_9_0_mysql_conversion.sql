-- KNL-576 provider_id field is too small for large site with long list of provider id
alter table SAKAI_REALM modify PROVIDER_ID varchar(4000);


-- KNL-705 new soft deletion of sites
alter table SAKAI_SITE add IS_SOFTLY_DELETED char(1) not null DEFAULT 0;
alter table SAKAI_SITE add SOFTLY_DELETED_DATE datetime;

-- KNL-725 use a column type that stores the timezone
alter table SAKAI_CLUSTER change UPDATE_TIME UPDATE_TIME TIMESTAMP;

-- KNL-734 type of session and event date column
alter table SAKAI_SESSION change SESSION_START SESSION_START TIMESTAMP;
alter table SAKAI_SESSION change SESSION_END SESSION_END TIMESTAMP;
alter table SAKAI_EVENT change EVENT_DATE EVENT_DATE TIMESTAMP;
