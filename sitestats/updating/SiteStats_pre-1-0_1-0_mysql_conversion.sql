-- This is the MySQL SiteStats 0.5.x -> 1.0.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- Run this before you run your first app server with the updated SiteStats.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- Create job run table
create table SST_JOB_RUN (ID bigint not null auto_increment, JOB_START_DATE datetime, JOB_END_DATE datetime, START_EVENT_ID bigint, END_EVENT_ID bigint, LAST_EVENT_DATE datetime, primary key (ID));
