-- set all template settings to be editable by default
UPDATE SAM_ASSESSMETADATA_T SET ENTRY='true' WHERE ASSESSMENTID=1;
-- correct typo
UPDATE SAM_ASSESSMETADATA_T SET LABEL='toGradebook_isInstructorEditable' WHERE ASSESSMENTID=1 AND LABEL='toGradebook_isInstructorEditablee';
commit;