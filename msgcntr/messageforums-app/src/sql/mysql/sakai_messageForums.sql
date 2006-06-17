DROP TABLE IF EXISTS `sakai`.`mfr_actor_permissions_t`;
CREATE TABLE `mfr_actor_permissions_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VERSION` int(11) NOT NULL default '0',
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sakai`.`mfr_area_t`;
CREATE TABLE `mfr_area_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VERSION` int(11) NOT NULL default '0',
  `UUID` varchar(36) NOT NULL default '',
  `CREATED` datetime NOT NULL default '0000-00-00 00:00:00',
  `CREATED_BY` varchar(36) NOT NULL default '',
  `MODIFIED` datetime NOT NULL default '0000-00-00 00:00:00',
  `MODIFIED_BY` varchar(36) NOT NULL default '',
  `CONTEXT_ID` varchar(255) NOT NULL default '',
  `NAME` varchar(255) NOT NULL default '',
  `HIDDEN` tinyint(1) NOT NULL default '0',
  `TYPE_UUID` varchar(36) NOT NULL default '',
  `ENABLED` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sakai`.`mfr_attachment_t`;
CREATE TABLE `mfr_attachment_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VERSION` int(11) NOT NULL default '0',
  `UUID` varchar(36) NOT NULL default '',
  `CREATED` datetime NOT NULL default '0000-00-00 00:00:00',
  `CREATED_BY` varchar(255) NOT NULL default '',
  `MODIFIED` datetime NOT NULL default '0000-00-00 00:00:00',
  `MODIFIED_BY` varchar(255) NOT NULL default '',
  `ATTACHMENT_ID` varchar(255) NOT NULL default '',
  `ATTACHMENT_URL` varchar(255) NOT NULL default '',
  `ATTACHMENT_NAME` varchar(255) NOT NULL default '',
  `ATTACHMENT_SIZE` varchar(255) NOT NULL default '',
  `ATTACHMENT_TYPE` varchar(255) NOT NULL default '',
  `m_surrogateKey` bigint(20) default NULL,
  `mes_index_col` int(11) default NULL,
  `of_surrogateKey` bigint(20) default NULL,
  `of_index_col` int(11) default NULL,
  `pf_surrogateKey` bigint(20) default NULL,
  `pf_index_col` int(11) default NULL,
  `t_surrogateKey` bigint(20) default NULL,
  `t_index_col` int(11) default NULL,
  `of_urrogateKey` bigint(20) default NULL,
  `f_index_col` int(11) default NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK7B2D5CDE2AFBA652` (`t_surrogateKey`),
  KEY `FK7B2D5CDEC6FDB1CF` (`of_surrogateKey`),
  KEY `FK7B2D5CDE20D91C10` (`pf_surrogateKey`),
  KEY `FK7B2D5CDEFDEB22F9` (`m_surrogateKey`),
  KEY `FK7B2D5CDEAD5AF852` (`of_urrogateKey`),
  CONSTRAINT `FK7B2D5CDE20D91C10` FOREIGN KEY (`pf_surrogateKey`) REFERENCES `mfr_private_forum_t` (`ID`),
  CONSTRAINT `FK7B2D5CDE2AFBA652` FOREIGN KEY (`t_surrogateKey`) REFERENCES `mfr_topic_t` (`ID`),
  CONSTRAINT `FK7B2D5CDEAD5AF852` FOREIGN KEY (`of_urrogateKey`) REFERENCES `mfr_open_forum_t` (`ID`),
  CONSTRAINT `FK7B2D5CDEC6FDB1CF` FOREIGN KEY (`of_surrogateKey`) REFERENCES `mfr_open_forum_t` (`ID`),
  CONSTRAINT `FK7B2D5CDEFDEB22F9` FOREIGN KEY (`m_surrogateKey`) REFERENCES `mfr_message_t` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sakai`.`mfr_control_permissions_t`;
