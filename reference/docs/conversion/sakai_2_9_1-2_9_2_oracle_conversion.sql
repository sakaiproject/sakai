-- This is the Oracle Sakai 2.9.2 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.9.0/2.9.1 to 2.9.2.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- BLTI-222
ALTER TABLE lti_content ADD (pagetitle VARCHAR2(255));
ALTER TABLE lti_content MODIFY (launch VARCHAR2(1024));
ALTER TABLE lti_content ADD (consumerkey VARCHAR2(255));
ALTER TABLE lti_content ADD (secret VARCHAR2(255));
ALTER TABLE lti_content ADD (settings CLOB);
ALTER TABLE lti_content ADD (placementsecret VARCHAR2(512));
ALTER TABLE lti_content ADD (oldplacementsecret VARCHAR2(512));
ALTER TABLE lti_tools ADD (allowtitle NUMBER(1) DEFAULT '0');
ALTER TABLE lti_tools ADD (pagetitle VARCHAR2(255));
ALTER TABLE lti_tools ADD (allowpagetitle NUMBER(1) DEFAULT '0');
ALTER TABLE lti_tools MODIFY (launch VARCHAR2(1024));
ALTER TABLE lti_tools ADD (allowlaunch NUMBER(1) DEFAULT '0');
ALTER TABLE lti_tools ADD (domain VARCHAR2(255));
ALTER TABLE lti_tools ADD (allowconsumerkey NUMBER(1) DEFAULT '0');
ALTER TABLE lti_tools ADD (allowsecret NUMBER(1) DEFAULT '0');
ALTER TABLE lti_tools ADD (allowoutcomes NUMBER(1) DEFAULT '0');
ALTER TABLE lti_tools ADD (allowroster NUMBER(1) DEFAULT '0');
ALTER TABLE lti_tools ADD (allowsettings NUMBER(1) DEFAULT '0');
ALTER TABLE lti_tools ADD (allowlori NUMBER(1) DEFAULT '0');

-- BLTI-208
ALTER TABLE lti_tools MODIFY (launch NULL);
ALTER TABLE lti_tools MODIFY (consumerkey NULL);
ALTER TABLE lti_tools MODIFY (secret NULL);