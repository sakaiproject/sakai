
-- Already handled by Hibernate as of 3.6.x
-- alter table searchbuilderitem add index isearchbuilderitem_name (name);
-- alter table searchbuilderitem add index isearchbuilderitem_ctx (context);
-- alter table searchbuilderitem add index isearchbuilderitem_act_sta (searchstate,searchaction);




create table search_transaction ( 
	txname varchar(64) not null, 
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
	indexwriter varchar(255)  not null, 
	status varchar(32) not null,  
	primary key  (txid) );

create table search_node_status ( 
	 jid bigint  not null, 
	 jidts bigint  not null, 
	 serverid varchar(255)  not null, 
	 primary key (serverid) );
	 
	 
insert into search_transaction ( txid, txname ) values (0,'optimizeSequence');
insert into search_transaction ( txid, txname ) values (0,'mergeSequence');
insert into search_transaction ( txid, txname ) values (0,'sharedOptimizeSequence');
insert into search_transaction ( txid, txname ) values (0,'indexerTransaction');
