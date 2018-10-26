CREATE TABLE IF NOT EXISTS `tagservice_collection` (
  `tagcollectionid` CHAR(36) PRIMARY KEY,
  `description` TEXT,
  `externalsourcename` VARCHAR(255) UNIQUE,
  `externalsourcedescription` TEXT,
  `name` VARCHAR(255) UNIQUE,
  `createdby` VARCHAR(255),
  `creationdate` BIGINT,
  `lastmodifiedby` VARCHAR(255),
  `lastmodificationdate` BIGINT,
  `lastsynchronizationdate` BIGINT,
  `externalupdate` BOOLEAN,
  `externalcreation` BOOLEAN,
  `lastupdatedateinexternalsystem` BIGINT
);

CREATE TABLE IF NOT EXISTS `tagservice_tag` (
  `tagid` CHAR(36) PRIMARY KEY,
  `tagcollectionid` CHAR(36) NOT NULL,
  `externalid` VARCHAR(255),
  `taglabel` VARCHAR(255),
  `description` TEXT,
  `alternativelabels` TEXT,
  `createdby` VARCHAR(255),
  `creationdate` BIGINT,
  `externalcreation` BOOLEAN,
  `externalcreationDate` BIGINT,
  `externalupdate` BOOLEAN,
  `lastmodifiedby` VARCHAR(255),
  `lastmodificationdate` BIGINT,
  `lastupdatedateinexternalsystem` BIGINT,
  `parentid` VARCHAR(255),
  `externalhierarchycode` TEXT,
  `externaltype` VARCHAR(255),
  `data` TEXT,
  INDEX tagservice_tag_taglabel (taglabel),
  INDEX tagservice_tag_tagcollectionid (tagcollectionid),
  INDEX tagservice_tag_externalid (externalid),
  FOREIGN KEY (tagcollectionid)
  REFERENCES tagservice_collection(tagcollectionid)
    ON DELETE RESTRICT
);


INSERT IGNORE INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('tagservice.manage');
