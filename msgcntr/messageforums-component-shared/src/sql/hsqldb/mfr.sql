-- insert types and default permissions for Author, Reviewer, Contributor, None

INSERT INTO CMN_TYPE_T VALUES (NULL, 0, '00000000-0000-0000-1111-000000000000', 'admin', 
  SYSDATE, 'admin', SYSDATE, 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Author Permission Level', 'Author Permission Level', 'Author Permission Level');
  
INSERT INTO CMN_TYPE_T VALUES (NULL, 0, '00000000-0000-0000-2222-000000000000', 'admin', 
  SYSDATE, 'admin', SYSDATE, 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Reviewer Permission Level', 'Reviewer Permission Level', 'Reviewer Permission Level');
  
INSERT INTO CMN_TYPE_T VALUES (NULL, 0, '00000000-0000-0000-3333-000000000000', 'admin', 
  SYSDATE, 'admin', SYSDATE, 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Contributor Permission Level', 'Contributor Permission Level', 'Contributor Permission Level');
  
INSERT INTO CMN_TYPE_T VALUES (NULL, 0, '00000000-0000-0000-4444-000000000000', 'admin', 
  SYSDATE, 'admin', SYSDATE, 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'None Permission Level', 'None Permission Level', 'None Permission Level');      
  
 -- insert permission levels
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  NULL, 0, '00000000-0000-0000-0000-111111111111', SYSDATE, 'admin', SYSDATE, 'admin', 
  '00000000-0000-0000-1111-000000000000', 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, NULL, NULL, NULL);
  
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  NULL, 0, '00000000-0000-0000-0000-111111111112', SYSDATE, 'admin',  SYSDATE, 'admin',
  '00000000-0000-0000-2222-000000000000', 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, NULL, NULL, NULL);
  
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  NULL, 0, '00000000-0000-0000-0000-111111111113', SYSDATE, 'admin',  SYSDATE, 'admin',
  '00000000-0000-0000-3333-000000000000', 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, NULL, NULL, NULL);
  
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  NULL, 0, '00000000-0000-0000-0000-111111111114', SYSDATE, 'admin',  SYSDATE, 'admin',
  '00000000-0000-0000-4444-000000000000', 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, NULL, NULL, NULL);      
  