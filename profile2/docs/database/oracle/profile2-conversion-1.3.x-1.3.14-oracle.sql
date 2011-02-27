/* change row size of image URI columns (PRFL-392) */
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN varchar2(4000);
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB varchar2(4000);

alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN varchar2(4000);
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB varchar2(4000);
