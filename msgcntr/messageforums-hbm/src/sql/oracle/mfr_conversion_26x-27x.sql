--////////////////////////////////////////////////////
--// SAK-11740
--// Email notification of new posts to forum
--////////////////////////////////////////////////////

--You may need to run these drop commands if this table still exists in your db
--DROP TABLE MFR_EMAIL_NOTIFICATION_TIF EXISTS ;
--drop sequence MFR_EMAIL_NOTIFICATION_S;

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

alter table MFR_MESSAGE_T add THREADID NUMBER(20);
alter table MFR_MESSAGE_T add LASTTHREADATE TIMESTAMP;
alter table MFR_MESSAGE_T add LASTTHREAPOST NUMBER(20);

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

--if MFR_MESSAGE_T is missing NUM_READERS, run alter and update commands
--alter table MFR_MESSAGE_T add NUM_READERS int;
--update MFR_MESSAGE_T set NUM_READERS = 0;


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

create sequence MFR_SYNOPTIC_ITEM_S;

create index MRF_SYN_USER on MFR_SYNOPTIC_ITEM (USER_ID);



--////////////////////////////////////////////////////
--// MSGCNTR-177
--// MyWorkspace/Home does now show the Messages & Forums Notifications by default
--////////////////////////////////////////////////////

update SAKAI_SITE_TOOL
Set TITLE = 'Unread Messages and Forums'
Where REGISTRATION = 'sakai.synoptic.messagecenter'; 

INSERT INTO SAKAI_SITE_TOOL VALUES('!user-145', '!user-100', '!user', 'sakai.synoptic.messagecenter', 2, 'Unread Messages and Forums', '1,1' );

create table MSGCNTR_TMP(
    PAGE_ID VARCHAR2(99),
    SITE_ID VARCHAR2(99)
);

insert into MSGCNTR_TMP
(   
    Select PAGE_ID, SITE_ID 
    from SAKAI_SITE_PAGE 
    where SITE_ID like '~%' 
    and TITLE = 'Home'
    and PAGE_ID not in (Select PAGE_ID from SAKAI_SITE_TOOL where REGISTRATION = 'sakai.synoptic.messagecenter')
);

insert into SAKAI_SITE_TOOL
(select SYS_GUID(), PAGE_ID, SITE_ID, 'sakai.synoptic.messagecenter', 2, 'Unread Messages and Forums', '1,1' from MSGCNTR_TMP);

drop table MSGCNTR_TMP;

--////////////////////////////////////////////////////
--//  MSGCNTR-25
--//  .UIPermissionsManagerImpl - query did not return a unique result: 4 Error in catalina.out
--////////////////////////////////////////////////////

alter table MFR_AREA_T add constraint MFR_AREA_CONTEXT_UUID_UNIQUE unique (CONTEXT_ID, TYPE_UUID);



--////////////////////////////////////////////////////
--//  MSGCNTR-148
--//  Unique constraint not created on MFR_PRIVATE_FORUM_T
--////////////////////////////////////////////////////

--If this alter query fails, use this select query to find duplicates and remove the duplicate:
--select OWNER, surrogateKey, COUNT(OWNER) FROM MFR_PRIVATE_FORUM_T GROUP BY OWNER, surrogateKey HAVING COUNT(OWNER)>1;
 
CREATE UNIQUE INDEX MFR_PVT_FRM_OWNER ON  MFR_PRIVATE_FORUM_T (OWNER, surrogateKey); 

--////////////////////////////////////////////////////
--//  MSGCNTR-132
--//  Drop unused MC table columns
--////////////////////////////////////////////////////

ALTER TABLE MFR_MESSAGE_T
DROP COLUMN GRADEBOOK;

ALTER TABLE MFR_MESSAGE_T
DROP COLUMN GRADEBOOK_ASSIGNMENT;

ALTER TABLE MFR_MESSAGE_T
DROP COLUMN GRADECOMMENT;

ALTER TABLE MFR_TOPIC_T
DROP COLUMN GRADEBOOK;

ALTER TABLE MFR_TOPIC_T
DROP COLUMN GRADEBOOK_ASSIGNMENT;

--////////////////////////////////////////////////////
--//  SAK-8421
--//  Statistics page is very slow
--////////////////////////////////////////////////////

create index MFR_UNREAD_MESSAGE_C_ID on MFR_UNREAD_STATUS_T (MESSAGE_C);
create index MFR_UNREAD_TOPIC_C_ID on MFR_UNREAD_STATUS_T (TOPIC_C);
create index MFR_UNREAD_USER_C_ID on MFR_UNREAD_STATUS_T (USER_C);
create index MFR_UNREAD_READ_C_ID on MFR_UNREAD_STATUS_T (READ_C);

