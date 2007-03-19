-- Gradebook table changes between Sakai 2.3.* and 2.4.

-- Add grade commments.
create table GB_COMMENT_T (
	ID number(19,0) not null,
	VERSION number(10,0) not null,
	GRADER_ID varchar2(255 char) not null,
	STUDENT_ID varchar2(255 char) not null,
	COMMENT_TEXT clob,
	DATE_RECORDED timestamp not null,
	GRADABLE_OBJECT_ID number(19,0) not null,
	primary key (ID),
	unique (STUDENT_ID, GRADABLE_OBJECT_ID));

-- Remove database-caching of calculated course grades.
alter table GB_GRADE_RECORD_T drop column SORT_GRADE;
