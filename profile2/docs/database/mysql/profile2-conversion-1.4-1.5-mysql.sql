/* add the gravatar column, default to 0, (PRFL-498) */
alter table PROFILE_PREFERENCES_T add USE_GRAVATAR bit not null DEFAULT false;

/* add the wall email notification column, default to 1, (PRFL-528) */
alter table PROFILE_PREFERENCES_T add EMAIL_WALL_ITEM_NEW bit not null DEFAULT true;

/* add the worksite email notification column, default to 1, (PRFL-388) */
alter table PROFILE_PREFERENCES_T add EMAIL_WORKSITE_NEW bit not null DEFAULT true;

/* add the wall privacy setting, default to 0 (PRFL-513) */
alter table PROFILE_PRIVACY_T add MY_WALL int not null DEFAULT 0;

/* add profile wall items table (PRFL-518) */
create table PROFILE_WALL_ITEMS_T (
	WALL_ITEM_ID bigint not null auto_increment,
	USER_UUID varchar(99) not null,
	CREATOR_UUID varchar(99) not null,
	WALL_ITEM_TYPE integer not null,
	WALL_ITEM_TEXT text not null,
	WALL_ITEM_DATE datetime not null,
	primary key (WALL_ITEM_ID)
);
create index PROFILE_WI_USER_UUID_I on PROFILE_WALL_ITEMS_T (USER_UUID);

create table PROFILE_WALL_ITEM_COMMENTS_T (
	WALL_ITEM_COMMENT_ID bigint not null auto_increment,
	WALL_ITEM_ID bigint not null,
	CREATOR_UUID varchar(99) not null,
	WALL_ITEM_COMMENT_TEXT text not null,
	WALL_ITEM_COMMENT_DATE datetime not null,
	primary key (WALL_ITEM_COMMENT_ID)
);

alter table PROFILE_WALL_ITEM_COMMENTS_T 
	add index FK32185F67BEE209 (WALL_ITEM_ID), 
	add constraint FK32185F67BEE209 
	foreign key (WALL_ITEM_ID) 
	references PROFILE_WALL_ITEMS_T (WALL_ITEM_ID);
	
/* add the show online status column, default to 1, (PRFL-350) */
alter table PROFILE_PREFERENCES_T add SHOW_ONLINE_STATUS bit not null DEFAULT true;
alter table PROFILE_PRIVACY_T add ONLINE_STATUS int not null DEFAULT 0;

/* add avatar image url to uploaded and external image records (PRFL-612) */
ALTER TABLE PROFILE_IMAGES_T ADD RESOURCE_AVATAR text not null;
ALTER TABLE PROFILE_IMAGES_EXTERNAL_T ADD URL_AVATAR text;
