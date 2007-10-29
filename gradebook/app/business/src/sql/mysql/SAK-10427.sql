alter table GB_GRADABLE_OBJECT_T add column UNGRADED bit;

update GB_GRADABLE_OBJECT_T set UNGRADED = false;

alter table GB_GRADE_RECORD_T add NON_CALCULATE_GRADE varchar(255);