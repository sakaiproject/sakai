-----------------------------------------------------------------------------
-- SAKAI_REALM
-- Note: REALM_ID is the old "resource reference" string id for the realm
--       _KEY is the "internal" integer id used to crossreference the other tables
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM
(
       REALM_KEY            INTEGER NOT NULL IDENTITY,
       REALM_ID             NVARCHAR (255) NOT NULL,
       PROVIDER_ID          NVARCHAR (4000) NULL,
       MAINTAIN_ROLE        INTEGER NULL,
       CREATEDBY            NVARCHAR (99) NULL,
       MODIFIEDBY           NVARCHAR (99) NULL,
       CREATEDON            DATETIME NULL,
       MODIFIEDON           DATETIME NULL,
       PRIMARY KEY(REALM_KEY)
);

CREATE UNIQUE INDEX AK_SAKAI_REALM_ID ON SAKAI_REALM
(
       REALM_ID                       ASC
);

CREATE INDEX IE_SAKAI_REALM_CREATED ON SAKAI_REALM
(
       CREATEDBY                      ASC,
       CREATEDON                      ASC
);

CREATE INDEX IE_SAKAI_REALM_MODDED ON SAKAI_REALM
(
       MODIFIEDBY                     ASC,
       MODIFIEDON                     ASC
);

-----------------------------------------------------------------------------
-- SAKAI_REALM_PROPERTY
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_PROPERTY
(
       REALM_KEY            INTEGER NOT NULL,
       NAME                 NVARCHAR (99) NOT NULL,
       VALUE                NVARCHAR(MAX) NULL
);
sp_tableoption 'SAKAI_REALM_PROPERTY', 'large value types out of row','true';
;

ALTER TABLE SAKAI_REALM_PROPERTY
       ADD  CONSTRAINT SAKAI_REALM_PROPERTY_PK PRIMARY KEY (REALM_KEY, NAME)

CREATE INDEX FK_SAKAI_REALM_PROPERTY ON SAKAI_REALM_PROPERTY
(
       REALM_KEY                       ASC
)
;
-----------------------------------------------------------------------------
-- SAKAI_REALM_ROLE
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_ROLE
(
       ROLE_KEY             INTEGER NOT NULL IDENTITY,
       ROLE_NAME            NVARCHAR (99) NOT NULL,
       PRIMARY KEY (ROLE_KEY)
)

CREATE UNIQUE INDEX IE_SAKAI_REALM_ROLE_NAME ON SAKAI_REALM_ROLE
(
       ROLE_NAME                       ASC
)
;
-----------------------------------------------------------------------------
-- SAKAI_REALM_ROLE_DESC
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_ROLE_DESC
(
       REALM_KEY            INTEGER NOT NULL,
       ROLE_KEY             INTEGER NOT NULL,
       DESCRIPTION          NVARCHAR(MAX) NULL
)
;
sp_tableoption 'SAKAI_REALM_ROLE_DESC', 'large value types out of row', 'true';
;

ALTER TABLE SAKAI_REALM_ROLE_DESC
       ADD  CONSTRAINT SAKAI_REALM_ROLE_DESC_PK PRIMARY KEY (REALM_KEY, ROLE_KEY)

CREATE INDEX FK_SAKAI_REALM_ROLE_DESC_REALM ON SAKAI_REALM_ROLE_DESC
(
       REALM_KEY                      ASC
)
;
-----------------------------------------------------------------------------
-- SAKAI_REALM_FUNCTION
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_FUNCTION
(
       FUNCTION_KEY         INTEGER NOT NULL IDENTITY,
       FUNCTION_NAME        NVARCHAR (99) NOT NULL,
       PRIMARY KEY (FUNCTION_KEY)
)

CREATE UNIQUE INDEX IE_SAKAI_REALM_FUNCTION_NAME ON SAKAI_REALM_FUNCTION
(
       FUNCTION_NAME                       ASC
)

CREATE INDEX SAKAI_REALM_FUNCTION_KN ON SAKAI_REALM_FUNCTION
(
	FUNCTION_KEY,
	FUNCTION_NAME
)
;
-----------------------------------------------------------------------------
-- SAKAI_REALM_PROVIDER
-- provider id here is individual ids, where in the main table it may be
-- a single compound id
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_PROVIDER
(
       REALM_KEY            INTEGER NOT NULL,
       PROVIDER_ID          NVARCHAR (200) NOT NULL
)

ALTER TABLE SAKAI_REALM_PROVIDER
       ADD  CONSTRAINT SAKAI_REALM_PROVIDER_PK PRIMARY KEY (REALM_KEY, PROVIDER_ID)

CREATE INDEX FK_SAKAI_REALM_PROVIDER ON SAKAI_REALM_PROVIDER
(
       REALM_KEY                       ASC
)

