drop table if exists SST_EVENTS;
drop table if exists SST_PREFS;
drop table if exists SST_RESOURCES;
drop table if exists SST_SITEACTIVITY;
drop table if exists SST_SITEVISITS;
create table SST_EVENTS (ID bigint not null auto_increment, USER_ID varchar(99) not null, SITE_ID varchar(99) not null, EVENT_ID varchar(32) not null, EVENT_DATE date not null, EVENT_COUNT bigint not null, primary key (ID));
create table SST_PREFS (ID bigint not null auto_increment, SITE_ID varchar(99) not null, EVENT_ID varchar(99) not null, PAGE integer not null, primary key (ID));
create table SST_RESOURCES (ID bigint not null auto_increment, USER_ID varchar(99) not null, SITE_ID varchar(99) not null, RESOURCE_REF varchar(255) not null, RESOURCE_ACTION varchar(12) not null, RESOURCE_DATE date not null, RESOURCE_COUNT bigint not null, primary key (ID));
create table SST_SITEACTIVITY (ID bigint not null auto_increment, SITE_ID varchar(99) not null, ACTIVITY_DATE date not null, EVENT_ID varchar(32) not null, ACTIVITY_COUNT bigint not null, primary key (ID));
create table SST_SITEVISITS (ID bigint not null auto_increment, SITE_ID varchar(99) not null, VISITS_DATE date not null, TOTAL_VISITS bigint not null, TOTAL_UNIQUE bigint not null, primary key (ID));
create index SST_EVENTS_SITE_ID_IX on SST_EVENTS (SITE_ID);
create index SST_EVENTS_USER_ID_IX on SST_EVENTS (USER_ID);
create index SST_EVENTS_EVENT_ID_IX on SST_EVENTS (EVENT_ID);
create index SST_EVENTS_DATE_IX on SST_EVENTS (EVENT_DATE);
create index SITE_ID_IDX on SST_PREFS (SITE_ID);
create index SST_RESOURCES_DATE_IX on SST_RESOURCES (RESOURCE_DATE);
create index SST_RESOURCES_RES_ACT_IDX on SST_RESOURCES (RESOURCE_ACTION);
create index SST_RESOURCES_USER_ID_IX on SST_RESOURCES (USER_ID);
create index SST_RESOURCES_SITE_ID_IX on SST_RESOURCES (SITE_ID);
create index SST_SITEACTIVITY_DATE_IX on SST_SITEACTIVITY (ACTIVITY_DATE);
create index SST_SITEACTIVITY_EVENT_ID_IX on SST_SITEACTIVITY (EVENT_ID);
create index SST_SITEACTIVITY_SITE_ID_IX on SST_SITEACTIVITY (SITE_ID);
create index SST_SITEVISITS_SITE_ID_IX on SST_SITEVISITS (SITE_ID);
create index SST_SITEVISITS_DATE_IX on SST_SITEVISITS (VISITS_DATE);

-- John Leasia suggestion to increase performance
create index idx_SST_EVENTS_SID_EID_EDATE on SST_EVENTS (SITE_ID,EVENT_ID,EVENT_DATE);
