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

