-- This is the MySql Sakai 1.5 -> 2.0 conversion script

-- new tables will be established by running auto-ddl.  If this is not desired, you must manually
-- create the tables for any new Sakai feature

-- tables were renamed
alter table CHEF_EVENT rename to SAKAI_EVENT;
alter table CHEF_DIGEST rename to SAKAI_DIGEST;
alter table CHEF_NOTIFICATION rename to SAKAI_NOTIFICATION;
alter table CHEF_PREFERENCES rename to SAKAI_PREFERENCES;
alter table CHEF_PRESENCE rename to SAKAI_PRESENCE;
alter table CHEF_SESSION rename to SAKAI_SESSION;

DROP TABLE if exists CHEF_ID_SEQ;

-- fields were expanded
alter table ANNOUNCEMENT_MESSAGE modify MESSAGE_ID VARCHAR(36);
ALTER TABLE CALENDAR_EVENT modify EVENT_ID VARCHAR(36) NOT NULL DEFAULT '';
alter table CHAT_MESSAGE modify MESSAGE_ID VARCHAR(36);
alter table DISCUSSION_MESSAGE modify MESSAGE_ID VARCHAR(36);
alter table DISCUSSION_MESSAGE modify REPLY VARCHAR(36);
alter table MAILARCHIVE_MESSAGE modify MESSAGE_ID VARCHAR(36);
alter table SAKAI_EVENT modify SESSION_ID VARCHAR(36);
alter table SAKAI_PRESENCE modify SESSION_ID VARCHAR(36);
alter table SAKAI_SESSION modify SESSION_ID VARCHAR(36);
alter table SAKAI_LOCKS modify USAGE_SESSION_ID VARCHAR(36);

-- tool ids were changed
update SAKAI_SITE_TOOL
set REGISTRATION=concat('sakai', substr(REGISTRATION,5)) where UPPER(substr(REGISTRATION,1,4)) = 'CHEF';
update SAKAI_SITE_TOOL set REGISTRATION='sakai.preferences' where REGISTRATION='sakai.noti.prefs';
update SAKAI_SITE_TOOL set REGISTRATION='sakai.online' where REGISTRATION='sakai.presence';
update SAKAI_SITE_TOOL set REGISTRATION='sakai.siteinfo' where REGISTRATION='sakai.siteinfogeneric';
update SAKAI_SITE_TOOL set REGISTRATION='sakai.sitesetup' where REGISTRATION='sakai.sitesetupgeneric';
update SAKAI_SITE_TOOL set REGISTRATION='sakai.discussion' where REGISTRATION='sakai.threadeddiscussion';

-- change the old site types
update SAKAI_SITE set TYPE='project' where TYPE='CTNG-project';
update SAKAI_SITE set TYPE='course' where TYPE='CTNG-course';

-- optional: drop old skins, everyone re-starts as default
/*
update SAKAI_SITE set SKIN=null;
*/

-- optional: drop all user myWorkspaces, so they get new ones (with new stuff)
/*
delete from sakai_site_user where site_id like '~%' and site_id != '~admin';
delete from sakai_site_tool_property where site_id like '~%' and site_id != '~admin';
delete from sakai_site_tool where site_id like '~%' and site_id != '~admin';
delete from sakai_site_page where site_id like '~%' and site_id != '~admin';
delete from sakai_site where site_id like '~%' and site_id != '~admin';
*/

