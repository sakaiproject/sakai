-- SAK-25784 Convert News to RSS Portlet
-- ---------------------------
-- Add the titles from all existing news tools
INSERT INTO sakai_site_tool_property (site_id, tool_id, name, value)
	SELECT site_id, tool_id, 'javax.portlet-portlet_title', title FROM sakai_site_tool WHERE registration = 'sakai.news';

-- Setup all instances with the URL
UPDATE sakai_site_tool_property SET name = 'javax.portlet-feed_url' WHERE name = 'channel-url';

-- Finally, convert all news tools to the new portlet (must run last)
UPDATE sakai_site_tool SET registration = 'sakai.simple.rss' WHERE registration = 'sakai.news';
-- End SAK-25784

-- Access
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- maintain
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- access
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- maintain
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Instructor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Student
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Teaching Assistant
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- CIG Coord
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- CIT Part
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Reviewer
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Evaluator
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Program Admin
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Program Coord
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Student
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Faculty
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Faculty'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Faculty'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Faculty'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Faculty'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Member
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Member'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Member'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Member'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Learner
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Learner'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Learner'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Learner'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Mentor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Mentor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Mentor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Mentor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Mentor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Staff
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Staff'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Staff'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Staff'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Staff'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Alumni
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Alumni'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.roles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Alumni'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Alumni'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Alumni'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Prospective Student
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'ProspectiveStudent'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'ProspectiveStudent'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'ProspectiveStudent'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Other
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Other'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Other'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Other'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Guest
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Guest'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Guest'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Guest'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Observer
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Observer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Observer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Observer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Observer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Administrator
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Instructor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));



-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new permissions into existing realms
--
-- msg.permissions.allowToField.myGroupMembers
-- msg.permissions.allowToField.myGroups
-- msg.permissions.allowToField.users
-- msg.permissions.viewHidden.groups
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

-- maintain
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.viewHidden.groups');

-- access
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.users');

-- Instructor
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.viewHidden.groups');

-- TA
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.viewHidden.groups');

-- Student
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.users');

-- CIG Coordinator
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.viewHidden.groups');

-- Evaluator
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.viewHidden.groups');

-- Reviewer
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.viewHidden.groups');

-- CIG Participant
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.viewHidden.groups');



-- lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper" OR "!user.template")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper' AND SR.REALM_ID NOT LIKE '!user.template%'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;


-- ------------------------------
--  END permission backfill -----
-- ------------------------------

-- KNL-1336 - Add status for all nodes in a cluster.
ALTER TABLE SAKAI_CLUSTER ADD COLUMN STATUS VARCHAR(8);
-- We rename the column so we don't have update the primary key index
RENAME COLUMN SAKAI_CLUSTER.SERVER_ID TO SERVER_ID_INSTANCE;
ALTER TABLE SAKAI_CLUSTER ADD COLUMN SERVER_ID VARCHAR (64); 

-- SAK-27937 Add a course grade option to disable course points
alter table GB_GRADEBOOK_T add COURSE_POINTS_DISPLAYED number(1,0) default '0' not null;

-- END SAK-27937


--
-- SAK-27929 Add Dashboard to default !user site
--

INSERT INTO SAKAI_SITE_PAGE VALUES('!user-99', '!user', 'Dashboard', '0', 0, '0' );
INSERT INTO SAKAI_SITE_TOOL VALUES('!user-999', '!user-99', '!user', 'sakai.dashboard', 1, 'Dashboard', NULL );

-- SAK-25385
ALTER TABLE GB_GRADABLE_OBJECT_T MODIFY DUE_DATE TIMESTAMP;
-- End SAK-25385

-- SAK-23666 Add OAuth Admin tool to Administration workspace
INSERT INTO sakai_site_page VALUES('!admin-1500', '!admin', 'OAuth Admin', '0', 20, '0');
INSERT INTO sakai_site_tool VALUES('!admin-1550', '!admin-1500', '!admin', 'sakai.oauth.admin', 1, 'OAuth Admin', NULL);

