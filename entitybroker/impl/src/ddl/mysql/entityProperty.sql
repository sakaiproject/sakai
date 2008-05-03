-- MySQL DDL to create table
CREATE TABLE {TABLENAME} ( 
    {ID}            bigint(20) AUTO_INCREMENT NOT NULL PRIMARY KEY,
    entityRef       varchar(255) NOT NULL,
    entityPrefix    varchar(255) NOT NULL,
    propertyName    varchar(255) NOT NULL,
    propertyValue   text NOT NULL,
    INDEX (entityRef, entityPrefix, propertyName) );