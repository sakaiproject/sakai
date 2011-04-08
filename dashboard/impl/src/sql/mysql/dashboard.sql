create table dash_person (
	id bigint not null auto_increment primary key,
	user_id varchar(255) not null
);

create unique index dash_person_user_id_idx on dash_person (user_id);

create table dash_context (
	id bigint not null auto_increment primary key,
	context_id varchar(255) not null, 
	context_url varchar(255) not null,
	context_title varchar(255) not null
);

create unique index dash_context_idx on dash_context (context_id);

create table dash_source (
	id bigint not null auto_increment primary key,
	source_id varchar(255) not null
);

create unique index dash_source_idx on dash_source (source_id);

create table dash_realm (
	id bigint not null auto_increment primary key,
	realm_id varchar(255) not null
);

create unique index dash_realm_idx on dash_realm (realm_id);

create table dash_event_item (
	id bigint not null auto_increment primary key,
	event_time timestamp not null,
	title varchar(255) not null,
	access_url varchar(255) not null,
	entity_id varchar(255) not null,
	context_id bigint not null,
	realm_id bigint not null
);

create index dash_event_time_idx on dash_event_item (event_time);
create unique index dash_event_entity_idx on dash_event_item (entity_id);

create table dash_news_item (
	id bigint not null auto_increment primary key,
	news_time timestamp,
	title varchar(255) not null,
	access_url varchar(255) not null,
	entity_id varchar(255) not null,
	entity_type bigint not null,
	context_id bigint not null,
	realm_id bigint not null
);

create index dash_news_time_idx on dash_news_item (news_time);
create unique index dash_news_entity_idx on dash_news_item (entity_id);

create table dash_event_join (
	id bigint not null auto_increment primary key,
	person_id bigint not null,
	context_id bigint not null,
	item_id bigint not null,
	realm_id bigint not null,
	hidden bit default 0,
	sticky bit default 0,
	unique (person_id, context_id, item_id)
);

create index dash_event_join_idx on dash_event_join (person_id, context_id, item_id, hidden, sticky);

create table dash_news_join (
	id bigint not null auto_increment primary key,
	person_id bigint not null,
	context_id bigint not null,
	item_id bigint not null,
	realm_id bigint,
	hidden bit default 0,
	sticky bit default 0,
	unique (person_id, context_id, item_id)
);

create index dash_news_join_idx on dash_news_join (person_id, context_id, item_id, hidden, sticky);

create table dash_context_hidden (
	id bigint not null auto_increment primary key,
	person_id bigint not null,
	context_id bigint not null,
	hidden bit default 0
);

create unique index dash_context_hidden_idx on dash_context_hidden (person_id, context_id);

create table dash_source_hidden (
	id bigint not null auto_increment primary key,
	person_id bigint not null,
	context_id bigint not null,
	source_id bigint not null,
	hidden bit default 0
);

create unique index dash_source_hidden_idx on dash_source_hidden(person_id, source_id);