-- SAK-23666 Add OAuth Admin tool to My Workspace template 
INSERT INTO sakai_site_page VALUES('!user-650', '!user', 'Trusted Applications', '0', 9, '0');
INSERT INTO sakai_site_tool VALUES('!user-655', '!user-650', '!user', 'sakai.oauth', 1, 'Trusted Applications', NULL);

-- SAK-28084 Roles for adding .auth/.anon to a site.
INSERT INTO SAKAI_REALM VALUES (SAKAI_REALM_SEQ.NEXTVAL, '!site.roles', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));

-- SAK-29432 Add dropDate field to Enrollment
ALTER TABLE CM_ENROLLMENT_T ADD COLUMN DROP_DATE DATE;


-- SAK-29422 Incorporate NYU's "public announcement system"

CREATE TABLE pasystem_popup_screens (
  uuid char(255) PRIMARY KEY ,
  descriptor varchar2(255),
  start_time NUMBER,
  end_time NUMBER,
  open_campaign number(1) DEFAULT NULL
);

CREATE INDEX popup_screen_descriptor on pasystem_popup_screens (descriptor);
CREATE INDEX popup_screen_start_time on pasystem_popup_screens (start_time);
CREATE INDEX popup_screen_end_time on pasystem_popup_screens (end_time);

CREATE TABLE pasystem_popup_content (
  uuid char(255),
  template_content CLOB,
  CONSTRAINT popup_content_uuid_fk FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid)
);

CREATE TABLE pasystem_popup_assign (
  uuid char(255),
  user_eid varchar2(255) DEFAULT NULL,
  CONSTRAINT popup_assign_uuid_fk FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid)
);

CREATE INDEX popup_assign_lower_user_eid on pasystem_popup_assign (lower(user_eid));

CREATE TABLE pasystem_popup_dismissed (
  uuid char(255),
  user_eid varchar2(255) DEFAULT NULL,
  state varchar2(50) DEFAULT NULL,
  dismiss_time NUMBER,
  CONSTRAINT popup_dismissed_uuid_fk FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid),
  CONSTRAINT popup_dismissed_unique UNIQUE (user_eid, state, uuid)
);

CREATE INDEX popup_dismissed_lower_user_eid on pasystem_popup_dismissed (lower(user_eid));
CREATE INDEX popup_dismissed_state on pasystem_popup_dismissed (state);

CREATE TABLE pasystem_banner_alert
( uuid CHAR(255) NOT NULL PRIMARY KEY,
  message VARCHAR2(4000) NOT NULL,
  hosts VARCHAR2(512),
  active NUMBER(1,0) DEFAULT 0 NOT NULL,
  start_time NUMBER,
  end_time NUMBER,
  banner_type VARCHAR2(255) DEFAULT 'warning'
);

MERGE INTO SAKAI_REALM_FUNCTION srf
USING (
SELECT -123 as function_key,
'pasystem.manage' as function_name
FROM dual
) t on (srf.function_name = t.function_name)
WHEN NOT MATCHED THEN
INSERT (function_key, function_name)
VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, t.function_name);

CREATE TABLE pasystem_banner_dismissed (
  uuid char(255),
  user_eid varchar2(255) DEFAULT NULL,
  state varchar2(50) DEFAULT NULL,
  dismiss_time NUMBER,
  CONSTRAINT banner_dismissed_uuid_fk FOREIGN KEY (uuid) REFERENCES pasystem_banner_alert(uuid),
  CONSTRAINT banner_dismissed_unique UNIQUE (user_eid, state, uuid)
);

CREATE INDEX banner_dismissed_lcase_eid on pasystem_banner_dismissed (lower(user_eid));
CREATE INDEX banner_dismissed_state on pasystem_banner_dismissed (state);

