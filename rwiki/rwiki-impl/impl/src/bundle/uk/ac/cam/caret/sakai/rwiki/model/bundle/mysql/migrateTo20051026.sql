update rwikihistory set rwikihistory.userid = rwikihistory.user;
update rwikiobject set rwikiobject.userid = rwikiobject.user;
message You may want to execute alter table rwikihistory drop rwikihistory.user;
message You may want to execute alter table rwikiobject drop rwikiobject.user;
 
