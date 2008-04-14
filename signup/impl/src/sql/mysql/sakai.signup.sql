CREATE TABLE  `signup_meetings` (
  `id` bigint(20) NOT NULL auto_increment,
  `title` varchar(255) NOT NULL,
  `description` text default NULL,
  `creator_id` varchar(255) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `no_of_time_slots` int(11) default NULL,
  `signup_begins` datetime default NULL,
  `signup_deadline` datetime default NULL,
  `cancel` tinyint(1) default NULL,
  `locked` tinyint(1) default NULL,
  `recurrence_id` bigint(20) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE  `signup_sites` (
  `id` bigint(20) NOT NULL auto_increment,
  `title` varchar(255) default NULL,
  `site_id` varchar(255) NOT NULL default '',
  `calendar_event_id` varchar(255) default NULL,
  `calendar_id` varchar(255) default NULL,
  `meeting_id` bigint(20) NOT NULL,
  `list_index` int(11) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK_MEETING_S` (`meeting_id`),
  CONSTRAINT `FK_MEETING_S` FOREIGN KEY (`meeting_id`) REFERENCES `signup_meetings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  `signup_timeslots` (
  `id` bigint(20) NOT NULL auto_increment,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `no_of_attendees` int(11) default NULL,
  `cancel` tinyint(1) default NULL,
  `locked` tinyint(1) default NULL,
  `meeting_id` bigint(20) NOT NULL ,
  `list_index` int(11) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK_MEETING_TS` (`meeting_id`),
  CONSTRAINT `FK_MEETING_TS` FOREIGN KEY (`meeting_id`) REFERENCES `signup_meetings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE  `signup_attendees` (
  `id` bigint(20) NOT NULL auto_increment,
  `attendee_id` varchar(255) NOT NULL,
  `comment` text default NULL,
  `calendar_event_id` varchar(255) default NULL,
  `calendar_id` varchar(255) default NULL,
  `timeslot_id` bigint(20) NOT NULL,
  `list_index` int(11) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK_TIMESLOT` (`timeslot_id`),
  CONSTRAINT `FK_TIMESLOT` FOREIGN KEY (`timeslot_id`) REFERENCES `signup_timeslots` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  `signup_groups` (
  `id` bigint(20) NOT NULL auto_increment,
  `title` varchar(255) default NULL,
  `group_id` varchar(255) NOT NULL,
  `signup_site_id` bigint(20) NOT NULL,
  `list_index` int(11) default NULL,
  PRIMARY KEY  (`id`),
  KEY `FK_SITE` (`signup_site_id`),
  CONSTRAINT `FK_SITE` FOREIGN KEY (`signup_site_id`) REFERENCES `signup_sites` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