INSERT INTO SAKAI_SITE_PAGE VALUES('!admin-1500', '!admin', 'PA System', '0', 20, '0' );
INSERT INTO SAKAI_SITE_TOOL VALUES('!admin-1550', '!admin-1500', '!admin', 'sakai.pasystem', 1, 'PA System', NULL );
INSERT INTO SAKAI_SITE_PAGE_PROPERTY VALUES('!admin', '!admin-1500', 'sitePage.customTitle', 'true');

-- END SAK-29422 Incorporate NYU's "public announcement system"
-- SAK-29571 MFR_MESSAGE_DELETD_I causes bad performance
drop index MFR_MESSAGE_DELETED_I;
-- END SAK-29571 MFR_MESSAGE_DELETD_I causes bad performance

-- SAK-29546 Add site visit totals per user
CREATE TABLE SST_PRESENCE_TOTALS (
                ID NUMBER(19,0) NOT NULL auto_increment,
                SITE_ID varchar2(99) NOT NULL,
                USER_ID varchar2(99) NOT NULL,
                TOTAL_VISITS NUMBER(10,0) NOT NULL,
                LAST_VISIT_TIME DATE NOT NULL,
                UNIQUE KEY(SITE_ID, USER_ID),
                PRIMARY KEY(ID));
-- END SAK-29546

CREATE SEQUENCE SST_PRESENCE_TOTALS_ID START WITH 1 INCREMENT BY 1 nomaxvalue;
                
-- KNL-1369 Update kernel DDL with new roster.viewsitevisits permission
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'roster.viewsitevisits');
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsitevisits'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsitevisits'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsitevisits'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsitevisits'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsitevisits'));
-- END KNL-1369

-- SAK-29497 Improve usability of Schedule's "List of events" page
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'calendar.view.audience');
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.view.audience'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.view.audience'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.view.audience'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.view.audience'));
-- END SAK-29497

-- SAK-29271 Feedback Tool
CREATE TABLE sakai_feedback (
                id number not null primary key,
                user_id varchar2(99) null,
                email varchar2(255) not null,
                site_id varchar2(99) not null,
                report_type varchar2(15) not null,
                title varchar2(40) not null,
                content varchar2(4000) not null,
                CONSTRAINT cons_report_type CHECK (report_type IN ('content','technical', 'helpdesk')));
CREATE SEQUENCE sakai_feedback_seq START WITH 1 INCREMENT BY 1 nomaxvalue;
INSERT INTO SAKAI_SITE VALUES('!contact-us', 'Contact Us', null, null, null, '', '', null, 1, 0, 0, '', 'admin', 'admin', sysdate, sysdate, 1, 0, 0, 0, null);
INSERT INTO SAKAI_SITE_PAGE VALUES('!contact-us', '!contact-us', 'Contact Us', '0', 1, '0' );
INSERT INTO SAKAI_SITE_TOOL VALUES('!contact-us', '!contact-us', '!contact-us', 'sakai.feedback', 1, 'Contact Us', NULL );
-- END SAK-29271

--KNL-1379 Bigger SESSION_USER_AGENT
ALTER TABLE SAKAI_SESSION MODIFY SESSION_USER_AGENT VARCHAR2 (255 CHAR);
-- END KNL-1379

-- SAK-29974 Nested citation lists
ALTER TABLE CITATION_COLLECTION_ORDER ADD SECTION_TYPE ENUM('HEADING1','HEADING2', 'HEADING3', 'DESCRIPTION', 'CITATION') DEFAULT NULL;
ALTER TABLE CITATION_COLLECTION_ORDER ADD VALUE TEXT DEFAULT NULL;
ALTER TABLE CITATION_COLLECTION_ORDER MODIFY ( CITATION_ID VARCHAR(36) NULL);
-- End SAK-29974

--  SAK-29733 Change Schedule to Calendar for existing sites
UPDATE SAKAI_SITE_TOOL SET TITLE="Calendar" WHERE REGISTRATION = "sakai.schedule" AND TITLE = "Schedule";
UPDATE SAKAI_SITE_PAGE SET TITLE="Calendar" WHERE TITLE = "Schedule";

