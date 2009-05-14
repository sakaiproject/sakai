/* add the new academic column, default to 0, (PRFL-38) */
alter table PROFILE_PRIVACY_T add ACADEMIC_INFO number(1,0) default 0;
