-- SAK-23666/SAK-31603 

create table OAUTH_ACCESSORS (token varchar2(255 char) not null, secret varchar2(255 char), consumerId varchar2(255 char), userId varchar2(255 char), callbackUrl varchar2(255 char), verifier varchar2(255 char), creationDate timestamp, expirationDate timestamp, status number(10,0), type number(10,0), accessorSecret varchar2(255 char), primary key (token));
create table OAUTH_CONSUMERS (id varchar2(255 char) not null, name varchar2(255 char) not null, description varchar2(255 char), url varchar2(255 char), callbackUrl varchar2(255 char), secret varchar2(255 char) not null, accessorSecret varchar2(255 char), recordModeEnabled number(1,0), defaultValidity number(10,0) not null, primary key (id));
create table OAUTH_RIGHTS (id varchar2(255 char) not null, accessright varchar2(255 char) not null, primary key (id, accessright));
create index user_idx on OAUTH_ACCESSORS (userId);
alter table OAUTH_RIGHTS add constraint FK5992789F74F42154 foreign key (id) references OAUTH_CONSUMERS;

--
-- SAK-31636 Rename existing 'Home' tools
--

update sakai_site_page set title = 'Overview' where title = 'Home';

