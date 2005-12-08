alter table SAM_ASSESSFEEDBACK_T
add (FEEDBACKAUTHORING integer);
alter table SAM_PUBLISHEDFEEDBACK_T
add (FEEDBACKAUTHORING integer);

INSERT INTO SAM_ASSESSMETADATA_T ("ASSESSMENTMETADATAID", "ASSESSMENTID","LABEL", "ENTRY")
    VALUES(sam_assessMetaData_id_s.nextVal, 1, 'feedbackAuthoring_isInstructorEditable', 'true');

