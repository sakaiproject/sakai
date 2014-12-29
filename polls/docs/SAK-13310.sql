
#mysql
alter table POLL_POLL change POLL_DETAILS POLL_DETAILS text;

#oracle
alter table POLL_POLL modify POLL_DETAILS VARCHAR2(4000); 