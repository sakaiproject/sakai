--change Preferences to split out email into different fields that can be controlled individually.
--will default to true for all users.

alter table PROFILE_PREFERENCES_T drop column EMAIL;
alter table PROFILE_PREFERENCES_T add (EMAIL_REQUEST number(1,0) default 1, EMAIL_CONFIRM number(1,0) default 1);
