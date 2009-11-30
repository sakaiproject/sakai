
--////////////////////////////////////////////////////
--// SAK-11740
--// Email notification of new posts to forum
--////////////////////////////////////////////////////

DROP TABLE MFR_EMAIL_NOTIFICATION_T;
drop sequence MFR_EMAIL_NOTIFICATION_S;

CREATE TABLE  "MFR_EMAIL_NOTIFICATION_T"
   (    "ID" NUMBER(19,0) NOT NULL ENABLE,
        "VERSION" NUMBER(10,0) NOT NULL ENABLE,
        "USER_ID" VARCHAR2(255 BYTE) NOT NULL ENABLE,
        "CONTEXT_ID" VARCHAR2(255 BYTE) NOT NULL ENABLE,
        "NOTIFICATION_LEVEL" NUMBER(1,0) NOT NULL ENABLE,
         PRIMARY KEY ("ID")
  
   )  ;
CREATE INDEX "MFR_EMAIL_USER_ID_I" ON  "MFR_EMAIL_NOTIFICATION_T" ("USER_ID")  ;

CREATE INDEX  "MFR_EMAIL_CONTEXT_ID_I" ON  "MFR_EMAIL_NOTIFICATION_T" ("CONTEXT_ID") ;



create sequence MFR_EMAIL_NOTIFICATION_S;


--////////////////////////////////////////////////////
--// SAK-15052
--// update cafe versions to 2.7.0-SNAPSHOT
--////////////////////////////////////////////////////

alter table MFR_MESSAGE_T add column THREADID bigint(20);
alter table MFR_MESSAGE_T add column LASTTHREADATE datetime;
alter table MFR_MESSAGE_T add column LASTTHREAPOST bigint(20);

update MFR_MESSAGE_T set THREADID=IN_REPLY_TO,LASTTHREADATE=CREATED;

--////////////////////////////////////////////////////
--// SAK-10869
--// Displaying all messages should mark them as read
--////////////////////////////////////////////////////


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



--////////////////////////////////////////////////////
--// SAK-10559
--// View who has read a message
--////////////////////////////////////////////////////

--Pending...



--////////////////////////////////////////////////////
--// SAK-15655
--// Rework MyWorkspace Synoptic view of Messages & Forums
--////////////////////////////////////////////////////

create table MFR_SYNOPTIC_ITEM
(SYNOPTIC_ITEM_ID number(19,0) not null,
VERSION number(10,0) not null,
USER_ID varchar2(99 char) not null,
SITE_ID varchar2(99 char) not null,
SITE_TITLE varchar2(255 char),
NEW_MESSAGES_COUNT number(10,0),
MESSAGES_LAST_VISIT_DT timestamp,
NEW_FORUM_COUNT number(10,0),
FORUM_LAST_VISIT_DT timestamp,
HIDE_ITEM NUMBER(1,0),
primary key (SYNOPTIC_ITEM_ID),
unique (USER_ID, SITE_ID));

create index MFR_SYN_STU_I on MFR_SYNOPTIC_ITEM (USER_ID, SITE_ID);

create index MRF_SYN_USER on MFR_SYNOPTIC_ITEM (USER_ID);
