
/* remove twitter from preferences (PRFL-94) */
alter table PROFILE_PREFERENCES_T drop TWITTER_ENABLED;
alter table PROFILE_PREFERENCES_T drop TWITTER_USERNAME;
alter table PROFILE_PREFERENCES_T drop TWITTER_PASSWORD;

/* add external integration table (PRFL-94) */
create table PROFILE_EXTERNAL_INTEGRATION_T (
	USER_UUID varchar(99) not null,
	TWITTER_TOKEN varchar(255),
	TWITTER_SECRET varchar(255),
	primary key (USER_UUID)
);