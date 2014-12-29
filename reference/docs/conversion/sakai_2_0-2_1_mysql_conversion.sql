-- This is the MySQL Sakai 2.0 -> 2.1 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.0.0 or 2.0.1 to 2.1.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------
-- YOU MUST MANUALLY ADJUST THIS SCRIPT!
-- The section where permissions are adjusted must be customize to your specific environment.
-- Look for the ADJUST ME notes below
-- --------------------------------------------------------------------------------------------------------------------------------------


-- --------------------------------------------------------------------------------------------------------------------------------------
-- from file "gradebook/component/src/sql/mysql/sakai_gradebook_2.0.1_to_2.1.sql"
-- Gradebook related TABLEs changes needed between Sakai 2.01 and 2.1
ALTER TABLE GB_GRADABLE_OBJECT_T ADD column (NOT_COUNTED bit);
UPDATE GB_GRADABLE_OBJECT_T SET NOT_COUNTED=0 WHERE NOT_COUNTED IS NULL AND POINTS_POSSIBLE IS NOT NULL;
-- --------------------------------------------------------------------------------------------------------------------------------------


-- --------------------------------------------------------------------------------------------------------------------------------------
-- from file "legacy/component/src/sql/mysql/sakai_site_group.sql"
-- Site related TABLEs added in Sakai 2.1
-- ---------------------------------------------------------------------------
-- SAKAI_SITE_GROUP
-- ---------------------------------------------------------------------------

CREATE TABLE SAKAI_SITE_GROUP (
       GROUP_ID             VARCHAR (99) NOT NULL,
       SITE_ID              VARCHAR (99) NOT NULL,
       TITLE                VARCHAR (99) NULL,
       DESCRIPTION          LONGTEXT NULL
);

ALTER TABLE SAKAI_SITE_GROUP
       ADD  ( PRIMARY KEY (GROUP_ID) ) ;

ALTER TABLE SAKAI_SITE_GROUP
       ADD  ( FOREIGN KEY (SITE_ID)
                             REFERENCES SAKAI_SITE ) ;

CREATE INDEX IE_SAKAI_SITE_GRP_SITE ON SAKAI_SITE_GROUP
(
       SITE_ID                       ASC
);

-- ---------------------------------------------------------------------------
-- SAKAI_SITE_GROUP_PROPERTY
-- ---------------------------------------------------------------------------

CREATE TABLE SAKAI_SITE_GROUP_PROPERTY (
       SITE_ID              VARCHAR (99) NOT NULL,
       GROUP_ID             VARCHAR (99) NOT NULL,
       NAME                 VARCHAR (99) NOT NULL,
       VALUE                LONGTEXT NULL
);

ALTER TABLE SAKAI_SITE_GROUP_PROPERTY
       ADD  ( PRIMARY KEY (GROUP_ID, NAME) ) ;

ALTER TABLE SAKAI_SITE_GROUP_PROPERTY
       ADD  ( FOREIGN KEY (GROUP_ID)
                             REFERENCES SAKAI_SITE_GROUP ) ;

ALTER TABLE SAKAI_SITE_GROUP_PROPERTY
       ADD  ( FOREIGN KEY (SITE_ID)
                             REFERENCES SAKAI_SITE ) ;

CREATE INDEX IE_SAKAI_SITE_GRP_PROP_SITE ON SAKAI_SITE_GROUP_PROPERTY
(
       SITE_ID                       ASC
);
-- --------------------------------------------------------------------------------------------------------------------------------------


-- --------------------------------------------------------------------------------------------------------------------------------------
-- from file "legacy/component/src/sql/mysql/sakai_site_2_1_0_003.sql"
-- Site related TABLEs changes needed after 2.1.0.003
ALTER TABLE SAKAI_SITE_PAGE ADD (POPUP CHAR(1) DEFAULT '0' CHECK (POPUP IN (1, 0)));
-- --------------------------------------------------------------------------------------------------------------------------------------


