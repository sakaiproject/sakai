-- this script should be used to upgrade your database from any prior version of signups to the 1.0 release.

ALTER TABLE `signup_meetings` ADD `category` VARCHAR(255)  NOT NULL  DEFAULT '';


