-- Guard against any bugs that might result in duplicate grade records.
-- See http://bugs.sakaiproject.org/jira/browse/SAK-3187 for details.
alter table GB_GRADE_RECORD_T add unique (GRADABLE_OBJECT_ID, STUDENT_ID);
create index GB_GRADE_RECORD_G_O_IDX on GB_GRADE_RECORD_T (GRADABLE_OBJECT_ID);
create index GB_GRADE_RECORD_STUDENT_ID_IDX on GB_GRADE_RECORD_T (STUDENT_ID);
