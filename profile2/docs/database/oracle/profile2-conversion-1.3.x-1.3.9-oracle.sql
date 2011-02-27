/* remove twitter from preferences (PRFL-94) */
alter table PROFILE_PREFERENCES_T drop column TWITTER_ENABLED;
alter table PROFILE_PREFERENCES_T drop column TWITTER_USERNAME;
alter table PROFILE_PREFERENCES_T drop column TWITTER_PASSWORD;

/* add external integration table (PRFL-94) */
create table PROFILE_EXTERNAL_INTEGRATION_T (
	USER_UUID varchar2(99) not null,
	TWITTER_TOKEN varchar2(255),
	TWITTER_SECRET varchar2(255),
	primary key (USER_UUID)
);