alter table MFR_TOPIC_T add POST_ANONYMOUS NUMBER(1,0) default 0 not null;
alter table MFR_TOPIC_T add REVEAL_IDS_TO_ROLES NUMBER(1,0) default 0 not null;
alter table MFR_PERMISSION_LEVEL_T add IDENTIFY_ANON_AUTHORS NUMBER(1,0) default 0 not null;
update MFR_PERMISSION_LEVEL_T set IDENTIFY_ANON_AUTHORS = 1 where name = 'Owner';
