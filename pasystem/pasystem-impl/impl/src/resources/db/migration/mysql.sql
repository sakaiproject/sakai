CREATE TABLE IF NOT EXISTS `pasystem_popup_screens` (
  `uuid` CHAR(36) PRIMARY KEY,
  `descriptor` VARCHAR(255),
  `start_time` BIGINT,
  `end_time` BIGINT,
  `open_campaign` int(1) DEFAULT NULL,
  INDEX `start_time` (`start_time`),
  INDEX `descriptor` (`descriptor`)
);

CREATE TABLE IF NOT EXISTS `pasystem_popup_content` (
  `uuid` char(36) PRIMARY KEY,
  `template_content` MEDIUMTEXT,
  FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid)
);

CREATE TABLE IF NOT EXISTS `pasystem_popup_assign` (
  `uuid` char(36),
  `user_id` varchar(99) DEFAULT NULL,
   FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid),
   INDEX `user_id` (`user_id`)
);

CREATE TABLE IF NOT EXISTS `pasystem_popup_dismissed` (
  `uuid` char(36),
  `user_id` varchar(99) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `dismiss_time` BIGINT,
   UNIQUE KEY `unique_popup_dismissed` (`user_id`,`state`, `uuid`),
   FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid),
   INDEX `user_id` (`user_id`),
   INDEX `state` (`state`)
);


CREATE TABLE IF NOT EXISTS pasystem_banner_alert
( `uuid` CHAR(36) PRIMARY KEY,
  `message` VARCHAR(4000) NOT NULL,
  `hosts` VARCHAR(512) DEFAULT NULL,
  `active` INT(1) NOT NULL DEFAULT 0,
  `start_time` BIGINT DEFAULT NULL,
  `end_time` BIGINT DEFAULT NULL,
  `banner_type` VARCHAR(255) DEFAULT 'warning'
);

INSERT IGNORE INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('pasystem.manage');

CREATE TABLE IF NOT EXISTS `pasystem_banner_dismissed` (
  `uuid` char(36),
  `user_id` varchar(99) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `dismiss_time` BIGINT,
   UNIQUE KEY `unique_banner_dismissed` (`user_id`,`state`, `uuid`),
   FOREIGN KEY (uuid) REFERENCES pasystem_banner_alert(uuid),
   INDEX `user_id` (`user_id`),
   INDEX `state` (`state`)
);

