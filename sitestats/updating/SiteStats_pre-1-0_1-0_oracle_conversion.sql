-- This is the MySQL SiteStats 0.5.x -> 1.0.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- Run this before you run your first app server with the updated SiteStats.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- Create new job run table
create table SST_JOB_RUN (ID number(19,0) not null, JOB_START_DATE timestamp, JOB_END_DATE timestamp, START_EVENT_ID number(19,0), END_EVENT_ID number(19,0), LAST_EVENT_DATE timestamp, primary key (ID));
