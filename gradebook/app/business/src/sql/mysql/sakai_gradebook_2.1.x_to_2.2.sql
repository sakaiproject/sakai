-- Gradebook table changes between Sakai 2.1.* and 2.2.

-- Add grading scale support.
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
create table GB_GRADING_SCALE_PERCENTS_T (
   GRADING_SCALE_ID bigint not null,
   PERCENT double precision,
   LETTER_GRADE varchar(255) not null,
   primary key (GRADING_SCALE_ID, LETTER_GRADE)
);
alter table GB_GRADE_MAP_T add column (GB_GRADING_SCALE_T bigint);
alter table GB_GRADING_SCALE_GRADES_T add index FK5D3F0C955A72817B (GRADING_SCALE_ID), add constraint FK5D3F0C955A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T (ID);

alter table GB_GRADING_SCALE_PERCENTS_T add index FKC98BE4675A72817B (GRADING_SCALE_ID), add constraint FKC98BE4675A72817B foreign key (GRADING_SCALE_ID) references GB_GRADING_SCALE_T (ID);

-- Add indexes for improved performance and reduced locking.
create index GB_GRADABLE_OBJ_ASN_IDX on GB_GRADABLE_OBJECT_T (OBJECT_TYPE_ID, GRADEBOOK_ID, NAME, REMOVED);
create index GB_GRADE_RECORD_O_T_IDX on GB_GRADE_RECORD_T (OBJECT_TYPE_ID);

-- This may have already been defined via the 2.1.1 upgrade.
create index GB_GRADE_RECORD_STUDENT_ID_IDX on GB_GRADE_RECORD_T (STUDENT_ID);
