-- SAK-16835 columns for new quartz version
alter table QRTZ_TRIGGERS add PRIORITY number(2);
alter table QRTZ_FIRED_TRIGGERS add PRIORITY number(2); 


