alter table SAKAI_SYLLABUS_DATA drop constraint FK3BC123AA4FDCE067
alter table SAKAI_SYLLABUS_ATTACH drop constraint FK4BF41E45A09831E0
drop table SAKAI_SYLLABUS_DATA cascade constraints
drop table SAKAI_SYLLABUS_ITEM cascade constraints
drop table SAKAI_SYLLABUS_ATTACH cascade constraints
drop sequence SyllabusDataImpl_SEQ
drop sequence SyllabusItemImpl_SEQ
drop sequence SyllabusAttachImpl_SEQ
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
create table SAKAI_SYLLABUS_ATTACH (
   syllabusAttachId number(19,0) not null,
   lockId number(10,0) not null,
   attachmentId varchar2(256) not null,
   syllabusAttachName varchar2(256) not null,
   syllabusAttachSize varchar2(256),
   syllabusAttachType varchar2(256),
   createdBy varchar2(256),
   syllabusAttachUrl varchar2(256) not null,
   lastModifiedBy varchar2(256),
   syllabusId number(19,0),
   primary key (syllabusAttachId)
)
create index syllabus_position on SAKAI_SYLLABUS_DATA (position)
alter table SAKAI_SYLLABUS_DATA add constraint FK3BC123AA4FDCE067 foreign key (surrogateKey) references SAKAI_SYLLABUS_ITEM
create index syllabus_userId on SAKAI_SYLLABUS_ITEM (userId)
create index syllabus_contextId on SAKAI_SYLLABUS_ITEM (contextId)
alter table SAKAI_SYLLABUS_ATTACH add constraint FK4BF41E45A09831E0 foreign key (syllabusId) references SAKAI_SYLLABUS_DATA
create sequence SyllabusDataImpl_SEQ
create sequence SyllabusItemImpl_SEQ
create sequence SyllabusAttachImpl_SEQ
