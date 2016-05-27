-- SAK-30141 New permissions
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.nextval, 'syllabus.add.item');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.nextval, 'syllabus.bulk.add.item');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.nextval, 'syllabus.bulk.edit.item');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.nextval, 'syllabus.redirect');

-- maintain
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- maintain
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Instructor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Administrator
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Instructor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Permission backfill

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

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper" OR "!user.template")
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

-- END SAK-30141
--
-- KNL-1200 Oracle conversion, size increases of Samigo columns
--
alter table SAM_PUBLISHED_ASSESSMENT_T add tempcol clob;
update SAM_PUBLISHED_ASSESSMENT_T set tempcol=description;
alter table SAM_PUBLISHED_ASSESSMENT_T drop column description;
alter table SAM_PUBLISHED_ASSESSMENT_T rename column tempcol to description;

alter table SAM_PUBLISHEDSECTION_T add tempcol clob;
update SAM_PUBLISHEDSECTION_T set tempcol=description;
alter table SAM_PUBLISHEDSECTION_T drop column description;
alter table SAM_PUBLISHEDSECTION_T rename column tempcol to description;

alter table SAM_ASSESSMENTBASE_T add tempcol clob;
update SAM_ASSESSMENTBASE_T set tempcol=description;
alter table SAM_ASSESSMENTBASE_T drop column description;
alter table SAM_ASSESSMENTBASE_T rename column tempcol to description;

alter table SAM_SECTION_T add tempcol clob;
update SAM_SECTION_T set tempcol=description;
alter table SAM_SECTION_T drop column description;
alter table SAM_SECTION_T rename column tempcol to description;


alter table SAM_ITEMGRADING_T add tempcol clob;
update SAM_ITEMGRADING_T set tempcol=comments;
alter table SAM_ITEMGRADING_T drop column comments;
alter table SAM_ITEMGRADING_T rename column tempcol to comments;

alter table SAM_ASSESSMENTGRADING_T add tempcol clob;
update SAM_ASSESSMENTGRADING_T set tempcol=comments;
alter table SAM_ASSESSMENTGRADING_T drop column comments;
alter table SAM_ASSESSMENTGRADING_T rename column tempcol to comments;

-- ------------------------------
-- DASHBOARD                -----
-- ------------------------------

create table dash_availability_check 
( id number not null primary key, entity_ref varchar2(255) not null, 
entity_type_id varchar2(255) not null, scheduled_time timestamp(0) not null); 
create sequence dash_availability_check_seq start with 1 increment by 1 nomaxvalue; 
create unique index dash_avail_check_idx on dash_availability_check(entity_ref, scheduled_time); 
create index dash_avail_check_time_idx on dash_availability_check(scheduled_time);

create table dash_calendar_item 
( id number not null primary key, calendar_time timestamp(0) not null, calendar_time_label_key varchar2(40), 
title varchar2(255) not null, 
entity_ref varchar2(255) not null, entity_type number not null, subtype varchar2(255), context_id number not null, 
repeating_event_id number, sequence_num integer ); 
create sequence dash_calendar_item_seq start with 1 increment by 1 nomaxvalue; 
create index dash_cal_time_idx on dash_calendar_item (calendar_time); 
create unique index dash_cal_entity_label_idx on dash_calendar_item (entity_ref, calendar_time_label_key, sequence_num); 
create index dash_cal_entity_idx on dash_calendar_item (entity_ref);

create table dash_calendar_link 
( id number not null primary key, person_id number not null, context_id number not null, 
item_id number not null, hidden number(1,0) default 0, sticky number(1,0) default 0, 
unique (person_id, context_id, item_id) ); 
create sequence dash_calendar_link_seq start with 1 increment by 1 nomaxvalue; 
create index dash_calendar_link_idx on dash_calendar_link (person_id, context_id, item_id, hidden, sticky);
create index dash_calendar_link_item_id_idx on dash_calendar_link (item_id);

