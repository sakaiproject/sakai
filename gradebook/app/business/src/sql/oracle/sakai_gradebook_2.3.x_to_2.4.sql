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
alter table GB_COMMENT_T 
	add constraint FK7977DFF06F98CFF foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T;
create sequence GB_COMMENT_S;

-- Remove database-caching of calculated course grades.
alter table GB_GRADE_RECORD_T drop column SORT_GRADE;
