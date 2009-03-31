-- ---------------------------------------------------------------------------
-- IMSENT_PERSON
-- ---------------------------------------------------------------------------

CREATE TABLE IMSENT_PERSON (
       USERID            VARCHAR (99) NOT NULL,
       FN                VARCHAR (255) NOT NULL,
       SORT              VARCHAR (255) NULL,
       PASSWORD          VARCHAR (99)  NULL,
       FAMILY            VARCHAR (255) NULL,
       GIVEN             VARCHAR (255) NULL,
       EMAIL             VARCHAR (255) NULL,
       PRIMARY KEY (USERID)
);

CREATE INDEX IE_IMSENT_PERSON_EMAIL ON IMSENT_PERSON
(
       EMAIL                          ASC
);

-- Users

INSERT INTO IMSENT_PERSON VALUES ('user1', 'One User', NULL, 'user1', 'User', 'One', 'user1@nowhere.com');
INSERT INTO IMSENT_PERSON VALUES ('user2', 'Two User', NULL, 'user2', 'User', 'Two', 'user2@nowhere.com');
INSERT INTO IMSENT_PERSON VALUES ('user3', 'User Three', NULL, 'user3', 'User', 'Three', 'user3@nowhere.com');
INSERT INTO IMSENT_PERSON VALUES ('user4', 'User Four', NULL, 'user4', 'User', 'Four', 'user4@nowhere.com');
INSERT INTO IMSENT_PERSON VALUES ('user5', 'User Five', NULL, 'user5', 'User', 'Five', 'user5@nowhere.com');
INSERT INTO IMSENT_PERSON VALUES ('user6', 'User Six', NULL, 'user6', 'User', 'Six', 'user6@nowhere.com');
INSERT INTO IMSENT_PERSON VALUES ('user7', 'User Seven', NULL, 'user7', 'User', 'Seven', 'user7@nowhere.com');
INSERT INTO IMSENT_PERSON VALUES ('user8', 'User Eight', NULL, 'user8', 'User', 'Eight', 'user8@nowhere.com');
INSERT INTO IMSENT_PERSON VALUES ('user9', 'User Nine', NULL, 'user9', 'User', 'Nine', 'user9@nowhere.com');


CREATE TABLE IMSENT_GROUP (
       SOURCEDID_ID                  VARCHAR (99) NOT NULL,
       DESCRIPTION_SHORT          VARCHAR (255) NOT NULL,
       ORG_ID                     VARCHAR (255) NULL,
       TIMEFRAME_BEGIN            DATETIME NULL,
       TIMEFRAME_END              DATETIME NULL,
       TIMEFRAME_ADMINPERIOD      VARCHAR (255) NULL,
       RELATION                   INTEGER NULL,
--                                   CHECK (RELATION IN (1, 4)),
       RELATION_SOURCEDID_ID      VARCHAR (99) NULL,
       RELATION_LABEL             VARCHAR (255) NULL,
       PRIMARY KEY (SOURCEDID_ID)
);

-- A Basic Course - SOURCEDID_ID must be unique across time
INSERT INTO IMSENT_GROUP VALUES ('F05:AH200', 'Art History', 'AH', NULL, NULL,
   'Fall:2005', NULL, NULL, NULL);

-- - A simple hierarchy - parent relationship (child is not supported)
-- - Parent membership is the merge between direct membership in the parent, and 
-- - membership in any of the children
INSERT INTO IMSENT_GROUP VALUES ('F05:CS101-000', 'Intro Computer Science - Lecture', 'CS', NULL, NULL,
   'Fall:2005', NULL, NULL, NULL);

INSERT INTO IMSENT_GROUP VALUES ('F05:CS101-101', 'Lab 1', 'CS', NULL, NULL,
   'Fall:2005', 1, 'F05:CS101-000', 'Lecture Section');

INSERT INTO IMSENT_GROUP VALUES ('F05:CS101-102', 'Lab 2', 'CS', NULL, NULL,
   'Fall 2005', 1, 'F05:CS101-000', 'Lecture Section');

