/* change row size of image URI columns (PRFL-392) */
alter table PROFILE_IMAGES_T modify RESOURCE_MAIN text;
alter table PROFILE_IMAGES_T modify RESOURCE_THUMB text;

alter table PROFILE_IMAGES_EXTERNAL_T modify URL_MAIN text;
alter table PROFILE_IMAGES_EXTERNAL_T modify URL_THUMB text;
