-- drop table GB_COMMENT_T cascade constraints;
-- drop table GB_GRADABLE_OBJECT_T cascade constraints;
-- drop table GB_GRADEBOOK_T cascade constraints;
-- drop table GB_GRADE_MAP_T cascade constraints;
-- drop table GB_GRADE_RECORD_T cascade constraints;
-- drop table GB_GRADE_TO_PERCENT_MAPPING_T cascade constraints;
-- drop table GB_GRADING_EVENT_T cascade constraints;
-- drop table GB_GRADING_SCALE_GRADES_T cascade constraints;
-- drop table GB_GRADING_SCALE_PERCENTS_T cascade constraints;
-- drop table GB_GRADING_SCALE_T cascade constraints;
-- drop table GB_PROPERTY_T cascade constraints;
-- drop sequence GB_COMMENT_S;
-- drop sequence GB_GRADABLE_OBJECT_S;
-- drop sequence GB_GRADEBOOK_S;
-- drop sequence GB_GRADE_MAPPING_S;
-- drop sequence GB_GRADE_RECORD_S;
-- drop sequence GB_GRADING_EVENT_S;
-- drop sequence GB_GRADING_SCALE_S;
-- drop sequence GB_PROPERTY_S;
-- drop sequence GB_SPREADSHEET_T

create table GB_COMMENT_T (
	ID number(19,0) not null, 
	VERSION number(10,0) not null, 
	GRADER_ID varchar2(255 char) not null, 
	STUDENT_ID varchar2(255 char) not null, 
	COMMENT_TEXT clob, 
	DATE_RECORDED timestamp not null, 
	GRADABLE_OBJECT_ID number(19,0) not null, 
	primary key (ID), 
	unique (STUDENT_ID, GRADABLE_OBJECT_ID));
create table GB_GRADABLE_OBJECT_T (
	ID number(19,0) not null,
	OBJECT_TYPE_ID number(10,0) not null,
	VERSION number(10,0) not null,
	GRADEBOOK_ID number(19,0) not null,
	NAME varchar2(255 char) not null,
	REMOVED number(1,0),
	POINTS_POSSIBLE double precision,
	DUE_DATE date,
	NOT_COUNTED number(1,0),
	EXTERNALLY_MAINTAINED number(1,0),
	EXTERNAL_STUDENT_LINK varchar2(255 char),
	EXTERNAL_INSTRUCTOR_LINK varchar2(255 char),
	EXTERNAL_ID varchar2(255 char),
	EXTERNAL_APP_NAME varchar2(255 char),
	RELEASED number(1,0) default 0 not null,
	primary key (ID));
create table GB_GRADEBOOK_T (
	ID number(19,0) not null,
	VERSION number(10,0) not null,
	GRADEBOOK_UID varchar2(255 char) not null unique,
	NAME varchar2(255 char) not null,
	SELECTED_GRADE_MAPPING_ID number(19,0),
	ASSIGNMENTS_DISPLAYED number(1,0) not null,
	COURSE_GRADE_DISPLAYED number(1,0) not null,
	TOTAL_POINTS_DISPLAYED number(1,0) not null,
	COURSE_AVERAGE_DISPLAYED number(1,0) not null,
	ALL_ASSIGNMENTS_ENTERED number(1,0) not null,
	LOCKED number(1,0) not null,
	primary key (ID));
create table GB_GRADE_MAP_T (
	ID number(19,0) not null,
	OBJECT_TYPE_ID number(10,0) not null,
	VERSION number(10,0) not null,
	GRADEBOOK_ID number(19,0) not null,
	GB_GRADING_SCALE_T number(19,0),
	primary key (ID));
create table GB_GRADE_RECORD_T (
	ID number(19,0) not null,
	OBJECT_TYPE_ID number(10,0) not null,
	VERSION number(10,0) not null,
	GRADABLE_OBJECT_ID number(19,0) not null,
	STUDENT_ID varchar2(255 char) not null,
	GRADER_ID varchar2(255 char) not null,
	DATE_RECORDED timestamp not null,
	POINTS_EARNED double precision,
	ENTERED_GRADE varchar2(255 char),
	primary key (ID),
	unique (GRADABLE_OBJECT_ID, STUDENT_ID));
create table GB_GRADE_TO_PERCENT_MAPPING_T (
	GRADE_MAP_ID number(19,0) not null,
	PERCENT double precision,
	LETTER_GRADE varchar2(255 char) not null,
	primary key (GRADE_MAP_ID, LETTER_GRADE));
