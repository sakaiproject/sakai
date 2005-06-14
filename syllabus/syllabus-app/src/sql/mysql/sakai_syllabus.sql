alter table SAKAI_SYLLABUS_DATA drop foreign key FK3BC123AA4FDCE067
drop table if exists SAKAI_SYLLABUS_DATA
drop table if exists SAKAI_SYLLABUS_ITEM
create table SAKAI_SYLLABUS_DATA (
   id bigint not null auto_increment,
   lockId integer not null,
   asset text,
   position integer not null,
   title text,
   xview varchar(16),
   status varchar(64),
   emailNotification varchar(128),
   surrogateKey bigint,
   primary key (id)
)
create table SAKAI_SYLLABUS_ITEM (
   id bigint not null auto_increment,
   lockId integer not null,
   userId varchar(36) not null,
   contextId varchar(36) not null,
   redirectURL text,
   primary key (id),
   unique (userId, contextId)
)
create index syllabus_position on SAKAI_SYLLABUS_DATA (position)
alter table SAKAI_SYLLABUS_DATA add index FK3BC123AA4FDCE067 (surrogateKey), add constraint FK3BC123AA4FDCE067 foreign key (surrogateKey) references SAKAI_SYLLABUS_ITEM (id)
create index syllabus_userId on SAKAI_SYLLABUS_ITEM (userId)
create index syllabus_contextId on SAKAI_SYLLABUS_ITEM (contextId)
