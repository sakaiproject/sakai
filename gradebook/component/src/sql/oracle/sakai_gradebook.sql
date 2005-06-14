alter table GB_GRADE_TO_PERCENT_MAPPING_T drop constraint FKCDEA021164995486;
alter table GB_GRADE_RECORD_T drop constraint FK46ACF752B5399B44;
alter table GB_TEACHING_ASSIGNMENT_T drop constraint FKE0F822B06AB1529A;
alter table GB_TEACHING_ASSIGNMENT_T drop constraint FKE0F822B02206F20F;
alter table GB_ENROLLMENT_T drop constraint FK2D6586BD2206F20F;
alter table GB_ENROLLMENT_T drop constraint FK2D6586BD6AB1529A;
alter table GB_GRADABLE_OBJECT_T drop constraint FK759996A76AB1529A;
alter table GB_GRADE_MAP_T drop constraint FKADE112256AB1529A;
alter table GB_GRADING_EVENT_T drop constraint FK4C9D99E0B5399B44;
alter table GB_GRADEBOOK_T drop constraint FK7C870191F11270F8;
drop table GB_USER_T cascade constraints;
drop table GB_GRADE_TO_PERCENT_MAPPING_T cascade constraints;
drop table GB_GRADE_RECORD_T cascade constraints;
drop table GB_TEACHING_ASSIGNMENT_T cascade constraints;
drop table GB_ENROLLMENT_T cascade constraints;
drop table GB_GRADABLE_OBJECT_T cascade constraints;
drop table GB_GRADE_MAP_T cascade constraints;
drop table GB_GRADING_EVENT_T cascade constraints;
drop table GB_GRADEBOOK_T cascade constraints;
drop sequence GB_GRADE_MAPPING_S;
drop sequence GB_ENROLLMENT_S;
drop sequence GB_GRADABLE_OBJECT_S;
drop sequence GB_GRADING_EVENT_S;
drop sequence GB_USER_S;
drop sequence GB_TA_S;
drop sequence GB_GRADEBOOK_S;
drop sequence GB_GRADE_RECORD_S;
create table GB_USER_T (
   ID number(19,0) not null,
   AUTH_ID varchar2(255) not null unique,
   DISPLAY_NAME varchar2(255) not null,
   SORT_NAME varchar2(255) not null,
   DISPLAY_UID varchar2(255) not null,
   primary key (ID)
);
create table GB_GRADE_TO_PERCENT_MAPPING_T (
   GRADE_MAP_ID number(19,0) not null,
   PERCENT double precision,
   LETTER_GRADE varchar2(255) not null,
   primary key (GRADE_MAP_ID, LETTER_GRADE)
);
create table GB_GRADE_RECORD_T (
   ID number(19,0) not null,
   OBJECT_TYPE_ID number(10,0) not null,
   VERSION number(10,0) not null,
   GRADABLE_OBJECT_ID number(19,0) not null,
   STUDENT_ID varchar2(255) not null,
   GRADER_ID varchar2(255) not null,
   DATE_RECORDED date not null,
   POINTS_EARNED double precision,
   ENTERED_GRADE varchar2(255),
   SORT_GRADE double precision,
   primary key (ID)
);
create table GB_TEACHING_ASSIGNMENT_T (
   ID number(19,0) not null,
   GRADEBOOK_ID number(19,0) not null,
   USER_ID number(19,0) not null,
   primary key (ID),
   unique (GRADEBOOK_ID, USER_ID)
);
create table GB_ENROLLMENT_T (
   ID number(19,0) not null,
   GRADEBOOK_ID number(19,0) not null,
   USER_ID number(19,0) not null,
   primary key (ID),
   unique (GRADEBOOK_ID, USER_ID)
);
create table GB_GRADABLE_OBJECT_T (
   ID number(19,0) not null,
   OBJECT_TYPE_ID number(10,0) not null,
   VERSION number(10,0) not null,
   GRADEBOOK_ID number(19,0) not null,
   NAME varchar2(255) not null,
   REMOVED number(1,0),
   POINTS_POSSIBLE double precision,
   DUE_DATE date,
   EXTERNALLY_MAINTAINED number(1,0),
   EXTERNAL_STUDENT_LINK varchar2(255),
   EXTERNAL_INSTRUCTOR_LINK varchar2(255),
   EXTERNAL_ID varchar2(255),
   EXTERNAL_APP_NAME varchar2(255),
   primary key (ID)
);
create table GB_GRADE_MAP_T (
   ID number(19,0) not null,
   OBJECT_TYPE_ID number(10,0) not null,
   VERSION number(10,0) not null,
   GRADEBOOK_ID number(19,0) not null,
   primary key (ID)
);
create table GB_GRADING_EVENT_T (
   ID number(19,0) not null,
   GRADABLE_OBJECT_ID number(19,0) not null,
   GRADER_ID varchar2(255) not null,
   STUDENT_ID varchar2(255) not null,
   DATE_GRADED date not null,
   GRADE varchar2(255),
   primary key (ID)
);
create table GB_GRADEBOOK_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   GRADEBOOK_UID varchar2(255) not null unique,
   NAME varchar2(255) not null,
   SELECTED_GRADE_MAPPING_ID number(19,0),
   ASSIGNMENTS_DISPLAYED number(1,0) not null,
   COURSE_GRADE_DISPLAYED number(1,0) not null,
   ALL_ASSIGNMENTS_ENTERED number(1,0) not null,
   LOCKED number(1,0) not null,
   primary key (ID)
);
alter table GB_GRADE_TO_PERCENT_MAPPING_T add constraint FKCDEA021164995486 foreign key (GRADE_MAP_ID) references GB_GRADE_MAP_T;
alter table GB_GRADE_RECORD_T add constraint FK46ACF752B5399B44 foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T;
alter table GB_TEACHING_ASSIGNMENT_T add constraint FKE0F822B06AB1529A foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T;
alter table GB_TEACHING_ASSIGNMENT_T add constraint FKE0F822B02206F20F foreign key (USER_ID) references GB_USER_T;
alter table GB_ENROLLMENT_T add constraint FK2D6586BD2206F20F foreign key (USER_ID) references GB_USER_T;
alter table GB_ENROLLMENT_T add constraint FK2D6586BD6AB1529A foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T;
alter table GB_GRADABLE_OBJECT_T add constraint FK759996A76AB1529A foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T;
alter table GB_GRADE_MAP_T add constraint FKADE112256AB1529A foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T;
alter table GB_GRADING_EVENT_T add constraint FK4C9D99E0B5399B44 foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T;
alter table GB_GRADEBOOK_T add constraint FK7C870191F11270F8 foreign key (SELECTED_GRADE_MAPPING_ID) references GB_GRADE_MAP_T;
create sequence GB_GRADE_MAPPING_S;
create sequence GB_ENROLLMENT_S;
create sequence GB_GRADABLE_OBJECT_S;
create sequence GB_GRADING_EVENT_S;
create sequence GB_USER_S;
create sequence GB_TA_S;
create sequence GB_GRADEBOOK_S;
create sequence GB_GRADE_RECORD_S;
