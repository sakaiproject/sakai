-----------------------------------------------------------------------------
-- Fix http://bugs.sakaiproject.org/jira/browse/SAK-13842
-- 
-- Remove discussions and recent discussions from mercury and !worksite
-- 
-- Sites which still run discussions from contrib - can simply patch
-- this so it never runs - either by commenting out all this SQL
-- or by removing the call to this script in SiteService.
-----------------------------------------------------------------------------

-- Undo the hard-coded inserts

-- INSERT INTO SAKAI_SITE_TOOL VALUES('mercury-130', 'mercury-100', 
-- 'mercury', 'sakai.synoptic.discussion', 3, 'Recent Discussion Items', NULL );

DELETE FROM SAKAI_SITE_TOOL WHERE 
       TOOL_ID              = 'mercury-130' AND
       PAGE_ID              = 'mercury-100' AND 
       SITE_ID              = 'mercury' AND 
       REGISTRATION         = 'sakai.synoptic.discussion';

-- INSERT INTO SAKAI_SITE_TOOL_PROPERTY VALUES('mercury', 'mercury-510', 'category', 'false' );

DELETE FROM SAKAI_SITE_TOOL_PROPERTY WHERE
       SITE_ID = 'mercury' and
       TOOL_ID = 'mercury-510';

-- sakai_site.sql:INSERT INTO SAKAI_SITE_TOOL VALUES('mercury-510', 'mercury-500', 
-- 'mercury', 'sakai.discussion', 1, 'Discussion', NULL );

DELETE FROM SAKAI_SITE_TOOL WHERE 
       TOOL_ID              = 'mercury-510' AND
       PAGE_ID              = 'mercury-500' AND 
       SITE_ID              = 'mercury' AND 
       REGISTRATION         = 'sakai.discussion';

-- INSERT INTO SAKAI_SITE_TOOL VALUES('!worksite-130', '!worksite-100', 
-- '!worksite', 'sakai.synoptic.discussion', 3, 'Recent Discussion Items', NULL );

DELETE FROM SAKAI_SITE_TOOL WHERE 
       TOOL_ID              = '!worksite-130' AND
       PAGE_ID              = '!worksite-100' AND 
       SITE_ID              = '!worksite' AND 
       REGISTRATION         = 'sakai.synoptic.discussion';

-- INSERT INTO SAKAI_SITE_TOOL_PROPERTY VALUES('!worksite', '!worksite-510', 'category', 'false' );

DELETE FROM SAKAI_SITE_TOOL_PROPERTY WHERE
       SITE_ID = '!worksite' and
       TOOL_ID = '!worksite-510';

-- sakai_site.sql:INSERT INTO SAKAI_SITE_TOOL VALUES('!worksite-510', '!worksite-500', 
--  '!worksite', 'sakai.discussion', 1, 'Discussion', NULL );

DELETE FROM SAKAI_SITE_TOOL WHERE 
       TOOL_ID              = '!worksite-510' AND
       PAGE_ID              = '!worksite-500' AND 
       SITE_ID              = '!worksite' AND 
       REGISTRATION         = 'sakai.discussion';

