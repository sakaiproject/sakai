-- insert types and default permissions for Author, Reviewer, Contributor, None

-- insert permission level types
-- owner type
INSERT INTO CMN_TYPE_T VALUES (DEFAULT, 0, '00000000-0000-0000-1111-000000000000', 'admin', 
  NOW(), 'admin', NOW(), 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Owner Permission Level', 'Owner Permission Level', 'Owner Permission Level');
  
-- author type  
INSERT INTO CMN_TYPE_T VALUES (DEFAULT, 0, '00000000-0000-0000-2222-000000000000', 'admin', 
  NOW(), 'admin', NOW(), 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Author Permission Level', 'Author Permission Level', 'Author Permission Level');
  
-- nonediting author type
INSERT INTO CMN_TYPE_T VALUES (DEFAULT, 0, '00000000-0000-0000-3333-000000000000', 'admin', 
  NOW(), 'admin', NOW(), 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Nonediting Author Permission Level', 'Nonediting Author Permission Level', 'Nonediting Author Permission Level');
    
-- contributor type    
INSERT INTO CMN_TYPE_T VALUES (DEFAULT, 0, '00000000-0000-0000-4444-000000000000', 'admin', 
  NOW(), 'admin', NOW(), 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Contributor Permission Level', 'Contributor Permission Level', 'Contributor Permission Level');  
  
-- reviewer type  
INSERT INTO CMN_TYPE_T VALUES (DEFAULT, 0, '00000000-0000-0000-5555-000000000000', 'admin', 
  NOW(), 'admin', NOW(), 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Reviewer Permission Level', 'Reviewer Permission Level', 'Reviewer Permission Level');
  
-- none type  
INSERT INTO CMN_TYPE_T VALUES (DEFAULT, 0, '00000000-0000-0000-6666-000000000000', 'admin', 
  NOW(), 'admin', NOW(), 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'None Permission Level', 'None Permission Level', 'None Permission Level');      
  
-- custom type  
INSERT INTO CMN_TYPE_T VALUES (DEFAULT, 0, '00000000-0000-0000-7777-000000000000', 'admin', 
  NOW(), 'admin', NOW(), 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Custom Permission Level', 'Custom Permission Level', 'Custom Permission Level');    
  
-- insert permission levels
-- owner permission level
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  DEFAULT, 0, '00000000-0000-0000-0000-111111111111', NOW(), 'admin', NOW(), 'admin', 
  'Owner', '00000000-0000-0000-1111-000000000000', 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1);

-- author permission level
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  DEFAULT, 0, '00000000-0000-0000-0000-222222222222', NOW(), 'admin', NOW(), 'admin', 
  'Author', '00000000-0000-0000-2222-000000000000', 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0);
  
-- nonediting author permission level  
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  DEFAULT, 0, '00000000-0000-0000-0000-333333333333', NOW(), 'admin',  NOW(), 'admin',
  'Nonediting Author', '00000000-0000-0000-3333-000000000000', 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0);

-- contributor permission level
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  DEFAULT, 0, '00000000-0000-0000-0000-444444444444', NOW(), 'admin',  NOW(), 'admin',
  'Contributor', '00000000-0000-0000-4444-000000000000', 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0);

-- reviewer permission level
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  DEFAULT, 0, '00000000-0000-0000-0000-555555555555', NOW(), 'admin',  NOW(), 'admin',
  'Reviewer', '00000000-0000-0000-5555-000000000000', 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
  
-- none permission level  
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  DEFAULT, 0, '00000000-0000-0000-0000-666666666666', NOW(), 'admin',  NOW(), 'admin',
  'None', '00000000-0000-0000-6666-000000000000', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);      
  