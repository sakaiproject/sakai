-- This is the Oracle Sakai 2.1.0 -> 2.1.1 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.1.0 to 2.1.1.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------


-- Gradebook
ALTER TABLE GB_GRADE_RECORD_T ADD UNIQUE (GRADABLE_OBJECT_ID, STUDENT_ID);

-- RWiki
create table rwikipagetrigger (id varchar2(36) not null, userid varchar2(64) not null, pagespace varchar2(255), pagename varchar2(255), lastseen date, triggerspec clob, primary key (id));
create table rwikipagemessage (id varchar2(36) not null, sessionid varchar2(255), userid varchar2(64) not null, pagespace varchar2(255), pagename varchar2(255), lastseen date, message clob, primary key (id));
create table rwikipagepresence (id varchar2(36) not null, sessionid varchar2(255), userid varchar2(64) not null, pagespace varchar2(255), pagename varchar2(255), lastseen date, primary key (id));
create table rwikipreference (id varchar2(36) not null, userid varchar2(64) not null, lastseen date, preference clob, primary key (id));


-- SAM
ALTER TABLE SAM_PUBLISHEDFEEDBACK_T ADD FEEDBACKAUTHORING integer;
ALTER TABLE SAM_ASSESSFEEDBACK_T ADD FEEDBACKAUTHORING integer;

-- Will update the default template with the correct default value for 
-- AuthoringSetting (default is to only show question level feedback input 
-- fields for questions in authoring, to make the page easier to read)
UPDATE SAM_ASSESSFEEDBACK_T SET FEEDBACKAUTHORING = 1 WHERE ASSESSMENTID = 1;

-- If you want to change all the existing assessments to use this default then 
-- you can run this version of the update statement
-- UPDATE SAM_ASSESSFEEDBACK_T SET FEEDBACKAUTHORING = 1;

INSERT INTO SAM_ASSESSMETADATA_T ("ASSESSMENTMETADATAID", "ASSESSMENTID","LABEL", "ENTRY")
    VALUES(sam_assessMetaData_id_s.nextVal, 1, 'feedbackAuthoring_isInstructorEditable', 'true');
