-- This is the Oracle Sakai 2.1.1 -> 2.1.2 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.1.1 to 2.1.2.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------


-- RWiki
alter table rwikipreference add column prefcontext varchar(255), add column preftype varchar(64);

-- SAM
alter table SAM_PUBLISHEDASSESSMENT_T modify ASSESSMENTID integer;
alter table SAM_ITEMGRADING_T modify PUBLISHEDITEMID integer not null;
alter table SAM_ITEMGRADING_T modify PUBLISHEDITEMTEXTID integer not null;
alter table SAM_ITEMGRADING_T modify PUBLISHEDANSWERID integer;
alter table SAM_ASSESSMENTGRADING_T modify (SUBMITTEDDATE date null);

-- Grading.hbm.xml
CREATE INDEX SAM_ASSESSMENTGRADING_I ON SAM_ITEMGRADING_T (ASSESSMENTGRADINGID);
CREATE INDEX SAM_ITEMGRADING_PUBANS_I ON SAM_ITEMGRADING_T (PUBLISHEDANSWERID);
CREATE INDEX SAM_ITEMGRADING_ITEM_I ON SAM_ITEMGRADING_T (PUBLISHEDITEMID); 
CREATE INDEX SAM_ITEMGRADING_ITEMTEXT_I ON SAM_ITEMGRADING_T (PUBLISHEDITEMTEXTID);
CREATE INDEX SAM_PUBLISHEDASSESSMENT_I ON SAM_ASSESSMENTGRADING_T (PUBLISHEDASSESSMENTID);

-- PublishedAssessment.hbm.xml
CREATE INDEX SAM_PUBA_ASSESSMENT_I ON SAM_PUBLISHEDASSESSMENT_T (ASSESSMENTID);
CREATE INDEX SAM_PUBSECTION_ASSESSMENT_I ON SAM_PUBLISHEDSECTION_T (ASSESSMENTID);
CREATE INDEX SAM_PUBIP_ASSESSMENT_I ON SAM_PUBLISHEDSECUREDIP_T (ASSESSMENTID);

-- PublishedItemData.hbm.xml
CREATE INDEX SAM_PUBITEM_SECTION_I ON SAM_PUBLISHEDITEM_T (SECTIONID);
CREATE INDEX SAM_PUBITEMTEXT_ITEM_I ON SAM_PUBLISHEDITEMTEXT_T (ITEMID);
CREATE INDEX SAM_PUBITEMMETA_ITEM_I ON SAM_PUBLISHEDITEMMETADATA_T (ITEMID); 
CREATE INDEX SAM_PUBITEMFB_ITEM_I ON SAM_PUBLISHEDITEMFEEDBACK_T (ITEMID);
CREATE INDEX SAM_PUBANSWER_ITEMTEXT_I ON SAM_PUBLISHEDANSWER_T (ITEMTEXTID);
CREATE INDEX SAM_PUBANSWER_ITEM_I ON SAM_PUBLISHEDANSWER_T (ITEMID); 
CREATE INDEX SAM_PUBANSWERFB_ANSWER_I ON SAM_PUBLISHEDANSWERFEEDBACK_T (ANSWERID);

-- MediaData.hbm.xml
CREATE INDEX SAM_MEDIA_ITEMGRADING_I ON SAM_MEDIA_T (ITEMGRADINGID);

commit;
-- change constraint name for hbm change
alter table SAM_PUBLISHEDANSWER_T drop constraint FKB41EA361B9BF0B8E;
alter table SAM_PUBLISHEDANSWER_T add constraint FKB41EA3618152036E foreign key (ITEMID) references SAM_PUBLISHEDITEM_T;


-- OSP
alter table osp_structured_artifact_def add schema_hash varchar(255);
