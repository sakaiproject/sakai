-- Most automatic database initialization will be taken care of automatically
-- by Hibernate's SchemaUpdate tool, triggered by the hibernate.hbm2ddl.auto
-- property in vanilla Hibernate applications and by the auto.ddl property
-- in the Sakai framework.
--
-- Not all necessary elements might be created by SchemaUpdate, however.
-- Notably, in versions of Hibernate through at least 3.1.3, no explicit
-- index definitions in the mapping file will be honored except during a
-- full SchemaExport.

-- Add indexes for improved performance and reduced locking.
alter table SAM_MEDIA_T modify (MEDIA blob);

create index SAM_AMETADATA_ASSESSMENTID_I on SAM_ASSESSMETADATA_T (ASSESSMENTID);
create index SAM_SECUREDIP_ASSESSMENTID_I on SAM_SECUREDIP_T (ASSESSMENTID);
create index SAM_SECTION_ASSESSMENTID_I on SAM_SECTION_T (ASSESSMENTID);
create index SAM_SECTIONMETA_SECTIONID_I on SAM_SECTIONMETADATA_T (SECTIONID);

create index SAM_ANSWER_ITEMID_I on SAM_ANSWER_T (ITEMID);

create index SAM_PUBA_ASSESSMENT_I on SAM_PUBLISHEDASSESSMENT_T (ASSESSMENTID);
create index SAM_PUBMETDATA_ASSESSMENT_I on SAM_PUBLISHEDMETADATA_T(ASSESSMENTID);


create index SAM_ITEMGRADING_ITEM_I on SAM_ITEMGRADING_T (PUBLISHEDITEMID);
create index SAM_ITEMGRADING_ITEMTEXT_I on SAM_ITEMGRADING_T (PUBLISHEDITEMTEXTID);
create index SAM_ITEMGRADING_PUBANS_I on SAM_ITEMGRADING_T (PUBLISHEDANSWERID);
create index SAM_PUBLISHEDASSESSMENT_I on SAM_ASSESSMENTGRADING_T (PUBLISHEDASSESSMENTID);
create index SAM_PUBLISHEDASSESSMENT2_I on SAM_STUDENTGRADINGSUMMARY_T (PUBLISHEDASSESSMENTID);
create index SAM_ASSGRAD_AID_PUBASSEID_T on SAM_ASSESSMENTGRADING_T (AGENTID,PUBLISHEDASSESSMENTID);

create index SAM_QPOOL_OWNER_I on SAM_QUESTIONPOOL_T (OWNERID);
create index SAM_QPOOLITEM_QPOOL_I on SAM_QUESTIONPOOLITEM_T (QUESTIONPOOLID);