create table GB_GRADING_EVENT_T (
	ID number(19,0) not null,
	GRADABLE_OBJECT_ID number(19,0) not null,
	GRADER_ID varchar2(255 char) not null,
	STUDENT_ID varchar2(255 char) not null,
	DATE_GRADED timestamp not null,
	GRADE varchar2(255 char),
	primary key (ID));
create table GB_GRADING_SCALE_GRADES_T (
	GRADING_SCALE_ID number(19,0) not null,
	LETTER_GRADE varchar2(255 char),
	GRADE_IDX number(10,0) not null,
	primary key (GRADING_SCALE_ID, GRADE_IDX));
create table GB_GRADING_SCALE_PERCENTS_T (
	GRADING_SCALE_ID number(19,0) not null,
	PERCENT double precision,
	LETTER_GRADE varchar2(255 char) not null,
	primary key (GRADING_SCALE_ID, LETTER_GRADE));
create table GB_GRADING_SCALE_T (
	ID number(19,0) not null,
	OBJECT_TYPE_ID number(10,0) not null,
	VERSION number(10,0) not null,
	SCALE_UID varchar2(255 char) not null unique,
	NAME varchar2(255 char) not null,
	UNAVAILABLE number(1,0),
	primary key (ID));
create table GB_PROPERTY_T (
	ID number(19,0) not null,
	VERSION number(10,0) not null,
	NAME varchar2(255 char) not null unique,
	VALUE varchar2(255 char),
	primary key (ID));
create table GB_SPREADSHEET_T ( 
    ID          	NUMBER(19,0) NOT NULL,
    VERSION     	NUMBER(10,0) NOT NULL,
    CREATOR     	VARCHAR2(255) NOT NULL,
    NAME        	VARCHAR2(255) NOT NULL,
    CONTENT     	CLOB NOT NULL,
    DATE_CREATED	DATE NOT NULL,
    GRADEBOOK_ID	NUMBER(19,0) NOT NULL,
    PRIMARY KEY(ID)
);
	

alter table GB_COMMENT_T 
	add constraint FK7977DFF06F98CFF foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T;
alter table GB_GRADABLE_OBJECT_T
	add constraint FK759996A7325D7986 foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T;
alter table GB_GRADEBOOK_T
	add constraint FK7C870191552B7E63 foreign key (SELECTED_GRADE_MAPPING_ID) references GB_GRADE_MAP_T;
alter table GB_GRADE_MAP_T
	add constraint FKADE11225325D7986 foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T;
alter table GB_GRADE_MAP_T
	add constraint FKADE11225181E947A foreign key (GB_GRADING_SCALE_T) references GB_GRADING_SCALE_T;
alter table GB_GRADE_RECORD_T
	add constraint FK46ACF7526F98CFF foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T;
alter table GB_GRADE_TO_PERCENT_MAPPING_T
	add constraint FKCDEA021162B659F1 foreign key (GRADE_MAP_ID) references GB_GRADE_MAP_T;
alter table GB_GRADING_EVENT_T
	add constraint FK4C9D99E06F98CFF foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T;
alter table GB_GRADING_SCALE_GRADES_T
	add constraint FK5D3F0C95605CD0C5 foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T;
alter table GB_GRADING_SCALE_PERCENTS_T
	add constraint FKC98BE467605CD0C5 foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T;
create sequence GB_COMMENT_S;
create sequence GB_GRADABLE_OBJECT_S;
create sequence GB_GRADEBOOK_S;
create sequence GB_GRADE_MAPPING_S;
create sequence GB_GRADE_RECORD_S;
create sequence GB_GRADING_EVENT_S;
create sequence GB_GRADING_SCALE_S;
create sequence GB_PROPERTY_S;
create sequence GB_SPREADSHEET_S;
create index GRADEBOOK_ID on GB_GRADABLE_OBJECT_T (GRADEBOOK_ID);
create index GB_GRADE_MAP_GB_IDX on GB_GRADE_MAP_T (GRADEBOOK_ID);
create index GB_GRADE_RECORD_STUDENT_ID_IDX on GB_GRADE_RECORD_T (STUDENT_ID);
create index GB_GRADE_RECORD_G_O_IDX on GB_GRADE_RECORD_T (GRADABLE_OBJECT_ID);
create index GB_GRADABLE_OBJ_ASN_IDX on GB_GRADABLE_OBJECT_T (OBJECT_TYPE_ID, GRADEBOOK_ID, NAME, REMOVED);
create index GB_GRADE_RECORD_O_T_IDX on GB_GRADE_RECORD_T (OBJECT_TYPE_ID);
