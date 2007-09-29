create index isearchbuilderitem_name on  searchbuilderitem (name);
create index isearchbuilderitem_ctx on  searchbuilderitem (context);
create index isearchbuilderitem_act on  searchbuilderitem (searchaction);
create index isearchbuilderitem_sta on  searchbuilderitem (searchstate);
create index isearchwriterlock_lk on  searchwriterlock (lockkey);



create table search_transaction ( 
	txname varchar2(64) not null, 
	txid bigint,  
	primary key (txname));
	
-- this is performed by hbm at the moment
-- create table searchbuilderitem ( id varchar(64) not null, 
--	 version timestamp not null, 
--	 name varchar(255) not null, 
--	 context varchar(255) not null, 
--	 searchaction int default null, 
--	 searchstate int default null, 
--	 itemscope int default null, 
--	 primary key  (id), 
--	 unique (name) );

create table search_journal ( 
	txid bigint  not null, 
	txts bigint not null, 
	indexwriter varchar2(255)  not null, 
	status varchar2(32) not null,  
	primary key  (txid) );

create table search_node_status ( 
	 jid bigint  not null, 
	 jidts bigint  not null, 
	 serverid varchar2(255)  not null, 
	 primary key (serverid) );
