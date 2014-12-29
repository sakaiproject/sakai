
#mysql
alter table POLL_POLL add column POLL_UUID varchar(255);
alter table POLL_OPTION add column OPTION_UUID varchar(255);

#oracle
alter table POLL_POLL add POLL_UUID varchar2(255);
alter table POLL_OPTION add OPTION_UUID varchar2(255);
