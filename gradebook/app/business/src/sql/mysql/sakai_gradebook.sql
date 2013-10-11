-- alter table GB_COMMENT_T drop foreign key FK7977DFF06F98CFF;
-- alter table GB_GRADABLE_OBJECT_T drop foreign key FK759996A7325D7986;
-- alter table GB_GRADEBOOK_T drop foreign key FK7C870191552B7E63;
-- alter table GB_GRADE_MAP_T drop foreign key FKADE11225325D7986;
-- alter table GB_GRADE_MAP_T drop foreign key FKADE11225181E947A;
-- alter table GB_GRADE_RECORD_T drop foreign key FK46ACF7526F98CFF;
-- alter table GB_GRADE_TO_PERCENT_MAPPING_T drop foreign key FKCDEA021162B659F1;
-- alter table GB_GRADING_EVENT_T drop foreign key FK4C9D99E06F98CFF;
-- alter table GB_GRADING_SCALE_GRADES_T drop foreign key FK5D3F0C95605CD0C5;
-- alter table GB_GRADING_SCALE_PERCENTS_T drop foreign key FKC98BE467605CD0C5;
-- drop table if exists GB_COMMENT_T;
-- drop table if exists GB_GRADABLE_OBJECT_T;
-- drop table if exists GB_GRADEBOOK_T;
-- drop table if exists GB_GRADE_MAP_T;
-- drop table if exists GB_GRADE_RECORD_T;
-- drop table if exists GB_GRADE_TO_PERCENT_MAPPING_T;
-- drop table if exists GB_GRADING_EVENT_T;
-- drop table if exists GB_GRADING_SCALE_GRADES_T;
-- drop table if exists GB_GRADING_SCALE_PERCENTS_T;
-- drop table if exists GB_GRADING_SCALE_T;
-- drop table if exists GB_PROPERTY_T;
-- drop table if exists GB_SPREADSHEET_T;

create table GB_COMMENT_T (
	ID bigint not null auto_increment, 
	VERSION integer not null, 
	GRADER_ID varchar(255) not null, 
	STUDENT_ID varchar(255) not null, 
	COMMENT_TEXT text, 
	DATE_RECORDED datetime not null, 
	GRADABLE_OBJECT_ID bigint not null, 
	primary key (ID), 
	unique (STUDENT_ID, GRADABLE_OBJECT_ID)) ENGINE=InnoDB;
create table GB_GRADABLE_OBJECT_T (
	ID bigint not null auto_increment,
	OBJECT_TYPE_ID integer not null,
	VERSION integer not null,
	GRADEBOOK_ID bigint not null,
	NAME varchar(255) not null,
	REMOVED bit,
	POINTS_POSSIBLE double precision,
	DUE_DATE date,
	NOT_COUNTED bit,
	EXTERNALLY_MAINTAINED bit,
	EXTERNAL_STUDENT_LINK varchar(255),
	EXTERNAL_INSTRUCTOR_LINK varchar(255),
	EXTERNAL_ID varchar(255),
	EXTERNAL_APP_NAME varchar(255),
	RELEASED bit(1) default 1,
	primary key (ID)) ENGINE=InnoDB;
create table GB_GRADEBOOK_T (
	ID bigint not null auto_increment,
	VERSION integer not null,
	GRADEBOOK_UID varchar(255) not null unique,
	NAME varchar(255) not null,
	SELECTED_GRADE_MAPPING_ID bigint,
	ASSIGNMENTS_DISPLAYED bit not null,
	COURSE_GRADE_DISPLAYED bit not null,
	TOTAL_POINTS_DISPLAYED bit not null,
	COURSE_AVERAGE_DISPLAYED bit not null,
	ALL_ASSIGNMENTS_ENTERED bit not null,
	LOCKED bit not null,
	primary key (ID)) ENGINE=InnoDB;
create table GB_GRADE_MAP_T (
	ID bigint not null auto_increment,
	OBJECT_TYPE_ID integer not null,
	VERSION integer not null,
	GRADEBOOK_ID bigint not null,
	GB_GRADING_SCALE_T bigint,
	primary key (ID)) ENGINE=InnoDB;
create table GB_GRADE_RECORD_T (
	ID bigint not null auto_increment,
	OBJECT_TYPE_ID integer not null,
	VERSION integer not null,
	GRADABLE_OBJECT_ID bigint not null,
	STUDENT_ID varchar(255) not null,
	GRADER_ID varchar(255) not null,
	DATE_RECORDED datetime not null,
	POINTS_EARNED double precision,
	ENTERED_GRADE varchar(255),
	primary key (ID),
	unique (GRADABLE_OBJECT_ID, STUDENT_ID)) ENGINE=InnoDB;