-- replace the gateway site
/*
delete from sakai_site_user where site_id = '!gateway';
delete from sakai_site_tool_property where site_id = '!gateway';
delete from sakai_site_tool where site_id = '!gateway';
delete from sakai_site_page where site_id = '!gateway';
delete from sakai_site where site_id like '!gateway';
INSERT INTO SAKAI_SITE VALUES('!gateway', 'Gateway', null, null, 'The Gateway Site', null, null, null, 1, 0, 0, '', null, null, null, null, 1, 0 );
UPDATE SAKAI_SITE SET MODIFIEDBY='admin' WHERE SITE_ID = '!gateway';
UPDATE SAKAI_SITE SET MODIFIEDON='2003-11-26 03:45:22' WHERE SITE_ID = '!gateway';
INSERT INTO SAKAI_SITE_PAGE VALUES('!gateway-100', '!gateway', 'Welcome', '0', 1 );
INSERT INTO SAKAI_SITE_TOOL VALUES('!gateway-110', '!gateway-100', '!gateway', 'sakai.motd', 1, 'Message of the day', NULL );
INSERT INTO SAKAI_SITE_TOOL VALUES('!gateway-120', '!gateway-100', '!gateway', 'sakai.iframe', 2, 'Welcome!', NULL );
INSERT INTO SAKAI_SITE_TOOL_PROPERTY VALUES('!gateway', '!gateway-120', 'special', 'site' );
INSERT INTO SAKAI_SITE_PAGE VALUES('!gateway-200', '!gateway', 'About', '0', 2 );
INSERT INTO SAKAI_SITE_TOOL VALUES('!gateway-210', '!gateway-200', '!gateway', 'sakai.iframe', 1, 'About', NULL );
INSERT INTO SAKAI_SITE_TOOL_PROPERTY VALUES('!gateway', '!gateway-210', 'height', '500px' );
INSERT INTO SAKAI_SITE_TOOL_PROPERTY VALUES('!gateway', '!gateway-210', 'source', '/library/content/gateway/about.html' );
INSERT INTO SAKAI_SITE_PAGE VALUES('!gateway-300', '!gateway', 'Features', '0', 3 );
INSERT INTO SAKAI_SITE_TOOL VALUES('!gateway-310', '!gateway-300', '!gateway', 'sakai.iframe', 1, 'Features', NULL );
INSERT INTO SAKAI_SITE_TOOL_PROPERTY VALUES('!gateway', '!gateway-310', 'height', '500px' );
INSERT INTO SAKAI_SITE_TOOL_PROPERTY VALUES('!gateway', '!gateway-310', 'source', '/library/content/gateway/features.html' );
INSERT INTO SAKAI_SITE_PAGE VALUES('!gateway-400', '!gateway', 'Sites', '0', 4 );
INSERT INTO SAKAI_SITE_TOOL VALUES('!gateway-410', '!gateway-400', '!gateway', 'sakai.sitebrowser', 1, 'Sites', NULL );
INSERT INTO SAKAI_SITE_PAGE VALUES('!gateway-500', '!gateway', 'Training', '0', 5 );
INSERT INTO SAKAI_SITE_TOOL VALUES('!gateway-510', '!gateway-500', '!gateway', 'sakai.iframe', 1, 'Training', NULL );
INSERT INTO SAKAI_SITE_TOOL_PROPERTY VALUES('!gateway', '!gateway-510', 'height', '500px' );
INSERT INTO SAKAI_SITE_TOOL_PROPERTY VALUES('!gateway', '!gateway-510', 'source', '/library/content/gateway/training.html' );
INSERT INTO SAKAI_SITE_PAGE VALUES('!gateway-600', '!gateway', 'New Account', '0', 6 );
INSERT INTO SAKAI_SITE_TOOL VALUES('!gateway-610', '!gateway-600', '!gateway', 'sakai.createuser', 1, 'New Account', NULL );
*/

-- skin has changed, so most should have defaults and icons set
-- here's an example of finding old skins and changing them
/*
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/arc.gif' where skin='arc.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/art.gif' where skin='art.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/bus.gif' where skin='bus.css';
update SAKAI_SITE set SKIN=null where skin='chef.css';
update SAKAI_SITE set SKIN=null where skin='ctng.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/den.gif' where skin='den.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/edu.gif' where skin='edu.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/eng.gif' where skin='eng.css';
update SAKAI_SITE set SKIN=null where skin='examp-u.css';
update SAKAI_SITE set SKIN=null where skin='glrc.css';
update SAKAI_SITE set SKIN=null where skin='hkitty.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/inf.gif' where skin='inf.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/kin.gif' where skin='kin.css';
update SAKAI_SITE set SKIN=null where skin='komisar.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/law.gif' where skin='law';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/law.gif' where skin='law.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/lsa.gif' where skin='lsa';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/lsa.gif' where skin='lsa.css';
update SAKAI_SITE set SKIN=null where skin='lynx.css';
update SAKAI_SITE set SKIN='med', ICON_URL='/ctlib/icon/med.jpg' where skin='med.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/mus.gif' where skin='mus.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/nre.gif' where skin='nre.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/nur.gif' where skin='nur.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/off.gif' where skin='off.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/pha.gif' where skin='pha.css';
update SAKAI_SITE set SKIN=null where skin='pro.css';
update SAKAI_SITE set SKIN=null where skin='prp.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/rac.gif' where skin='rac.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/sph.gif' where skin='sph.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/spp.gif' where skin='spp.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/ssw.gif' where skin='ssw.css';
update SAKAI_SITE set SKIN=null where skin='um';
update SAKAI_SITE set SKIN=null where skin='um.css';
update SAKAI_SITE set SKIN=null where skin='sakai_core.css';
update SAKAI_SITE set SKIN=null, ICON_URL='/ctlib/icon/umd.gif' where skin='umd.css';
*/

