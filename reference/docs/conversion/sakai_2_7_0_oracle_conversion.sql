-- This is the Oracle Sakai 2.6.x -> 2.7.0 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.6.x to 2.7.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- --------------------------------------------------------------------------------------------------------------------------------------

-- SAK-16610 introduced a new osp presentation review permission
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.presentation.review');

-- SAK-16686/KNL-241 Support exceptions to dynamic page localization
INSERT INTO SAKAI_SITE_PAGE_PROPERTY VALUES ('~admin','~admin-400','sitePage.customTitle','true');

-- SAK-16832
ALTER TABLE SAM_PUBLISHEDASSESSMENT_T ADD LASTNEEDRESUBMITDATE DATE NULL;

-- SAK-16880 collaborative portfolio editing
ALTER TABLE osp_presentation ADD isCollab number(1,0) DEFAULT 0 NOT NULL;

-- SAK-17447
alter table EMAIL_TEMPLATE_ITEM add HTMLMESSAGE clob; 

-- SAK-16984 new column in sakai-Person
alter TABLE SAKAI_PERSON_T add NORMALIZEDMOBILE varchar2(255) NULL;

-- SAK-15165 new fields for SakaiPerson
alter table SAKAI_PERSON_T add FAVOURITE_BOOKS varchar2(4000); 
alter table SAKAI_PERSON_T add FAVOURITE_TV_SHOWS varchar2(4000); 
alter table SAKAI_PERSON_T add FAVOURITE_MOVIES varchar2(4000); 
alter table SAKAI_PERSON_T add FAVOURITE_QUOTES varchar2(4000); 
alter table SAKAI_PERSON_T add EDUCATION_COURSE varchar2(4000); 
alter table SAKAI_PERSON_T add EDUCATION_SUBJECTS varchar2(4000);

-- SAK-17485/SAK-10559
alter table MFR_MESSAGE_T add NUM_READERS int;
update MFR_MESSAGE_T set NUM_READERS = 0; 


-- SAK-15710

ALTER TABLE osp_wizard_page_def 
  ADD (defaultCustomForm number(1,0), defaultReflectionForm number(1,0), defaultFeedbackForm number(1,0), 
  defaultReviewers number(1,0), defaultEvaluationForm number(1,0), defaultEvaluators number(1,0));
UPDATE osp_wizard_page_def 
  SET defaultCustomForm = 0, defaultReflectionForm = 0, defaultFeedbackForm = 0, 
  defaultReviewers = 0, defaultEvaluationForm = 0, defaultEvaluators = 0;

ALTER TABLE osp_scaffolding ADD (allowRequestFeedback number(1,0));
UPDATE osp_scaffolding SET allowRequestFeedback = 0;

ALTER TABLE osp_scaffolding ADD (hideEvaluations number(1,0));
UPDATE osp_scaffolding SET hideEvaluations = 0;

ALTER TABLE osp_wizard_page_def ADD (allowRequestFeedback number(1,0));
UPDATE osp_wizard_page_def SET allowRequestFeedback = 0;

ALTER TABLE osp_wizard_page_def ADD (hideEvaluations number(1,0));
UPDATE osp_wizard_page_def SET hideEvaluations = 0;

ALTER TABLE osp_scaffolding Drop column reviewerGroupAccess;

ALTER TABLE osp_scaffolding ADD (defaultFormsMatrixVersion number(1,0));
UPDATE osp_scaffolding SET defaultFormsMatrixVersion = 1;

alter table osp_scaffolding add (returnedColor varchar2(7), modifiedDate timestamp);
update osp_scaffolding set returnedColor = '';

create table osp_scaffolding_attachments (
        id varchar2(36) not null,
        artifact_id varchar2(255),
        seq_num number(10,0) not null,
        primary key (id, seq_num)
    );

create table osp_scaffolding_form_defs (
        id varchar2(36) not null,
        form_def_id varchar2(255),
        seq_num number(10,0) not null,
        primary key (id, seq_num)
    );
    
create table SITEASSOC_CONTEXT_ASSOCIATION (
		FROM_CONTEXT varchar2(99 char) not null, 
		TO_CONTEXT varchar2(99 char) not null, 
		VERSION number(10,0) not null, 
		primary key (FROM_CONTEXT, TO_CONTEXT)
	);

