alter table signup_site_groups drop foreign key FKC72B75255084316;
alter table signup_sites drop foreign key FKCCD4AC25CB1E8A17;
alter table signup_ts drop foreign key FK41154B06CB1E8A17;
alter table signup_ts_attendees drop foreign key FKBAB08100CDB30B3D;
alter table signup_ts_waitinglist drop foreign key FK3AB9A8B2CDB30B3D;
alter table signup_attachments drop foreign key FK3BCB709CB1E8A17;
drop table if exists signup_meetings;
drop table if exists signup_site_groups;
drop table if exists signup_sites;
drop table if exists signup_ts;
drop table if exists signup_ts_attendees;
drop table if exists signup_ts_waitinglist;
drop table if exists signup_attachments;

create table signup_meetings (
	id bigint not null auto_increment, 
	version integer not null, 
	title varchar(255) not null, 
	description text, 
	location varchar(255) not null,
	category varchar(255) default null,
	meeting_type varchar(50) not null, 
	creator_user_id varchar(255) not null,
	coordinators_user_Ids   varchar(1000) default null,
	start_time datetime not null, 
	end_time datetime not null, 
	signup_begins datetime, 
	signup_deadline datetime, 
	canceled bit, 
	locked bit,
	receive_email_owner bit default false,
	default_send_email_by_owner bit(1) default '\0',
	recurrence_id bigint,
	repeat_type varchar(20) default null,
	allow_waitList bit(1) default 1,
  	allow_comment bit(1) default 1,
  	eid_input_mode bit(1) default '\0',
  	auto_reminder bit(1) default '\0',
  	allow_attendance bit(1) default '\0',
  	create_groups bit(1) default '\0',
  	maxnumof_slot integer default 1,
  	vevent_uuid  VARCHAR(255)  default NULL,
	primary key (id)
) ENGINE=InnoDB;

create table signup_site_groups (
	signup_site_id bigint not null, 
	title varchar(255), 
	group_id varchar(255) not null, 
	calendar_event_id varchar(2000), 
	calendar_id varchar(255), 
	list_index integer not null, 
	primary key (signup_site_id, list_index)
) ENGINE=InnoDB;

create table signup_sites (
	id bigint not null auto_increment, 
	version integer not null, 
	title varchar(255), 
	site_id varchar(255) not null, 
	calendar_event_id varchar(2000), 
	calendar_id varchar(255), 
	meeting_id bigint not null, 
	list_index integer, 
	primary key (id)
) ENGINE=InnoDB;

create table signup_ts (
	id bigint not null auto_increment, 
	version integer not null, 
	start_time datetime not null, 
	end_time datetime not null, 
	max_no_of_attendees integer, 
	display_attendees bit, 
	canceled bit, locked bit, 
	meeting_id bigint not null, 
	list_index integer, 
	group_id varchar(255),
	vevent_uuid VARCHAR(255) DEFAULT NULL,
	primary key (id)
) ENGINE=InnoDB;

create table signup_ts_attendees (
	timeslot_id bigint not null, 
	attendee_user_id varchar(255) not null, 
	comments text, 
	signup_site_id varchar(255) not null, 
	calendar_event_id varchar(255), 
	calendar_id varchar(255), 
	list_index integer not null, 
	attended bit(1) default '\0',
	primary key (timeslot_id, list_index)
) ENGINE=InnoDB;

create table signup_ts_waitinglist (
	timeslot_id bigint not null, 
	attendee_user_id varchar(255) not null, 
	comments text, 
	signup_site_id varchar(255) not null, 
	calendar_event_id varchar(255), 
	calendar_id varchar(255), 
	list_index integer not null, 
  	attended bit(1) default '\0',
	primary key (timeslot_id, list_index)
) ENGINE=InnoDB;


CREATE TABLE  signup_attachments (
  	meeting_id bigint(20) NOT NULL,
  	resource_Id varchar(255) default NULL,
  	file_name varchar(255) default NULL,
  	mime_type varchar(80) default NULL,
  	fileSize bigint(20) default NULL,
  	location text,
  	isLink bit(1) default NULL,
  	timeslot_id bigint(20) default NULL,
  	view_by_all bit(1) default 1,
  	created_by varchar(255) NOT NULL,
  	created_date datetime NOT NULL,
  	last_modified_by varchar(255) NOT NULL,
  	last_modified_date datetime NOT NULL,
  	list_index integer not null,
  PRIMARY KEY  (meeting_id,list_index)
) ENGINE=InnoDB;



alter table signup_site_groups 
	add index FKC72B75255084316 (signup_site_id), 
	add constraint FKC72B75255084316 
	foreign key (signup_site_id) 
	references signup_sites (id);
alter table signup_sites 
	add index FKCCD4AC25CB1E8A17 (meeting_id), 
	add constraint FKCCD4AC25CB1E8A17 
	foreign key (meeting_id) 
	references signup_meetings (id);
alter table signup_ts 
	add index FK41154B06CB1E8A17 (meeting_id), 
	add constraint FK41154B06CB1E8A17 
	foreign key (meeting_id) 
	references signup_meetings (id);
alter table signup_ts_attendees 
	add index FKBAB08100CDB30B3D (timeslot_id), 
	add constraint FKBAB08100CDB30B3D 
	foreign key (timeslot_id) 
	references signup_ts (id);
alter table signup_ts_waitinglist 
	add index FK3AB9A8B2CDB30B3D (timeslot_id), 
	add constraint FK3AB9A8B2CDB30B3D 
	foreign key (timeslot_id) 
	references signup_ts (id);
alter table signup_attachments 
	add index FK3BCB709CB1E8A17 (meeting_id), 
	add constraint FK3BCB709CB1E8A17 
	foreign key (meeting_id) 
	references signup_meetings (id);

create index IDX_SITE_ID on signup_sites (site_id);

