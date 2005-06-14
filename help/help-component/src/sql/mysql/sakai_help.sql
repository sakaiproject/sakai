alter table SAKAI_HELP_RESOURCE drop foreign key FKC23F5132DBFCB7FC
drop table if exists SAKAI_HELP_CATEGORY
drop table if exists SAKAI_HELP_RESOURCE
create table SAKAI_HELP_CATEGORY (
   id bigint not null auto_increment,
   NAME varchar(255) not null unique,
   primary key (id)
)
create table SAKAI_HELP_RESOURCE (
   id bigint not null auto_increment,
   document_id varchar(36) not null unique,
   NAME varchar(255),
   LOCATION varchar(255),
   CATEGORY_ID bigint not null,
   primary key (id)
)
create index help_cat_idx on SAKAI_HELP_RESOURCE (NAME)
alter table SAKAI_HELP_RESOURCE add index FKC23F5132DBFCB7FC (CATEGORY_ID), add constraint FKC23F5132DBFCB7FC foreign key (CATEGORY_ID) references SAKAI_HELP_CATEGORY (id)
