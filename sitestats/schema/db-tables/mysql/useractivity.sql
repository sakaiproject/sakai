create table SST_DETAILED_EVENTS
	(ID bigint not null auto_increment,
	USER_ID varchar(99) not null,
	SITE_ID varchar(99) not null,
	EVENT_ID varchar(32) not null,
	EVENT_DATE datetime not null,
	EVENT_REF varchar(255) not null,
	primary key (ID));

create index idx_site_id_date on sst_detailed_events(site_id,event_date);
create index idx_site_id_user_id_date on sst_detailed_events(site_id,user_id,event_date);
