create table GB_LETTERGRADE_MAPPING (id number(19,0) not null, value double precision, grade varchar2(255 char) not null, primary key (id, grade));
create table GB_LETTERGRADE_PERCENT_MAPPING (ID number(19,0) not null, VERSION number(10,0) not null, MAPPING_TYPE number(10,0) not null, GRADEBOOK_ID number(19,0), primary key (ID), unique (MAPPING_TYPE, GRADEBOOK_ID));
alter table GB_LETTERGRADE_MAPPING add constraint FKC8CDDC5CE7F3A761 foreign key (id) references GB_LETTERGRADE_PERCENT_MAPPING;
create sequence GB_LETTER_MAPPING_S;

insert into GB_LETTERGRADE_PERCENT_MAPPING values (GB_LETTER_MAPPING_S.NEXTVAL, 0, 1, null);
insert into GB_LETTERGRADE_MAPPING values (1, 98.0, 'A+');
insert into GB_LETTERGRADE_MAPPING values (1, 95.0, 'A');
insert into GB_LETTERGRADE_MAPPING values (1, 90.0, 'A-');
insert into GB_LETTERGRADE_MAPPING values (1, 88.0, 'B+');
insert into GB_LETTERGRADE_MAPPING values (1, 85.0, 'B');
insert into GB_LETTERGRADE_MAPPING values (1, 80.0, 'B-');
insert into GB_LETTERGRADE_MAPPING values (1, 78.0, 'C+');
insert into GB_LETTERGRADE_MAPPING values (1, 75.0, 'C');
insert into GB_LETTERGRADE_MAPPING values (1, 70.0, 'C-');
insert into GB_LETTERGRADE_MAPPING values (1, 68.0, 'D+');
insert into GB_LETTERGRADE_MAPPING values (1, 65.0, 'D');
insert into GB_LETTERGRADE_MAPPING values (1, 60.0, 'D-');
insert into GB_LETTERGRADE_MAPPING values (1, 0.0, 'F');