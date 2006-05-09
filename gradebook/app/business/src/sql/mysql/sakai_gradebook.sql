alter table GB_GRADING_SCALE_GRADES_T drop foreign key FK5D3F0C955A72817B;
alter table GB_GRADE_TO_PERCENT_MAPPING_T drop foreign key FKCDEA021164995486;
alter table GB_GRADE_RECORD_T drop foreign key FK46ACF752B5399B44;
alter table GB_GRADABLE_OBJECT_T drop foreign key FK759996A76AB1529A;
alter table GB_GRADE_MAP_T drop foreign key FKADE11225108F4490;
alter table GB_GRADE_MAP_T drop foreign key FKADE112256AB1529A;
alter table GB_GRADING_SCALE_PERCENTS_T drop foreign key FKC98BE4675A72817B;
alter table GB_GRADING_EVENT_T drop foreign key FK4C9D99E0B5399B44;
alter table GB_GRADEBOOK_T drop foreign key FK7C870191F11270F8;
drop table if exists GB_PROPERTY_T;
drop table if exists GB_GRADING_SCALE_GRADES_T;
drop table if exists GB_GRADING_SCALE_T;
drop table if exists GB_GRADE_TO_PERCENT_MAPPING_T;
drop table if exists GB_GRADE_RECORD_T;
drop table if exists GB_GRADABLE_OBJECT_T;
drop table if exists GB_GRADE_MAP_T;
drop table if exists GB_GRADING_SCALE_PERCENTS_T;
drop table if exists GB_GRADING_EVENT_T;
drop table if exists GB_GRADEBOOK_T;
create table GB_PROPERTY_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   NAME varchar(255) not null unique,
   VALUE varchar(255),
   primary key (ID)
);
create table GB_GRADING_SCALE_GRADES_T (
   GRADING_SCALE_ID bigint not null,
   LETTER_GRADE varchar(255),
   GRADE_IDX integer not null,
   primary key (GRADING_SCALE_ID, GRADE_IDX)
);
create table GB_GRADING_SCALE_T (
   ID bigint not null auto_increment,
   OBJECT_TYPE_ID integer not null,
   VERSION integer not null,
   SCALE_UID varchar(255) not null unique,
   NAME varchar(255) not null,
   UNAVAILABLE bit,
   primary key (ID)
);
create table GB_GRADE_TO_PERCENT_MAPPING_T (
   GRADE_MAP_ID bigint not null,
   PERCENT double precision,
   LETTER_GRADE varchar(255) not null,
   primary key (GRADE_MAP_ID, LETTER_GRADE)
);
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
   SORT_GRADE double precision,
   primary key (ID),
   unique (GRADABLE_OBJECT_ID, STUDENT_ID)
);
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
   primary key (ID)
);
create table GB_GRADE_MAP_T (
   ID bigint not null auto_increment,
   OBJECT_TYPE_ID integer not null,
   VERSION integer not null,
   GRADEBOOK_ID bigint not null,
   GB_GRADING_SCALE_T bigint,
   primary key (ID)
);
create table GB_GRADING_SCALE_PERCENTS_T (
   GRADING_SCALE_ID bigint not null,
   PERCENT double precision,
   LETTER_GRADE varchar(255) not null,
   primary key (GRADING_SCALE_ID, LETTER_GRADE)
);
create table GB_GRADING_EVENT_T (
   ID bigint not null auto_increment,
   GRADABLE_OBJECT_ID bigint not null,
   GRADER_ID varchar(255) not null,
   STUDENT_ID varchar(255) not null,
   DATE_GRADED datetime not null,
   GRADE varchar(255),
   primary key (ID)
);
create table GB_GRADEBOOK_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   GRADEBOOK_UID varchar(255) not null unique,
   NAME varchar(255) not null,
   SELECTED_GRADE_MAPPING_ID bigint,
   ASSIGNMENTS_DISPLAYED bit not null,
   COURSE_GRADE_DISPLAYED bit not null,
   ALL_ASSIGNMENTS_ENTERED bit not null,
   LOCKED bit not null,
   primary key (ID)
);
alter table GB_GRADING_SCALE_GRADES_T add index FK5D3F0C955A72817B (GRADING_SCALE_ID), add constraint FK5D3F0C955A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T (ID);
alter table GB_GRADE_TO_PERCENT_MAPPING_T add index FKCDEA021164995486 (GRADE_MAP_ID), add constraint FKCDEA021164995486 foreign key (GRADE_MAP_ID) references GB_GRADE_MAP_T (ID);
alter table GB_GRADE_RECORD_T add index FK46ACF752B5399B44 (GRADABLE_OBJECT_ID), add constraint FK46ACF752B5399B44 foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T (ID);
alter table GB_GRADABLE_OBJECT_T add index FK759996A76AB1529A (GRADEBOOK_ID), add constraint FK759996A76AB1529A foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T (ID);
alter table GB_GRADE_MAP_T add index FKADE11225108F4490 (GB_GRADING_SCALE_T), add constraint FKADE11225108F4490 foreign key (GB_GRADING_SCALE_T) references GB_GRADING_SCALE_T (ID);
alter table GB_GRADE_MAP_T add index FKADE112256AB1529A (GRADEBOOK_ID), add constraint FKADE112256AB1529A foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T (ID);
alter table GB_GRADING_SCALE_PERCENTS_T add index FKC98BE4675A72817B (GRADING_SCALE_ID), add constraint FKC98BE4675A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T (ID);
alter table GB_GRADING_EVENT_T add index FK4C9D99E0B5399B44 (GRADABLE_OBJECT_ID), add constraint FK4C9D99E0B5399B44 foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T (ID);
alter table GB_GRADEBOOK_T add index FK7C870191F11270F8 (SELECTED_GRADE_MAPPING_ID), add constraint FK7C870191F11270F8 foreign key (SELECTED_GRADE_MAPPING_ID) references GB_GRADE_MAP_T (ID);
create index GB_GRADE_RECORD_STUDENT_ID_IDX on GB_GRADE_RECORD_T (STUDENT_ID);
create index GB_GRADE_RECORD_G_O_IDX on GB_GRADE_RECORD_T (GRADABLE_OBJECT_ID);