alter table osp_wizard_page_def add (type varchar2(1) default '0');

update osp_wizard_page_def set type = '0' where id in (
select distinct s.wiz_page_def_id From osp_scaffolding_cell s );

update osp_wizard_page_def set type = '1' where id in (
select distinct wps.wiz_page_def_id
From osp_wizard w
join osp_wizard_category wc on wc.wizard_id = w.id
join osp_wizard_page_sequence wps on wps.category_id = wc.id
where w.wizard_type = 'org.theospi.portfolio.wizard.model.Wizard.hierarchical'
);

update osp_wizard_page_def set type = '2' where id in (
select distinct wps.wiz_page_def_id 
From osp_wizard w
join osp_wizard_category wc on wc.wizard_id = w.id
join osp_wizard_page_sequence wps on wps.category_id = wc.id
where w.wizard_type = 'org.theospi.portfolio.wizard.model.Wizard.sequential'
);

-- since scaffolding are now extending osp_workflow_parent
insert into OSP_WORKFLOW_PARENT select s.id, null, null, null, null, null, null from osp_scaffolding s where s.id not in (select wp.id from osp_workflow_parent wp);

-- Move the use permission from site to each newly created scaffolding realms and delete the old osp.matrix.scaffolding.use permissions --

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.revise.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.revise.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.delete.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.delete.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.publish.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.publish.own');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.export.any');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffolding.export.own');

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.accessAll');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.viewEvalOther');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.viewFeedbackOther');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.accessUserList');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.viewAllGroups');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.matrix.scaffoldingSpecific.use');

INSERT INTO SAKAI_REALM VALUES (SAKAI_REALM_SEQ.NEXTVAL, '!matrix.template.portfolio', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));


INSERT INTO SAKAI_REALM VALUES (SAKAI_REALM_SEQ.NEXTVAL, '!matrix.template.course', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));


