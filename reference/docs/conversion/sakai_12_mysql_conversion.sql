-- New permissions for SAK-30141
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'syllabus.add.item');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'syllabus.bulk.add.item');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'syllabus.bulk.edit.item');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'syllabus.redirect');

-- Maintain
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Instructor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Admininstrator
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Instructor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));


-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new permissions into existing realms
--
-- msg.permissions.allowToField.myGroupMembers
-- syllabus.bulk.add.item
-- syllabus.bulk.edit.item
-- syllabus.redirect
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

-- maintain
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','syllabus.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','syllabus.bulk.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','syllabus.bulk.edit.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','syllabus.redirect');

-- Instructor
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','syllabus.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','syllabus.bulk.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','syllabus.bulk.edit.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','syllabus.redirect');

-- lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper" or "!user.template")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper' AND SR.REALM_ID NOT LIKE '!user.template%'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;

-- ------------------------------
--  END permission backfill -----
-- ------------------------------

-- End permissions for SAK-30141
--
-- SAM-1200 - Increase column data sizes
--
alter table SAM_PUBLISHED_ASSESSMENT_T change description description mediumtext null;
alter table SAM_PUBLISHEDSECTION_T change description description mediumtext null;
alter table SAM_ASSESSMENTBASE_T change description description mediumtext null;
alter table SAM_SECTION_T change description description mediumtext null;
alter table SAM_ITEMGRADING_T change comments comments mediumtext null;
alter table SAM_ASSESSMENTGRADING_T change comments comments mediumtext null

-- ------------------------------
-- DASHBOARD                -----
-- ------------------------------
      
create table dash_availability_check 
( id bigint not null auto_increment, entity_ref varchar(255) not null, 
entity_type_id varchar(255) not null, scheduled_time datetime not null, primary key (id)); 
create unique index dash_availability_check_idx on dash_availability_check(entity_ref, scheduled_time); 
create index dash_availability_check_time_idx on dash_availability_check(scheduled_time);

create table if not exists dash_calendar_item ( id bigint not null auto_increment, 
calendar_time datetime not null, calendar_time_label_key varchar(40), title varchar(255) not null, 
entity_ref varchar(255) not null, entity_type bigint not null, subtype varchar(255), context_id bigint not null, 
repeating_event_id bigint, sequence_num integer, primary key (id) ); 
create index dash_calendar_time_idx on dash_calendar_item (calendar_time); 
create unique index dash_calendar_entity_label_idx on dash_calendar_item (entity_ref, calendar_time_label_key, sequence_num); 
create index dash_calendar_entity_idx on dash_calendar_item (entity_ref); 

create table if not exists dash_calendar_link ( id bigint not null auto_increment, 
person_id bigint not null, context_id bigint not null, item_id bigint not null, hidden bit default 0, 
sticky bit default 0, unique (person_id, context_id, item_id), primary key (id) ); 
create index dash_calendar_link_idx on dash_calendar_link (person_id, context_id, item_id, hidden, sticky);
create index dash_calendar_link_item_id_idx on dash_calendar_link (item_id);

create table if not exists dash_config ( id bigint not null auto_increment, 
property_name varchar(99) not null, property_value integer not null, primary key (id) ); 
create unique index dash_config_name_idx on dash_config(property_name); 
insert into dash_config (property_name, property_value) values ('PROP_DEFAULT_ITEMS_IN_PANEL', 5); 
insert into dash_config (property_name, property_value) values ('PROP_DEFAULT_ITEMS_IN_DISCLOSURE', 20); 
insert into dash_config (property_name, property_value) values ('PROP_DEFAULT_ITEMS_IN_GROUP', 2); 
insert into dash_config (property_name, property_value) values ('PROP_REMOVE_NEWS_ITEMS_AFTER_WEEKS', 8); 
insert into dash_config (property_name, property_value) values ('PROP_REMOVE_STARRED_NEWS_ITEMS_AFTER_WEEKS', 26); 
insert into dash_config (property_name, property_value) values ('PROP_REMOVE_HIDDEN_NEWS_ITEMS_AFTER_WEEKS', 4); 
insert into dash_config (property_name, property_value) values ('PROP_REMOVE_CALENDAR_ITEMS_AFTER_WEEKS', 2); 
insert into dash_config (property_name, property_value) values ('PROP_REMOVE_STARRED_CALENDAR_ITEMS_AFTER_WEEKS', 26); 
insert into dash_config (property_name, property_value) values ('PROP_REMOVE_HIDDEN_CALENDAR_ITEMS_AFTER_WEEKS', 1); 
insert into dash_config (property_name, property_value) values ('PROP_REMOVE_NEWS_ITEMS_WITH_NO_LINKS', 1); 
insert into dash_config (property_name, property_value) values ('PROP_REMOVE_CALENDAR_ITEMS_WITH_NO_LINKS', 1); 
insert into dash_config (property_name, property_value) values ('PROP_DAYS_BETWEEN_HORIZ0N_UPDATES', 1); 
insert into dash_config (property_name, property_value) values ('PROP_WEEKS_TO_HORIZON', 4); 
insert into dash_config (property_name, property_value) values ('PROP_MOTD_MODE', 1); 
insert into dash_config (property_name, property_value) values ('PROP_LOG_MODE_FOR_NAVIGATION_EVENTS', 2); 
insert into dash_config (property_name, property_value) values ('PROP_LOG_MODE_FOR_ITEM_DETAIL_EVENTS', 2); 
insert into dash_config (property_name, property_value) values ('PROP_LOG_MODE_FOR_PREFERENCE_EVENTS', 2); 
insert into dash_config (property_name, property_value) values ('PROP_LOG_MODE_FOR_DASH_NAV_EVENTS', 2);
insert into dash_config (property_name, property_value) values ('PROP_LOOP_TIMER_ENABLED', 0);


