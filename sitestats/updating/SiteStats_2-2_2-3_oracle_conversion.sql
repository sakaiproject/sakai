-- This is the Oracle SiteStats 2.2 -> 2.3 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- Run this before you run your first app server with the updated SiteStats.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- STAT-299: Reimplementation of server wide stats
create table SST_SERVERSTATS (ID number(19,0) not null, ACTIVITY_DATE date not null, EVENT_ID varchar2(32 char) not null, ACTIVITY_COUNT number(19,0) default 0 not null, primary key (ID));
create index SST_SERVERSTATS_DATE_IX on SST_SERVERSTATS (ACTIVITY_DATE);
create index SST_SERVERSTATS_EVENT_ID_IX on SST_SERVERSTATS (EVENT_ID);
create sequence SST_SERVERSTATS_ID;

create table SST_USERSTATS (ID number(19,0) not null, LOGIN_DATE date not null, USER_ID varchar2(99 char) not null, LOGIN_COUNT number(19,0) default 0 not null, primary key (ID));
create index SST_USERSTATS_DATE_IX on SST_USERSTATS (LOGIN_DATE);
create index SST_USERSTATS_USER_ID_IX on SST_USERSTATS (USER_ID);
create sequence SST_USERSTATS_ID;