-- --------------------------------------------------------------------------------------------------------------------------------------
-- from file "legacy/component/src/sql/mysql/sakai_user_2_1_0_004.sql"
-- User related TABLEs changes needed after 2.1.0.004
ALTER TABLE SAKAI_USER ADD (EMAIL_LC VARCHAR (255));
UPDATE SAKAI_USER SET EMAIL_LC = LOWER(EMAIL);
DROP INDEX IE_SAKAI_USER_EMAIL ON SAKAI_USER;
CREATE INDEX IE_SAKAI_USER_EMAIL ON SAKAI_USER( EMAIL_LC ASC );
-- --------------------------------------------------------------------------------------------------------------------------------------


-- --------------------------------------------------------------------------------------------------------------------------------------
-- from file "legacy/component/src/sql/mysql/sakai_user_2_1_0.sql"
-- ---------------------------------------------------------------------------
-- Clear the password field for the postmaster if it has not yet been changed
-- ---------------------------------------------------------------------------
UPDATE SAKAI_USER SET PW='' WHERE USER_ID='postmaster' AND PW='ISMvKXpXpadDiUoOSoAf';
-- --------------------------------------------------------------------------------------------------------------------------------------


-- --------------------------------------------------------------------------------------------------------------------------------------
-- from file "legacy/component/src/sql/mysql/sakai_content_delete.sql"
-- ---------------------------------------------------------------------------
-- CONTENT_RESOURCE_DELETE
-- TODO: ADD CONTENT_RESOURCE_BODY_BINARY_DELETE TABLE if required
-- ---------------------------------------------------------------------------

CREATE TABLE CONTENT_RESOURCE_DELETE
(
    RESOURCE_ID VARCHAR (255) NOT NULL,
    RESOURCE_UUID VARCHAR (36),
	IN_COLLECTION VARCHAR (255),
	FILE_PATH VARCHAR (128),
	DELETE_DATE DATE,
	DELETE_USERID VARCHAR (36),
    XML LONGTEXT
);

CREATE UNIQUE INDEX CONTENT_RESOURCE_UUID_DELETE_I ON CONTENT_RESOURCE_DELETE
(
	RESOURCE_UUID
);

CREATE INDEX CONTENT_RESOURCE_DELETE_INDEX ON CONTENT_RESOURCE_DELETE
(
	RESOURCE_ID
);
-- -------------------------------------------------------------------------------------------------------------------------------------


-- -------------------------------------------------------------------------------------------------------------------------------------
-- from file "legacy/component/src/sql/mysql/sakai_content_2_1_0.sql"
ALTER TABLE CONTENT_RESOURCE ADD (RESOURCE_UUID VARCHAR (36));

CREATE INDEX CONTENT_UUID_RESOURCE_INDEX ON CONTENT_RESOURCE
(
	RESOURCE_UUID
);
-- -------------------------------------------------------------------------------------------------------------------------------------


-- -------------------------------------------------------------------------------------------------------------------------------------
-- for the Samigo conversion
ALTER TABLE SAM_ASSESSFEEDBACK_T ADD (SHOWSTUDENTQUESTIONSCORE INTEGER);
ALTER TABLE SAM_PUBLISHEDFEEDBACK_T ADD (SHOWSTUDENTQUESTIONSCORE INTEGER);
-- -------------------------------------------------------------------------------------------------------------------------------------


-- -------------------------------------------------------------------------------------------------------------------------------------
-- Permissions have changed.  The following will catch your database up.
-- Note there are ADJUST MEs here