create table dash_config ( id number not null primary key, 
property_name varchar2(99) not null, property_value number(10,0) not null ); 
create sequence dash_config_seq start with 1 increment by 1 nomaxvalue;  
create unique index dash_config_name_idx on dash_config(property_name); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_DEFAULT_ITEMS_IN_PANEL', 5); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_DEFAULT_ITEMS_IN_DISCLOSURE', 20); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_DEFAULT_ITEMS_IN_GROUP', 2); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_NEWS_ITEMS_AFTER_WEEKS', 8); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_STARRED_NEWS_ITEMS_AFTER_WEEKS', 26); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_HIDDEN_NEWS_ITEMS_AFTER_WEEKS', 4); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_CALENDAR_ITEMS_AFTER_WEEKS', 2); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_STARRED_CALENDAR_ITEMS_AFTER_WEEKS', 26); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_HIDDEN_CALENDAR_ITEMS_AFTER_WEEKS', 1); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_NEWS_ITEMS_WITH_NO_LINKS', 1); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_CALENDAR_ITEMS_WITH_NO_LINKS', 1); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_DAYS_BETWEEN_HORIZ0N_UPDATES', 1); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_WEEKS_TO_HORIZON', 4); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_MOTD_MODE', 1); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_LOG_MODE_FOR_NAVIGATION_EVENTS', 2); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_LOG_MODE_FOR_ITEM_DETAIL_EVENTS', 2); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_LOG_MODE_FOR_PREFERENCE_EVENTS', 2); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_LOG_MODE_FOR_DASH_NAV_EVENTS', 2);
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_LOOP_TIMER_ENABLED', 0);

create table dash_context 
( id number not null primary key, context_id varchar2(255) not null, 
context_url varchar2(1024) not null, context_title varchar2(255) not null ); 
create sequence dash_context_seq start with 1 increment by 1 nomaxvalue; 
create unique index dash_context_idx on dash_context (context_id);

create table dash_event (event_id number not null primary key, event_date timestamp with time zone, 
event varchar2 (32), ref varchar2 (255), context varchar2 (255), session_id varchar2 (163), event_code varchar2 (1)); 
--create unique index dash_event_index on dash_event (event_id asc); 
create sequence dash_event_seq start with 1 increment by 1 nomaxvalue;

create table dash_news_item ( id number not null primary key, 
news_time timestamp(0) not null, news_time_label_key varchar2(40), title varchar2(255) not null, 
entity_ref varchar2(255) not null, 
entity_type number not null, subtype varchar2(255), context_id number not null, grouping_id varchar2(90) ); 
create sequence dash_news_item_seq start with 1 increment by 1 nomaxvalue; 
create index dash_news_time_idx on dash_news_item (news_time); 
create index dash_news_grouping_idx on dash_news_item (grouping_id); 
create unique index dash_news_entity_idx on dash_news_item (entity_ref);

create table dash_news_link 
( id number not null primary key, person_id number not null, context_id number not null, 
item_id number not null, hidden number(1,0) default 0, sticky number(1,0) default 0, unique (person_id, context_id, item_id) ); 
create sequence dash_news_link_seq start with 1 increment by 1 nomaxvalue; 
create index dash_news_link_idx on dash_news_link (person_id, context_id, item_id, hidden, sticky);
create index dash_news_link_item_id_idx on dash_news_link (item_id);

create table dash_person 
( id number not null primary key,user_id varchar2(99) not null, sakai_id varchar2(99) ); 
create sequence dash_person_seq start with 1 increment by 1 nomaxvalue; 
create unique index dash_person_user_id_idx on dash_person (user_id); 
create unique index dash_person_sakai_id_idx on dash_person (sakai_id);

create table dash_repeating_event (id number not null primary key, 
first_time timestamp(0) not null, last_time timestamp(0), frequency varchar2(40) not null, max_count integer, 
calendar_time_label_key varchar2(40), title varchar2(255) not null, 
entity_ref varchar2(255) not null, subtype varchar2(255), entity_type number not null, context_id number not null ); 
create sequence dash_repeating_event_seq start with 1 increment by 1 nomaxvalue; 
create index dash_repeating_event_first_idx on dash_repeating_event (first_time); 
create index dash_repeating_event_last_idx on dash_repeating_event (last_time);

create table dash_sourcetype 
( id number not null primary key, identifier varchar2(255) not null ); 
create sequence dash_sourcetype_seq start with 1 increment by 1 nomaxvalue; 
create unique index dash_source_idx on dash_sourcetype (identifier);

create table dash_task_lock
( id number not null primary key, 
task varchar2(255) not null, 
server_id varchar2(255) not null, 
claim_time timestamp(9), 
last_update timestamp(9), 
has_lock number(1,0) default 0); 
create sequence dash_task_lock_seq start with 1 increment by 1 nomaxvalue; 
create index dash_lock_ct_idx on dash_task_lock (claim_time); 
create unique index dash_lock_ts_idx on dash_task_lock (task, server_id);

--
-- SAK-27929 Add Dashboard to default !user site
--

INSERT INTO SAKAI_SITE_PAGE VALUES('!user-99', '!user', 'Dashboard', '0', 0, '0' );
INSERT INTO SAKAI_SITE_TOOL VALUES('!user-999', '!user-99', '!user', 'sakai.dashboard', 1, 'Dashboard', NULL );
