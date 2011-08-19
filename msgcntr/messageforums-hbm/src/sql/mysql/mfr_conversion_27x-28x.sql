--MSGCNTR-309
--Start and End dates on Forums and Topics

alter table MFR_AREA_T add column AVAILABILITY_RESTRICTED bit;
update MFR_AREA_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is null;
alter table MFR_AREA_T modify column AVAILABILITY_RESTRICTED bit not null default false;

alter table MFR_AREA_T add column AVAILABILITY bit;
update MFR_AREA_T set AVAILABILITY=1 where AVAILABILITY is null;
alter table MFR_AREA_T modify column AVAILABILITY bit not null default true;

alter table MFR_AREA_T add (OPEN_DATE datetime);

alter table MFR_AREA_T add column CLOSE_DATE datetime;


alter table MFR_OPEN_FORUM_T add column AVAILABILITY_RESTRICTED bit;
update MFR_OPEN_FORUM_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is null;
alter table MFR_OPEN_FORUM_T modify column AVAILABILITY_RESTRICTED bit not null default false;

alter table MFR_OPEN_FORUM_T add column AVAILABILITY bit;
update MFR_OPEN_FORUM_T set AVAILABILITY=1 where AVAILABILITY is null;
alter table MFR_OPEN_FORUM_T modify column AVAILABILITY bit not null default true;

alter table MFR_OPEN_FORUM_T add column OPEN_DATE datetime;

alter table MFR_OPEN_FORUM_T add column CLOSE_DATE datetime;

alter table MFR_TOPIC_T add column AVAILABILITY_RESTRICTED bit;
update MFR_TOPIC_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is null;
alter table MFR_TOPIC_T modify column AVAILABILITY_RESTRICTED bit not null default false;

alter table MFR_TOPIC_T add column AVAILABILITY bit;
update MFR_TOPIC_T set AVAILABILITY=1 where AVAILABILITY is null;
alter table MFR_TOPIC_T modify column AVAILABILITY bit not null default true;

alter table MFR_TOPIC_T add column OPEN_DATE datetime null;

alter table MFR_TOPIC_T add column CLOSE_DATE datetime null;


--MSGCNTR-355
insert into MFR_TOPIC_T (UUID, MODERATED, AUTO_MARK_THREADS_READ, SORT_INDEX, MUTABLE, TOPIC_DTYPE, VERSION, CREATED, CREATED_BY, MODIFIED, MODIFIED_BY, TITLE, SHORT_DESCRIPTION, EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID)

    select UUID, MODERATED, AUTO_MARK_THREADS_READ, 3 as SORT_INDEX, 0 as MUTABLE, TOPIC_DTYPE, VERSION, CREATED, CREATED_BY, MODIFIED, MODIFIED_BY, TITLE, SHORT_DESCRIPTION, EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID from (
                    select count(*) as c1, uuid() as UUID, mtt.MODERATED, mtt.AUTO_MARK_THREADS_READ, mtt.TOPIC_DTYPE, 0 as VERSION, mtt.CREATED, mtt.CREATED_BY, mtt.MODIFIED, mtt.MODIFIED_BY, 'pvt_drafts' as TITLE, 'short-desc' as SHORT_DESCRIPTION, 'ext-desc' as EXTENDED_DESCRIPTION, mtt.TYPE_UUID, mtt.pf_surrogateKey, mtt.USER_ID
                    from MFR_PRIVATE_FORUM_T mpft, MFR_TOPIC_T mtt
                    where mpft.ID = mtt.pf_surrogateKey and mpft.TYPE_UUID = mtt.TYPE_UUID
                    Group By mtt.USER_ID, mtt.pf_surrogateKey) s1
    where s1.c1 = 3;

--MSGCNTR-360
--Hibernate could have missed this index, if this fails, then the index may already be in the table
CREATE INDEX user_type_context_idx ON MFR_PVT_MSG_USR_T ( USER_ID(36), TYPE_UUID(36), CONTEXT_ID(36), READ_STATUS);

--MSGCNTR-429
--Hibernate could have missed this index, if this fails, then the index may already be in the table
CREATE INDEX MFR_UNREAD_STATUS_I2 ON MFR_UNREAD_STATUS_T (MESSAGE_C, USER_C, READ_C);

--MSGCNTR-449
--Restores correct thread sorting
update MFR_MESSAGE_T set LASTTHREADATE = CREATED where THREADID is null and LASTTHREADATE is null;