create table if not exists dash_context ( id bigint not null auto_increment, context_id varchar(255) not null, 
context_url varchar(1024) not null, context_title varchar(255) not null, primary key (id) ); 
create unique index dash_context_idx on dash_context (context_id);

create table dash_event (event_id bigint auto_increment, event_date timestamp, event varchar (32), 
ref varchar (255), context varchar (255), session_id varchar (163), event_code varchar (1), primary key (event_id));

create table if not exists dash_news_item ( id bigint not null auto_increment, news_time datetime not null, 
news_time_label_key varchar(40), title varchar(255) not null, 
entity_ref varchar(255) not null, entity_type bigint not null, subtype varchar(255), 
context_id bigint not null, grouping_id varchar(90), primary key (id) ); 
create index dash_news_time_idx on dash_news_item (news_time); 
create index dash_news_grouping_idx on dash_news_item (grouping_id); 
create unique index dash_news_entity_idx on dash_news_item (entity_ref);

create table if not exists dash_news_link ( id bigint not null auto_increment, person_id bigint not null, 
context_id bigint not null, item_id bigint not null, hidden bit default 0, sticky bit default 0, 
unique (person_id, context_id, item_id), primary key (id) ); 
create index dash_news_link_idx on dash_news_link (person_id, context_id, item_id, hidden, sticky);
create index dash_news_link_item_id_idx on dash_news_link (item_id);

create table if not exists dash_person ( id bigint not null auto_increment,user_id varchar(99) not null,
sakai_id varchar(99), primary key (id) ); 
create unique index dash_person_user_id_idx on dash_person (user_id); 
create unique index dash_person_sakai_id_idx on dash_person (sakai_id);

create table if not exists dash_repeating_event (id bigint not null auto_increment, 
first_time datetime not null, last_time datetime, frequency varchar(40) not null, max_count integer, 
calendar_time_label_key varchar(40), title varchar(255) not null, 
entity_ref varchar(265) not null, subtype varchar(255), entity_type bigint not null, context_id bigint not null, 
primary key (id) ); 
create index dash_repeating_event_first_idx on dash_repeating_event (first_time); 
create index dash_repeating_event_last_idx on dash_repeating_event (last_time);

create table if not exists dash_sourcetype 
( id bigint not null auto_increment, identifier varchar(255) not null, primary key (id) ); 
create unique index dash_source_idx on dash_sourcetype (identifier);

create table if not exists dash_task_lock
( id bigint not null auto_increment, 
task varchar(255) not null, 
server_id varchar(255) not null, 
claim_time timestamp, 
last_update timestamp, 
has_lock bit default 0,
primary key (id));
create index dash_lock_ct_idx on dash_task_lock (claim_time); 
create unique index dash_lock_ts_idx on dash_task_lock (task, server_id);

--
-- SAK-27929 Add Dashboard to default !user site
--

INSERT INTO SAKAI_SITE_PAGE VALUES('!user-99', '!user', 'Dashboard', '0', 0, '0' );
INSERT INTO SAKAI_SITE_TOOL VALUES('!user-999', '!user-99', '!user', 'sakai.dashboard', 1, 'Dashboard', NULL );