-- - A Course and a "Also Known As"
INSERT INTO IMSENT_GROUP VALUES ('F05:GE400', 'Geological Studies', 'GE', NULL, NULL,
   'Fall:2005', NULL, NULL, NULL);

INSERT INTO IMSENT_GROUP VALUES ('F05:MU442', 'Ancient Rock Music', 'MU', NULL, NULL,
   'Fall:2005', 3, 'F05:GE400', 'Cross List');
   
-- - A Course and a "Redirect" - the redirected course is silently redirected
-- - there is no user visible evidence of the redirected course
-- - Membership is computed as the union of the membership of both courses
INSERT INTO IMSENT_GROUP VALUES ('F05:ST201', 'Intro Statistics', 'ST', NULL, NULL,
   'Fall:2005', NULL, NULL, NULL);

INSERT INTO IMSENT_GROUP VALUES ('F05:ST201-001', 'Stat Laboratory 1', 'ST', NULL, NULL,
   'Fall:2005', 4, 'F05:ST201', 'Same Course');

INSERT INTO IMSENT_GROUP VALUES ('F05:ST201-002', 'Stat Laboratory 2', 'ST', NULL, NULL,
   'Fall:2005', 4, 'F05:ST201', 'Same Course');
   
-- Set the common information for timeframes
UPDATE IMSENT_GROUP SET TIMEFRAME_ADMINPERIOD = 'Fall:2005';
UPDATE IMSENT_GROUP SET TIMEFRAME_BEGIN='2005-09-02 00:01:01.000';
UPDATE IMSENT_GROUP SET TIMEFRAME_BEGIN='2005-12-15 00:01:01.000';

CREATE TABLE IMSENT_MEMBERSHIP (
       SOURCEDID_ID               VARCHAR (99) NOT NULL,
       MEMBER_SOURCEDID_ID        VARCHAR (255) NOT NULL,
       MEMBER_ROLE_ROLETYPE       VARCHAR (99) NOT NULL,
       PRIMARY KEY (SOURCEDID_ID, MEMBER_SOURCEDID_ID, MEMBER_ROLE_ROLETYPE)
);

-- - Single Course
INSERT INTO IMSENT_MEMBERSHIP VALUES('F05:AH200', 'user1', 'Instructor');
INSERT INTO IMSENT_MEMBERSHIP VALUES('F05:AH200', 'user2', 'Learner');
INSERT INTO IMSENT_MEMBERSHIP VALUES('F05:AH200', 'user3', 'Learner');

-- - Standard Parent relationship - roles roll up
INSERT INTO IMSENT_MEMBERSHIP VALUES('F05:CS101-000', 'user1', 'Instructor');
INSERT INTO IMSENT_MEMBERSHIP VALUES('F05:CS101-101', 'user2', 'Learner');
INSERT INTO IMSENT_MEMBERSHIP VALUES('F05:CS101-102', 'user3', 'Learner');

-- - Cross list relationship - This is one course (F05:GE400) with 
-- - (F05:MU442) noted as its cross-list - memebership is merged
INSERT INTO IMSENT_MEMBERSHIP VALUES('F05:GE400', 'user1', 'Instructor');
INSERT INTO IMSENT_MEMBERSHIP VALUES('F05:GE400', 'user2', 'Learner');
INSERT INTO IMSENT_MEMBERSHIP VALUES('F05:MU442', 'user3', 'Learner');

-- - Equivalence relationship / redirect - two courses 
-- - (F05:ST201-001) and (F05:ST201-002) are invisibly 
-- - folded into (F05:ST201) 
INSERT INTO IMSENT_MEMBERSHIP VALUES('F05:ST201', 'user1', 'Instructor');
INSERT INTO IMSENT_MEMBERSHIP VALUES('F05:ST201-001', 'user2', 'Learner');
INSERT INTO IMSENT_MEMBERSHIP VALUES('F05:ST201-002', 'user3', 'Learner');