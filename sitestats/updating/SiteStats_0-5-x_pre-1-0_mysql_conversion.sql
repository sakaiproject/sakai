-- This is the MySQL SiteStats 0.5.x -> 1.0.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- Run this before you run your first app server with the updated SiteStats.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- Create new preferences table
create table SST_PREFERENCES (ID bigint not null auto_increment, SITE_ID varchar(99) not null unique, PREFS text not null, primary key (ID));
create index SST_PREFERENCES_SITE_ID_IX on SST_PREFERENCES (SITE_ID);
