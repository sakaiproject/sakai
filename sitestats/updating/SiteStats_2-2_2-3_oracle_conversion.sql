-- This is the Oracle SiteStats 2.2 -> 2.3 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- Run this before you run your first app server with the updated SiteStats.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- STAT-241: Tracking of time spent in site
create table SST_SERVERSTATS (ID number(19,0) not null, ACTIVITY_DATE date not null, EVENT_ID varchar2(32 char) not null, EVENT_COUNT number(19,0) default 0 not null, primary key (ID));
create index SST_SERVERSTATS_DATE_IX on SST_SERVERSTATS (ACTIVITY_DATE);
create index SST_SERVERSTATS_EVENT_ID_IX on SST_SERVERSTATS (EVENT_ID);
create sequence SST_SERVERSTATS_ID;
