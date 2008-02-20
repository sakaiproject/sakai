alter table GB_GRADABLE_OBJECT_T change DUE_DATE due_date datetime;

update GB_GRADABLE_OBJECT_T set DUE_DATE = DATE_ADD(DUE_DATE, INTERVAL 86399 SECOND) where DUE_DATE is not null;