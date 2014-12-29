-- Add AutoMarkThreadsRead functionality to Message Center (SAK-10869)

-- add column to allow AutoMarkThreadsRead as template setting
alter table MFR_AREA_T add column (AUTO_MARK_THREADS_READ bit);
update MFR_AREA_T set AUTO_MARK_THREADS_READ=0 where AUTO_MARK_THREADS_READ is NULL;
alter table MFR_AREA_T modify column AUTO_MARK_THREADS_READ bit not null;

-- add column to allow AutoMarkThreadsRead to be set at the forum level
alter table MFR_OPEN_FORUM_T add column (AUTO_MARK_THREADS_READ bit);
update MFR_OPEN_FORUM_T set AUTO_MARK_THREADS_READ=0 where AUTO_MARK_THREADS_READ is NULL;
alter table MFR_OPEN_FORUM_T modify column AUTO_MARK_THREADS_READ bit not null;

-- add column to allow AutoMarkThreadsRead to be set at the topic level
alter table MFR_TOPIC_T add column (AUTO_MARK_THREADS_READ bit);
update MFR_TOPIC_T set AUTO_MARK_THREADS_READ=0 where AUTO_MARK_THREADS_READ is NULL;
alter table MFR_TOPIC_T modify column AUTO_MARK_THREADS_READ bit not null;