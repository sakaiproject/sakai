-- Oracle DDL to create table
CREATE TABLE {TABLENAME} (
    {ID}            number(20) NOT NULL PRIMARY KEY,
    entityRef       varchar(255) NOT NULL,
    entityPrefix    varchar(255) NOT NULL,
    tag             varchar(255) NOT NULL);
CREATE INDEX entityTagApp_entityRef ON {TABLENAME}(entityRef);
CREATE INDEX entityTagApp_entityPrefix ON {TABLENAME}(entityPrefix);
CREATE INDEX entityTagApp_tag ON {TABLENAME}(tag);
create sequence {IDSEQNAME};
create trigger bef_ins_ENTITYTAGAPPS 
    before insert on {TABLENAME}
    for each row 
    begin 
	    select {IDSEQNAME}.nextval 
        into :new.{ID} from dual; end;
