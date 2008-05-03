-- HSQLDB DDL to create table
CREATE TABLE {TABLENAME} ( 
    {ID}            BIGINT NOT NULL IDENTITY,
    entityRef       VARCHAR(255) NOT NULL,
    entityPrefix    VARCHAR(255) NOT NULL,
    propertyName    VARCHAR(255) NOT NULL,
    propertyValue   LONGVARCHAR NOT NULL);
