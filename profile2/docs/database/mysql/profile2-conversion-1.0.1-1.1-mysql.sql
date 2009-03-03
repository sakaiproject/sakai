--change Preferences to split out email into different fields that can be controlled individually.

--add the new columns, default to false as we update them further down.
alter table PROFILE_PREFERENCES_T 
	add EMAIL_REQUEST bit not null DEFAULT false,
    add EMAIL_CONFIRM bit not null DEFAULT false
;

--update the new columns based on the old data
--if all emails, then both true
update PROFILE_PREFERENCES_T set EMAIL_REQUEST=true, EMAIL_CONFIRM=true where EMAIL=0;
--if just requests, set requests to true, confirms to false
update PROFILE_PREFERENCES_T set EMAIL_REQUEST=true, EMAIL_CONFIRM=false where EMAIL=1;
--if just confirms, set confirms to true, requests to false
update PROFILE_PREFERENCES_T set EMAIL_REQUEST=false, EMAIL_CONFIRM=true where EMAIL=2;
--if all off, set both false
update PROFILE_PREFERENCES_T set EMAIL_REQUEST=false, EMAIL_CONFIRM=false where EMAIL=3;

--now drop the old column
alter table PROFILE_PREFERENCES_T 
	drop COLUMN EMAIL
;



--change name of profile field to be profile_image since it only controls that now (PRFL-24)
alter table PROFILE_PRIVACY_T change PROFILE PROFILE_IMAGE int not null;