create table GB_CATEGORY_T (ID bigint not null auto_increment, VERSION integer not null, GRADEBOOK_ID bigint not null, NAME varchar(255) not null, WEIGHT double precision, DROP_LOWEST integer, REMOVED bit, primary key (ID));

alter table GB_GRADABLE_OBJECT_T add CATEGORY_ID bigint;

alter table GB_GRADEBOOK_T add GRADE_TYPE integer not null;

alter table GB_GRADEBOOK_T add CATEGORY_TYPE integer not null;

alter table GB_CATEGORY_T add index FKCD333737325D7986 (GRADEBOOK_ID), add constraint FKCD333737325D7986 foreign key (GRADEBOOK_ID) references GB_GRADEBOOK_T (ID);

alter table GB_GRADABLE_OBJECT_T add index FK759996A7F09DEFAE (CATEGORY_ID), add constraint FK759996A7F09DEFAE foreign key (CATEGORY_ID) references GB_CATEGORY_T (ID);

create index GB_CATEGORY_GB_IDX on GB_CATEGORY_T (GRADEBOOK_ID);

create index GB_GRADABLE_OBJ_CT_IDX on GB_GRADABLE_OBJECT_T (CATEGORY_ID);

update GB_GRADEBOOK_T set GRADE_TYPE = 1, CATEGORY_TYPE = 1;
