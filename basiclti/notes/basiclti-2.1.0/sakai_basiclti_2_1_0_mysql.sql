ALTER TABLE lti_content ADD     pagetitle VARCHAR(255);
ALTER TABLE lti_content MODIFY     launch TEXT(1024);
ALTER TABLE lti_content ADD     consumerkey VARCHAR(255);
ALTER TABLE lti_content ADD     secret VARCHAR(255);
ALTER TABLE lti_content ADD     settings TEXT(8096);
ALTER TABLE lti_content ADD     placementsecret TEXT(512);
ALTER TABLE lti_content ADD     oldplacementsecret TEXT(512);
ALTER TABLE lti_tools ADD     allowtitle TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD     pagetitle VARCHAR(255);
ALTER TABLE lti_tools ADD     allowpagetitle TINYINT DEFAULT '0';
ALTER TABLE lti_tools MODIFY     launch TEXT(1024);
ALTER TABLE lti_tools ADD     allowlaunch TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD     domain VARCHAR(255);
ALTER TABLE lti_tools ADD     allowconsumerkey TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD     allowsecret TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD     allowoutcomes TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD     allowroster TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD     allowsettings TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD     allowlori TINYINT DEFAULT '0';

-- From BLTI-208
ALTER TABLE lti_tools MODIFY launch VARCHAR(255) NULL;
ALTER TABLE lti_tools MODIFY consumerkey VARCHAR(255) NULL;
ALTER TABLE lti_tools MODIFY secret VARCHAR(255) NULL;
