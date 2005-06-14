alter table SAKAI_SYLLABUS_DATA drop constraint FK3BC123AA4FDCE067
drop table SAKAI_SYLLABUS_DATA cascade constraints
drop table SAKAI_SYLLABUS_ITEM cascade constraints
drop sequence SyllabusDataImpl_SEQ
drop sequence SyllabusItemImpl_SEQ
create table SAKAI_SYLLABUS_DATA (
   id number(19,0) not null,
   lockId number(10,0) not null,
   asset clob,
   position number(10,0) not null,
   title varchar2(256),
   xview varchar2(16),
   status varchar2(64),
   emailNotification varchar2(128),
   surrogateKey number(19,0),
   primary key (id)
)
create table SAKAI_SYLLABUS_ITEM (
   id number(19,0) not null,
   lockId number(10,0) not null,
   userId varchar2(36) not null,
   contextId varchar2(36) not null,
   redirectURL varchar2(512),
   primary key (id),
   unique (userId, contextId)
)
create index syllabus_position on SAKAI_SYLLABUS_DATA (position)
alter table SAKAI_SYLLABUS_DATA add constraint FK3BC123AA4FDCE067 foreign key (surrogateKey) references SAKAI_SYLLABUS_ITEM
create index syllabus_userId on SAKAI_SYLLABUS_ITEM (userId)
create index syllabus_contextId on SAKAI_SYLLABUS_ITEM (contextId)
create sequence SyllabusDataImpl_SEQ
create sequence SyllabusItemImpl_SEQ
