


create table search_transaction ( 
	txname varchar2(64) not null, 
	txid number(19,0),  
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
	txid number(19,0)  not null, 
	txts number(19,0) not null, 
	indexwriter varchar2(255)  not null, 
	status varchar2(32) not null,  
	primary key  (txid) );

create table search_node_status ( 
	 jid number(19,0)  not null, 
	 jidts number(19,0)  not null, 
	 serverid varchar2(255)  not null, 
	 primary key (serverid) );

create index isearchbuilderitem_ctx on  searchbuilderitem (context);
create index isearchbuilderitem_act on  searchbuilderitem (searchaction);
create index isearchbuilderitem_sta_act on  searchbuilderitem (searchstate,searchaction);

-- HBM now does this correctly on oracle create index isearchwriterlock_lk on  searchwriterlock (lockkey);
	 	 
insert into search_transaction ( txid, txname ) values (0,'optimizeSequence');
insert into search_transaction ( txid, txname ) values (0,'mergeSequence');
insert into search_transaction ( txid, txname ) values (0,'sharedOptimizeSequence');
insert into search_transaction ( txid, txname ) values (0,'indexerTransaction');
