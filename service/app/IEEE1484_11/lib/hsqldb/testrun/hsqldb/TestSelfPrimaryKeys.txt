--
-- TestSelfPrimaryKeys.txt
--
-- Tests for add / drop primary key constraints
--
drop table testtable if exists;
create cached table testtable (
    aString              varchar(256)                   not null,
    firstNum             integer                        not null,
    aDate                date                           not null,
    secondNum            integer                        not null,
    thirdNum             integer                        not null,
    aName                varchar(32)                    not null
  );
insert into TESTTABLE(aString, firstNum, aDate, secondNum, thirdNum, aName)
  values ('Current', 22, '2003-11-10', 18, 3, 'my name goes here');
insert into TESTTABLE(aString, firstNum, aDate, secondNum, thirdNum, aName)
  values ('Popular', 23, '2003-11-10', 18, 3, 'my name goes here');
insert into TESTTABLE(aString, firstNum, aDate, secondNum, thirdNum, aName)
  values ('New', 5, '2003-11-10', 18, 3, 'my name goes here');
insert into TESTTABLE(aString, firstNum, aDate, secondNum, thirdNum, aName)
  values ('Old', 5, '2003-11-10', 18, 3, 'my name goes here');
insert into TESTTABLE(aString, firstNum, aDate, secondNum, thirdNum, aName)
  values ('CCurrent', 5, '2003-11-10', 18, 3, 'my name goes here');
insert into TESTTABLE(aString, firstNum, aDate, secondNum, thirdNum, aName)
  values ('ELV', 5, '2003-11-10', 18, 3, 'my name goes here');
insert into TESTTABLE(aString, firstNum, aDate, secondNum, thirdNum, aName)
  values ('ELNA', 5, '2003-11-10', 18, 3, 'my name goes here');
insert into TESTTABLE(aString, firstNum, aDate, secondNum, thirdNum, aName)
  values ('Older', 5, '2003-11-10', 18, 3, 'my name goes here');
insert into TESTTABLE(aString, firstNum, aDate, secondNum, thirdNum, aName)
  values ('RA', 20, '2003-11-10', 18, 3, 'my name goes here');
insert into TESTTABLE(aString, firstNum, aDate, secondNum, thirdNum, aName)
  values ('RP', 2, '2003-11-10', 18, 3, 'my name goes here');

alter table testtable add constraint pk_testtable primary key (aString);
alter table testtable drop constraint pk_testtable
