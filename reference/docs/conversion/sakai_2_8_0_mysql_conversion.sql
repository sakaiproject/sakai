-- SAK-16835 columns for new quartz version
alter table QRTZ_TRIGGERS add column PRIORITY int;
alter table QRTZ_FIRED_TRIGGERS add column PRIORITY int; 
