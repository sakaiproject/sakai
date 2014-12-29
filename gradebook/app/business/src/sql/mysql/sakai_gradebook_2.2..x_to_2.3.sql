-- Gradebook table changes between Sakai 2.2.* and 2.3.

-- Add spreadsheet upload support.
create table GB_SPREADSHEET_T (
  ID bigint(20) NOT NULL auto_increment,
  VERSION int(11) NOT NULL,
  CREATOR varchar(255) NOT NULL,
  NAME varchar(255) NOT NULL,
  CONTENT text NOT NULL,
  DATE_CREATED datetime NOT NULL,
  GRADEBOOK_ID bigint(20) NOT NULL,
  PRIMARY KEY  (`ID`)
);


-- Add selective release support

alter table GB_GRADABLE_OBJECT_T add column (RELEASED bit);
update GB_GRADABLE_OBJECT_T set RELEASED=1 where RELEASED is NULL;
