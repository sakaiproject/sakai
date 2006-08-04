-- Most automatic database initialization will be taken care of automatically
-- by Hibernate's SchemaUpdate tool, triggered by the hibernate.hbm2ddl.auto
-- property in vanilla Hibernate applications and by the auto.ddl property
-- in the Sakai framework.
--
-- Not all necessary elements might be created by SchemaUpdate, however.
-- Notably, in versions of Hibernate through at least 3.1.3, no explicit
-- index definitions in the mapping file will be honored except during a
-- full SchemaExport.
--
-- This file creates schema in reverse order of when they were added to
-- SiteStats out-of-the-box SQL, to increase the chances that the script
-- will have useful results as an upgrader as well as an initializer.
select 1 from DUAL;
ALTER INDEX SITE_ID_IX RENAME TO SST_EVENTS_SITE_ID_IX; 
ALTER INDEX USER_ID_IX RENAME TO SST_EVENTS_USER_ID_IX; 
ALTER INDEX EVENT_ID_IX RENAME TO SST_EVENTS_EVENT_ID_IX; 
ALTER INDEX DATE_ID_IX RENAME TO SST_EVENTS_DATE_ID_IX;
