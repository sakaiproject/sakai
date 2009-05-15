/* add the new academic column, default to 0, (PRFL-38) */
alter table PROFILE_PRIVACY_T add ACADEMIC_INFO number(1,0) default 0;

/*increase size of UUID columns (PRFL-44) */
alter table PROFILE_FRIENDS_T modify USER_UUID varchar2(99);
alter table PROFILE_FRIENDS_T modify FRIEND_UUID varchar2(99);
alter table PROFILE_IMAGES_EXTERNAL_T modify USER_UUID varchar2(99);
alter table PROFILE_IMAGES_T modify USER_UUID varchar2(99);
alter table PROFILE_PREFERENCES_T modify USER_UUID varchar2(99);
alter table PROFILE_PRIVACY_T modify USER_UUID varchar2(99);
alter table PROFILE_STATUS_T modify USER_UUID varchar2(99);

/* resize column (PRFL-44) but also change its type (PRFL-45) */
alter table SAKAI_PERSON_META_T modify USER_UUID varchar2(99); 