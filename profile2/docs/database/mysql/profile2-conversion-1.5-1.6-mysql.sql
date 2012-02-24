
/* add avatar image url to uploaded and external image records (PRFL-612) */
ALTER TABLE PROFILE_IMAGES_T ADD RESOURCE_AVATAR text;
ALTER TABLE PROFILE_IMAGES_EXTERNAL_T ADD URL_AVATAR text;