CREATE TABLE `mfr_control_permissions_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VERSION` int(11) NOT NULL default '0',
  `ROLE` varchar(255) NOT NULL default '',
  `NEW_FORUM` tinyint(1) NOT NULL default '0',
  `NEW_TOPIC` tinyint(1) NOT NULL default '0',
  `NEW_RESPONSE` tinyint(1) NOT NULL default '0',
  `RESPONSE_TO_RESPONSE` tinyint(1) NOT NULL default '0',
  `MOVE_POSTINGS` tinyint(1) NOT NULL default '0',
  `CHANGE_SETTINGS` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sakai`.`mfr_date_restrictions_t`;
CREATE TABLE `mfr_date_restrictions_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VERSION` int(11) NOT NULL default '0',
  `VISIBLE` datetime NOT NULL default '0000-00-00 00:00:00',
  `VISIBLE_POST_ON_SCHEDULE` tinyint(1) NOT NULL default '0',
  `POSTING_ALLOWED` datetime NOT NULL default '0000-00-00 00:00:00',
  `POSTING_ALLOWED_POST_ON_SCHEDULE` tinyint(1) NOT NULL default '0',
  `READ_ONLY` datetime NOT NULL default '0000-00-00 00:00:00',
  `READ_ONLY_POST_ON_SCHEDULE` tinyint(1) NOT NULL default '0',
  `HIDDEN` datetime NOT NULL default '0000-00-00 00:00:00',
  `HIDDEN_POST_ON_SCHEDULE` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sakai`.`mfr_label_t`;
CREATE TABLE `mfr_label_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VERSION` int(11) NOT NULL default '0',
  `UUID` varchar(36) NOT NULL default '',
  `CREATED` datetime NOT NULL default '0000-00-00 00:00:00',
  `CREATED_BY` varchar(36) NOT NULL default '',
  `MODIFIED` datetime NOT NULL default '0000-00-00 00:00:00',
  `MODIFIED_BY` varchar(36) NOT NULL default '',
  `KEY_C` varchar(255) NOT NULL default '',
  `VALUE_C` varchar(255) NOT NULL default '',
  `df_surrogateKey` bigint(20) default NULL,
  `df_index_col` int(11) default NULL,
  `dt_surrogateKey` bigint(20) default NULL,
  `dt_index_col` int(11) default NULL,
  PRIMARY KEY  (`ID`),
  KEY `FKC6611543EA902104` (`df_surrogateKey`),
  KEY `FKC661154344B127B6` (`dt_surrogateKey`),
  CONSTRAINT `FKC661154344B127B6` FOREIGN KEY (`dt_surrogateKey`) REFERENCES `mfr_topic_t` (`ID`),
  CONSTRAINT `FKC6611543EA902104` FOREIGN KEY (`df_surrogateKey`) REFERENCES `mfr_open_forum_t` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sakai`.`mfr_message_forums_user_t`;
CREATE TABLE `mfr_message_forums_user_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VERSION` int(11) NOT NULL default '0',
  `UUID` varchar(36) NOT NULL default '',
  `apaSurrogateKey` bigint(20) default NULL,
  `ap1_index_col` int(11) default NULL,
  `apmSurrogateKey` bigint(20) default NULL,
  `ap2_index_col` int(11) default NULL,
  `apcSurrogateKey` bigint(20) default NULL,
  `ap3_index_col` int(11) default NULL,
  `mesSurrogateKey` bigint(20) default NULL,
  `mes_index_col` int(11) default NULL,
  PRIMARY KEY  (`ID`),
  KEY `FKF3B8460F737F309B` (`apcSurrogateKey`),
  KEY `FKF3B8460FC49D71A5` (`apmSurrogateKey`),
  KEY `FKF3B8460F96792399` (`apaSurrogateKey`),
  KEY `FKF3B8460FDD70E9E2` (`mesSurrogateKey`),
  CONSTRAINT `FKF3B8460F737F309B` FOREIGN KEY (`apcSurrogateKey`) REFERENCES `mfr_actor_permissions_t` (`ID`),
  CONSTRAINT `FKF3B8460F96792399` FOREIGN KEY (`apaSurrogateKey`) REFERENCES `mfr_actor_permissions_t` (`ID`),
  CONSTRAINT `FKF3B8460FC49D71A5` FOREIGN KEY (`apmSurrogateKey`) REFERENCES `mfr_actor_permissions_t` (`ID`),
  CONSTRAINT `FKF3B8460FDD70E9E2` FOREIGN KEY (`mesSurrogateKey`) REFERENCES `mfr_message_t` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sakai`.`mfr_message_permissions_t`;
