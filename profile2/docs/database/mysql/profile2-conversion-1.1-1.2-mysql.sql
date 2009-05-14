
/* add the new academic column, default to 0, (PRFL-38) */
alter table PROFILE_PRIVACY_T add ACADEMIC_INFO int not null DEFAULT 0;