-- functions new to Sakai 2.1
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'annc.all.groups');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'asn.grade');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.createAssessment');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.deleteAssessment.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.deleteAssessment.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.editAssessment.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.editAssessment.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.gradeAssessment.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.gradeAssessment.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.publishAssessment.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.publishAssessment.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.questionpool.copy.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.questionpool.create');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.questionpool.delete.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.questionpool.edit.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.submitAssessmentForGrade');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.takeAssessment');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.template.create');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.template.delete.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'assessment.template.edit.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'dropbox.maintain');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'gradebook.editAssignments');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'gradebook.gradeAll');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'gradebook.gradeSection');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'gradebook.viewOwnGrades');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'metaobj.create');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'metaobj.edit');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'metaobj.publish');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'metaobj.suggest.global.publish');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'rwiki.admin');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'rwiki.create');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'rwiki.delete');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'rwiki.read');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'rwiki.superadmin');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'rwiki.update');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'section.role.instructor');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'section.role.student');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'section.role.ta');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'site.upd.grp.mbrshp');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'site.upd.site.mbrshp');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'site.viewRoster');

-- possibly new role names from Sakai 2.1
INSERT INTO SAKAI_REALM_ROLE VALUES (DEFAULT, 'Instructor');
INSERT INTO SAKAI_REALM_ROLE VALUES (DEFAULT, 'Student');
INSERT INTO SAKAI_REALM_ROLE VALUES (DEFAULT, 'Teaching Assistant');

-- for each realm that has a role matching something in this TABLE, we will ADD to that role the function from this TABLE
-- these are the new permissions granted in the templates in Sakai 2.1
-- ADJUST ME: modify this TABLE to match your role names and the functions you want to INSERT
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

-- these are the new 'access' permissions (roles access, student, etc - also good for ta / teaching assistant)
-- ADJUST ME: adjust theses for your needs, either with different permissions, or duplicate for other roles than 'access'
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','assessment.submitAssessmentForGrade');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','assessment.takeAssessment');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','gradebook.viewOwnGrades');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','rwiki.create');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','rwiki.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','rwiki.update');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','section.role.student');

-- these are the new 'maintain' permissions (roles maintain, instructor, etc
-- ADJUST ME: adjust these for your needs, either with different permissions, or duplicate for other roles than 'maintain'
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','annc.all.groups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','asn.grade');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','asn.submit');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.createAssessment');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.deleteAssessment.any');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.deleteAssessment.own');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.editAssessment.any');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.editAssessment.own');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.gradeAssessment.any');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.gradeAssessment.own');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.publishAssessment.any');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.publishAssessment.own');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.questionpool.copy.own');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.questionpool.create');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.questionpool.delete.own');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.questionpool.edit.own');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.template.create');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.template.delete.own');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','assessment.template.edit.own');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','dropbox.maintain');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','gradebook.editAssignments');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','gradebook.gradeAll');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','metaobj.create');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','metaobj.edit');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','metaobj.publish');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','metaobj.suggest.global.publish');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','rwiki.admin');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','rwiki.create');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','rwiki.read');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','rwiki.update');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','section.role.instructor');

-- lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;

-- some permissions are no longer used - run this to clean them out
-- ADJUST ME: make sure this does not remove any functions you need!
-- ADJUST ME: Note that the GradTools "dis.*" permissions are removed - don't do that if you are using GradTools

-- the ones no longer needed for access
-- ADJUST ME: apply to other access roles (student, ta / teaching assistant, etc)
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.upd');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.add');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.del');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.upd');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.access');

-- the ones no longer needed for maintain
-- ADJUST ME: apply to other maintain roles (instructor, etc)
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.any');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.own');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.add');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.del');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.upd');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.add');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.del');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.add');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.del');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.upd');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.add');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.del');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.upd');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read.drafts');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.maintain');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.own');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.revise.any');
DELETE FROM SAKAI_REALM_RL_FN WHERE ROLE_KEY = (SELECT ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain') AND FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.revise.own');

