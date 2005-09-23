alter table SEC_PARTICIPATION_T drop foreign key FKF99100A8CB25818C;
alter table SEC_SECTION_T drop foreign key FK841BEACCFD775FBF;
alter table SEC_SECTION_T drop foreign key FK841BEACC91B;
alter table SEC_COURSE_T drop foreign key FKAD8BDD7E91B;
drop table if exists SEC_PARTICIPATION_T;
drop table if exists SEC_SECTION_T;
drop table if exists SEC_COURSE_T;
drop table if exists SEC_LEARNING_CONTEXT_T;
create table SEC_PARTICIPATION_T (
   ID bigint not null auto_increment,
   DISCR integer not null,
   VERSION integer not null,
   LEARNING_CONTEXT_ID bigint not null,
   USER_ID varchar(255) not null,
   PARTICIPATION_UUID varchar(255) not null unique,
   STATUS varchar(255),
   primary key (ID)
);
create table SEC_SECTION_T (
   ID bigint not null,
   COURSE_ID bigint,
   LOCATION varchar(255),
   CATEGORY varchar(255),
   MAX_ENROLLMENTS integer,
   MONDAY bit,
   TUESDAY bit,
   WEDNESDAY bit,
   THURSDAY bit,
   FRIDAY bit,
   SATURDAY bit,
   SUNDAY bit,
   START_TIME time,
   END_TIME time,
   primary key (ID)
);
create table SEC_COURSE_T (
   ID bigint not null,
   SITE_CONTEXT varchar(255) not null unique,
   EXTERNAL_MGMT bit,
   SELF_REG bit,
   SELF_SWITCH bit,
   primary key (ID)
);
create table SEC_LEARNING_CONTEXT_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   LEARNING_CONTEXT_UUID varchar(255) not null unique,
   TITLE varchar(255) not null,
   primary key (ID)
);
alter table SEC_PARTICIPATION_T add index FKF99100A8CB25818C (LEARNING_CONTEXT_ID), add constraint FKF99100A8CB25818C foreign key (LEARNING_CONTEXT_ID) references SEC_LEARNING_CONTEXT_T (ID);
alter table SEC_SECTION_T add index FK841BEACCFD775FBF (COURSE_ID), add constraint FK841BEACCFD775FBF foreign key (COURSE_ID) references SEC_COURSE_T (ID);
alter table SEC_SECTION_T add index FK841BEACC91B (ID), add constraint FK841BEACC91B foreign key (ID) references SEC_LEARNING_CONTEXT_T (ID);
alter table SEC_COURSE_T add index FKAD8BDD7E91B (ID), add constraint FKAD8BDD7E91B foreign key (ID) references SEC_LEARNING_CONTEXT_T (ID);
