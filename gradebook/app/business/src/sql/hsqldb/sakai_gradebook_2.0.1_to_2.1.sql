-- Gradebook related tables changes needed between Sakai 2.01 and 2.1
alter table GB_GRADABLE_OBJECT_T add column (NOT_COUNTED bit);
