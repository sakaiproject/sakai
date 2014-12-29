CREATE TABLE GB_PERMISSION_T ( 
    GB_PERMISSION_ID number(19,0) not null,
    VERSION     	 number(10,0) not null,
    GRADEBOOK_ID	 number(19,0) not null,
    USER_ID     	 varchar2(99) not null,
    FUNCTION_NAME 	 varchar2(5) not null,
    CATEGORY_ID 	 number(19,0) null,
    GROUP_ID    	 varchar2(255) null,
    PRIMARY KEY(GB_PERMISSION_ID)
);

create sequence GB_PERMISSION_S;



