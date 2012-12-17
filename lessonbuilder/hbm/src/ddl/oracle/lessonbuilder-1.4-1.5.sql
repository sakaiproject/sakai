alter table lesson_builder_items add groupOwned number(1,0);
alter table lesson_builder_items add ownerGroups varchar2(4000);
alter table lesson_builder_pages add groupid varchar2(36);
alter table lesson_builder_student_pages add groupid varchar2(36);
