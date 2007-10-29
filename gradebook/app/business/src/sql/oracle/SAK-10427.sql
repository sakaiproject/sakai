alter table GB_GRADABLE_OBJECT_T add (UNGRADED number(1,0));

update GB_GRADABLE_OBJECT_T set UNGRADED = 0;

alter table GB_GRADE_RECORD_T add NON_CALCULATE_GRADE varchar2(255);