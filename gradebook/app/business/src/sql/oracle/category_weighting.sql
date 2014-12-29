create table GB_CATEGORY_T (ID number(19,0) not null, VERSION number(10,0) not null, GRADEBOOK_ID number(19,0) not null, NAME varchar2(255 char) not null, WEIGHT double precision, DROP_LOWEST number(10,0), REMOVED number(1,0), primary key (ID));

alter table GB_GRADABLE_OBJECT_T add CATEGORY_ID number(19,0);

alter table GB_GRADEBOOK_T add GRADE_TYPE number(10,0);

alter table GB_GRADEBOOK_T add CATEGORY_TYPE number(10,0);

alter table GB_CATEGORY_T add constraint FKCD333737325D7986 foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T;

alter table GB_GRADABLE_OBJECT_T add constraint FK759996A7F09DEFAE foreign key (CATEGORY_ID) references GB_CATEGORY_T;

create sequence GB_CATEGORY_S;

create index GB_CATEGORY_GB_IDX on GB_CATEGORY_T (GRADEBOOK_ID);

create index GB_GRADABLE_OBJ_CT_IDX on GB_GRADABLE_OBJECT_T (CATEGORY_ID);

update GB_GRADEBOOK_T set GRADE_TYPE = 1, CATEGORY_TYPE = 1;

alter table GB_GRADEBOOK_T modify ( GRADE_TYPE number(10,0) not null, CATEGORY_TYPE number(10,0) not null );
