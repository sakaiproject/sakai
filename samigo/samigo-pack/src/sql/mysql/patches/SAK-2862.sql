alter table SAM_ASSESSFEEDBACK_T
add column  FEEDBACKAUTHORING integer;
alter table SAM_PUBLISHEDFEEDBACK_T
add column  FEEDBACKAUTHORING integer;


INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, 1, 'feedbackAuthoring_isInstructorEditable', 'true')
;

-- if you want to update all the assessment then run 
update SAM_ASSESSFEEDBACK_T set FEEDBACKAUTHORING=1;

-- if you only want to update the template then run 
update SAM_ASSESSFEEDBACK_T set FEEDBACKAUTHORING=1 WHERE ASSESSMENTID =  1;

-- Note: For example, if a faculty wants to add another multiple choice question in an existing assessment, by running the first script( update all assessment), now he doesn't see the selection level feedbacks for choice a, b, c, d..etc, so the page looks shorter. But on the other side, if he wants to modify an existing question, now he can't see the selection feedbacks he entered for them.

 


;
