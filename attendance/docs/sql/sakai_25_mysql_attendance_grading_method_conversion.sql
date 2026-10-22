-- MySQL conversion script for Sakai 25 attendance tool
-- Converts from boolean-based grading flags to integer-based grading method
-- This script handles the migration from AUTO_GRADING and GRADE_BY_SUBTRACTION columns
-- to the new GRADING_METHOD column

-- Add last modified audit columns to core tables
ALTER TABLE `attendance_event_t` ADD COLUMN `LAST_MODIFIED_BY` varchar(99) NOT NULL DEFAULT '';
ALTER TABLE `attendance_event_t` ADD COLUMN `LAST_MODIFIED_DATE` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE `attendance_record_t` ADD COLUMN `LAST_MODIFIED_BY` varchar(99) NOT NULL DEFAULT '';
ALTER TABLE `attendance_record_t` ADD COLUMN `LAST_MODIFIED_DATE` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE `attendance_grade_t` ADD COLUMN `LAST_MODIFIED_BY` varchar(99) NOT NULL DEFAULT '';
ALTER TABLE `attendance_grade_t` ADD COLUMN `LAST_MODIFIED_DATE` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add the new GRADING_METHOD column
ALTER TABLE `attendance_site_t` ADD COLUMN `GRADING_METHOD` int(11) DEFAULT 0;

-- Migrate existing data:
-- GRADING_METHOD_NONE = 0 (when auto grading was disabled)
-- GRADING_METHOD_SUBTRACT = 1 (when auto grading was enabled with subtraction)
-- GRADING_METHOD_ADD = 2 (when auto grading was enabled with addition)

-- Set GRADING_METHOD based on existing boolean values
UPDATE `attendance_site_t` SET `GRADING_METHOD` = 0 WHERE `AUTO_GRADING` IS NULL OR `AUTO_GRADING` = 0;
UPDATE `attendance_site_t` SET `GRADING_METHOD` = 1 WHERE `AUTO_GRADING` = 1 AND (`GRADE_BY_SUBTRACTION` IS NULL OR `GRADE_BY_SUBTRACTION` = 1);
UPDATE `attendance_site_t` SET `GRADING_METHOD` = 2 WHERE `AUTO_GRADING` = 1 AND `GRADE_BY_SUBTRACTION` = 0;

-- Drop the old boolean columns
ALTER TABLE `attendance_site_t` DROP COLUMN `AUTO_GRADING`;
ALTER TABLE `attendance_site_t` DROP COLUMN `GRADE_BY_SUBTRACTION`;

-- Add missing columns that may be needed for grading rules
ALTER TABLE `attendance_rule_t` ADD COLUMN `LAST_MODIFIED_BY` varchar(99) NOT NULL DEFAULT '';
ALTER TABLE `attendance_rule_t` ADD COLUMN `LAST_MODIFIED_DATE` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add unique constraint for grading rules to prevent duplicates
ALTER TABLE `attendance_rule_t` ADD CONSTRAINT `UK_ATTENDANCE_RULE` UNIQUE (`A_SITE_ID`, `STATUS`, `START_RANGE`, `END_RANGE`);