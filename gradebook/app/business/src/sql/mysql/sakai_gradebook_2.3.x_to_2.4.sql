-- Gradebook table changes between Sakai 2.3.* and 2.4.

-- Add grade commments.
create table GB_COMMENT_T (
	ID bigint not null auto_increment,
	VERSION integer not null,
	GRADER_ID varchar(255) not null,
	STUDENT_ID varchar(255) not null,
	COMMENT_TEXT text,
	DATE_RECORDED datetime not null,
	GRADABLE_OBJECT_ID bigint not null,
	primary key (ID),
	unique (STUDENT_ID, GRADABLE_OBJECT_ID)) TYPE=InnoDB;
alter table GB_COMMENT_T 
	add index FK7977DFF06F98CFF (GRADABLE_OBJECT_ID), 
	add constraint FK7977DFF06F98CFF foreign key (GRADABLE_OBJECT_ID) references GB_GRADABLE_OBJECT_T (ID);

-- Remove database-caching of calculated course grades.
alter table GB_GRADE_RECORD_T drop column SORT_GRADE;
