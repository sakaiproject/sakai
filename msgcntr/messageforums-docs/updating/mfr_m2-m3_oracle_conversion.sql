
-- This is the Oracle Message Center m2 -> m3 conversion script

-- MFR_TOPIC_T
alter table MFR_TOPIC_T add CONTEXT_ID varchar2(36);
create index MRF_TOPIC_DTYPE_I on MFR_TOPIC_T (TOPIC_DTYPE);
create index MFR_PT_CONTEXT_I on MFR_TOPIC_T (CONTEXT_ID);
create index MFR_TOPIC_CREATED_I on MFR_TOPIC_T (CREATED);

-- MFR_PERMISSION_LEVEL_T
create table MFR_PERMISSION_LEVEL_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(255) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(255) not null,
   NAME varchar2(50) not null,
   TYPE_UUID varchar2(36) not null,
   CHANGE_SETTINGS number(1,0) not null,
   DELETE_ANY number(1,0) not null,
   DELETE_OWN number(1,0) not null,
   MARK_AS_READ number(1,0) not null,
   MOVE_POSTING number(1,0) not null,
   NEW_FORUM number(1,0) not null,
   NEW_RESPONSE number(1,0) not null,
   NEW_RESPONSE_TO_RESPONSE number(1,0) not null,
   NEW_TOPIC number(1,0) not null,
   POST_TO_GRADEBOOK number(1,0) not null,
   X_READ number(1,0) not null,
   REVISE_ANY number(1,0) not null,
   REVISE_OWN number(1,0) not null,
   MODERATE_POSTINGS number(1,0) not null,
   primary key (ID)
);
create sequence MFR_PERMISSION_LEVEL_S;

-- MFR_MESSAGE_T
alter table MFR_MESSAGE_T add RECIPIENTS_AS_TEXT clob;
create index MFR_MESSAGE_LABEL_I on MFR_MESSAGE_T (LABEL);
create index MFR_MESSAGE_HAS_ATTACHMENTS_I on MFR_MESSAGE_T (HAS_ATTACHMENTS);
create index MFR_MESSAGE_CREATED_I on MFR_MESSAGE_T (CREATED);
create index MFR_MESSAGE_AUTHOR_I on MFR_MESSAGE_T (AUTHOR);
create index MFR_MESSAGE_DTYPE_I on MFR_MESSAGE_T (MESSAGE_DTYPE);
create index MFR_MESSAGE_TITLE_I on MFR_MESSAGE_T (TITLE);

-- MFR_MEMBERSHIP_ITEM_T
create table MFR_MEMBERSHIP_ITEM_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(255) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(255) not null,
   NAME varchar2(255) not null,
   TYPE number(10,0) not null,
   PERMISSION_LEVEL_NAME varchar2(255) not null,
   PERMISSION_LEVEL number(19,0) unique,
   a_surrogateKey number(19,0),
   of_surrogateKey number(19,0),
   t_surrogateKey number(19,0),
   primary key (ID)
);
alter table MFR_MEMBERSHIP_ITEM_T add constraint FKE03761CB6785AF85 foreign key (a_surrogateKey) references MFR_AREA_T;
alter table MFR_MEMBERSHIP_ITEM_T add constraint FKE03761CBC6FDB1CF foreign key (of_surrogateKey) references MFR_OPEN_FORUM_T;
alter table MFR_MEMBERSHIP_ITEM_T add constraint FKE03761CB2AFBA652 foreign key (t_surrogateKey) references MFR_TOPIC_T;
alter table MFR_MEMBERSHIP_ITEM_T add constraint FKE03761CB925CE0F4 foreign key (PERMISSION_LEVEL) references MFR_PERMISSION_LEVEL_T;
create sequence MFR_MEMBERSHIP_ITEM_S;

-- MFR_OPEN_FORUM_T
alter table MFR_OPEN_FORUM_T drop column ACTOR_PERMISSIONS;
create index MFR_OPEN_FORUM_DTYPE_I on MFR_OPEN_FORUM_T (FORUM_DTYPE);
create index MFR_OPEN_FORUM_TYPE_I on MFR_OPEN_FORUM_T (TYPE_UUID);

-- MFR_AREA_T
create index MFR_AREA_CONTEXT_I on MFR_AREA_T (CONTEXT_ID);
create index MFR_AREA_TYPE_I on MFR_AREA_T (TYPE_UUID);

-- MFR_PRIVATE_FORUM_T
create index MFR_PRIVATE_FORUM_CREATED_I on MFR_PRIVATE_FORUM_T (CREATED);


