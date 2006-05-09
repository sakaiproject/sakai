-- Gradebook table changes between Sakai 2.1.* and 2.2.

-- Add grading scale support.
create table GB_PROPERTY_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   NAME varchar2(255) not null unique,
   VALUE varchar2(255),
   primary key (ID)
);
create table GB_GRADING_SCALE_GRADES_T (
   GRADING_SCALE_ID number(19,0) not null,
   LETTER_GRADE varchar2(255),
   GRADE_IDX number(10,0) not null,
   primary key (GRADING_SCALE_ID, GRADE_IDX)
);
create table GB_GRADING_SCALE_T (
   ID number(19,0) not null,
   OBJECT_TYPE_ID number(10,0) not null,
   VERSION number(10,0) not null,
   SCALE_UID varchar2(255) not null unique,
   NAME varchar2(255) not null,
   UNAVAILABLE number(1,0),
   primary key (ID)
);
create table GB_GRADING_SCALE_PERCENTS_T (
   GRADING_SCALE_ID number(19,0) not null,
   PERCENT double precision,
   LETTER_GRADE varchar2(255) not null,
   primary key (GRADING_SCALE_ID, LETTER_GRADE)
);
alter table GB_GRADE_MAP_T add (GB_GRADING_SCALE_T number(19,0));
alter table GB_GRADING_SCALE_GRADES_T add constraint FK5D3F0C955A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T;
alter table GB_GRADE_MAP_T add constraint FKADE11225108F4490 foreign key (GB_GRADING_SCALE_T) references GB_GRADING_SCALE_T;
alter table GB_GRADING_SCALE_PERCENTS_T add constraint FKC98BE4675A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T;
create sequence GB_PROPERTY_S;
create sequence GB_GRADING_SCALE_S;
