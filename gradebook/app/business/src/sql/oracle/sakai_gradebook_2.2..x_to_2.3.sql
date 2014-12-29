-- Gradebook table changes between Sakai 2.2.* and 2.3.

-- Add spreadsheet upload support.
create table GB_SPREADSHEET_T (
    ID          	NUMBER(19,0) NOT NULL,
    VERSION     	NUMBER(10,0) NOT NULL,
    CREATOR     	VARCHAR2(255) NOT NULL,
    NAME        	VARCHAR2(255) NOT NULL,
    CONTENT     	CLOB NOT NULL,
    DATE_CREATED	DATE NOT NULL,
    GRADEBOOK_ID	NUMBER(19,0) NOT NULL,
    PRIMARY KEY(ID)
);

create sequence GB_SPREADSHEET_S;

alter table GB_GRADABLE_OBJECT_T add (RELEASED NUMBER(1,0));
update GB_GRADABLE_OBJECT_T set RELEASED=1 where RELEASED is NULL;
