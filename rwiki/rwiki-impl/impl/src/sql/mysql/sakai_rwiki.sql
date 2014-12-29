alter table rwikicurrentcontent add index irwikicurrentcontent_rwi (rwikiid);
alter table rwikihistorycontent add index irwikihistorycontent_rwi (rwikiid); 
alter table rwikipagepresence add index irwikipagepresence_sid (sessionid);
alter table rwikiobject add index irwikiobject_realm (realm);

