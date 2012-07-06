-- this script should be used to upgrade your database from any prior version of signups to the 1.0 release.

ALTER TABLE `signup_meetings` ADD `category` VARCHAR(255)  NOT NULL  DEFAULT '';

ALTER TABLE `signup_meetings` ADD create_groups bit(1) default '\0';

ALTER TABLE `signup_ts` ADD `group_id` VARCHAR(255)  NULL  DEFAULT NULL;

-- add the UUID columns
ALTER TABLE `signup_meetings` ADD `vevent_uuid` VARCHAR(255)  NULL  DEFAULT NULL;
ALTER TABLE `signup_ts` ADD `vevent_uuid` VARCHAR(255)  NULL  DEFAULT NULL;