CREATE INDEX IE_SAKAI_REALM_PROVIDER_ID ON SAKAI_REALM_PROVIDER
(
       PROVIDER_ID                       ASC
)
;
-----------------------------------------------------------------------------
-- SAKAI_REALM_RL_FN
-- role function
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_RL_FN
(
       REALM_KEY            INTEGER NOT NULL,
       ROLE_KEY             INTEGER NOT NULL,
       FUNCTION_KEY         INTEGER NOT NULL
)

ALTER TABLE SAKAI_REALM_RL_FN
       ADD  CONSTRAINT SAKAI_REALM_RL_FN_PK PRIMARY KEY (REALM_KEY, ROLE_KEY, FUNCTION_KEY)

CREATE INDEX FK_SAKAI_REALM_RL_FN_REALM ON SAKAI_REALM_RL_FN
(
       REALM_KEY                      ASC
)

CREATE INDEX FK_SAKAI_REALM_RL_FN_FUNC ON SAKAI_REALM_RL_FN
(
       FUNCTION_KEY                   ASC
)

CREATE INDEX FJ_SAKAI_REALM_RL_FN_ROLE ON SAKAI_REALM_RL_FN
(
       ROLE_KEY                       ASC
)
;
-----------------------------------------------------------------------------
-- SAKAI_REALM_RL_GR
-- role grant
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_RL_GR
(
       REALM_KEY            INTEGER NOT NULL,
       USER_ID              NVARCHAR (99) NOT NULL,
       ROLE_KEY             INTEGER NOT NULL,
       ACTIVE               CHAR(1) NULL
                                   CHECK (ACTIVE IN (1, 0)),
       PROVIDED             CHAR(1) NULL
                                   CHECK (PROVIDED IN (1, 0))
)

ALTER TABLE SAKAI_REALM_RL_GR
       ADD  CONSTRAINT SAKAI_REALM_RL_GR_PK PRIMARY KEY (REALM_KEY, USER_ID)

CREATE INDEX FK_SAKAI_REALM_RL_GR_REALM ON SAKAI_REALM_RL_GR
(
       REALM_KEY                      ASC
)

CREATE INDEX FK_SAKAI_REALM_RL_GR_ROLE ON SAKAI_REALM_RL_GR
(
       ROLE_KEY                      ASC
)

CREATE INDEX IE_SAKAI_REALM_RL_GR_ACT ON SAKAI_REALM_RL_GR
(
       ACTIVE                       ASC
)

CREATE INDEX IE_SAKAI_REALM_RL_GR_USR ON SAKAI_REALM_RL_GR
(
       USER_ID                       ASC
)

CREATE INDEX IE_SAKAI_REALM_RL_GR_PRV ON SAKAI_REALM_RL_GR
(
       PROVIDED                       ASC
)

CREATE INDEX SAKAI_REALM_RL_GR_RAU ON SAKAI_REALM_RL_GR
(
	ROLE_KEY,
	ACTIVE,
	USER_ID
)
;
-----------------------------------------------------------------------------
-- FOREIGN KEYS
-----------------------------------------------------------------------------

ALTER TABLE SAKAI_REALM
       ADD  CONSTRAINT SAKAI_REALM_FK FOREIGN KEY (MAINTAIN_ROLE)
                             REFERENCES SAKAI_REALM_ROLE (ROLE_KEY)

ALTER TABLE SAKAI_REALM_PROPERTY
       ADD  CONSTRAINT SAKAI_REALM_PROPERTY_FK FOREIGN KEY (REALM_KEY)
                             REFERENCES SAKAI_REALM (REALM_KEY)

ALTER TABLE SAKAI_REALM_PROVIDER
       ADD  CONSTRAINT SAKAI_REALM_PROVIDER_FK FOREIGN KEY (REALM_KEY)
                             REFERENCES SAKAI_REALM (REALM_KEY)

ALTER TABLE SAKAI_REALM_RL_FN
       ADD  CONSTRAINT SAKAI_REALM_RL_FN_FK FOREIGN KEY (REALM_KEY)
                             REFERENCES SAKAI_REALM (REALM_KEY)

ALTER TABLE SAKAI_REALM_RL_FN
       ADD  CONSTRAINT SAKAI_REALM_RL_FN_FK2 FOREIGN KEY (ROLE_KEY)
                             REFERENCES SAKAI_REALM_ROLE (ROLE_KEY)

ALTER TABLE SAKAI_REALM_RL_FN
       ADD  CONSTRAINT SAKAI_REALM_RL_FN_FK3 FOREIGN KEY (FUNCTION_KEY)
                             REFERENCES SAKAI_REALM_FUNCTION (FUNCTION_KEY)

ALTER TABLE SAKAI_REALM_ROLE_DESC
       ADD  CONSTRAINT SAKAI_REALM_ROLE_DESC_FK FOREIGN KEY (REALM_KEY)
                             REFERENCES SAKAI_REALM (REALM_KEY)

