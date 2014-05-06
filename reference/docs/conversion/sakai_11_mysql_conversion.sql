-- SAK-25784 Convert News to RSS Portlet
-- ---------------------------
-- Add the titles from all existing news tools
INSERT INTO SAKAI_SITE_TOOL_PROPERTY (site_id, tool_id, name, value)
	SELECT site_id, tool_id, 'javax.portlet:portlet_title', title FROM SAKAI_SITE_TOOL WHERE registration = 'sakai.news';

-- Setup all instances with the URL
UPDATE SAKAI_SITE_TOOL_PROPERTY SET name = 'javax.portlet:feed_url' WHERE name = 'channel-url';

-- Finally, convert all news tools to the new portlet (must run last)
UPDATE SAKAI_SITE_TOOL SET registration = 'sakai.simple.rss' WHERE registration = 'sakai.news';
-- End SAK-25784
