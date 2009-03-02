--change Preferences to split out email into different fields that can be controlled individually.
--will default to true for all users.

alter table PROFILE_PREFERENCES_T 
	drop COLUMN EMAIL,
	add EMAIL_REQUEST bit not null DEFAULT true,
    add EMAIL_CONFIRM bit not null DEFAULT true
;