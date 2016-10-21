-- This is the MySQL Sakai 2.1.1 -> 2.1.2 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.1.1 to 2.1.2.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------


-- RWiki
alter table rwikipreference add column prefcontext varchar(255), add column preftype varchar(64);

-- SAM
alter table SAM_PUBLISHEDASSESSMENT_T modify column ASSESSMENTID integer;
alter table SAM_ITEMGRADING_T modify column PUBLISHEDITEMID integer not null;
alter table SAM_ITEMGRADING_T modify column PUBLISHEDITEMTEXTID integer not null;
alter table SAM_ITEMGRADING_T modify column PUBLISHEDANSWERID integer;
alter table SAM_ASSESSMENTGRADING_T change SUBMITTEDDATE SUBMITTEDDATE datetime null;

-- Grading.hbm.xml
CREATE INDEX SAM_ITEMGRADING_PUBANS_I ON SAM_ITEMGRADING_T (PUBLISHEDANSWERID);
CREATE INDEX SAM_ITEMGRADING_ITEM_I ON SAM_ITEMGRADING_T (PUBLISHEDITEMID); 
CREATE INDEX SAM_ITEMGRADING_ITEMTEXT_I ON SAM_ITEMGRADING_T (PUBLISHEDITEMTEXTID);
CREATE INDEX SAM_PUBLISHEDASSESSMENT_I ON SAM_ASSESSMENTGRADING_T (PUBLISHEDASSESSMENTID);

-- PublishedAssessment.hbm.xml
CREATE INDEX SAM_PUBA_ASSESSMENT_I ON SAM_PUBLISHEDASSESSMENT_T (ASSESSMENTID);

-- PublishedItemData.hbm.xml

-- MediaData.hbm.xml

commit;

-- change constraint name for hbm change
alter table SAM_PUBLISHEDANSWER_T drop foreign key FKB41EA361B9BF0B8E;
alter table SAM_PUBLISHEDANSWER_T add constraint FKB41EA3618152036E foreign key (ITEMID) references SAM_PUBLISHEDITEM_T(ITEMID);


-- OSP
alter table osp_structured_artifact_def add schema_hash varchar(255);
