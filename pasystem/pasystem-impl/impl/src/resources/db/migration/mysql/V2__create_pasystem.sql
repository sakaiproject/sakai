-- Popup screens

CREATE TABLE `pasystem_popup_screens` (
  `uuid` VARCHAR(255) PRIMARY KEY,
  `descriptor` VARCHAR(255),
  `start_time` BIGINT,
  `end_time` BIGINT,
  `open_campaign` int(1) DEFAULT NULL,
  INDEX `start_time` (`start_time`),
  INDEX `descriptor` (`descriptor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `pasystem_popup_content` (
  `uuid` varchar(255) PRIMARY KEY,
  `template_content` MEDIUMTEXT,
  FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `pasystem_popup_assign` (
  `uuid` varchar(255),
  `user_eid` varchar(255) DEFAULT NULL,
   FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid),
   INDEX `user_eid` (`user_eid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `pasystem_popup_dismissed` (
  `uuid` varchar(255),
  `user_eid` varchar(255) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `dismiss_time` BIGINT,
   UNIQUE KEY `unique_popup_dismissed` (`user_eid`,`state`, `uuid`),
   FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid),
   INDEX `user_eid` (`user_eid`),
   INDEX `state` (`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- Banners
CREATE TABLE pasystem_banner_alert
( `uuid` VARCHAR(255) PRIMARY KEY,
  `message` VARCHAR(4000) NOT NULL,
  `hosts` VARCHAR(512) DEFAULT NULL,
  `active` INT(1) NOT NULL DEFAULT 0,
  `start_time` BIGINT DEFAULT NULL,
  `end_time` BIGINT DEFAULT NULL,
  `banner_type` VARCHAR(255) DEFAULT 'warning'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT IGNORE INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('pasystem.manage');

CREATE TABLE `pasystem_banner_dismissed` (
  `uuid` varchar(255),
  `user_eid` varchar(255) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `dismiss_time` BIGINT,
   UNIQUE KEY `unique_banner_dismissed` (`user_eid`,`state`, `uuid`),
   FOREIGN KEY (uuid) REFERENCES pasystem_banner_alert(uuid),
   INDEX `user_eid` (`user_eid`),
   INDEX `state` (`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

