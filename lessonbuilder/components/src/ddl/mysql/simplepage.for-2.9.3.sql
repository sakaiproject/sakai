-- this should be harmless if it's already been done
-- it applies to versions before sakai 2.9.3

alter table lesson_builder_items modify description text;
alter table lesson_builder_items modify groups text;
alter table lesson_builder_items modify ownerGroups text;

