create table GB_LETTERGRADE_MAPPING (LG_MAPPING_ID number(19,0) not null, VALUE double precision, GRADE varchar2(255 char) not null, primary key (LG_MAPPING_ID, GRADE));
create table GB_LETTERGRADE_PERCENT_MAPPING (LGP_MAPPING_ID number(19,0) not null, VERSION number(10,0) not null, MAPPING_TYPE number(10,0) not null, GRADEBOOK_ID number(19,0), primary key (LGP_MAPPING_ID), unique (MAPPING_TYPE, GRADEBOOK_ID));
alter table GB_LETTERGRADE_MAPPING add constraint FKC8CDDC5CE7F3A761 foreign key (LG_MAPPING_ID) references GB_LETTERGRADE_PERCENT_MAPPING (LGP_MAPPING_ID);
create sequence GB_LETTER_MAPPING_S;

insert into GB_LETTERGRADE_PERCENT_MAPPING values (GB_LETTER_MAPPING_S.NEXTVAL, 0, 1, null);
insert into GB_LETTERGRADE_MAPPING values (1, 100.0, 'A+');
insert into GB_LETTERGRADE_MAPPING values (1, 95.0, 'A');
insert into GB_LETTERGRADE_MAPPING values (1, 90.0, 'A-');
insert into GB_LETTERGRADE_MAPPING values (1, 87.0, 'B+');
insert into GB_LETTERGRADE_MAPPING values (1, 83.0, 'B');
insert into GB_LETTERGRADE_MAPPING values (1, 80.0, 'B-');
insert into GB_LETTERGRADE_MAPPING values (1, 77.0, 'C+');
insert into GB_LETTERGRADE_MAPPING values (1, 73.0, 'C');
insert into GB_LETTERGRADE_MAPPING values (1, 70.0, 'C-');
insert into GB_LETTERGRADE_MAPPING values (1, 67.0, 'D+');
insert into GB_LETTERGRADE_MAPPING values (1, 63.0, 'D');
insert into GB_LETTERGRADE_MAPPING values (1, 60.0, 'D-');
insert into GB_LETTERGRADE_MAPPING values (1, 0.0, 'F');