INSERT INTO SAKAI_REALM VALUES (SAKAI_REALM_SEQ.NEXTVAL, '!matrix.template.project', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessAll'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewEvalOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewFeedbackOther'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.manageStatus'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.accessUserList'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.viewAllGroups'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!matrix.template.project'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use'));


INSERT INTO SAKAI_REALM (REALM_KEY, REALM_ID, PROVIDER_ID, MAINTAIN_ROLE, CREATEDBY, MODIFIEDBY, CREATEDON, MODIFIEDON) 
(select SAKAI_REALM_SEQ.NEXTVAL, concat('/scaffolding/', concat(worksiteId, concat('/', id))) as new_realm_id, '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP from osp_scaffolding);


insert into SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select distinct sr.REALM_KEY, srrf.ROLE_KEY, (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffoldingSpecific.use')
from sakai_realm sr, osp_scaffolding os, SAKAI_REALM_RL_FN srrf 
where sr.REALM_ID = concat('/scaffolding/', concat(os.WORKSITEID, concat('/', os.id)))  
and srrf.FUNCTION_KEY = (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.scaffolding.use')
and srrf.REALM_KEY = (select REALM_KEY from SAKAI_REALM Where REALM_ID = concat('/site/', os.worksiteid));

-- delete from SAKAI_REALM_RL_FN where function_key = (select function_key From SAKAI_REALM_FUNCTION where function_name = 'osp.matrix.scaffolding.use');
-- delete From SAKAI_REALM_FUNCTION where function_name = 'osp.matrix.scaffolding.use';


create table permissions_backfill_src_temp (function_name varchar2(99), TYPE INTEGER);
CREATE TABLE permissions_backfill_temp (FUNCTION_KEY INTEGER, TYPE INTEGER);

INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.accessAll', 1);
INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.viewEvalOther', 1);
INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.viewFeedbackOther', 1);
INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.accessUserList', 1);
INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.accessAll', 2);
INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.viewFeedbackOther', 2);
INSERT INTO permissions_backfill_src_temp values ('osp.matrix.scaffoldingSpecific.accessUserList', 2);

insert into permissions_backfill_temp
select rf.function_key, pbst.type 
from SAKAI_REALM_FUNCTION rf
join permissions_backfill_src_temp pbst on (pbst.function_name = rf.FUNCTION_NAME);


insert into SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select distinct sr.REALM_KEY, srrf.ROLE_KEY, pbt.FUNCTION_KEY
from sakai_realm sr, osp_scaffolding os, SAKAI_REALM_RL_FN srrf, permissions_backfill_temp pbt
where sr.REALM_ID = concat('/scaffolding/', concat(os.WORKSITEID, concat('/', os.id))) 
and srrf.FUNCTION_KEY = (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.evaluate')
and srrf.REALM_KEY = (select REALM_KEY from SAKAI_REALM Where REALM_ID = concat('/site/', os.worksiteid))
and pbt.TYPE = 1
and not exists (select 1 from SAKAI_REALM_RL_FN rrf_tmp where rrf_tmp.REALM_KEY = sr.REALM_KEY and rrf_tmp.ROLE_KEY = srrf.ROLE_KEY and rrf_tmp.FUNCTION_KEY = pbt.FUNCTION_KEY);

insert into SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select distinct sr.REALM_KEY, srrf.ROLE_KEY, pbt.FUNCTION_KEY
from sakai_realm sr, osp_scaffolding os, SAKAI_REALM_RL_FN srrf, permissions_backfill_temp pbt
where sr.REALM_ID = concat('/scaffolding/', concat(os.WORKSITEID, concat('/', os.id))) 
and srrf.FUNCTION_KEY = (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'osp.matrix.review')
and srrf.REALM_KEY = (select REALM_KEY from SAKAI_REALM Where REALM_ID = concat('/site/', os.worksiteid))
and pbt.TYPE = 2
and not exists (select 1 from SAKAI_REALM_RL_FN rrf_tmp where rrf_tmp.REALM_KEY = sr.REALM_KEY and rrf_tmp.ROLE_KEY = srrf.ROLE_KEY and rrf_tmp.FUNCTION_KEY = pbt.FUNCTION_KEY);

drop table permissions_backfill_src_temp;
drop table permissions_backfill_temp;

-- --- END ------

-- Backfill sites
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);

INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','osp.matrix.scaffoldingSpecific.viewAllGroups');

INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','osp.matrix.scaffoldingSpecific.viewAllGroups');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Project Owner','osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Project Owner','osp.matrix.scaffoldingSpecific.viewAllGroups');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','osp.matrix.scaffoldingSpecific.viewAllGroups');

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','osp.matrix.scaffoldingSpecific.viewAllGroups');

INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','osp.matrix.scaffoldingSpecific.manageStatus');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','osp.matrix.scaffoldingSpecific.viewAllGroups');


-- Lookup the role and function keys
insert into PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
select SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
join SAKAI_REALM_ROLE SRR on (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
join SAKAI_REALM_FUNCTION SRF on (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);


-- Insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper" or any group realms)
insert into SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
from
    (select distinct SRRF.REALM_KEY, SRRF.ROLE_KEY from SAKAI_REALM_RL_FN SRRF) SRRFD
    join PERMISSIONS_TEMP TMP on (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    join SAKAI_REALM SR on (SRRFD.REALM_KEY = SR.REALM_KEY)
    where SR.REALM_ID like '/scaffolding/%'
   and not exists (
        select 1
            from SAKAI_REALM_RL_FN SRRFI
            where SRRFI.REALM_KEY=SRRFD.REALM_KEY and SRRFI.ROLE_KEY=SRRFD.ROLE_KEY and  SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );


-- clean up the temp tables to use again for group permissions
drop table PERMISSIONS_TEMP;
drop table PERMISSIONS_SRC_TEMP;


CREATE TABLE permissions_convertl_temp (OLD_FUNCTION_KEY INTEGER, OLD_FUNCTION_NAME VARCHAR2(99), FUNCTION_KEY INTEGER, FUNCTION_NAME VARCHAR2(99));

INSERT INTO permissions_convertl_temp
select rf.FUNCTION_KEY, rf.FUNCTION_NAME, rf2.FUNCTION_KEY, rf2.FUNCTION_NAME 
from SAKAI_REALM_FUNCTION rf, SAKAI_REALM_FUNCTION rf2 
where rf.FUNCTION_NAME = 'osp.matrix.scaffolding.edit' and (rf2.function_name = 'osp.matrix.scaffolding.revise.any' or rf2.function_name = 'osp.matrix.scaffolding.revise.own');

INSERT INTO permissions_convertl_temp
select rf.FUNCTION_KEY, rf.FUNCTION_NAME, rf2.FUNCTION_KEY, rf2.FUNCTION_NAME 
from SAKAI_REALM_FUNCTION rf, SAKAI_REALM_FUNCTION rf2 
where rf.FUNCTION_NAME = 'osp.matrix.scaffolding.delete' and (rf2.function_name = 'osp.matrix.scaffolding.delete.any' or rf2.function_name = 'osp.matrix.scaffolding.delete.own');

INSERT INTO permissions_convertl_temp
select rf.FUNCTION_KEY, rf.FUNCTION_NAME, rf2.FUNCTION_KEY, rf2.FUNCTION_NAME 
from SAKAI_REALM_FUNCTION rf, SAKAI_REALM_FUNCTION rf2 
where rf.FUNCTION_NAME = 'osp.matrix.scaffolding.export' and (rf2.function_name = 'osp.matrix.scaffolding.export.any' or rf2.function_name = 'osp.matrix.scaffolding.export.own');

INSERT INTO permissions_convertl_temp
select rf.FUNCTION_KEY, rf.FUNCTION_NAME, rf2.FUNCTION_KEY, rf2.FUNCTION_NAME 
from SAKAI_REALM_FUNCTION rf, SAKAI_REALM_FUNCTION rf2 
where rf.FUNCTION_NAME = 'osp.matrix.scaffolding.publish' and (rf2.function_name = 'osp.matrix.scaffolding.publish.any' or rf2.function_name = 'osp.matrix.scaffolding.publish.own');



insert into SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select distinct srrf.REALM_KEY, srrf.ROLE_KEY, pct.function_key
from SAKAI_REALM_RL_FN srrf
join permissions_convertl_temp pct on (srrf.FUNCTION_KEY = pct.old_function_key);

drop table permissions_convertl_temp;

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'osp.portfolio.evaluation.use');

update SAKAI_REALM_RL_FN set FUNCTION_KEY = (select function_key from SAKAI_REALM_FUNCTION where function_name = 'osp.portfolio.evaluation.use')
where function_key = (select function_key From SAKAI_REALM_FUNCTION where function_name = 'osp.matrix.evaluate');


-- ****** backfill for new returned status
create table tmp_workflow_guid_map (old_id varchar2(99), new_id varchar2(99), 
  parent_id varchar2(99), old_status varchar2(99), new_status varchar2(99));

insert into tmp_workflow_guid_map select id, sys_guid(), parent_id, 'READY', 'RETURNED' from OSP_WORKFLOW where title = 'Return Workflow';

delete From tmp_workflow_guid_map where PARENT_ID in (
select distinct parent_id from osp_workflow where title = 'Returned Workflow');

insert into osp_workflow
select NEW_ID, 'Returned Workflow', parent_id from tmp_workflow_guid_map;

insert into osp_workflow_item
select sys_guid(), owi.actiontype, owi.action_object_id, t.new_status, t.new_id 
from osp_workflow_item owi
join tmp_workflow_guid_map t on (t.old_id = owi.WORKFLOW_ID and t.old_status = owi.ACTION_VALUE);

insert into osp_workflow_item
select sys_guid(), owi.actiontype, owi.action_object_id, owi.action_value, t.new_id 
from osp_workflow_item owi
join tmp_workflow_guid_map t on (t.old_id = owi.WORKFLOW_ID)
where t.old_status <> owi.ACTION_VALUE;

drop table tmp_workflow_guid_map;
-- ****** end backfill for returned status

-- END SAK-15710

-- SAK-16835 columns for new quartz version
alter table QRTZ_TRIGGERS add PRIORITY number(2);
alter table QRTZ_FIRED_TRIGGERS add PRIORITY number(2);