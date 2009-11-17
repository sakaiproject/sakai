-- Add AutoMarkThreadsRead functionality to Message Center (SAK-10869)

-- add column to allow AutoMarkThreadsRead as template setting
alter table MFR_AREA_T add (AUTO_MARK_THREADS_READ NUMBER(1,0));
update MFR_AREA_T set AUTO_MARK_THREADS_READ=0 where AUTO_MARK_THREADS_READ is NULL;
alter table MFR_AREA_T modify (AUTO_MARK_THREADS_READ NUMBER(1,0) not null);

-- add column to allow AutoMarkThreadsRead to be set at the forum level
alter table MFR_OPEN_FORUM_T add (AUTO_MARK_THREADS_READ NUMBER(1,0));
update MFR_OPEN_FORUM_T set AUTO_MARK_THREADS_READ=0 where AUTO_MARK_THREADS_READ is NULL;
alter table MFR_OPEN_FORUM_T modify (AUTO_MARK_THREADS_READ NUMBER(1,0) not null);

-- add column to allow AutoMarkThreadsRead to be set at the topic level
alter table MFR_TOPIC_T add (AUTO_MARK_THREADS_READ NUMBER(1,0));
update MFR_TOPIC_T set AUTO_MARK_THREADS_READ=0 where AUTO_MARK_THREADS_READ is NULL;
alter table MFR_TOPIC_T modify (AUTO_MARK_THREADS_READ NUMBER(1,0) not null);