-- SAK-23666/SAK-31603

create table OAUTH_ACCESSORS (token varchar(255) not null, secret varchar(255), consumerId varchar(255), userId varchar(255), callbackUrl varchar(255), verifier varchar(255), creationDate datetime, expirationDate datetime, status integer, type integer, accessorSecret varchar(255), primary key (token));
create table OAUTH_CONSUMERS (id varchar(255) not null, name varchar(255) not null, description varchar(255), url varchar(255), callbackUrl varchar(255), secret varchar(255) not null, accessorSecret varchar(255), recordModeEnabled bit, defaultValidity integer not null, primary key (id));
create table OAUTH_RIGHTS (id varchar(255) not null, accessright varchar(255) not null, primary key (id, accessright));
create index user_idx on OAUTH_ACCESSORS (userId);
alter table OAUTH_RIGHTS add index FK5992789F74F42154 (id), add constraint FK5992789F74F42154 foreign key (id) references OAUTH_CONSUMERS (id);

--
-- SAK-31636 Rename existing 'Home' tools
--

update SAKAI_SITE_PAGE set title = 'Overview' where title = 'Home';

