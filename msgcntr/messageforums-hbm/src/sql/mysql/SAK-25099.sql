alter table MFR_TOPIC_T add column (POST_ANONYMOUS bit);
alter table MFR_TOPIC_T modify column POST_ANONYMOUS bit not null;
alter table MFR_TOPIC_T add column (REVEAL_IDS_TO_ROLES bit);
alter table MFR_TOPIC_T modify column REVEAL_IDS_TO_ROLES bit not null;
alter table MFR_PERMISSION_LEVEL_T add column (IDENTIFY_ANON_AUTHORS bit);
alter table MFR_PERMISSION_LEVEL_T modify column IDENTIFY_ANON_AUTHORS bit not null;
update MFR_PERMISSION_LEVEL_T set IDENTIFY_ANON_AUTHORS = 1 where name = 'Owner';
