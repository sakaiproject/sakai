
--////////////////////////////////////////////////////
--// MSGCNTR-411
--// Post First Option in Forums
--////////////////////////////////////////////////////
-- add column to allow POST_FIRST as template setting
alter table MFR_AREA_T add (POST_FIRST NUMBER(1,0));
update MFR_AREA_T set POST_FIRST =0 where POST_FIRST is NULL;
alter table MFR_AREA_T modify (POST_FIRST NUMBER(1,0) not null);

-- add column to allow POST_FIRST to be set at the forum level
alter table MFR_OPEN_FORUM_T add (POST_FIRST NUMBER(1,0));
update MFR_OPEN_FORUM_T set POST_FIRST =0 where POST_FIRST is NULL;
alter table MFR_OPEN_FORUM_T modify (POST_FIRST NUMBER(1,0) not null);

-- add column to allow POST_FIRST to be set at the topic level
alter table MFR_TOPIC_T add (POST_FIRST NUMBER(1,0));
update MFR_TOPIC_T set POST_FIRST =0 where POST_FIRST is NULL;
alter table MFR_TOPIC_T modify (POST_FIRST NUMBER(1,0) not null);