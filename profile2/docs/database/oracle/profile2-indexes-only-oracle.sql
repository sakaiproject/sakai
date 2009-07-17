/* 
Profile2 indexes only (Oracle)
-----------------------------

If you didn't run any of the conversion scripts, run this SQL to add the required indexes to the database tables. 
If you are upgrading Profile2, use the proper conversion scripts instead

*/

/* Use this for: v1.1 and earlier to v1.2: */
create index PROFILE_FRIENDS_USER_UUID_I on PROFILE_FRIENDS_T (USER_UUID);
create index PROFILE_FRIENDS_FRIEND_UUID_I on PROFILE_FRIENDS_T (FRIEND_UUID);
create index PROFILE_IMAGES_USER_UUID_I on PROFILE_IMAGES_T (USER_UUID);
create index PROFILE_IMAGES_IS_CURRENT_I on PROFILE_IMAGES_T (IS_CURRENT);
create index SAKAI_PERSON_META_USER_UUID_I on SAKAI_PERSON_META_T (USER_UUID);
create index SAKAI_PERSON_META_PROPERTY_I on SAKAI_PERSON_META_T (PROPERTY);