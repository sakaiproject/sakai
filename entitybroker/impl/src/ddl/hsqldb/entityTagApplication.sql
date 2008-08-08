-- HSQLDB DDL to create table
CREATE TABLE {TABLENAME} ( 
    {ID}            BIGINT NOT NULL IDENTITY,
    entityRef       VARCHAR(255) NOT NULL,
    entityPrefix    VARCHAR(255) NOT NULL,
    tag             VARCHAR(255) NOT NULL);
