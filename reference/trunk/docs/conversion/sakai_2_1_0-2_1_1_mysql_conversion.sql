-- This is the MySQL Sakai 2.1.0 -> 2.1.1 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.1.0 to 2.1.1.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------


-- Gradebook
ALTER TABLE GB_GRADE_RECORD_T ADD CONSTRAINT UNIQUE (GRADABLE_OBJECT_ID, STUDENT_ID);

-- RWiki
create table rwikipagetrigger (id varchar(36) not null, userid varchar(64) not null, pagespace varchar(255), pagename varchar(255), lastseen datetime, triggerspec text, primary key (id));
create table rwikipagemessage (id varchar(36) not null, sessionid varchar(255), userid varchar(64) not null, pagespace varchar(255), pagename varchar(255), lastseen datetime, message text, primary key (id));
create table rwikipagepresence (id varchar(36) not null, sessionid varchar(255), userid varchar(64) not null, pagespace varchar(255), pagename varchar(255), lastseen datetime, primary key (id));
create table rwikipreference (id varchar(36) not null, userid varchar(64) not null, lastseen datetime, preference text, primary key (id));

-- SAM
ALTER TABLE SAM_PUBLISHEDFEEDBACK_T ADD COLUMN FEEDBACKAUTHORING integer;
ALTER TABLE SAM_ASSESSFEEDBACK_T ADD COLUMN FEEDBACKAUTHORING integer;

-- Will update the default template with the correct default value for 
-- AuthoringSetting (default is to only show question level feedback input 
-- fields for questions in authoring, to make the page easier to read)
UPDATE SAM_ASSESSFEEDBACK_T SET FEEDBACKAUTHORING = 1 WHERE ASSESSMENTID = 1;

-- If you want to change all the existing assessments to use this default then 
-- you can run this version of the update statement
-- UPDATE SAM_ASSESSFEEDBACK_T SET FEEDBACKAUTHORING = 1;

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, 1, 'feedbackAuthoring_isInstructorEditable', 'true')
;

-- SAK-2753 this patch is for those who use mySQL DB and mySQL ConnectorJ  3.1.x. 
-- updating label and hint fields for True and False questions, from NULL to "". 
-- update SAM_ANSWER_T answer set answer.label="" where  answer.itemid in (select itemid from SAM_ITEM_T item where item.itemid = answer.itemid and item.typeid =4  );
-- update SAM_ITEM_T item set item.hint="" where  item.typeid =4 ;
-- update SAM_PUBLISHEDANSWER_T answer set answer.label="" where  answer.itemid in (select itemid from SAM_PUBLISHEDITEM_T item where item.itemid = answer.itemid and item.typeid =4  );
-- update SAM_PUBLISHEDITEM_T item set item.hint="" where  item.typeid =4 ;
