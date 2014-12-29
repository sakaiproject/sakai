drop table SAKAI_PRIVACY_RECORD cascade constraints;
drop sequence PrivacyRecordImpl_SEQ;
create table SAKAI_PRIVACY_RECORD (id number(19,0) not null, lockId number(10,0) not null, contextId varchar2(255 char) not null, recordType varchar2(255 char) not null, userId varchar2(255 char) not null, viewable number(1,0) not null, primary key (id), unique (contextId, recordType, userId));
create sequence PrivacyRecordImpl_SEQ;
