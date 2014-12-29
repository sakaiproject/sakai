-- This is the MySQL SiteStats 0.x -> 0.4 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a SiteStats database from 0.x to 0.4.  Run this before you run your first app server with the updated SiteStats.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- SiteStats Preferences
ALTER TABLE SST_PREFS DROP PRIMARY KEY;
ALTER TABLE SST_PREFS ADD ID BIGINT NOT NULL AUTO_INCREMENT, ADD PRIMARY KEY (ID);
ALTER TABLE SST_PREFS ADD COLUMN PAGE INT DEFAULT 1;

