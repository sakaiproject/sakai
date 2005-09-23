alter table SEC_PARTICIPATION_T drop constraint FKF99100A8CB25818C;
alter table SEC_SECTION_T drop constraint FK841BEACCFD775FBF;
alter table SEC_SECTION_T drop constraint FK841BEACC91B;
alter table SEC_COURSE_T drop constraint FKAD8BDD7E91B;
drop table SEC_PARTICIPATION_T cascade constraints;
drop table SEC_SECTION_T cascade constraints;
drop table SEC_COURSE_T cascade constraints;
drop table SEC_LEARNING_CONTEXT_T cascade constraints;
drop sequence SEC_PARTICIPATION_S;
drop sequence SEC_LEARNING_CONTEXT_S;
create table SEC_PARTICIPATION_T (
   ID number(19,0) not null,
   DISCR number(10,0) not null,
   VERSION number(10,0) not null,
   LEARNING_CONTEXT_ID number(19,0) not null,
   USER_ID varchar2(255) not null,
   PARTICIPATION_UUID varchar2(255) not null unique,
   STATUS varchar2(255),
   primary key (ID)
);
create table SEC_SECTION_T (
   ID number(19,0) not null,
   COURSE_ID number(19,0),
   LOCATION varchar2(255),
   CATEGORY varchar2(255),
   MAX_ENROLLMENTS number(10,0),
   MONDAY number(1,0),
   TUESDAY number(1,0),
   WEDNESDAY number(1,0),
   THURSDAY number(1,0),
   FRIDAY number(1,0),
   SATURDAY number(1,0),
   SUNDAY number(1,0),
   START_TIME date,
   END_TIME date,
   primary key (ID)
);
create table SEC_COURSE_T (
   ID number(19,0) not null,
   SITE_CONTEXT varchar2(255) not null unique,
   EXTERNAL_MGMT number(1,0),
   SELF_REG number(1,0),
   SELF_SWITCH number(1,0),
   primary key (ID)
);
create table SEC_LEARNING_CONTEXT_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   LEARNING_CONTEXT_UUID varchar2(255) not null unique,
   TITLE varchar2(255) not null,
   primary key (ID)
);
alter table SEC_PARTICIPATION_T add constraint FKF99100A8CB25818C foreign key (LEARNING_CONTEXT_ID) references SEC_LEARNING_CONTEXT_T;
alter table SEC_SECTION_T add constraint FK841BEACCFD775FBF foreign key (COURSE_ID) references SEC_COURSE_T;
alter table SEC_SECTION_T add constraint FK841BEACC91B foreign key (ID) references SEC_LEARNING_CONTEXT_T;
alter table SEC_COURSE_T add constraint FKAD8BDD7E91B foreign key (ID) references SEC_LEARNING_CONTEXT_T;
create sequence SEC_PARTICIPATION_S;
create sequence SEC_LEARNING_CONTEXT_S;