ALTER TABLE SAKAI_REALM_ROLE_DESC
       ADD  CONSTRAINT SAKAI_REALM_ROLE_DESC_FK2 FOREIGN KEY (ROLE_KEY)
                             REFERENCES SAKAI_REALM_ROLE (ROLE_KEY)

ALTER TABLE SAKAI_REALM_RL_GR
       ADD  CONSTRAINT SAKAI_REALM_RL_GR_FK FOREIGN KEY (REALM_KEY)
                             REFERENCES SAKAI_REALM (REALM_KEY)

ALTER TABLE SAKAI_REALM_RL_GR
       ADD  CONSTRAINT SAKAI_REALM_RL_GR_FK2 FOREIGN KEY (ROLE_KEY)
                             REFERENCES SAKAI_REALM_ROLE (ROLE_KEY)
;


INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ '.anon')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ '.auth')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'access')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'admin')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'CIG Coordinator');
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'CIG Participant');
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Evaluator');
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Instructor')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'maintain')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Program Admin');
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Program Coordinator');
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'pubview')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Reviewer');
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Student')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Teaching Assistant')
-- rSmart CLE customizations
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Guest')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Tech Support')


--
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.all.groups')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.read.drafts')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.revise.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.revise.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.grade')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.revise')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.submit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.all.groups')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.createAssessment')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.deleteAssessment.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.deleteAssessment.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.editAssessment.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.editAssessment.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.gradeAssessment.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.gradeAssessment.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.publishAssessment.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.publishAssessment.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.questionpool.copy.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.questionpool.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.questionpool.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.questionpool.edit.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.submitAssessmentForGrade')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.takeAssessment')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.template.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.template.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.template.edit.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.revise.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.revise.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.all.groups')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'chat.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'chat.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'chat.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'chat.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.revise.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.revise.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.all.groups')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.new.topic')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.revise.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.revise.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'dropbox.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'dropbox.maintain')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'gradebook.editAssignments')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'gradebook.gradeAll')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'gradebook.gradeSection')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'gradebook.viewOwnGrades')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'mail.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'mail.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'mail.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'msg.emailout')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.edit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.export')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.publish')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.suggest.global.publish')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'prefs.add')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'prefs.del')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'prefs.upd')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'realm.add')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'realm.del')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'realm.upd')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'realm.upd.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'reports.view')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'reports.run')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'reports.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'reports.edit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'reports.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'reports.share')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'section.role.instructor')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'section.role.student')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'section.role.ta')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.add')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.add.course')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.add.usersite')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.del')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.upd')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.upd.site.mbrshp')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.upd.grp.mbrshp')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.viewRoster')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.visit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.visit.unp')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'user.add')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'user.upd.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'rwiki.admin')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'rwiki.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'rwiki.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'rwiki.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'rwiki.superadmin')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'rwiki.update')
-- rSmart CLE customizations
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'jforum.admin')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'jforum.manage')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'jforum.member')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'melete.author')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'melete.student')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.export')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.import')
-- osp
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.style.globalPublish')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.style.publish')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.style.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.style.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.style.edit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.style.suggestGlobalPublish')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.help.glossary.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.help.glossary.add')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.help.glossary.edit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.help.glossary.export')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffolding.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffolding.revise.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffolding.revise.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffolding.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffolding.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffolding.publish.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffolding.publish.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffolding.export.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffolding.export.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.viewOwner')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffoldingSpecific.accessAll')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffoldingSpecific.viewEvalOther')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffoldingSpecific.viewFeedbackOther')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffoldingSpecific.manageStatus')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffoldingSpecific.accessUserList')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffoldingSpecific.viewAllGroups')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.matrix.scaffoldingSpecific.use')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.portfolio.evaluation.use')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.edit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.copy')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.comment')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.review')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.template.copy')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.template.publish')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.template.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.template.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.template.edit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.template.export')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.layout.publish')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.layout.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.layout.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.layout.edit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.presentation.layout.suggestPublish')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.wizard.publish')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.wizard.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.wizard.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.wizard.edit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.wizard.review')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.wizard.export')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.wizard.view')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'osp.wizard.evaluate')
--
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!site.helper', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

declare @realm_helper int, @role_maintain int, @function_realm_del int, @function_realm_upd int
declare @realm_site_user int, @role_access int, @realm_site_template int, @realm_user_template int, @realm_site_course_template int
declare @realm_group_template int, @realm_group_course_template int
declare @role_anon int, @role_auth int, @role_instructor int, @role_student int, @role_ta int, @role_guest int, @role_ts int

select @realm_helper=REALM_KEY from SAKAI_REALM where REALM_ID = '!site.helper'

