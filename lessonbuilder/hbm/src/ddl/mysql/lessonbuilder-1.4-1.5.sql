alter table lesson_builder_items add groupOwned bit;
alter table lesson_builder_items add ownerGroups text;
alter table lesson_builder_pages add groupid varchar(36);
alter table lesson_builder_student_pages add groupid varchar(36);
