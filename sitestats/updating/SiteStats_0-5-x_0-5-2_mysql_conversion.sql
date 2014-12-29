-- This is the MySQL SiteStats 0.5.x -> 0.5.2 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- Run this before you run your first app server with the updated SiteStats.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- Change 'DATE' column name: incompatible with ORACLE
-- No conversion script for oracle as tables were not previously created
ALTER TABLE SST_EVENTS CHANGE DATE EVENT_DATE DATE;
ALTER TABLE SST_EVENTS CHANGE COUNT EVENT_COUNT LONG;
ALTER TABLE SST_RESOURCES CHANGE DATE RESOURCE_DATE DATE;
ALTER TABLE SST_RESOURCES CHANGE COUNT RESOURCE_COUNT LONG;
ALTER TABLE SST_SITEACTIVITY CHANGE DATE ACTIVITY_DATE DATE;
ALTER TABLE SST_SITEACTIVITY CHANGE COUNT ACTIVITY_COUNT LONG;
ALTER TABLE SST_SITEVISITS CHANGE DATE VISITS_DATE DATE;