-- SAK-30000 Site creation notification email template updates
UPDATE email_template_item
SET message = '
From Worksite Setup to ${serviceName} support:

<#if courseSite ="true">Official Course Site<#else>Site </#if> ${siteTitle} (ID ${siteId}) was set up by ${currentUserDisplayName} (${currentUserDisplayId}, email ${currentUserEmail}) on ${dateDisplay} <#if courseSite ="true">for ${termTitle} </#if>
<#if numSections = "1">with access to the roster for this section:<#elseif numSections != "0">with access to rosters for these ${numSections} sections:</#if>

${sections}
'
WHERE template_key = 'sitemanage.notifySiteCreation' AND template_locale = 'default';
UPDATE email_template_item
SET subject = 'Site "${siteTitle}" was successfully created by ${currentUserDisplayName}', message = '
Hi, ${currentUserDisplayName}:

Your site "${siteTitle}" has been successfully created. The following is a copy of the site creation notification email sent to ${serviceName} support:


From Worksite Setup to ${serviceName} support:

<#if courseSite ="true">Official Course Site<#else>Site </#if> ${siteTitle} (ID ${siteId}) was set up by ${currentUserDisplayName} (${currentUserDisplayId}, email ${currentUserEmail}) on ${dateDisplay} <#if courseSite ="true">for ${termTitle} </#if>
<#if numSections = "1">with access to the roster for this section:<#elseif numSections != "0">with access to rosters for these ${numSections} sections:</#if>

${sections}
'
WHERE template_key = 'sitemanage.notifySiteCreation.confirmation' AND template_locale = 'default';
-- END SAK-30000

-- SAK-29740 update gradebook settings
ALTER TABLE gb_gradebook_t ADD course_letter_grade_displayed NUMBER(1,0) DEFAULT 1 NOT NULL;

-- SAK-29401/SAK-29977 Role based access to sites --
INSERT INTO SAKAI_REALM_ROLE VALUES (DEFAULT, '.default');
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));

-- SAK-25099 Anonymous topics in forums
ALTER TABLE MFR_TOPIC_T ADD POST_ANONYMOUS NUMBER(1,0) DEFAULT 0 NOT NULL;
ALTER TABLE MFR_TOPIC_T ADD REVEAL_IDS_TO_ROLES NUMBER(1,0) DEFAULT 0 NOT NULL;
ALTER TABLE MFR_PERMISSION_LEVEL_T ADD IDENTIFY_ANON_AUTHORS NUMBER(1,0) DEFAULT 0 NOT NULL;
UPDATE MFR_PERMISSION_LEVEL_T SET IDENTIFY_ANON_AUTHORS = 1 WHERE NAME = 'Owner';

-- SAM-2627
ALTER TABLE SAM_GRADINGATTACHMENT_T ADD ASSESSMENTGRADINGID NUMBER(19,0) NULL;