-- GradTools specific conversions
/*
update sakai_site_tool_property set value='http://gradtools.umich.edu/about.html'
	where dbms_lob.substr( value, 4000, 1 ) ='/content/public/GradToolsInfo.html';
update sakai_site_tool_property set value='http://gradtools.umich.edu/help'
	where dbms_lob.substr( value, 4000, 1 ) ='https://coursetools.ummu.umich.edu/disstools/help.nsf';
*/

-- get rid of the old "contact support" help, if using the new portal supplied help
/*
delete from sakai_site_tool_property where tool_id in
	(select tool_id from sakai_site_tool where page_id in
		(select page_id from sakai_site_page where title='Help')
	);
delete from sakai_site_tool where page_id in
	(select page_id from sakai_site_page where title='Help');
delete from sakai_site_page_property where page_id in
	(select page_id from sakai_site_page where title='Help');
delete from sakai_site_page where title='Help';
*/

-- add some additional keys

ALTER TABLE `ANNOUNCEMENT_MESSAGE`
ADD  KEY `ANNOUNCEMENT_MESSAGE_CDD` (`CHANNEL_ID`,`MESSAGE_DATE`,`DRAFT`);

ALTER TABLE CALENDAR_EVENT modify EVENT_ID VARCHAR(36) NOT NULL DEFAULT '';

ALTER TABLE `CHAT_MESSAGE`
ADD KEY `CHAT_MSG_CDD` (`CHANNEL_ID`,`MESSAGE_DATE`,`DRAFT`);

ALTER TABLE `DISCUSSION_MESSAGE`
ADD  KEY `DISC_MSG_CDD` (`CHANNEL_ID`,`MESSAGE_DATE`,`DRAFT`);

ALTER TABLE `MAILARCHIVE_MESSAGE`
ADD  KEY `MAILARC_MSG_CDD` (`CHANNEL_ID`,`MESSAGE_DATE`,`DRAFT`);

-- add gradebook permissions

INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT,'gradebook.access');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT,'gradebook.maintain');

-- add admin role

INSERT INTO `SAKAI_REALM_ROLE` VALUES (DEFAULT,'admin');

-- add the mercury and !admin sites and associated realms

