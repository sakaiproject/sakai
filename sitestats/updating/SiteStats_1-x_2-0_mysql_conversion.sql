-- This is the MySQL SiteStats 1.x -> 2.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- Run this before you run your first app server with the updated SiteStats.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- Create new reports table
create table SST_REPORTS (ID bigint not null auto_increment, SITE_ID varchar(99), TITLE varchar(255) not null, DESCRIPTION longtext, HIDDEN bit, REPORT_DEF text not null, CREATED_BY varchar(99) not null, CREATED_ON datetime not null, MODIFIED_BY varchar(99), MODIFIED_ON datetime, primary key (ID));
create index SST_REPORTS_SITE_ID_IX on SST_REPORTS (SITE_ID);
