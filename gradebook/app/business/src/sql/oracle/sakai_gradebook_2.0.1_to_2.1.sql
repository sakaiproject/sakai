-- Gradebook related tables changes needed between Sakai 2.01 and 2.1
alter table GB_GRADABLE_OBJECT_T add (NOT_COUNTED number(1,0));
update GB_GRADABLE_OBJECT_T set NOT_COUNTED=0 where NOT_COUNTED is NULL and POINTS_POSSIBLE is not NULL;
