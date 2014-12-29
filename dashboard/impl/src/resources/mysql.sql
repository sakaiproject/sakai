
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
