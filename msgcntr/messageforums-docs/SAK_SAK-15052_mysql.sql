alter table MFR_MESSAGE_T add column THREADID bigint(20);
alter table MFR_MESSAGE_T add column LASTTHREADATE datetime;
alter table MFR_MESSAGE_T add column LASTTHREAPOST bigint(20);

update MFR_MESSAGE_T set THREADID=IN_REPLY_TO,LASTTHREADATE=CREATED;