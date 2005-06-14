alter table SAKAI_HELP_RESOURCE drop constraint FKC23F5132DBFCB7FC
drop table SAKAI_HELP_CATEGORY cascade constraints
drop table SAKAI_HELP_RESOURCE cascade constraints
drop sequence Help_Category_SEQ
drop sequence Help_Resource_SEQ
create table SAKAI_HELP_CATEGORY (
   id number(19,0) not null,
   NAME varchar2(255) not null unique,
   primary key (id)
)
create table SAKAI_HELP_RESOURCE (
   id number(19,0) not null,
   document_id varchar2(36) not null unique,
   NAME varchar2(255),
   LOCATION varchar2(255),
   CATEGORY_ID number(19,0) not null,
   primary key (id)
)
create index help_cat_idx on SAKAI_HELP_RESOURCE (NAME)
alter table SAKAI_HELP_RESOURCE add constraint FKC23F5132DBFCB7FC foreign key (CATEGORY_ID) references SAKAI_HELP_CATEGORY
create sequence Help_Category_SEQ
create sequence Help_Resource_SEQ
