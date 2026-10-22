-- These are SQL statements to manually upgrade the DB
-- in instances where auto.ddl is not available.
-- A beset effort is made to create this, that is it is
-- UNTESTED.

--
-- Add new column to ATTENDANCE_GRADE_T
--
ALTER TABLE `attendance_record_t` MODIFY `RECORD_COMMENT` longtext;

--
-- Table structure for table `attendance_rule_t`
--
CREATE TABLE `attendance_rule_t` (
  `GRADING_RULE_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `A_SITE_ID` bigint(20) NOT NULL,
  `STATUS` varchar(255) NOT NULL,
  `START_RANGE` int(11) NOT NULL,
  `END_RANGE` int(11) DEFAULT NULL,
  `POINTS` double NOT NULL,
  PRIMARY KEY (`GRADING_RULE_ID`),
  KEY `FKCF1B11E7E9210FFF` (`A_SITE_ID`),
  CONSTRAINT `FKCF1B11E7E9210FFF` FOREIGN KEY (`A_SITE_ID`) REFERENCES `attendance_site_t` (`A_SITE_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

--
-- Add new columnt to attendance_grade_t
--
ALTER TABLE `attendance_grade_t` ADD COLUMN `OVERRIDE` BIT(1) DEFAULT NULL;

--
-- Table structure for table `attendance_item_stats_t`
--
CREATE TABLE `attendance_item_stats_t` (
  `A_ITEM_STATS_ID` bigint(20) NOT NULL,
  `PRESENT` int(11) DEFAULT NULL,
  `UNEXCUSED` int(11) DEFAULT NULL,
  `EXCUSED` int(11) DEFAULT NULL,
  `LATE` int(11) DEFAULT NULL,
  `LEFT_EARLY` int(11) DEFAULT NULL,
  PRIMARY KEY (`A_ITEM_STATS_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `attendance_user_stats_t`
--
CREATE TABLE `attendance_user_stats_t` (
  `A_USER_STATS_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `USER_ID` varchar(255) DEFAULT NULL,
  `A_SITE_ID` bigint(20) NOT NULL,
  `PRESENT` int(11) DEFAULT NULL,
  `UNEXCUSED` int(11) DEFAULT NULL,
  `EXCUSED` int(11) DEFAULT NULL,
  `LATE` int(11) DEFAULT NULL,
  `LEFT_EARLY` int(11) DEFAULT NULL,
  PRIMARY KEY (`A_USER_STATS_ID`),
  KEY `FKF97921B6E9210FFF` (`A_SITE_ID`),
  CONSTRAINT `FKF97921B6E9210FFF` FOREIGN KEY (`A_SITE_ID`) REFERENCES `attendance_site_t` (`A_SITE_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

--
-- Add New column's to ATTENDANCE_SITE_T
--
ALTER TABLE `attendance_site_t`
  ADD COLUMN `SYNC` bit(1) DEFAULT NULL,
  ADD COLUMN `SYNC_TIME` datetime DEFAULT NULL,
  ADD COLUMN `AUTO_GRADING` bit(1) DEFAULT NULL,
  ADD COLUMN `GRADE_BY_SUBTRACTION` bit(1) DEFAULT NULL;