-- AUTO.DDL (including for those not using auto.ddl flag in Sakai)
-- insert types and default permissions for Author, Reviewer, Contributor, None

-- insert permission level types
-- owner type
INSERT INTO CMN_TYPE_T VALUES (CMN_TYPE_S.nextval, 0, '00000000-0000-0000-1111-000000000000', 'admin', 
  SYSDATE, 'admin', SYSDATE, 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Owner Permission Level', 'Owner Permission Level', 'Owner Permission Level');
  
-- author type  
INSERT INTO CMN_TYPE_T VALUES (CMN_TYPE_S.nextval, 0, '00000000-0000-0000-2222-000000000000', 'admin', 
  SYSDATE, 'admin', SYSDATE, 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Author Permission Level', 'Author Permission Level', 'Author Permission Level');
  
-- nonediting author type
INSERT INTO CMN_TYPE_T VALUES (CMN_TYPE_S.nextval, 0, '00000000-0000-0000-3333-000000000000', 'admin', 
  SYSDATE, 'admin', SYSDATE, 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Nonediting Author Permission Level', 'Nonediting Author Permission Level', 'Nonediting Author Permission Level');
    
-- contributor type    
INSERT INTO CMN_TYPE_T VALUES (CMN_TYPE_S.nextval, 0, '00000000-0000-0000-4444-000000000000', 'admin', 
  SYSDATE, 'admin', SYSDATE, 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Contributor Permission Level', 'Contributor Permission Level', 'Contributor Permission Level');  
  
-- reviewer type  
INSERT INTO CMN_TYPE_T VALUES (CMN_TYPE_S.nextval, 0, '00000000-0000-0000-5555-000000000000', 'admin', 
  SYSDATE, 'admin', SYSDATE, 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Reviewer Permission Level', 'Reviewer Permission Level', 'Reviewer Permission Level');
  
-- none type  
INSERT INTO CMN_TYPE_T VALUES (CMN_TYPE_S.nextval, 0, '00000000-0000-0000-6666-000000000000', 'admin', 
  SYSDATE, 'admin', SYSDATE, 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'None Permission Level', 'None Permission Level', 'None Permission Level');      
  
-- custom type  
INSERT INTO CMN_TYPE_T VALUES (CMN_TYPE_S.nextval, 0, '00000000-0000-0000-7777-000000000000', 'admin', 
  SYSDATE, 'admin', SYSDATE, 'org.sakaiproject.component.app.messageforums', 'sakai_messageforums',
  'Custom Permission Level', 'Custom Permission Level', 'Custom Permission Level');      
  
-- insert permission levels
-- owner permission level
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  MFR_PERMISSION_LEVEL_S.nextval, 0, '00000000-0000-0000-0000-111111111111', SYSDATE, 'admin', SYSDATE, 'admin', 
  'Owner', '00000000-0000-0000-1111-000000000000', 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1);

-- author permission level
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  MFR_PERMISSION_LEVEL_S.nextval, 0, '00000000-0000-0000-0000-222222222222', SYSDATE, 'admin', SYSDATE, 'admin', 
  'Author', '00000000-0000-0000-2222-000000000000', 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0);
  
-- nonediting author permission level  
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  MFR_PERMISSION_LEVEL_S.nextval, 0, '00000000-0000-0000-0000-333333333333', SYSDATE, 'admin',  SYSDATE, 'admin',
  'Nonediting Author', '00000000-0000-0000-3333-000000000000', 1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 0);

-- contributor permission level
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  MFR_PERMISSION_LEVEL_S.nextval, 0, '00000000-0000-0000-0000-444444444444', SYSDATE, 'admin',  SYSDATE, 'admin',
  'Contributor', '00000000-0000-0000-4444-000000000000', 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0);

-- reviewer permission level
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  MFR_PERMISSION_LEVEL_S.nextval, 0, '00000000-0000-0000-0000-555555555555', SYSDATE, 'admin',  SYSDATE, 'admin',
  'Reviewer', '00000000-0000-0000-5555-000000000000', 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0);
  
-- none permission level  
INSERT INTO MFR_PERMISSION_LEVEL_T VALUES (
  MFR_PERMISSION_LEVEL_S.nextval, 0, '00000000-0000-0000-0000-666666666666', SYSDATE, 'admin',  SYSDATE, 'admin',
  'None', '00000000-0000-0000-6666-000000000000', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);      





