--MSGCNTR-309
--Start and End dates on Forums and Topics
alter table MFR_AREA_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_AREA_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_AREA_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) not null default false);

alter table MFR_AREA_T add (AVAILABILITY NUMBER(1,0));
update MFR_AREA_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_AREA_T modify (AVAILABILITY NUMBER(1,0) not null default false);

alter table MFR_AREA_T add (OPEN_DATE timestamp);

alter table MFR_AREA_T add (CLOSE_DATE timestamp);


alter table MFR_OPEN_FORUM_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_OPEN_FORUM_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) not null default false);

alter table MFR_OPEN_FORUM_T add (AVAILABILITY NUMBER(1,0));
update MFR_OPEN_FORUM_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY NUMBER(1,0) not null default false);

alter table MFR_OPEN_FORUM_T add (OPEN_DATE timestamp);

alter table MFR_OPEN_FORUM_T add (CLOSE_DATE timestamp);

alter table MFR_TOPIC_T add (AVAILABILITY_RESTRICTED NUMBER(1,0));
update MFR_TOPIC_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY_RESTRICTED NUMBER(1,0) not null default false);

alter table MFR_TOPIC_T add (AVAILABILITY NUMBER(1,0));
update MFR_TOPIC_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY NUMBER(1,0) not null default false);

alter table MFR_TOPIC_T add (OPEN_DATE timestamp);

alter table MFR_TOPIC_T add (CLOSE_DATE timestamp);