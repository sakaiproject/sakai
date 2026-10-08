-- Creates a sakai database at startup
create database sakai default character set utf8; 
create database sakai12 default character set utf8; 
create database sakai19 default character set utf8; 
create database sakai20 default character set utf8; 

-- Create some databases if you want to use it for other purposes
grant all on `sakai%`.* to sakai@'localhost' identified by 'ironchef'; 
grant all on `sakai%`.* to sakai@'127.0.0.1' identified by 'ironchef';
grant all on `sakai%`.* to sakai@'%' identified by 'ironchef';
