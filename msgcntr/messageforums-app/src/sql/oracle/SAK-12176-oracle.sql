-- add column sendEmailOut to table MFR_AREA_T
alter table MFR_AREA_T add (SENDEMAILOUT NUMBER(1,0));
update MFR_AREA_T set SENDEMAILOUT=0 where SENDEMAILOUT is NULL;
alter table MFR_AREA_T modify (SENDEMAILOUT NUMBER(1,0) not null);