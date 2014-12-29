
--////////////////////////////////////////////////////
--// SAK-11740
--// Email notification of new posts to forum
--////////////////////////////////////////////////////

CREATE TABLE `MFR_EMAIL_NOTIFICATION_T` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VERSION` int(11) NOT NULL,
  `USER_ID` varchar(255) NOT NULL,
  `CONTEXT_ID` varchar(255) NOT NULL,
  `NOTIFICATION_LEVEL` varchar(255) NOT NULL,
  PRIMARY KEY  (`ID`)
);

 
CREATE INDEX MFR_EMAIL_USER_ID_I ON  MFR_EMAIL_NOTIFICATION_T(USER_ID);
CREATE INDEX  MFR_EMAIL_CONTEXT_ID_I ON  MFR_EMAIL_NOTIFICATION_T(CONTEXT_ID);


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


--////////////////////////////////////////////////////
--// SAK-10559
--// View who has read a message
--////////////////////////////////////////////////////

--if MFR_MESSAGE_T is missing NUM_READERS, run alter and update commands
--alter table MFR_MESSAGE_T add column NUM_READERS int;
--update MFR_MESSAGE_T set NUM_READERS = 0;

--////////////////////////////////////////////////////
--// SAK-15655
--// Rework MyWorkspace Synoptic view of Messages & Forums
--////////////////////////////////////////////////////


CREATE TABLE MFR_SYNOPTIC_ITEM ( 
    SYNOPTIC_ITEM_ID      	bigint(20) AUTO_INCREMENT NOT NULL,
    VERSION               	int(11) NOT NULL,
    USER_ID               	varchar(36) NOT NULL,
    SITE_ID               	varchar(99) NOT NULL,
    SITE_TITLE            	varchar(255) NULL,
    NEW_MESSAGES_COUNT    	int(11) NULL,
    MESSAGES_LAST_VISIT_DT	datetime NULL,
    NEW_FORUM_COUNT       	int(11) NULL,
    FORUM_LAST_VISIT_DT   	datetime NULL,
    HIDE_ITEM             	bit(1) NULL,
    PRIMARY KEY(SYNOPTIC_ITEM_ID)
);
ALTER TABLE MFR_SYNOPTIC_ITEM
    ADD CONSTRAINT USER_ID
	UNIQUE (USER_ID, SITE_ID);
CREATE UNIQUE INDEX USER_ID
    ON MFR_SYNOPTIC_ITEM(USER_ID, SITE_ID);


--////////////////////////////////////////////////////
--// MSGCNTR-177
--// MyWorkspace/Home does now show the Messages & Forums Notifications by default
--////////////////////////////////////////////////////

    
update SAKAI_SITE_TOOL
Set TITLE = 'Unread Messages and Forums'
Where REGISTRATION = 'sakai.synoptic.messagecenter'; 

INSERT INTO SAKAI_SITE_TOOL VALUES('!user-145', '!user-100', '!user', 'sakai.synoptic.messagecenter', 2, 'Unread Messages and Forums', '1,1' );

create table MSGCNTR_TMP(
    PAGE_ID VARCHAR(99),
    SITE_ID VARCHAR(99)
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
(select uuid(), PAGE_ID, SITE_ID, 'sakai.synoptic.messagecenter', 2, 'Unread Messages and Forums', '1,1' from MSGCNTR_TMP);

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
 
CREATE UNIQUE INDEX MFR_PVT_FRM_OWNER ON MFR_PRIVATE_FORUM_T(OWNER, surrogateKey);


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
