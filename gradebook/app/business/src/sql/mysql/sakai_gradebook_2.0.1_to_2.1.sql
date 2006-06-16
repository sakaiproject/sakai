-- Gradebook related tables changes needed between Sakai 2.01 and 2.1
alter table GB_GRADABLE_OBJECT_T add column (NOT_COUNTED bit);
update GB_GRADABLE_OBJECT_T set NOT_COUNTED=0 where NOT_COUNTED is NULL and POINTS_POSSIBLE is not NULL;
