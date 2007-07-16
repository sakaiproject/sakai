create table GB_LETTERGRADE_MAPPING (id number(19,0) not null, value double precision, grade varchar2(255 char) not null, primary key (id, grade));
create table GB_LETTERGRADE_PERCENT_MAPPING (ID number(19,0) not null, VERSION number(10,0) not null, MAPPING_TYPE number(10,0) not null, GRADEBOOK_ID number(19,0), primary key (ID), unique (MAPPING_TYPE, GRADEBOOK_ID));
alter table GB_LETTERGRADE_MAPPING add constraint FKC8CDDC5CE7F3A761 foreign key (id) references GB_LETTERGRADE_PERCENT_MAPPING;
create sequence GB_LETTER_MAPPING_S;

insert into GB_LETTERGRADE_PERCENT_MAPPING values (GB_LETTER_MAPPING_S.NEXTVAL, 0, 1, null);
insert into GB_LETTERGRADE_MAPPING values (1, 0.98, 'a+');
insert into GB_LETTERGRADE_MAPPING values (1, 0.95, 'a');
insert into GB_LETTERGRADE_MAPPING values (1, 0.90, 'a-');
insert into GB_LETTERGRADE_MAPPING values (1, 0.88, 'b+');
insert into GB_LETTERGRADE_MAPPING values (1, 0.85, 'b');
insert into GB_LETTERGRADE_MAPPING values (1, 0.80, 'b-');
insert into GB_LETTERGRADE_MAPPING values (1, 0.78, 'c+');
insert into GB_LETTERGRADE_MAPPING values (1, 0.75, 'c');
insert into GB_LETTERGRADE_MAPPING values (1, 0.70, 'c-');
insert into GB_LETTERGRADE_MAPPING values (1, 0.68, 'd+');
insert into GB_LETTERGRADE_MAPPING values (1, 0.65, 'd');
insert into GB_LETTERGRADE_MAPPING values (1, 0.60, 'd-');
insert into GB_LETTERGRADE_MAPPING values (1, 0.0, 'f');