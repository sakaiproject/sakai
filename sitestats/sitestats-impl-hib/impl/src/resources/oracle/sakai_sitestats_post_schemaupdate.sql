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
create index SITE_ID_IX on SST_EVENTS(SITE_ID);
create index USER_ID_IX on SST_EVENTS(USER_ID);
create index EVENT_ID_IX on SST_EVENTS(EVENT_ID);
create index DATE_ID_IX on SST_EVENTS(DATE);

create index SITE_ID_IX on SST_RESOURCES(SITE_ID);
create index USER_ID_IX on SST_RESOURCES(USER_ID);
create index RES_ACT_IDX on SST_RESOURCES(RESOURCE_ACTION);
create index DATE_ID_IX on SST_RESOURCES(DATE);

create index SITE_ID_IX on SST_SITEACTIVITY(SITE_ID);
create index EVENT_ID_IX on SST_SITEACTIVITY(EVENT_ID);
create index DATE_ID_IX on SST_SITEACTIVITY(DATE);

create index SITE_ID_IX on SST_SITEVISITS(SITE_ID);
create index DATE_ID_IX on SST_SITEVISITS(DATE);
