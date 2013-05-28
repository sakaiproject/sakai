-- this should be harmless if it's already been done
-- it applies to versions before sakai 2.9.3

--- alter table lesson_builder_items modify description clob;
alter table lesson_builder_items add temp clob;
update lesson_builder_items set temp=description;
alter table lesson_builder_items drop column description;
alter table lesson_builder_items rename column temp to description;

--- alter table lesson_builder_items modify groups clob;
alter table lesson_builder_items add temp clob;
update lesson_builder_items set temp=groups;
alter table lesson_builder_items drop column groups;
alter table lesson_builder_items rename column temp to groups;


--- alter table lesson_builder_items modify ownerGroups clob;
alter table lesson_builder_items add temp clob;
update lesson_builder_items set temp=ownerGroups;
alter table lesson_builder_items drop column ownerGroups;
alter table lesson_builder_items rename column temp to ownerGroups;



