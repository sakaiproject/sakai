-- Gradebook related tables changes needed between Sakai 2.01 and 2.1
alter table GB_GRADABLE_OBJECT_T add column (NOT_COUNTED bit);

-- Guard against any bugs that might result in duplicate grade records.
alter table GB_GRADE_RECORD_T add unique (GRADABLE_OBJECT_ID, STUDENT_ID);
create index GB_GRADE_RECORD_G_O_IDX on GB_GRADE_RECORD_T (GRADABLE_OBJECT_ID);
create index GB_GRADE_RECORD_STUDENT_ID_IDX on GB_GRADE_RECORD_T (STUDENT_ID);