CREATE TABLE `mfr_message_permissions_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VERSION` int(11) NOT NULL default '0',
  `ROLE_C` varchar(255) NOT NULL default '',
  `READ_C` tinyint(1) NOT NULL default '0',
  `REVISE_ANY` tinyint(1) NOT NULL default '0',
  `REVISE_OWN` tinyint(1) NOT NULL default '0',
  `DELETE_ANY` tinyint(1) NOT NULL default '0',
  `DELETE_OWN` tinyint(1) NOT NULL default '0',
  `READ_DRAFTS` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sakai`.`mfr_message_t`;
CREATE TABLE `mfr_message_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `MESSAGE_DTYPE` varchar(2) NOT NULL default '',
  `VERSION` int(11) NOT NULL default '0',
  `UUID` varchar(36) NOT NULL default '',
  `CREATED` datetime NOT NULL default '0000-00-00 00:00:00',
  `CREATED_BY` varchar(36) NOT NULL default '',
  `MODIFIED` datetime NOT NULL default '0000-00-00 00:00:00',
  `MODIFIED_BY` varchar(36) NOT NULL default '',
  `TITLE` varchar(255) NOT NULL default '',
  `BODY` text NOT NULL,
  `AUTHOR` varchar(255) NOT NULL default '',
  `LABEL` varchar(255) default NULL,
  `IN_REPLY_TO` bigint(20) default NULL,
  `GRADEBOOK` varchar(255) default NULL,
  `GRADEBOOK_ASSIGNMENT` varchar(255) default NULL,
  `TYPE_UUID` varchar(36) NOT NULL default '',
  `APPROVED` tinyint(1) NOT NULL default '0',
  `DRAFT` tinyint(1) NOT NULL default '0',
  `surrogateKey` bigint(20) default NULL,
  `t_index_col` int(11) default NULL,
  `EXTERNAL_EMAIL` tinyint(1) default NULL,
  `EXTERNAL_EMAIL_ADDRESS` varchar(255) default NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK80C1A316FE0789EA` (`IN_REPLY_TO`),
  KEY `FK80C1A3164FDCE067` (`surrogateKey`),
  CONSTRAINT `FK80C1A3164FDCE067` FOREIGN KEY (`surrogateKey`) REFERENCES `mfr_topic_t` (`ID`),
  CONSTRAINT `FK80C1A316FE0789EA` FOREIGN KEY (`IN_REPLY_TO`) REFERENCES `mfr_message_t` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sakai`.`mfr_open_forum_t`;
