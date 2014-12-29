drop table if exists SAKAI_PRIVACY_RECORD;
create table SAKAI_PRIVACY_RECORD (id bigint not null auto_increment, lockId integer not null, contextId varchar(255) not null, recordType varchar(255) not null, userId varchar(255) not null, viewable bit not null, primary key (id), unique (contextId, recordType, userId)) default charset=latin1;
