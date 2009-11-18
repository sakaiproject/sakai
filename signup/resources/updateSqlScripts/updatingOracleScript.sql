-- add a new table
-- drop table signup_attachments cascade constraints;
CREATE TABLE  signup_attachments (
   meeting_id 	number(19,0) NOT NULL,
   resource_Id 	varchar2(255 char) default NULL,
   file_name 	varchar2(255 char) default NULL,
   mime_type 	varchar2(80 char) default NULL,
   fileSize 	number(19,0) default NULL,
   location 	clob,
   isLink  	number(1,0)  default '0' NULL,
   timeslot_id 	number(19,0) default NULL,
   view_by_all 	number(1,0)  default '1' ,
   created_by 	varchar2(255 char) NOT NULL,
   created_date 	timestamp NOT NULL,
   last_modified_by 	varchar2(255 char) NOT NULL,
   last_modified_date 	timestamp NOT NULL,
   list_index 	number(10,0) NOT NULL,
   PRIMARY KEY  (meeting_id,list_index));

alter table signup_attachments add constraint FK_SIGNUP_MEETING_ATTACHMENT foreign key (meeting_id) references signup_meetings;

-- add four columns into signup_meetings table
ALTER TABLE signup_meetings  ADD  allow_waitList  number(1,0)  default '1' ;
ALTER TABLE signup_meetings  ADD  allow_comment   number(1,0)  default '1' ;
ALTER TABLE signup_meetings  ADD  eid_input_mode  number(1,0)  default '0' NULL;
ALTER TABLE signup_meetings  ADD  auto_reminder   number(1,0)  default '0' NULL;
ALTER TABLE signup_meetings  ADD  repeat_type	  varchar2(20 char)  default NULL;
