----------------------------------------------------------------------------------------------------------------------------------------

-- SAK-6780 added SQL update scripts to add new tables and alter existing tables to support selective release and spreadsheet upload

-- Gradebook table changes between Sakai 2.3.0 and 2.3.1.

-- Add selective release support

alter table GB_GRADABLE_OBJECT_T add (RELEASED NUMBER(1,0));
update GB_GRADABLE_OBJECT_T set RELEASED=1 where RELEASED is NULL;

----------------------------------------------------------------------------------------------------------------------------------------
