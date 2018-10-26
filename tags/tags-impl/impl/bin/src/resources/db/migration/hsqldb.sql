CREATE TABLE tagservice_collection (
  tagcollectionid CHAR(36) PRIMARY KEY,
  description LONGVARCHAR,
  externalsourcename VARCHAR(255),
  externalsourcedescription LONGVARCHAR,
  name VARCHAR(255),
  createdby VARCHAR(255),
  creationdate BIGINT,
  lastmodifiedby VARCHAR(255),
  lastmodificationdate BIGINT,
  lastsynchronizationdate BIGINT,
  externalupdate BOOLEAN,
  externalcreation BOOLEAN,
  lastupdatedateinexternalsystem BIGINT
);

CREATE TABLE tagservice_tag (
  tagid CHAR(36) PRIMARY KEY,
  tagcollectionid CHAR(36) NOT NULL,
  externalid VARCHAR(255),
  taglabel VARCHAR(255),
  description LONGVARCHAR,
  alternativelabels LONGVARCHAR,
  createdby VARCHAR(255),
  creationdate BIGINT,
  externalcreation BOOLEAN,
  externalcreationDate BIGINT,
  externalupdate BOOLEAN,
  lastmodifiedby VARCHAR(255),
  lastmodificationdate BIGINT,
  lastupdatedateinexternalsystem BIGINT,
  parentid VARCHAR(255),
  externalhierarchycode LONGVARCHAR,
  externaltype VARCHAR(255),
  data LONGVARCHAR,
  FOREIGN KEY (tagcollectionid)
  REFERENCES tagservice_collection(tagcollectionid)
    ON DELETE RESTRICT
);

CREATE INDEX tagservice_tag_taglabel ON tagservice_tag (taglabel);
CREATE INDEX tagservice_tag_tagcollectionid ON tagservice_tag (tagcollectionid);
CREATE INDEX tagservice_tag_externalid ON tagservice_tag (externalid);

ALTER TABLE tagservice_collection ADD CONSTRAINT externalsourcename_UNIQUE UNIQUE (externalsourcename);
ALTER TABLE tagservice_collection ADD CONSTRAINT name_UNIQUE UNIQUE (name);
