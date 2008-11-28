-- This is the MySQL SiteStats 1.x -> 2.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- Run this before you run your first app server with the updated SiteStats.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------


-- Create new reports table
create table SST_REPORTS (ID number(19,0) not null, SITE_ID varchar2(99 char), TITLE varchar2(255 char) not null, DESCRIPTION clob, HIDDEN number(1,0), REPORT_DEF clob not null, CREATED_BY varchar2(99 char) not null, CREATED_ON timestamp not null, MODIFIED_BY varchar2(99 char), MODIFIED_ON timestamp, primary key (ID));
create index SST_REPORTS_SITE_ID_IX on SST_REPORTS (SITE_ID);
create sequence SST_REPORTS_ID;
