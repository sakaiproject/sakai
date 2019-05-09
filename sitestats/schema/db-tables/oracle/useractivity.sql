CREATE TABLE SST_DETAILED_EVENTS
	(ID NUMBER(19,0) NOT NULL,
	USER_ID VARCHAR2(99 CHAR) NOT NULL,
	SITE_ID VARCHAR2(99 CHAR) NOT NULL,
	EVENT_ID VARCHAR2(32 CHAR) NOT NULL,
	EVENT_DATE TIMESTAMP (6) NOT NULL,
	EVENT_REF VARCHAR2(512 CHAR) NOT NULL,
	PRIMARY KEY (ID));

create index idx_site_id_date on sst_detailed_events(site_id,event_date);
create index idx_site_id_user_id_date on sst_detailed_events(site_id,user_id,event_date);

create sequence SST_DETAILED_EVENTS_ID;