-- SAK-25544 - Quartz updated to 2.2.1
--
-- drop tables that are no longer used
--
drop table qrtz_job_listeners;
drop table qrtz_trigger_listeners;
--
-- drop columns that are no longer used
--
alter table qrtz_job_details drop column is_volatile;
alter table qrtz_triggers drop column is_volatile;
alter table qrtz_fired_triggers drop column is_volatile;
--
-- add new columns that replace the 'is_stateful' column
--
alter table qrtz_job_details add is_nonconcurrent varchar2(1);
alter table qrtz_job_details add is_update_data varchar2(1);
update qrtz_job_details set is_nonconcurrent = is_stateful;
update qrtz_job_details set is_update_data = is_stateful;
alter table qrtz_job_details modify is_nonconcurrent not null;
alter table qrtz_job_details modify is_update_data not null;
alter table qrtz_job_details drop column is_stateful;
alter table qrtz_fired_triggers add is_nonconcurrent varchar2(1);
alter table qrtz_fired_triggers add is_update_data varchar2(1);
update qrtz_fired_triggers set is_nonconcurrent = is_stateful;
update qrtz_fired_triggers set is_update_data = is_stateful;
alter table qrtz_fired_triggers modify is_nonconcurrent not null;
alter table qrtz_fired_triggers modify is_update_data not null;
alter table qrtz_fired_triggers drop column is_stateful;
--
-- add new 'sched_name' column to all tables
--
alter table qrtz_blob_triggers add sched_name varchar(120) DEFAULT 'TestScheduler' not null;
alter table qrtz_calendars add sched_name varchar(120) DEFAULT 'TestScheduler' not null;
alter table qrtz_cron_triggers add sched_name varchar(120) DEFAULT 'TestScheduler' not null;
alter table qrtz_fired_triggers add sched_name varchar(120) DEFAULT 'TestScheduler' not null;
alter table qrtz_job_details add sched_name varchar(120) DEFAULT 'TestScheduler' not null;
alter table qrtz_locks add sched_name varchar(120) DEFAULT 'TestScheduler' not null;
alter table qrtz_paused_trigger_grps add sched_name varchar(120) DEFAULT 'TestScheduler' not null;
alter table qrtz_scheduler_state add sched_name varchar(120) DEFAULT 'TestScheduler' not null;
alter table qrtz_simple_triggers add sched_name varchar(120) DEFAULT 'TestScheduler' not null;
alter table qrtz_triggers add sched_name varchar(120) DEFAULT 'TestScheduler' not null;
-- 
-- add new 'sched_time' column to all tables
--
alter table qrtz_fired_triggers add sched_time NUMBER(19,0) not null;
--
-- drop all foreign key constraints, so that we can define new ones
--
begin
    for r in ( select table_name,constraint_name
                from user_constraints
                where table_name in (upper('qrtz_triggers'),upper('qrtz_blob_triggers'),upper('qrtz_cron_triggers'),upper('qrtz_simple_triggers'))
                and constraint_type='R')
    loop
        execute immediate 'alter table '||r.table_name||' drop constraint '||r.constraint_name;
    end loop;
end;
--
-- add all primary and foreign key constraints, based on new columns
--
alter table qrtz_job_details drop primary key;
alter table qrtz_job_details add primary key (sched_name, job_name, job_group);
alter table qrtz_fired_triggers drop primary key;
alter table qrtz_fired_triggers add primary key (sched_name, entry_id);
alter table qrtz_calendars drop primary key;
alter table qrtz_calendars add primary key (sched_name, calendar_name);
alter table qrtz_locks drop primary key;
alter table qrtz_locks add primary key (sched_name, lock_name);
alter table qrtz_paused_trigger_grps drop primary key;
alter table qrtz_paused_trigger_grps add primary key (sched_name, trigger_group);
alter table qrtz_scheduler_state drop primary key;
alter table qrtz_scheduler_state add primary key (sched_name, instance_name);
alter table qrtz_triggers drop primary key;
alter table qrtz_triggers add primary key (sched_name, trigger_name, trigger_group);
alter table qrtz_triggers add foreign key (sched_name, job_name, job_group) references qrtz_job_details(sched_name, job_name, job_group);
alter table qrtz_blob_triggers drop primary key;
alter table qrtz_blob_triggers add primary key (sched_name, trigger_name, trigger_group);
alter table qrtz_blob_triggers add foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers(sched_name, trigger_name, trigger_group);
alter table qrtz_cron_triggers drop primary key;
alter table qrtz_cron_triggers add primary key (sched_name, trigger_name, trigger_group);
alter table qrtz_cron_triggers add foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers(sched_name, trigger_name, trigger_group);
alter table qrtz_simple_triggers drop primary key;
alter table qrtz_simple_triggers add primary key (sched_name, trigger_name, trigger_group);
alter table qrtz_simple_triggers add foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers(sched_name, trigger_name, trigger_group);
--
-- add new simprop_triggers table
--
CREATE TABLE qrtz_simprop_triggers
 (          
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 NUMBER(10,0) NULL,
    INT_PROP_2 NUMBER(10,0) NULL,
    LONG_PROP_1 NUMBER(19,0) NULL,
    LONG_PROP_2 NUMBER(19,0) NULL,
    DEC_PROP_1 NUMBER(13,4) NULL,
    DEC_PROP_2 NUMBER(13,4) NULL,
    BOOL_PROP_1 VARCHAR2(1) NULL,
    BOOL_PROP_2 VARCHAR2(1) NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);
