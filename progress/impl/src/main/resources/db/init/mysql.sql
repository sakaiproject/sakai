CREATE TABLE IF NOT EXISTS `progress` (
    `id` INT PRIMARY KEY,
    `site_id` VARCHAR(99) DEFAULT NULL,
    `config` INT DEFAULT NULL,
    `date_created` TIMESTAMP NOT NULL DEFAULT NOW(),
    `date_edited` TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    `modified_by` VARCHAR(99) DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS `progress_item` (
    `id` INT PRIMARY KEY,
    `item_name` VARCHAR(99) DEFAULT NULL,
    `weight` INT DEFAULT NULL,
    `progress_header` INT DEFAULT NULL,
    `config` INT DEFAULT NULL,
    `date_created` TIMESTAMP NOT NULL DEFAULT NOW(),
    `date_edited` TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    `modified_by` VARCHAR(99) DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS `progress_config` (
    `id` INT PRIMARY KEY,
    `config_name` VARCHAR(99) DEFAULT NULL,
    `date_created` TIMESTAMP NOT NULL DEFAULT NOW(),
    `date_edited` TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    `modified_by` VARCHAR(99) DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS `progress_config_attributes` (
    `id` INT PRIMARY KEY,
    `config_id` INT DEFAULT NULL,
    `attribute_name` VARCHAR(99) DEFAULT NULL,
    `attribute_value` VARCHAR(99) DEFAULT NULL,
    `date_created` TIMESTAMP NOT NULL DEFAULT NOW(),
    `date_edited` TIMESTAMP NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    `modified_by` VARCHAR(99) DEFAULT NULL
);
