-- add column sendEmailOut to table MFR_AREA_T
alter table MFR_AREA_T add column (SENDEMAILOUT bit);
update MFR_AREA_T set SENDEMAILOUT=0 where SENDEMAILOUT is NULL;
alter table MFR_AREA_T modify column SENDEMAILOUT bit not null;