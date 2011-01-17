/* add the gravatar column, default to 0, (PRFL-498) */
alter table PROFILE_PREFERENCES_T add USE_GRAVATAR bit not null DEFAULT false;

/* add the wall email notification column, default to 0, (PRFL-528) */
alter table PROFILE_PREFERENCES_T add EMAIL_WALL_ITEM_NEW bit not null DEFAULT true;

/* add the wall privacy setting, default to 0 (PRFL-513) */
alter table PROFILE_PRIVACY_T add MY_WALL int not null DEFAULT 0;

/* add profile walls table (PRFL-518) */
create table PROFILE_WALLS_T (
	USER_UUID varchar(99) not null,
	primary key (USER_UUID)
);

/* add profile wall to wall items map table (PRFL-518) */
create table PROFILE_WALL_ITEMS_MAP_T (
	USER_UUID varchar(99) not null,
	WALL_ITEM_ID bigint not null
);

/* add profile wall items table (PRFL-518) */
create table PROFILE_WALL_ITEMS_T (
	WALL_ITEM_ID bigint not null auto_increment,
	CREATOR_UUID varchar(99) not null,
	TYPE integer not null,
	TEXT text not null,
	DATE datetime not null,
	primary key (WALL_ITEM_ID)
);

/* map foreign key (PRFL-518) */
alter table PROFILE_WALL_ITEMS_MAP_T 
	add index FK501A69B37BEE209 (WALL_ITEM_ID), 
	add constraint FK501A69B37BEE209 
	foreign key (WALL_ITEM_ID) 
	references PROFILE_WALL_ITEMS_T (WALL_ITEM_ID);

/* map foreign key (PRFL-518) */
alter table PROFILE_WALL_ITEMS_MAP_T 
	add index FK501A69B3D352B433 (USER_UUID), 
	add constraint FK501A69B3D352B433 
	foreign key (USER_UUID) 
	references PROFILE_WALLS_T (USER_UUID);

/* create profile wall index (PRFL-518) */	
create index PROFILE_WALLS_I on PROFILE_WALLS_T (USER_UUID);