CREATE TABLE `mfr_open_forum_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `FORUM_DTYPE` varchar(2) NOT NULL default '',
  `VERSION` int(11) NOT NULL default '0',
  `UUID` varchar(36) NOT NULL default '',
  `CREATED` datetime NOT NULL default '0000-00-00 00:00:00',
  `CREATED_BY` varchar(36) NOT NULL default '',
  `MODIFIED` datetime NOT NULL default '0000-00-00 00:00:00',
  `MODIFIED_BY` varchar(36) NOT NULL default '',
  `TITLE` varchar(255) NOT NULL default '',
  `SHORT_DESCRIPTION` varchar(255) NOT NULL default '',
  `EXTENDED_DESCRIPTION` text NOT NULL,
  `TYPE_UUID` varchar(36) NOT NULL default '',
  `SORT_INDEX` int(11) NOT NULL default '0',
  `CONTROL_PERMISSIONS` bigint(20) default NULL,
  `MESSAGE_PERMISSIONS` bigint(20) default NULL,
  `LOCKED` tinyint(1) NOT NULL default '0',
  `surrogateKey` bigint(20) default NULL,
  `area_index_col` int(11) default NULL,
  `DATE_RESTRICTIONS` bigint(20) default NULL,
  `ACTOR_PERMISSIONS` bigint(20) default NULL,
  `MODERATED` tinyint(1) default NULL,
  `area_index_col_2` int(11) default NULL,
  PRIMARY KEY  (`ID`),
  KEY `FKC1760847FDACE462` (`CONTROL_PERMISSIONS`),
  KEY `FKC17608474FDCE067` (`surrogateKey`),
  KEY `FKC17608477E4BD00C` (`MESSAGE_PERMISSIONS`),
  KEY `FKC1760847D83B3B18` (`DATE_RESTRICTIONS`),
  KEY `FKC1760847B88980FA` (`ACTOR_PERMISSIONS`),
  CONSTRAINT `FKC17608474FDCE067` FOREIGN KEY (`surrogateKey`) REFERENCES `mfr_area_t` (`ID`),
  CONSTRAINT `FKC17608477E4BD00C` FOREIGN KEY (`MESSAGE_PERMISSIONS`) REFERENCES `mfr_message_permissions_t` (`ID`),
  CONSTRAINT `FKC1760847B88980FA` FOREIGN KEY (`ACTOR_PERMISSIONS`) REFERENCES `mfr_actor_permissions_t` (`ID`),
  CONSTRAINT `FKC1760847D83B3B18` FOREIGN KEY (`DATE_RESTRICTIONS`) REFERENCES `mfr_date_restrictions_t` (`ID`),
  CONSTRAINT `FKC1760847FDACE462` FOREIGN KEY (`CONTROL_PERMISSIONS`) REFERENCES `mfr_control_permissions_t` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sakai`.`mfr_private_forum_t`;