select @role_maintain=ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'
select @role_access=ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'
select @role_anon=ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'
select @role_auth=ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'
select @role_cig_coordinator=ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'
select @role_cig_participant=ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'
select @role_evaluator=ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'
select @role_instructor = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'
select @role_program_admin = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Admin'
select @role_program_Coordinator = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Coordinator'
select @role_reviewer = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'
select @role_student = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'
select @role_ta = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'
select @role_guest = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Guest'
select @role_ts = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Tech Support'

select @function_realm_del=FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.del'
select @function_realm_upd=FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd'

declare @f1 int, @r_temp int, @role_temp int

INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_helper, @role_maintain, @function_realm_del)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_helper, @role_maintain, @function_realm_upd)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!site.user', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

select @realm_site_user=REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @function_realm_del)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @function_realm_upd)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!user.template', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_user_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.usersite'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!user.template.guest', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template.guest'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.usersite'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!user.template.maintain', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template.maintain'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.course'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.usersite'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!user.template.registered', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template.registered'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.course'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.usersite'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!user.template.sample', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template.sample'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.course'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.usersite'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!site.template', '', @role_maintain, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_site_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.submitAssessmentForGrade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.emailout'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.createAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.copy.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @function_realm_del)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @function_realm_upd)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.run'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.share'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.globalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.suggestGlobalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.viewOwner'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.portfolio.evaluation.use'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.suggestPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.review'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)

INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!site.template.course', '', @role_instructor, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_site_course_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.createAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.copy.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.emailout'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @function_realm_del)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @function_realm_upd)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.run'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.share'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.globalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.suggestGlobalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.viewOwner'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.portfolio.evaluation.use'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.suggestPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.review'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)


select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.submitAssessmentForGrade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeSection'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.run'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.share'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.ta'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)


INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!site.template.portfolio', '', @role_cig_coordinator, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_site_template_portfolio=REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.createAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.copy.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.emailout'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.run'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.share'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.globalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.suggestGlobalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.viewOwner'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.portfolio.evaluation.use'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.suggestPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.review'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_coordinator, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.submitAssessmentForGrade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_cig_participant, @f1)
  
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.submitAssessmentForGrade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.run'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.review'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_reviewer, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.submitAssessmentForGrade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.run'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.viewOwner'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.evaluate'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template_portfolio, @role_evaluator, @f1)


INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!group.template', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_group_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.globalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.suggestGlobalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.viewOwner'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.portfolio.evaluation.use'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.suggestPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.review'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)



INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!group.template.course', '', @role_instructor, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_group_course_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor,  @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.run'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.share'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.globalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.suggestGlobalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.viewOwner'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.portfolio.evaluation.use'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.suggestPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.review'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeSection'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.ta'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.run'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.share'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)

INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!group.template.portfolio', '', @role_cig_coordinator, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_group_template_portfolio=REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.portfolio'

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.createAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.copy.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.run'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.share'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.globalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.suggestGlobalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.viewOwner'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.portfolio.evaluation.use'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.suggestPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.review'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_coordinator, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.submitAssessmentForGrade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_cig_participant, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.submitAssessmentForGrade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.run'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.review'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_reviewer, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.submitAssessmentForGrade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.run'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'reports.view'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.viewOwner'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.evaluate'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolio, @role_evaluator, @f1)

INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!group.template.portfolioAdmin', '', @role_program_admin, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_group_template_portfolioAdmin=REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.portfolioAdmin'

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.createAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.copy.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.globalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.suggestGlobalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.viewOwner'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.portfolio.evaluation.use'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.suggestPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.review'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_admin, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.createAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.copy.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.globalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.style.suggestGlobalPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.help.glossary.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.publish.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.export.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.viewOwner'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.portfolio.evaluation.use'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.comment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.copy'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.template.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.presentation.layout.suggestPublish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.review'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.wizard.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template_portfolioAdmin, @role_program_coordinator, @f1)



INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/content/public/', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/content/public/'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/content/attachment/', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/content/attachment/'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/announcement/channel/!site/motd', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/announcement/channel/!site/motd'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!pubview', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '!pubview'
select @role_temp = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'pubview'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/site/!gateway', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/site/!gateway'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/site/!error', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/site/!error'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/site/!urlError', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/site/!urlError'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/site/mercury', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @function_realm_del)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @function_realm_upd)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
INSERT INTO SAKAI_REALM_RL_GR VALUES(@r_temp,'admin', @role_maintain, '1', '0')
select @role_temp = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'admin'
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/site/!admin', '', @role_temp, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/site/!admin'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
INSERT INTO SAKAI_REALM_RL_GR VALUES(@r_temp,'admin', @role_temp, '1', '0')
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES(@realm_site_course_template, @role_instructor, 'Can read, revise, delete and add both content and participants to a site.')
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES(@realm_site_course_template, @role_student, 'Can read content, and add content to a site where appropriate.')
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES(@realm_site_course_template, @role_ta, 'Can read, add, and revise most content in their sections.')
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES(@realm_group_course_template, @role_instructor, 'Can read, revise, delete and add both content and participants to a site.')
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES(@realm_group_course_template, @role_student, 'Can read content, and add content to a site where appropriate.')
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES(@realm_group_course_template, @role_ta, 'Can read, add, and revise most content in their sections.')

