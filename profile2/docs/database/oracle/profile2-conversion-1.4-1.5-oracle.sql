/* add the gravatar column, default to 0, (PRFL-498) */
alter table PROFILE_PREFERENCES_T add USE_GRAVATAR number(1,0) default 0;

/* add the wall email notification column, default to 1, (PRFL-528) */
alter table PROFILE_PREFERENCES_T add EMAIL_WALL_ITEM_NEW number(1,0) default 1;

/* add the worksite email notification column, default to 1, (PRFL-388) */
alter table PROFILE_PREFERENCES_T add EMAIL_WORKSITE_NEW number(1,0) default 1;

/* add the wall privacy setting, default to 0 (PRFL-513) */
alter table PROFILE_PRIVACY_T add MY_WALL number(1,0) default 0;

/* add profile wall items table (PRFL-518) */
create table PROFILE_WALL_ITEMS_T (
	WALL_ITEM_ID number(19,0) not null,
	USER_UUID varchar2(99) not null,
	CREATOR_UUID varchar2(99) not null,
	WALL_ITEM_TYPE number(10,0) not null,
	WALL_ITEM_TEXT varchar2(4000) not null,
	WALL_ITEM_DATE date not null,
	primary key (WALL_ITEM_ID)
);

create table PROFILE_WALL_ITEM_COMMENTS_T (
	WALL_ITEM_COMMENT_ID number(19,0) not null,
	WALL_ITEM_ID number(19,0) not null,
	CREATOR_UUID varchar2(99) not null,
	WALL_ITEM_COMMENT_TEXT varchar2(4000) not null,
	WALL_ITEM_COMMENT_DATE date not null,
	primary key (WALL_ITEM_COMMENT_ID)
);

alter table PROFILE_WALL_ITEM_COMMENTS_T 
	add constraint FK32185F67BEE209 
	foreign key (WALL_ITEM_ID) 
	references PROFILE_WALL_ITEMS_T;