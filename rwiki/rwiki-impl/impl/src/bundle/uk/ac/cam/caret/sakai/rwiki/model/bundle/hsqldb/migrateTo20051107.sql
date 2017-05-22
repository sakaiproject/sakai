update rwikihistory set rwikihistory.name = replace(lcase(rwikihistory.name), '.', '/');
update rwikihistory set rwikihistory.referenced = replace(lcase(rwikihistory.referenced), '.', '/');
update rwikiobject set rwikiobject.name = replace(lcase(rwikiobject.name), '.', '/');
update rwikiobject set rwikiobject.referenced = replace(lcase(rwikiobject.referenced), '.', '/');
message You may want to check the content of each object for subspace links
message You may want to check the content of each object for subspace links