-- remove any grants to the functions about to be removed
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.any');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.revise.own');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'crud.create');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'crud.delete');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'crud.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'crud.update');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.add');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.del');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.dis.upd');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.grp.add');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.grp.del');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.grp.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.grp.upd');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.info.add');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.info.del');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.info.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.info.upd');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.add');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.del');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.upd');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.path.upd.comm');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.add');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.del');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.status.upd');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.add');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.del');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dis.step.upd');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read.drafts');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.access');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.maintain');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.own');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.revise.any');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.revise.own');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'news.delete.own');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'news.new');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'news.read');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'news.revise.any');
DELETE FROM SAKAI_REALM_RL_FN WHERE FUNCTION_KEY = (SELECT FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'news.revise.own');

-- remove the unused function definitions
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='chat.revise.any';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='chat.revise.own';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='crud.create';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='crud.delete';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='crud.read';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='crud.update';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.dis.add';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.dis.del';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.dis.read';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.dis.upd';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.grp.add';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.grp.del';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.grp.read';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.grp.upd';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.info.add';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.info.del';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.info.read';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.info.upd';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.path.add';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.path.del';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.path.read';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.path.upd';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.path.upd.comm';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.status.add';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.status.del';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.status.read';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.status.upd';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.step.add';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.step.del';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.step.read';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='dis.step.upd';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='disc.read.drafts';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='gradebook.access';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='gradebook.maintain';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='mail.delete.own';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='mail.revise.any';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='mail.revise.own';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='news.delete.own';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='news.new';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='news.read';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='news.revise.any';
DELETE FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME='news.revise.own';

-- -------------------------------------------------------------------------------------------------------------------------------------


-- -------------------------------------------------------------------------------------------------------------------------------------
-- new AuthzGroup (realm) templates "!group.template", "!group.template.course", and "!site.template.course"
-- from file "legacy/component/src/sql/mysql/sakai_realm.sql" (partial)
INSERT INTO SAKAI_REALM VALUES (DEFAULT, '!site.template.course', '', (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 'admin', 'admin', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.all.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.createAssessment'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.copy.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.create'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.delete.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.edit.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.create'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.delete.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.edit.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.del'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.submitAssessmentForGrade'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeSection'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.ta'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));
INSERT INTO SAKAI_REALM VALUES (DEFAULT, '!group.template', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'));
INSERT INTO SAKAI_REALM VALUES (DEFAULT, '!group.template.course', '', (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 'admin', 'admin', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeSection'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.ta'));
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 'Can read, revise, delete and add both content and participants to a site.');
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), 'Can read content, and add content to a site where appropriate.');
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 'Can read, add, and revise most content in their sections.');
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), 'Can read, revise, delete and add both content and participants to a site.');
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), 'Can read content, and add content to a site where appropriate.');
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), 'Can read, add, and revise most content in their sections.');

-- OSP RELATED TABLES
CREATE TABLE osp_structured_artifact_def (id varchar(36) not null, description varchar(255), documentRoot varchar(255) not null, owner varchar(255) not null, created datetime not null, modified datetime not null, systemOnly bit not null, externalType varchar(255) not null, siteId varchar(255), siteState integer not null, globalState integer not null, schemaData longblob not null, instruction text, primary key (id));
CREATE TABLE osp_repository_lock (id varchar(36) not null, asset_id varchar(36), qualifier_id varchar(36), is_active bit, is_system bit, reason varchar(36), date_added datetime, date_removed datetime, primary key (id));

-- SYLLABUS ATTACHMENTS
CREATE TABLE SAKAI_SYLLABUS_ATTACH (syllabusAttachId bigint not null auto_increment, lockId integer not null, attachmentId text not null, syllabusAttachName text not null, syllabusAttachSize text, syllabusAttachType text, createdBy text, syllabusAttachUrl text not null, lastModifiedBy text, syllabusId bigint, primary key (syllabusAttachId))
ALTER TABLE SAKAI_SYLLABUS_ATTACH add index FK4BF41E45A09831E0 (syllabusId), add constraint FK4BF41E45A09831E0 foreign key (syllabusId) references SAKAI_SYLLABUS_DATA (id);




