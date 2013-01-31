-- this should be harmless if it's already been done
-- it applies to versions before sakai 2.9.2

alter table lesson_builder_items modify html mediumtext; 
