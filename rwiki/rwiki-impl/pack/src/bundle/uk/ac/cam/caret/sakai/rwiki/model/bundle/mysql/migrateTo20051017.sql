alter table rwikihistory drop index name;
alter table rwikihistory add index name_idx(name);
update rwikihistory 
	set 
	rwikihistory.ownerRead = 0,
	rwikihistory.ownerWrite = 0,
	rwikihistory.ownerAdmin = 0,
	rwikihistory.groupRead = 0,
	rwikihistory.groupWrite = 0,
	rwikihistory.groupAdmin = 0,
	rwikihistory.publicRead = 0,
	rwikihistory.publicWrite = 0;
update rwikihistory, rwikiobject 
	set 
	rwikihistory.name = rwikiobject.name,
	rwikihistory.realm = rwikiobject.realm,
	rwikihistory.referenced = rwikiobject.referenced,
	rwikihistory.owner = rwikiobject.owner,
	rwikihistory.ownerRead = rwikiobject.ownerRead,
	rwikihistory.ownerWrite = rwikiobject.ownerWrite,
	rwikihistory.ownerAdmin = rwikiobject.ownerAdmin,
	rwikihistory.groupRead = rwikiobject.groupRead,
	rwikihistory.groupWrite = rwikiobject.groupWrite,
	rwikihistory.groupAdmin = rwikiobject.groupAdmin,
	rwikihistory.publicRead = rwikiobject.publicRead,
	rwikihistory.publicWrite = rwikiobject.publicWrite,
	rwikihistory.rwikiobjectid = rwikiobject.id
  where rwikihistory.rwobjectid = rwikiobject.id and rwikihistory.rwikiobjectid is null;

update rwikihistory 
	set 
	revision = -1
	where revision is null;
	
update rwikihistory 
	set 
	rwikiobjectid = rwobjectid
	where rwikiobjectid is null;

update rwikihistory 
	set 
	rwikiobjectid = -1
	where rwikiobjectid is null;
	
message update rwikihistory set rwobjectid = null;

message You may want to execute alter table rwikihistory drop rwobjectid;
alter table rwikihistory add index rwikiobjectid_idx(rwikiobjectid);
update rwikiobject set 
 	rwikiobject.revision = (select max(rwikihistory.revision)+1 from rwikihistory where rwikihistory.rwikiobjectid = rwikiobject.id )
 	where rwikiobject.revision is null;
insert into rwikicurrentcontent ( id, rwikiid, content )  select id,id,content from rwikiobject ;
insert into rwikihistorycontent ( id, rwikiid, content )  select id, id, content from rwikihistory ;
message You may want to execute alter table rwikiobject drop content; 
message You may want to execute alter table rwikihistory drop content; 
 
