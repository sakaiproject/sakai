-- Add Moderator functionality to Message Center (SAK-8632)

-- add column to allow Moderator as template setting
alter table MFR_AREA_T add column (MODERATED bit);
update MFR_AREA_T set MODERATED=0 where MODERATED is NULL;
alter table MFR_AREA_T modify column MODERATED bit not null;

-- change APPROVED column to allow null values to represent pending approvals
alter table MFR_MESSAGE_T modify APPROVED bit null;

-- change MODERATED column in MFR_OPEN_FORUM_T to not null
update MFR_OPEN_FORUM_T set MODERATED=0 where MODERATED is NULL;
alter table MFR_OPEN_FORUM_T modify column MODERATED bit not null;

-- change MODERATED column in MFR_TOPIC_T to not null
update MFR_TOPIC_T set MODERATED=0 where MODERATED is NULL;
alter table MFR_TOPIC_T modify column MODERATED bit not null;