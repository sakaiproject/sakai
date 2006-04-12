update rwikihistory set rwikihistory.name = translate(lower(rwikihistory.name), '.', '/');
update rwikihistory set rwikihistory.referenced = translate(lower(rwikihistory.referenced), '.', '/');
update rwikiobject set rwikiobject.name = translate(lower(rwikiobject.name), '.', '/');
update rwikiobject set rwikiobject.referenced = translate(lower(rwikiobject.referenced), '.', '/');
message You may want to check the content of each object for subspace links
message You may want to check the content of each object for subspace links