-- rSmart additions follow
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'jforum.manage'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeSection'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'melete.author'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.import'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'melete.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student,  @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'jforum.member'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'jforum.manage'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'melete.author'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.createAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.any'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.any'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.copy.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'jforum.member'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'melete.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'jforum.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'melete.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @function_realm_upd)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
;
-- up to date with r514
-----------------------------------------------------------------------------
-- SAKAI_REALM
-- Note: REALM_ID is the old "resource reference" string id for the realm
--       _KEY is the "internal" integer id used to crossreference the other tables
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM
(
       REALM_KEY            INTEGER NOT NULL IDENTITY,
       REALM_ID             NVARCHAR (255) NOT NULL,
       PROVIDER_ID          NVARCHAR (1024) NULL,
       MAINTAIN_ROLE        INTEGER NULL,
       CREATEDBY            NVARCHAR (99) NULL,
       MODIFIEDBY           NVARCHAR (99) NULL,
       CREATEDON            DATETIME NULL,
       MODIFIEDON           DATETIME NULL,
       PRIMARY KEY(REALM_KEY)
);

CREATE UNIQUE INDEX AK_SAKAI_REALM_ID ON SAKAI_REALM
(
       REALM_ID                       ASC
);

CREATE INDEX IE_SAKAI_REALM_CREATED ON SAKAI_REALM
(
       CREATEDBY                      ASC,
       CREATEDON                      ASC
);

CREATE INDEX IE_SAKAI_REALM_MODDED ON SAKAI_REALM
(
       MODIFIEDBY                     ASC,
       MODIFIEDON                     ASC
);

-----------------------------------------------------------------------------
-- SAKAI_REALM_PROPERTY
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_PROPERTY
(
       REALM_KEY            INTEGER NOT NULL,
       NAME                 NVARCHAR (99) NOT NULL,
       VALUE                NVARCHAR(MAX) NULL
);
sp_tableoption 'SAKAI_REALM_PROPERTY', 'large value types out of row','true';
;

ALTER TABLE SAKAI_REALM_PROPERTY
       ADD  CONSTRAINT SAKAI_REALM_PROPERTY_PK PRIMARY KEY (REALM_KEY, NAME)

CREATE INDEX FK_SAKAI_REALM_PROPERTY ON SAKAI_REALM_PROPERTY
(
       REALM_KEY                       ASC
)
;
-----------------------------------------------------------------------------
-- SAKAI_REALM_ROLE
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_ROLE
(
       ROLE_KEY             INTEGER NOT NULL IDENTITY,
       ROLE_NAME            NVARCHAR (99) NOT NULL,
       PRIMARY KEY (ROLE_KEY)
)

CREATE UNIQUE INDEX IE_SAKAI_REALM_ROLE_NAME ON SAKAI_REALM_ROLE
(
       ROLE_NAME                       ASC
)
;
-----------------------------------------------------------------------------
-- SAKAI_REALM_ROLE_DESC
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_ROLE_DESC
(
       REALM_KEY            INTEGER NOT NULL,
       ROLE_KEY             INTEGER NOT NULL,
       DESCRIPTION          NVARCHAR(MAX) NULL
)
;
sp_tableoption 'SAKAI_REALM_ROLE_DESC', 'large value types out of row', 'true';
;

ALTER TABLE SAKAI_REALM_ROLE_DESC
       ADD  CONSTRAINT SAKAI_REALM_ROLE_DESC_PK PRIMARY KEY (REALM_KEY, ROLE_KEY)

CREATE INDEX FK_SAKAI_REALM_ROLE_DESC_REALM ON SAKAI_REALM_ROLE_DESC
(
       REALM_KEY                      ASC
)
;
-----------------------------------------------------------------------------
-- SAKAI_REALM_FUNCTION
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_FUNCTION
(
       FUNCTION_KEY         INTEGER NOT NULL IDENTITY,
       FUNCTION_NAME        NVARCHAR (99) NOT NULL,
       PRIMARY KEY (FUNCTION_KEY)
)

CREATE UNIQUE INDEX IE_SAKAI_REALM_FUNCTION_NAME ON SAKAI_REALM_FUNCTION
(
       FUNCTION_NAME                       ASC
)

CREATE INDEX SAKAI_REALM_FUNCTION_KN ON SAKAI_REALM_FUNCTION
(
	FUNCTION_KEY,
	FUNCTION_NAME
)
;
-----------------------------------------------------------------------------
-- SAKAI_REALM_PROVIDER
-- provider id here is individual ids, where in the main table it may be
-- a single compound id
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_PROVIDER
(
       REALM_KEY            INTEGER NOT NULL,
       PROVIDER_ID          NVARCHAR (200) NOT NULL
)

