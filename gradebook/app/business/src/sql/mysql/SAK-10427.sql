alter table GB_GRADABLE_OBJECT_T add column UNGRADED bit;

update GB_GRADABLE_OBJECT_T set UNGRADED = false;