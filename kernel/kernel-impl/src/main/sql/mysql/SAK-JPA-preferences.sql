-- Migration script for JPA-based preferences in Sakai
-- Modify SAKAI_PREFERENCES table to support JPA with Hibernate

-- First check if VERSION column already exists
SET @columnExist := 0;
SELECT COUNT(*) INTO @columnExist 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'SAKAI_PREFERENCES' 
AND COLUMN_NAME = 'VERSION';

-- Only add the VERSION column if it doesn't exist
SET @query = IF(@columnExist = 0, 
                'ALTER TABLE SAKAI_PREFERENCES ADD COLUMN VERSION BIGINT DEFAULT 0',
                'SELECT \'VERSION column already exists\' as Message');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update existing rows to set version to 0 where NULL
UPDATE SAKAI_PREFERENCES SET VERSION = 0 WHERE VERSION IS NULL;

-- Make VERSION column NOT NULL 
SET @query = IF(@columnExist = 0,
                'ALTER TABLE SAKAI_PREFERENCES MODIFY VERSION BIGINT NOT NULL',
                'SELECT \'VERSION column already set\' as Message');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if index exists on PREFERENCES_ID
SET @indexExists := 0;
SELECT COUNT(*) INTO @indexExists 
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'SAKAI_PREFERENCES' 
AND INDEX_NAME = 'idx_preferences_id';

-- Add index if it doesn't exist
SET @query = IF(@indexExists = 0,
                'ALTER TABLE SAKAI_PREFERENCES ADD INDEX idx_preferences_id (PREFERENCES_ID)',
                'SELECT \'Index already exists\' as Message');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;