
/* add avatar image url to uploaded and external image records (PRFL-612) */
alter table PROFILE_IMAGES_T add RESOURCE_AVATAR varchar2(4000);
alter table PROFILE_IMAGES_EXTERNAL_T add URL_AVATAR varchar2(4000);