create table GB_GRADE_TO_PERCENT_MAPPING_T (
	GRADE_MAP_ID bigint not null,
	PERCENT double precision,
	LETTER_GRADE varchar(255) not null,
	primary key (GRADE_MAP_ID, LETTER_GRADE)) ENGINE=InnoDB;
create table GB_GRADING_EVENT_T (
	ID bigint not null auto_increment,
	GRADABLE_OBJECT_ID bigint not null,
	GRADER_ID varchar(255) not null,
	STUDENT_ID varchar(255) not null,
	DATE_GRADED datetime not null,
	GRADE varchar(255),
	primary key (ID)) ENGINE=InnoDB;
create table GB_GRADING_SCALE_GRADES_T (
	GRADING_SCALE_ID bigint not null,
	LETTER_GRADE varchar(255),
	GRADE_IDX integer not null,
	primary key (GRADING_SCALE_ID, GRADE_IDX)) ENGINE=InnoDB;
create table GB_GRADING_SCALE_PERCENTS_T (
	GRADING_SCALE_ID bigint not null,
	PERCENT double precision,
	LETTER_GRADE varchar(255) not null,
	primary key (GRADING_SCALE_ID, LETTER_GRADE)) ENGINE=InnoDB;
create table GB_GRADING_SCALE_T (
	ID bigint not null auto_increment,
	OBJECT_TYPE_ID integer not null,
	VERSION integer not null,
	SCALE_UID varchar(255) not null unique,
	NAME varchar(255) not null,
	UNAVAILABLE bit,
	primary key (ID)) ENGINE=InnoDB;
create table GB_PROPERTY_T (
	ID bigint not null auto_increment,
	VERSION integer not null,
	NAME varchar(255) not null unique,
	VALUE varchar(255),
	primary key (ID)) ENGINE=InnoDB;
create table GB_SPREADSHEET_T (
  ID bigint(20) NOT NULL auto_increment,
  VERSION int(11) NOT NULL,
  CREATOR varchar(255) NOT NULL,
  NAME varchar(255) NOT NULL,
  CONTENT text NOT NULL,
  DATE_CREATED datetime NOT NULL,
  GRADEBOOK_ID bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`) 
) ENGINE=InnoDB;
	

alter table GB_COMMENT_T 
	add index FK7977DFF06F98CFF (GRADABLE_OBJECT_ID), 
	add constraint FK7977DFF06F98CFF foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T (ID);
alter table GB_GRADABLE_OBJECT_T
	add index FK759996A7325D7986 (GRADEBOOK_ID),
	add constraint FK759996A7325D7986 foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T (ID);
alter table GB_GRADEBOOK_T
	add index FK7C870191552B7E63 (SELECTED_GRADE_MAPPING_ID),
	add constraint FK7C870191552B7E63 foreign key (SELECTED_GRADE_MAPPING_ID) references GB_GRADE_MAP_T (ID);
alter table GB_GRADE_MAP_T
	add index FKADE11225325D7986 (GRADEBOOK_ID),
	add constraint FKADE11225325D7986 foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T (ID);
alter table GB_GRADE_MAP_T
	add index FKADE11225181E947A (GB_GRADING_SCALE_T),
	add constraint FKADE11225181E947A foreign key (GB_GRADING_SCALE_T) references GB_GRADING_SCALE_T (ID);
create index GB_GRADE_RECORD_STUDENT_ID_IDX on GB_GRADE_RECORD_T (STUDENT_ID);
alter table GB_GRADE_RECORD_T
	add index FK46ACF7526F98CFF (GRADABLE_OBJECT_ID),
	add constraint FK46ACF7526F98CFF foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T (ID);
alter table GB_GRADE_TO_PERCENT_MAPPING_T
	add index FKCDEA021162B659F1 (GRADE_MAP_ID),
	add constraint FKCDEA021162B659F1 foreign key (GRADE_MAP_ID) references GB_GRADE_MAP_T (ID);
alter table GB_GRADING_EVENT_T
	add index FK4C9D99E06F98CFF (GRADABLE_OBJECT_ID),
	add constraint FK4C9D99E06F98CFF foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T (ID);
alter table GB_GRADING_SCALE_GRADES_T
	add index FK5D3F0C95605CD0C5 (GRADING_SCALE_ID),
	add constraint FK5D3F0C95605CD0C5 foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T (ID);
alter table GB_GRADING_SCALE_PERCENTS_T
	add index FKC98BE467605CD0C5 (GRADING_SCALE_ID),
	add constraint FKC98BE467605CD0C5 foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T (ID);
create index GB_GRADABLE_OBJ_ASN_IDX on GB_GRADABLE_OBJECT_T (OBJECT_TYPE_ID, GRADEBOOK_ID, NAME, REMOVED);
create index GB_GRADE_RECORD_O_T_IDX on GB_GRADE_RECORD_T (OBJECT_TYPE_ID);