INSERT INTO SAKAI_REALM VALUES (DEFAULT,'/site/mercury','',NULL,'admin','admin',CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO SAKAI_REALM VALUES (DEFAULT,'/site/!admin','',(select ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME='admin'),'admin','admin',CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO `SAKAI_SITE` VALUES ('!admin','Administration Workspace',NULL,NULL,'Administration Workspace',NULL,NULL,NULL,1,'0','0','',NULL,NULL,NULL,NULL,'0','0');

INSERT INTO `SAKAI_SITE` VALUES ('mercury','mercury site',NULL,NULL,NULL,'','',NULL,1,'1','1','access','admin','admin',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),'0','0');

INSERT INTO `SAKAI_SITE_PAGE` VALUES ('!admin-100','!admin','Home','0',1),('!admin-200','!admin','Users','0',2),('!admin-250','!admin','Aliases','0',3),('!admin-300','!admin','Sites','0',4),('!admin-350','!admin','Realms','0',5),('!admin-360','!admin','Worksite Setup','0',6),('!admin-400','!admin','MOTD','0',7),('!admin-500','!admin','Resources','0',8),('!admin-600','!admin','On-Line','0',9),('!admin-700','!admin','Memory','0',10),('!admin-900','!admin','Site Archive','0',11);

INSERT INTO `SAKAI_SITE_PAGE` VALUES ('mercury-100','mercury','Home','1',1),('mercury-200','mercury','Schedule','0',2),('mercury-300','mercury','Announcements','0',3),('mercury-400','mercury','Resources','0',4),('mercury-500','mercury','Discussion','0',5),('mercury-600','mercury','Assignments','0',6),('mercury-700','mercury','Drop Box','0',7),('mercury-800','mercury','Chat','0',8),('mercury-900','mercury','Email Archive','0',9);

INSERT INTO `SAKAI_SITE_TOOL` VALUES 
('!admin-110','!admin-100','!admin','sakai.motd',1,'Message of the Day',NULL),('!admin-120','!admin-100','!admin','sakai.iframe',2,'My Workspace Information',NULL),('!admin-210','!admin-200','!admin','sakai.users',1,'Users',NULL),('!admin-260','!admin-250','!admin','sakai.aliases',1,'Aliases',NULL),('!admin-310','!admin-300','!admin','sakai.sites',1,'Sites',NULL),('!admin-355','!admin-350','!admin','sakai.realms',1,'Realms',NULL),('!admin-365','!admin-360','!admin','sakai.sitesetup',1,'Worksite Setup',NULL),('!admin-410','!admin-400','!admin','sakai.announcements',1,'MOTD',NULL),('!admin-510','!admin-500','!admin','sakai.resources',1,'Resources',NULL),('!admin-610','!admin-600','!admin','sakai.online',1,'On-Line',NULL),('!admin-710','!admin-700','!admin','sakai.memory',1,'Memory',NULL),('!admin-910','!admin-900','!admin','sakai.archive',1,'Site Archive / Import',NULL);

INSERT INTO `SAKAI_SITE_TOOL` VALUES 
('mercury-110','mercury-100','mercury','sakai.iframe',1,'My Workspace Information',NULL),('mercury-120','mercury-100','mercury','sakai.synoptic.announcement',2,'Recent Announcements',NULL),('mercury-130','mercury-100','mercury','sakai.synoptic.discussion',3,'Recent Discussion Items',NULL),('mercury-140','mercury-100','mercury','sakai.synoptic.chat',4,'Recent Chat Messages',NULL),('mercury-210','mercury-200','mercury','sakai.schedule',1,'Schedule',NULL),('mercury-310','mercury-300','mercury','sakai.announcements',1,'Announcements',NULL),('mercury-410','mercury-400','mercury','sakai.resources',1,'Resources',NULL),('mercury-510','mercury-500','mercury','sakai.discussion',1,'Discussion',NULL),('mercury-610','mercury-600','mercury','sakai.assignment',1,'Assignments',NULL),('mercury-710','mercury-700','mercury','sakai.dropbox',1,'Drop Box',NULL),('mercury-810','mercury-800','mercury','sakai.chat',1,'Chat',NULL),('mercury-910','mercury-900','mercury','sakai.mailbox',1,'Email Archive',NULL);

INSERT INTO `SAKAI_SITE_TOOL_PROPERTY` VALUES 
('!admin','!admin-120','special','workspace'),('!admin','!admin-410','channel','/announcement/channel/!site/motd'),('!admin','!admin-510','home','/');

INSERT INTO `SAKAI_SITE_TOOL_PROPERTY` VALUES 
('mercury','mercury-110','height','100px'),('mercury','mercury-110','special','workspace'),('mercury','mercury-110','source',''),('mercury','mercury-510','category','false'),('mercury','mercury-710','resources_mode','dropbox'),('mercury','mercury-810','display-date','true'),('mercury','mercury-810','filter-param','3'),('mercury','mercury-810','display-time','true'),('mercury','mercury-810','sound-alert','true'),('mercury','mercury-810','filter-type','SelectMessagesByTime'),('mercury','mercury-810','display-user','true');

INSERT INTO SAKAI_REALM_RL_GR VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/!admin'),
	'admin',(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'admin'), '1', '0');
INSERT INTO SAKAI_REALM_RL_GR VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	'admin',(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), '1', '0');

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.del'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.upd'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.upd'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.add'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.access'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read.drafts'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.revise.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.revise.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.del'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.del'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.upd'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.del'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.upd'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.add'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.add'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.upd'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.del'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.add'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.del'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.add'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.maintain'));

   
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '/site/!admin'),
	(select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'admin'),
	(select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'));
