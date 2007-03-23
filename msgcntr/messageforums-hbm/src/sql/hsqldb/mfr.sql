-- insert types and default permissions for Author, Reviewer, Contributor, None
  
-- insert permission levels
-- owner permission level
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  NULL, 0, '00000000-0000-0000-0000-111111111111', SYSDATE, 'admin', SYSDATE, 'admin', 
  'Owner', '00000000-0000-0000-1111-000000000000', 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1);

-- author permission level
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  NULL, 0, '00000000-0000-0000-0000-222222222222', SYSDATE, 'admin', SYSDATE, 'admin', 
  'Author', '00000000-0000-0000-2222-000000000000', 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0);
  
-- nonediting author permission level  
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  NULL, 0, '00000000-0000-0000-0000-333333333333', SYSDATE, 'admin',  SYSDATE, 'admin',
  'Nonediting Author', '00000000-0000-0000-3333-000000000000', 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0);

-- contributor permission level
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  NULL, 0, '00000000-0000-0000-0000-444444444444', SYSDATE, 'admin',  SYSDATE, 'admin',
  'Contributor', '00000000-0000-0000-4444-000000000000', 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0);

-- reviewer permission level
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  NULL, 0, '00000000-0000-0000-0000-555555555555', SYSDATE, 'admin',  SYSDATE, 'admin',
  'Reviewer', '00000000-0000-0000-5555-000000000000', 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
  
-- none permission level  
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  NULL, 0, '00000000-0000-0000-0000-666666666666', SYSDATE, 'admin',  SYSDATE, 'admin',
  'None', '00000000-0000-0000-6666-000000000000', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);  
  