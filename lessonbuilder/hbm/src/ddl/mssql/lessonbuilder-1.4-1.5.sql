alter table lesson_builder_items add groupOwned tinyint null;
alter table lesson_builder_items add ownerGroups varchar(4000) null;
alter table lesson_builder_pages add groupid varchar(36) null;
alter table lesson_builder_student_pages add groupid varchar(36) null;