ALTER TABLE SAKAI_REALM_PROVIDER
       ADD  CONSTRAINT SAKAI_REALM_PROVIDER_PK PRIMARY KEY (REALM_KEY, PROVIDER_ID)

CREATE INDEX FK_SAKAI_REALM_PROVIDER ON SAKAI_REALM_PROVIDER
(
       REALM_KEY                       ASC
)

CREATE INDEX IE_SAKAI_REALM_PROVIDER_ID ON SAKAI_REALM_PROVIDER
(
       PROVIDER_ID                       ASC
)
;
-----------------------------------------------------------------------------
-- SAKAI_REALM_RL_FN
-- role function
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_RL_FN
(
       REALM_KEY            INTEGER NOT NULL,
       ROLE_KEY             INTEGER NOT NULL,
       FUNCTION_KEY         INTEGER NOT NULL
)

ALTER TABLE SAKAI_REALM_RL_FN
       ADD  CONSTRAINT SAKAI_REALM_RL_FN_PK PRIMARY KEY (REALM_KEY, ROLE_KEY, FUNCTION_KEY)

CREATE INDEX FK_SAKAI_REALM_RL_FN_REALM ON SAKAI_REALM_RL_FN
(
       REALM_KEY                      ASC
)

CREATE INDEX FK_SAKAI_REALM_RL_FN_FUNC ON SAKAI_REALM_RL_FN
(
       FUNCTION_KEY                   ASC
)

CREATE INDEX FJ_SAKAI_REALM_RL_FN_ROLE ON SAKAI_REALM_RL_FN
(
       ROLE_KEY                       ASC
)
;
-----------------------------------------------------------------------------
-- SAKAI_REALM_RL_GR
-- role grant
-----------------------------------------------------------------------------

CREATE TABLE SAKAI_REALM_RL_GR
(
       REALM_KEY            INTEGER NOT NULL,
       USER_ID              NVARCHAR (99) NOT NULL,
       ROLE_KEY             INTEGER NOT NULL,
       ACTIVE               CHAR(1) NULL
                                   CHECK (ACTIVE IN (1, 0)),
       PROVIDED             CHAR(1) NULL
                                   CHECK (PROVIDED IN (1, 0))
)

ALTER TABLE SAKAI_REALM_RL_GR
       ADD  CONSTRAINT SAKAI_REALM_RL_GR_PK PRIMARY KEY (REALM_KEY, USER_ID)

CREATE INDEX FK_SAKAI_REALM_RL_GR_REALM ON SAKAI_REALM_RL_GR
(
       REALM_KEY                      ASC
)

CREATE INDEX FK_SAKAI_REALM_RL_GR_ROLE ON SAKAI_REALM_RL_GR
(
       ROLE_KEY                      ASC
)

CREATE INDEX IE_SAKAI_REALM_RL_GR_ACT ON SAKAI_REALM_RL_GR
(
       ACTIVE                       ASC
)

CREATE INDEX IE_SAKAI_REALM_RL_GR_USR ON SAKAI_REALM_RL_GR
(
       USER_ID                       ASC
)

CREATE INDEX IE_SAKAI_REALM_RL_GR_PRV ON SAKAI_REALM_RL_GR
(
       PROVIDED                       ASC
)

CREATE INDEX SAKAI_REALM_RL_GR_RAU ON SAKAI_REALM_RL_GR
(
	ROLE_KEY,
	ACTIVE,
	USER_ID
)
;
-----------------------------------------------------------------------------
-- FOREIGN KEYS
-----------------------------------------------------------------------------

ALTER TABLE SAKAI_REALM
       ADD  CONSTRAINT SAKAI_REALM_FK FOREIGN KEY (MAINTAIN_ROLE)
                             REFERENCES SAKAI_REALM_ROLE (ROLE_KEY)

ALTER TABLE SAKAI_REALM_PROPERTY
       ADD  CONSTRAINT SAKAI_REALM_PROPERTY_FK FOREIGN KEY (REALM_KEY)
                             REFERENCES SAKAI_REALM (REALM_KEY)

ALTER TABLE SAKAI_REALM_PROVIDER
       ADD  CONSTRAINT SAKAI_REALM_PROVIDER_FK FOREIGN KEY (REALM_KEY)
                             REFERENCES SAKAI_REALM (REALM_KEY)

ALTER TABLE SAKAI_REALM_RL_FN
       ADD  CONSTRAINT SAKAI_REALM_RL_FN_FK FOREIGN KEY (REALM_KEY)
                             REFERENCES SAKAI_REALM (REALM_KEY)

ALTER TABLE SAKAI_REALM_RL_FN
       ADD  CONSTRAINT SAKAI_REALM_RL_FN_FK2 FOREIGN KEY (ROLE_KEY)
                             REFERENCES SAKAI_REALM_ROLE (ROLE_KEY)

