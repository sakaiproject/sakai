/* add the gravatar column, default to 0, (PRFL-498) */
alter table PROFILE_PREFERENCES_T add USE_GRAVATAR number(1,0) default 0;

/* add the wall privacy setting, default to 0 (PRFL-513) */
alter table PROFILE_PRIVACY_T add MY_WALL number(1,0) default 0;

/* add profile walls table (PRFL-518) */
create table PROFILE_WALLS_T (
	USER_UUID varchar2(99) not null,
	primary key (USER_UUID)
);

/* add profile wall to wall items map table (PRFL-518) */
create table PROFILE_WALL_ITEMS_MAP_T (
	USER_UUID varchar2(99) not null,
	WALL_ITEM_ID number(19,0) not null
);

/* add profile wall items table (PRFL-518) */
create table PROFILE_WALL_ITEMS_T (
	WALL_ITEM_ID number(19,0) not null,
	CREATOR_UUID varchar2(99) not null,
	TYPE number(10,0) not null,
	TEXT varchar2(4000) not null,
	DATE date not null,
	primary key (WALL_ITEM_ID)
);

/* map foreign key (PRFL-518) */
alter table PROFILE_WALL_ITEMS_MAP_T 
	add constraint FK501A69B37BEE209 
	foreign key (WALL_ITEM_ID) 
	references PROFILE_WALL_ITEMS_T;

/* map foreign key (PRFL-518) */
alter table PROFILE_WALL_ITEMS_MAP_T 
	add constraint FK501A69B3D352B433 
	foreign key (USER_UUID) 
	references PROFILE_WALLS_T;

/* create profile wall index (PRFL-518) */	
create index PROFILE_WALLS_I on PROFILE_WALLS_T (USER_UUID);