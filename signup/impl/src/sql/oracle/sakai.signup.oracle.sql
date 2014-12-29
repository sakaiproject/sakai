drop table signup_meetings cascade constraints;
drop table signup_site_groups cascade constraints;
drop table signup_sites cascade constraints;
drop table signup_ts_attendees cascade constraints;
drop table signup_ts_waitinglist cascade constraints;
drop table signup_ts cascade constraints;
drop table signup_attachments cascade constraints;
drop sequence signup_meeting_ID_SEQ;
drop sequence signup_sites_ID_SEQ;
drop sequence signup_ts_ID_SEQ;

create table signup_meetings (
	id number(19,0) not null,
	version number(10,0) not null, 
	title varchar2(255 char) not null,
	description clob,
	location varchar2(255 char) not null,
	category varchar2(255 char) default null,
	meeting_type varchar2(50 char) not null,
	creator_user_id varchar2(99 char) not null,
	coordinators_user_Ids   varchar2(1000 char)  default NULL,
	start_time timestamp not null,
	end_time timestamp not null,
	signup_begins timestamp,
	signup_deadline timestamp,
	canceled number(1,0),
	locked number(1,0),
	allow_waitList  number(1,0)  default '1',
	allow_comment   number(1,0)  default '1',
	auto_reminder   number(1,0)  default '0' NULL,	
	eid_input_mode  number(1,0)  default '0' NULL,
	receive_email_owner number(1,0),
	default_send_email_by_owner   number(1,0)  default '0' NULL,
	allow_attendance  number(1,0)  default '0' NULL,
	recurrence_id number(19,0),
	repeat_type varchar2(20 char) default NULL,
	maxnumof_slot 	number(10,0) default 1,
	create_groups   number(1,0)  default '0' NULL,
	vevent_uuid		varchar2(36) default NULL,
	primary key (id));
	
create table signup_site_groups (
	signup_site_id number(19,0) not null,
	title varchar2(255 char),
	group_id varchar2(99 char) not null,
	calendar_event_id varchar2(2000 char),
	calendar_id varchar2(99 char),
	list_index number(10,0) not null,
	primary key (signup_site_id, list_index));
	
create table signup_sites (
	id number(19,0) not null,
	version number(10,0) not null,
	title varchar2(255 char),
	site_id varchar2(99 char) not null,
	calendar_event_id varchar2(2000 char),
	calendar_id varchar2(99 char),
	meeting_id number(19,0) not null,
	list_index number(10,0),
	primary key (id));

create table signup_ts (
	id number(19,0) not null,
	version number(10,0) not null,
	start_time timestamp not null,
	end_time timestamp not null,
	max_no_of_attendees number(10,0),
	display_attendees number(1,0),
	canceled number(1,0),
	locked number(1,0),
	group_id  VARCHAR2(99)  default NULL,
	vevent_uuid  VARCHAR2(36)  default NULL,
	meeting_id number(19,0) not null,
	list_index number(10,0),
	primary key (id));
	
create table signup_ts_attendees (
	timeslot_id number(19,0) not null,
	attendee_user_id varchar2(99 char) not null,
	comments clob,
	signup_site_id varchar2(99 char) not null,
	calendar_event_id varchar2(2000 char),
	calendar_id varchar2(99 char),
	attended  number(1,0)  default '0' NULL,
	list_index number(10,0) not null,
	primary key (timeslot_id, list_index));
	
create table signup_ts_waitinglist (
	timeslot_id number(19,0) not null, 
	attendee_user_id varchar2(99 char) not null,
	comments clob,
	signup_site_id varchar2(99 char) not null,
	calendar_event_id varchar2(2000 char),
	calendar_id varchar2(99 char),
	list_index number(10,0) not null,
	attended  number(1,0)  default '0' NULL,
	primary key (timeslot_id, list_index));
	

CREATE TABLE  signup_attachments (
   meeting_id 	number(19,0) NOT NULL,
   resource_Id 	varchar2(255 char) default NULL,
   file_name 	varchar2(255 char) default NULL,
   mime_type 	varchar2(80 char) default NULL,
   fileSize 	number(19,0) default NULL,
   location 	clob,
   isLink  		number(1,0),
   timeslot_id 	number(19,0) default NULL,
   view_by_all 	number(1,0)  default '1',
   created_by 	varchar2(99 char) NOT NULL,
   created_date 	timestamp NOT NULL,
   last_modified_by 	varchar2(99 char) NOT NULL,
   last_modified_date 	timestamp NOT NULL,
   list_index 	number(10,0) NOT NULL,
   PRIMARY KEY  (meeting_id,list_index));


alter table signup_site_groups add constraint FK_SIGNUP_SITE_GROUPS foreign key (signup_site_id) references signup_sites;
alter table signup_sites add constraint FK_SIGNUP_MEETING_SITES foreign key (meeting_id) references signup_meetings;
alter table signup_ts_attendees add constraint FK_SIGNUP_TIMESLOT_ATTENDEES foreign key (timeslot_id) references signup_ts;
alter table signup_ts_waitinglist add constraint FK_SIGNUP_TIMESLOT_WAITINGLIST foreign key (timeslot_id) references signup_ts;
alter table signup_ts add constraint FK_SIGNUP_MEETING_TIMESLOTS foreign key (meeting_id) references signup_meetings;
alter table signup_attachments add constraint FK_SIGNUP_MEETING_ATTACHMENT foreign key (meeting_id) references signup_meetings;

create index IDX_SITE_ID on signup_sites (site_id);

create sequence signup_meeting_ID_SEQ;
create sequence signup_sites_ID_SEQ;
create sequence signup_ts_ID_SEQ;