ALTER TABLE SAKAI_REALM_RL_FN
       ADD  CONSTRAINT SAKAI_REALM_RL_FN_FK3 FOREIGN KEY (FUNCTION_KEY)
                             REFERENCES SAKAI_REALM_FUNCTION (FUNCTION_KEY)

ALTER TABLE SAKAI_REALM_ROLE_DESC
       ADD  CONSTRAINT SAKAI_REALM_ROLE_DESC_FK FOREIGN KEY (REALM_KEY)
                             REFERENCES SAKAI_REALM (REALM_KEY)

ALTER TABLE SAKAI_REALM_ROLE_DESC
       ADD  CONSTRAINT SAKAI_REALM_ROLE_DESC_FK2 FOREIGN KEY (ROLE_KEY)
                             REFERENCES SAKAI_REALM_ROLE (ROLE_KEY)

ALTER TABLE SAKAI_REALM_RL_GR
       ADD  CONSTRAINT SAKAI_REALM_RL_GR_FK FOREIGN KEY (REALM_KEY)
                             REFERENCES SAKAI_REALM (REALM_KEY)

ALTER TABLE SAKAI_REALM_RL_GR
       ADD  CONSTRAINT SAKAI_REALM_RL_GR_FK2 FOREIGN KEY (ROLE_KEY)
                             REFERENCES SAKAI_REALM_ROLE (ROLE_KEY)
;


INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ '.anon')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ '.auth')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'access')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'admin')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Instructor')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'maintain')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'pubview')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Student')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Teaching Assistant')
-- rSmart CLE customizations
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Guest')
INSERT INTO SAKAI_REALM_ROLE VALUES (/* DEFAULT, */ 'Tech Support')
--
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.all.groups')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.read.drafts')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.revise.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'annc.revise.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.grade')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.revise')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.submit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'asn.all.groups')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.createAssessment')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.deleteAssessment.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.deleteAssessment.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.editAssessment.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.editAssessment.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.gradeAssessment.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.gradeAssessment.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.publishAssessment.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.publishAssessment.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.questionpool.copy.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.questionpool.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.questionpool.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.questionpool.edit.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.submitAssessmentForGrade')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.takeAssessment')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.template.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.template.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'assessment.template.edit.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.revise.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.revise.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.all.groups')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'chat.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'chat.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'chat.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'chat.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.revise.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.revise.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'content.all.groups')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.delete.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.new.topic')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.revise.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'disc.revise.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'dropbox.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'dropbox.maintain')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'gradebook.editAssignments')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'gradebook.gradeAll')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'gradebook.gradeSection')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'gradebook.viewOwnGrades')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'mail.delete.any')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'mail.new')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'mail.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.edit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.export')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.publish')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.suggest.global.publish')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'prefs.add')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'prefs.del')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'prefs.upd')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'realm.add')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'realm.del')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'realm.upd')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'realm.upd.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'section.role.instructor')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'section.role.student')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'section.role.ta')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.add')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.add.course')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.add.usersite')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.del')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.upd')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.upd.site.mbrshp')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.upd.grp.mbrshp')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.viewRoster')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.visit')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.visit.unp')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'site.viewRoster')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'user.add')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'user.upd.own')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'rwiki.admin')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'rwiki.create')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'rwiki.delete')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'rwiki.read')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'rwiki.superadmin')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'rwiki.update')
-- rSmart CLE customizations
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'jforum.admin')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'jforum.manage')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'jforum.member')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'melete.author')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'melete.student')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'metaobj.export')
INSERT INTO SAKAI_REALM_FUNCTION VALUES (/* DEFAULT, */ 'calendar.import')
--
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!site.helper', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

declare @realm_helper int, @role_maintain int, @function_realm_del int, @function_realm_upd int
declare @realm_site_user int, @role_access int, @realm_site_template int, @realm_user_template int, @realm_site_course_template int
declare @realm_group_template int, @realm_group_course_template int
declare @role_anon int, @role_auth int, @role_instructor int, @role_student int, @role_ta int, @role_guest int, @role_ts int

select @realm_helper=REALM_KEY from SAKAI_REALM where REALM_ID = '!site.helper'

select @role_maintain=ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'
select @role_access=ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'
select @role_anon=ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'
select @role_auth=ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'
select @role_instructor = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'
select @role_student = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'
select @role_ta = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'
select @role_guest = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Guest'
select @role_ts = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Tech Support'

select @function_realm_del=FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.del'
select @function_realm_upd=FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd'

declare @f1 int, @r_temp int, @role_temp int

INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_helper, @role_maintain, @function_realm_del)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_helper, @role_maintain, @function_realm_upd)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!site.user', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

