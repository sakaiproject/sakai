-- MSGCNTR-708 - change the boolean SENDEMAILOUT functionality to a multiple
-- option setting for finer grained control of email copies of messages

alter table MFR_AREA_T add SEND_TO_EMAIL NUMBER(10,0) default 1 not null;

-- set the SEND_TO_EMAIL column equal to the equivalent value for SENDEMAILOUT
update MFR_AREA_T area set area.SEND_TO_EMAIL=0 
where area.SENDEMAILOUT=0 and 
area.TYPE_UUID = (select types.uuid from cmn_type_t types where types.KEYWORD = 'privateForums');
        
-- MSGCNTR-241 - Ability to move a thread to a different topic 

DROP SEQUENCE MFR_MOVE_HISTORY_S;
DROP INDEX MFR_MOVE_HISTORY_MESSAGE_I;
DROP TABLE MFR_MOVE_HISTORY_T;

create table MFR_MOVE_HISTORY_T (
   ID number(19,0) not null,
   UUID varchar2(36) not null,
   VERSION number(10,0) not null,
   TO_TOPIC_ID number(19,0) not null,
   FROM_TOPIC_ID number(19,0) not null,
   MESSAGE_ID number(19,0) not null,
   REMINDER number(1,0) null,
   CREATED_BY varchar2(36) not null,
   CREATED date not null,
   MODIFIED_BY varchar2(36) not null,
   MODIFIED date not null,
   primary key (ID)
);
create sequence MFR_MOVE_HISTORY_S;
create index MFR_MOVE_HISTORY_MESSAGE_I on MFR_MOVE_HISTORY_T (MESSAGE_ID);

-- end MSGCNTR-241