CREATE TABLE `mfr_private_forum_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VERSION` int(11) NOT NULL default '0',
  `UUID` varchar(36) NOT NULL default '',
  `CREATED` datetime NOT NULL default '0000-00-00 00:00:00',
  `CREATED_BY` varchar(36) NOT NULL default '',
  `MODIFIED` datetime NOT NULL default '0000-00-00 00:00:00',
  `MODIFIED_BY` varchar(36) NOT NULL default '',
  `TITLE` varchar(255) NOT NULL default '',
  `SHORT_DESCRIPTION` varchar(255) NOT NULL default '',
  `EXTENDED_DESCRIPTION` text NOT NULL,
  `TYPE_UUID` varchar(36) NOT NULL default '',
  `SORT_INDEX` int(11) NOT NULL default '0',
  `AUTO_FORWARD` tinyint(1) NOT NULL default '0',
  `AUTO_FORWARD_EMAIL` varchar(255) NOT NULL default '',
  `PREVIEW_PANE_ENABLED` tinyint(1) NOT NULL default '0',
  `surrogateKey` bigint(20) default NULL,
  `area_index_col` int(11) default NULL,
  PRIMARY KEY  (`ID`),
  KEY `FKA9EE57544FDCE067` (`surrogateKey`),
  CONSTRAINT `FKA9EE57544FDCE067` FOREIGN KEY (`surrogateKey`) REFERENCES `mfr_area_t` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sakai`.`mfr_topic_t`;
CREATE TABLE `mfr_topic_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `TOPIC_DTYPE` varchar(2) NOT NULL default '',
  `VERSION` int(11) NOT NULL default '0',
  `UUID` varchar(36) NOT NULL default '',
  `CREATED` datetime NOT NULL default '0000-00-00 00:00:00',
  `CREATED_BY` varchar(36) NOT NULL default '',
  `MODIFIED` datetime NOT NULL default '0000-00-00 00:00:00',
  `MODIFIED_BY` varchar(36) NOT NULL default '',
  `TITLE` varchar(255) NOT NULL default '',
  `SHORT_DESCRIPTION` varchar(255) NOT NULL default '',
  `EXTENDED_DESCRIPTION` text NOT NULL,
  `MUTABLE` tinyint(1) NOT NULL default '0',
  `SORT_INDEX` int(11) NOT NULL default '0',
  `TYPE_UUID` varchar(36) NOT NULL default '',
  `of_surrogateKey` bigint(20) default NULL,
  `of_index_col` int(11) default NULL,
  `pf_surrogateKey` bigint(20) default NULL,
  `pf_index_col` int(11) default NULL,
  `CONTROL_PERMISSIONS` bigint(20) default NULL,
  `MESSAGE_PERMISSIONS` bigint(20) default NULL,
  `LOCKED` tinyint(1) default NULL,
  `CONFIDENTIAL_RESPONSES` tinyint(1) default NULL,
  `MUST_RESPOND_BEFORE_READING` tinyint(1) default NULL,
  `HOUR_BEFORE_RESPONSES_VISIBLE` int(11) default NULL,
  `DATE_RESTRICTIONS` bigint(20) default NULL,
  `ACTOR_PERMISSIONS` bigint(20) default NULL,
  `MODERATED` tinyint(1) default NULL,
  `GRADEBOOK` varchar(255) default NULL,
  `GRADEBOOK_ASSIGNMENT` varchar(255) default NULL,
  `bf_index_col` int(11) default NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK863DC0BEB88980FA` (`ACTOR_PERMISSIONS`),
  KEY `FK863DC0BEFDACE462` (`CONTROL_PERMISSIONS`),
  KEY `FK863DC0BEC6FDB1CF` (`of_surrogateKey`),
  KEY `FK863DC0BE7E4BD00C` (`MESSAGE_PERMISSIONS`),
  KEY `FK863DC0BE20D91C10` (`pf_surrogateKey`),
  KEY `FK863DC0BED83B3B18` (`DATE_RESTRICTIONS`),
  CONSTRAINT `FK863DC0BE20D91C10` FOREIGN KEY (`pf_surrogateKey`) REFERENCES `mfr_private_forum_t` (`ID`),
  CONSTRAINT `FK863DC0BE7E4BD00C` FOREIGN KEY (`MESSAGE_PERMISSIONS`) REFERENCES `mfr_message_permissions_t` (`ID`),
  CONSTRAINT `FK863DC0BEB88980FA` FOREIGN KEY (`ACTOR_PERMISSIONS`) REFERENCES `mfr_actor_permissions_t` (`ID`),
  CONSTRAINT `FK863DC0BEC6FDB1CF` FOREIGN KEY (`of_surrogateKey`) REFERENCES `mfr_open_forum_t` (`ID`),
  CONSTRAINT `FK863DC0BED83B3B18` FOREIGN KEY (`DATE_RESTRICTIONS`) REFERENCES `mfr_date_restrictions_t` (`ID`),
  CONSTRAINT `FK863DC0BEFDACE462` FOREIGN KEY (`CONTROL_PERMISSIONS`) REFERENCES `mfr_control_permissions_t` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `sakai`.`mfr_unread_status_t`;
CREATE TABLE `mfr_unread_status_t` (
  `ID` bigint(20) NOT NULL auto_increment,
  `VERSION` int(11) NOT NULL default '0',
  `TOPIC` bigint(20) default NULL,
  `MESSAGE` bigint(20) default NULL,
  `USER_C` varchar(255) NOT NULL default '',
  `READ_C` tinyint(1) NOT NULL default '0',
  `TOPIC_C` varchar(255) default NULL,
  `MESSAGE_C` varchar(255) default NULL,
  PRIMARY KEY  (`ID`),
  KEY `FK7C2D47714C4D50F` (`TOPIC`),
  KEY `FK7C2D477163B68BE7` (`MESSAGE`),
  CONSTRAINT `FK7C2D47714C4D50F` FOREIGN KEY (`TOPIC`) REFERENCES `mfr_topic_t` (`ID`),
  CONSTRAINT `FK7C2D477163B68BE7` FOREIGN KEY (`MESSAGE`) REFERENCES `mfr_message_t` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;