--
-- create indexes for faster queries
--
drop index idx_qrtz_j_req_recovery;
create index idx_qrtz_j_req_recovery on qrtz_job_details(SCHED_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_j_grp on qrtz_job_details(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_t_j on qrtz_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_t_jg on qrtz_triggers(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_t_c on qrtz_triggers(SCHED_NAME,CALENDAR_NAME);
create index idx_qrtz_t_g on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP);
drop index idx_qrtz_t_state;
create index idx_qrtz_t_state on qrtz_triggers(SCHED_NAME,TRIGGER_STATE);
create index idx_qrtz_t_n_state on qrtz_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_n_g_state on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
drop index idx_qrtz_t_next_fire_time;
create index idx_qrtz_t_next_fire_time on qrtz_triggers(SCHED_NAME,NEXT_FIRE_TIME);
drop index idx_qrtz_t_nft_st;
create index idx_qrtz_t_nft_st on qrtz_triggers(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
create index idx_qrtz_t_nft_st_misfire_grp on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);
drop index idx_qrtz_ft_trig_inst_name; 
create index idx_qrtz_ft_trig_inst_name on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME);
create index idx_qrtz_ft_inst_job_req_rcvry on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_ft_j_g on qrtz_fired_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_ft_jg on qrtz_fired_triggers(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_ft_t_g on qrtz_fired_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
create index idx_qrtz_ft_tg on qrtz_fired_triggers(SCHED_NAME,TRIGGER_GROUP);

--
-- SAM-948 - MIN_SCORE option in Samigo
--

alter table SAM_ITEM_T add MIN_SCORE double precision NULL;
alter table SAM_PUBLISHEDITEM_T add MIN_SCORE double precision NULL;

-- KNL-1405 Don't have defaults for TIMESTAMP in SAKAI_SESSION
ALTER TABLE SAKAI_SESSION MODIFY SESSION_START NULL;
ALTER TABLE SAKAI_SESSION MODIFY SESSION_END NULL;

-- 1389 GradebookNG sortable assignments within categories, add CATEGORIZED_SORT_ORDER to GB_GRADABLE_OBJECT_T
ALTER TABLE GB_GRADABLE_OBJECT_T ADD COLUMN CATEGORIZED_SORT_ORDER number;
-- 
-- SAM-1117 - Option to not display scores
--

alter table SAM_ASSESSACCESSCONTROL_T add DISPLAYSCORE integer;
alter table SAM_PUBLISHEDACCESSCONTROL_T add DISPLAYSCORE integer;
alter table SAM_ITEM_T add SCORE_DISPLAY_FLAG number(1,0) default '0' not null;
alter table SAM_PUBLISHEDITEM_T add SCORE_DISPLAY_FLAG number(1,0) default '0' not null;

INSERT INTO SAM_ASSESSMETADATA_T ("ASSESSMENTMETADATAID", "ASSESSMENTID","LABEL",
     "ENTRY")
     VALUES(sam_assessMetaData_id_s.nextVal, 1, 'displayScores_isInstructorEditable', 'true');

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayScores_isInstructorEditable', 'true');

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayScores_isInstructorEditable', 'true');

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayScores_isInstructorEditable', 'true');

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayScores_isInstructorEditable', 'true');

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayScores_isInstructorEditable', 'true');

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(sam_assessMetaData_id_s.nextVal, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayScores_isInstructorEditable', 'true');

