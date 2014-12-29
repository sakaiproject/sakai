-- Oracle DDL to create table
CREATE TABLE {TABLENAME} (
    {ID}            number(20) NOT NULL PRIMARY KEY,
    entityRef       varchar(255) NOT NULL,
    entityPrefix    varchar(255) NOT NULL,
    propertyName    varchar(255) NOT NULL,
    propertyValue   CLOB NOT NULL);
CREATE INDEX entityProp_entityRef ON {TABLENAME}(entityRef);
CREATE INDEX entityProp_entityPrefix ON {TABLENAME}(entityPrefix);
CREATE INDEX entityProp_propertyName ON {TABLENAME}(propertyName);
create sequence {IDSEQNAME};
create trigger bef_ins_ENTITYPROPS 
    before insert on {TABLENAME}
    for each row 
    begin 
	    select {IDSEQNAME}.nextval 
        into :new.{ID} from dual; end;
