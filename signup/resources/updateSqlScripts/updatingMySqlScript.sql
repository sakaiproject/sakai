DROP TABLE IF EXISTS `signup_attachments`;
CREATE TABLE  `signup_attachments` (
  `meeting_id` bigint(20) NOT NULL,
  `resource_Id` varchar(255) default NULL,
  `file_name` varchar(255) default NULL,
  `mime_type` varchar(80) default NULL,
  `fileSize` bigint(20) default NULL,
  `location` text,
  `isLink` bit(1) default NULL,
  `timeslot_id` bigint(20) default NULL,
  `view_by_all` bit(1) default 1,
  `created_by` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `last_modified_by` varchar(255) NOT NULL,
  `last_modified_date` datetime NOT NULL,
  `list_index` int(11) NOT NULL,
  PRIMARY KEY  (`meeting_id`,`list_index`),
  KEY `FK3BCB709CB1E8A17` (`meeting_id`),
  CONSTRAINT `FK3BCB709CB1E8A17` FOREIGN KEY (`meeting_id`) REFERENCES `signup_meetings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `signup_meetings`  ADD `allow_waitList` bit(1) default 1;
ALTER TABLE `signup_meetings`  ADD `allow_comment` bit(1) default 1;
ALTER TABLE `signup_meetings`  ADD `eid_input_mode` bit(1) default '\0';
ALTER TABLE `signup_meetings`  ADD `auto_reminder` bit(1) default '\0';
ALTER TABLE `signup_meetings`  ADD `repeat_type` varchar(20) default NULL;
