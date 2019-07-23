-- SAKAI-2720 when adding a quiz to the gradebook, allow instructor to also select to which category the quiz should belong
alter table SAM_ASSESSMENTBASE_T add CATEGORYID bigint(20);
alter table SAM_PUBLISHEDASSESSMENT_T add CATEGORYID bigint(20);