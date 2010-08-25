--MSGCNTR-309
--Start and End dates on Forums and Topics

alter table MFR_AREA_T add (AVAILABILITY_RESTRICTED bit);
update MFR_AREA_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_AREA_T modify (AVAILABILITY_RESTRICTED bit NOT NULL DEFAULT '');

alter table MFR_AREA_T add (AVAILABILITY bit);
update MFR_AREA_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_AREA_T modify (AVAILABILITY NUMBER(1,0) bit NOT NULL DEFAULT ''));

alter table MFR_AREA_T add (OPEN_DATE datetime);

alter table MFR_AREA_T add (CLOSE_DATE datetime);


alter table MFR_OPEN_FORUM_T add (AVAILABILITY_RESTRICTED bit);
update MFR_OPEN_FORUM_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY_RESTRICTED bit NOT NULL DEFAULT ''));

alter table MFR_OPEN_FORUM_T add (AVAILABILITY bit);
update MFR_OPEN_FORUM_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY bit NOT NULL DEFAULT ''));

alter table MFR_OPEN_FORUM_T add (OPEN_DATE datetime);

alter table MFR_OPEN_FORUM_T add (CLOSE_DATE datetime);

alter table MFR_TOPIC_T add (AVAILABILITY_RESTRICTED bit);
update MFR_TOPIC_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY_RESTRICTED bit NOT NULL DEFAULT ''));

alter table MFR_TOPIC_T add (AVAILABILITY bit);
update MFR_TOPIC_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY bit NOT NULL DEFAULT ''));

alter table MFR_TOPIC_T add (OPEN_DATE datetime null);

alter table MFR_TOPIC_T add (CLOSE_DATE datetime null);