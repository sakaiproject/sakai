
CREATE TABLE `lti_tools` (
	`id` mediumint(10) NOT NULL AUTO_INCREMENT,
	`course_id` mediumint(10) NULL DEFAULT '0',
	 SITE_ID VARCHAR (99) NOT NULL,
	`title` varchar(255) NOT NULL,
	`description` varchar(1024),
	`status` mediumint(4) NOT NULL DEFAULT '0',
	`timecreated` TIMESTAMP,
	`timemodified` TIMESTAMP,
	`toolurl` varchar(255) NOT NULL,
	`resourcekey` varchar(255) NOT NULL,
	`password` varchar(255) NOT NULL,
	`preferheight` mediumint(4) NOT NULL DEFAULT '0',
	`allowpreferheight` mediumint(1) NOT NULL DEFAULT '0',
	`sendname` mediumint(1) NOT NULL DEFAULT '0',
	`sendemailaddr` mediumint(1) NOT NULL DEFAULT '0',
	`acceptgrades` mediumint(1) NOT NULL DEFAULT '0',
	`allowroster` mediumint(1) NOT NULL DEFAULT '0',
	`allowsetting` mediumint(1) NOT NULL DEFAULT '0',
	`allowcustomparameters` mediumint(1) NOT NULL DEFAULT '0',
	`customparameters` text,
	`organizationid` varchar(255),
	`organizationurl` varchar(255),
	`organizationdescr` varchar(255),
	`launchinpopup` mediumint(1) NOT NULL DEFAULT '0',
	`debuglaunch` mediumint(1) NOT NULL DEFAULT '0',
	PRIMARY KEY ( `id`, `toolid` )
);

CREATE TABLE `lti_content` (
	`id` mediumint(10) NOT NULL AUTO_INCREMENT,
	`course_id` mediumint(10) NOT NULL DEFAULT '0',
	`tool_id` mediumint(10) NOT NULL DEFAULT '0',
	`toolurl` varchar(255) NULL,
	`preferheight` mediumint(4) NOT NULL DEFAULT '0',
	`acceptgrades` mediumint(1) NOT NULL DEFAULT '0',
	`launchinpopup` mediumint(1) NOT NULL DEFAULT '0',
	`customparameters` text,
	`debuglaunch` mediumint(1) NOT NULL DEFAULT '0',
	`placementsecret` varchar(1023),
	`timeplacementsecret` mediumint(10) NOT NULL DEFAULT '0',
	`oldplacementsecret` varchar(255),
	`setting` text(8192),
	`xmlimport` text(16384),
	PRIMARY KEY ( `id`, `course_id`, `tool_id` )
);

CREATE TABLE `lti_mapping` (
	`id` mediumint(10) NOT NULL AUTO_INCREMENT,
	`matchpattern` varchar(256) NOT NULL,
	`launchurl` varchar(1000) NOT NULL,
	PRIMARY KEY ( `id`, `matchpattern` )
);

