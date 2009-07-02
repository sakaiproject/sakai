-- This is the MYSQL Sakai 2.6.x -> 2.7.0 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.6.x to 2.7.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------

-- SAK-16610 introduced a new osp presentation review permission
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'osp.presentation.review');
