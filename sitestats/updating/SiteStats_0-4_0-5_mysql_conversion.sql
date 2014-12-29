-- This is the MySQL SiteStats 0.4 -> 0.5 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a SiteStats database from 0.x to 0.4.  Run this before you run your first app server with the updated SiteStats.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- SiteStats Update table
DROP TABLE SST_SITEUPDATE;