select @realm_site_user=REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @function_realm_del)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @function_realm_upd)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_user, @role_maintain, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!user.template', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_user_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.usersite'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_user_template, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!user.template.guest', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template.guest'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.usersite'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!user.template.maintain', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template.maintain'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.course'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.usersite'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!user.template.registered', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template.registered'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.course'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.usersite'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!user.template.sample', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '!user.template.sample'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'prefs.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.course'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.add.usersite'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.add'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'user.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!site.template', '', @role_maintain, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_site_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.submitAssessmentForGrade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.createAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.copy.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.emailout'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @function_realm_del)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @function_realm_upd)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_template, @role_maintain, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!site.template.course', '', @role_instructor, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_site_course_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.createAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.copy.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.all.groups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.emailout'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @function_realm_del)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @function_realm_upd)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.submitAssessmentForGrade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.gradeAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeSection'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.ta'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!group.template', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_group_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_template, @role_maintain, @f1)

INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!group.template.course', '', @role_instructor, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @realm_group_course_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor,  @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_instructor, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeSection'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.ta'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_group_course_template, @role_ta, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/content/public/', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/content/public/'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/content/attachment/', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/content/attachment/'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/announcement/channel/!site/motd', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/announcement/channel/!site/motd'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!pubview', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '!pubview'
select @role_temp = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'pubview'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/site/!gateway', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/site/!gateway'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/site/!error', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/site/!error'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/site/!urlError', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/site/!urlError'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_anon, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_auth, @f1)
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/site/mercury', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/site/mercury'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.viewOwnGrades'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_access, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.submit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.new.topic'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'disc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.delete'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @function_realm_del)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @function_realm_upd)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit.unp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_maintain, @f1)
INSERT INTO SAKAI_REALM_RL_GR VALUES(@r_temp,'admin', @role_maintain, '1', '0')
select @role_temp = ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'admin'
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '/site/!admin', '', @role_temp, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
select @r_temp = REALM_KEY from SAKAI_REALM where REALM_ID = '/site/!admin'
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@r_temp, @role_temp, @f1)
INSERT INTO SAKAI_REALM_RL_GR VALUES(@r_temp,'admin', @role_temp, '1', '0')
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES(@realm_site_course_template, @role_instructor, 'Can read, revise, delete and add both content and participants to a site.')
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES(@realm_site_course_template, @role_student, 'Can read content, and add content to a site where appropriate.')
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES(@realm_site_course_template, @role_ta, 'Can read, add, and revise most content in their sections.')
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES(@realm_group_course_template, @role_instructor, 'Can read, revise, delete and add both content and participants to a site.')
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES(@realm_group_course_template, @role_student, 'Can read content, and add content to a site where appropriate.')
INSERT INTO SAKAI_REALM_ROLE_DESC VALUES(@realm_group_course_template, @role_ta, 'Can read, add, and revise most content in their sections.')

-- rSmart additions follow
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'jforum.manage'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeSection'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'melete.author'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.import'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_instructor, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'melete.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student,  @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'jforum.member'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_student, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.grade'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.revise'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'jforum.manage'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.editAssignments'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'gradebook.gradeAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'melete.author'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.createAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.deleteAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.any'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.editAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.any'
-- INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.publishAssessment.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.copy.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.questionpool.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.template.edit.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ta, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'jforum.member'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'melete.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_guest, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read.drafts'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'asn.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'jforum.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'melete.student'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.new'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.delete.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.any'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.revise.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'assessment.takeAssessment'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @function_realm_upd)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'realm.upd.own'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'section.role.instructor'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.del'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.site.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.upd.grp.mbrshp'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.viewRoster'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.edit'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.suggest.global.publish'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'metaobj.export'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.create'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.update'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.admin'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_site_course_template, @role_ts, @f1)




--portfolio templates
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!matrix.template.portfolio', '', @role_cig_coordinator, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

declare @realm_matrix_portfolio_template int, @realm_matrix_course_template int, @realm_matrix_project_template int
select @realm_matrix_portfolio_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'
select @realm_matrix_course_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'
select @realm_matrix_project_template=REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'


select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_portfolio_template, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_portfolio_template, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_portfolio_template, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_portfolio_template, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_portfolio_template, @role_cig_coordinator, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_portfolio_template, @role_cig_coordinator, @f1)


select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_portfolio_template, @role_reviewer, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_portfolio_template, @role_evaluator, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_portfolio_template, @role_cig_participant, @f1)


--course templates
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!matrix.template.course', '', @role_instructor, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_instructor, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_instructor, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_ta, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_ta, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_course_template, @role_student, @f1)



--project templates
INSERT INTO SAKAI_REALM VALUES (/* DEFAULT, */ '!matrix.template.project', '', @role_maintain, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_project_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_project_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_project_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_project_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_project_template, @role_maintain, @f1)
select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_project_template, @role_maintain, @f1)

select @f1 = FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'
INSERT INTO SAKAI_REALM_RL_FN VALUES(@realm_matrix_project_template, @role_access, @f1)
;
