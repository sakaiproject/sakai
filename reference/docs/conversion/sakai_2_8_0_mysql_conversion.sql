-- SAK-16835 columns for new quartz version
alter table QRTZ_TRIGGERS add column PRIORITY int;
alter table QRTZ_FIRED_TRIGGERS add column PRIORITY int;

-- SAK-17821 Add additional fields to SakaiPerson
alter table SAKAI_PERSON_T add STAFF_PROFILE text;
alter table SAKAI_PERSON_T add UNIVERSITY_PROFILE_URL text;
alter table SAKAI_PERSON_T add ACADEMIC_PROFILE_URL text;
alter table SAKAI_PERSON_T add PUBLICATIONS text;
alter table SAKAI_PERSON_T add BUSINESS_BIOGRAPHY text;

-- Samigo
-- SAM-666
alter table SAM_ASSESSFEEDBACK_T add FEEDBACKCOMPONENTOPTION int(11) default null;
update SAM_ASSESSFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2;
alter table SAM_PUBLISHEDFEEDBACK_T add FEEDBACKCOMPONENTOPTION int(11) default null;
update SAM_PUBLISHEDFEEDBACK_T set FEEDBACKCOMPONENTOPTION = 2; 

-- SAM-971
alter table SAM_ASSESSMENTGRADING_T add LASTVISITEDPART integer default null;
alter table SAM_ASSESSMENTGRADING_T add LASTVISITEDQUESTION integer default null;

-- Gradebook2 support
-- SAK-19080 / GRBK-736
alter table GB_GRADE_RECORD_T add USER_ENTERED_GRADE varchar(127);


--MSGCNTR-309
--Start and End dates on Forums and Topics

alter table MFR_AREA_T add (AVAILABILITY_RESTRICTED bit);
update MFR_AREA_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_AREA_T modify (AVAILABILITY_RESTRICTED bit NOT NULL DEFAULT '');

alter table MFR_AREA_T add (AVAILABILITY bit);
update MFR_AREA_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_AREA_T modify (AVAILABILITY NUMBER(1,0) bit NOT NULL DEFAULT ''));

alter table MFR_AREA_T add (OPEN_DATE datetime);

alter table MFR_AREA_T add (CLOSE_DATE datetime);


alter table MFR_OPEN_FORUM_T add (AVAILABILITY_RESTRICTED bit);
update MFR_OPEN_FORUM_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY_RESTRICTED bit NOT NULL DEFAULT ''));

alter table MFR_OPEN_FORUM_T add (AVAILABILITY bit);
update MFR_OPEN_FORUM_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_OPEN_FORUM_T modify (AVAILABILITY bit NOT NULL DEFAULT ''));

alter table MFR_OPEN_FORUM_T add (OPEN_DATE datetime);

alter table MFR_OPEN_FORUM_T add (CLOSE_DATE datetime);

alter table MFR_TOPIC_T add (AVAILABILITY_RESTRICTED bit);
update MFR_TOPIC_T set AVAILABILITY_RESTRICTED=0 where AVAILABILITY_RESTRICTED is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY_RESTRICTED bit NOT NULL DEFAULT ''));

alter table MFR_TOPIC_T add (AVAILABILITY bit);
update MFR_TOPIC_T set AVAILABILITY=0 where AVAILABILITY is NULL;
alter table MFR_TOPIC_T modify (AVAILABILITY bit NOT NULL DEFAULT ''));

alter table MFR_TOPIC_T add (OPEN_DATE datetime null);

alter table MFR_TOPIC_T add (CLOSE_DATE datetime null);


--MSGCNTR-355
insert into MFR_TOPIC_T (UUID, MODERATED, AUTO_MARK_THREADS_READ, SORT_INDEX, MUTABLE, TOPIC_DTYPE, VERSION, CREATED, CREATED_BY, MODIFIED, MODIFIED_BY, TITLE, SHORT_DESCRIPTION, EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID)

    select UUID, MODERATED, AUTO_MARK_THREADS_READ, 3 as SORT_INDEX, 0 as MUTABLE, TOPIC_DTYPE, VERSION, CREATED, CREATED_BY, MODIFIED, MODIFIED_BY, TITLE, SHORT_DESCRIPTION, EXTENDED_DESCRIPTION, TYPE_UUID, pf_surrogateKey, USER_ID from (
                    select count(*) as c1, uuid() as UUID, mtt.MODERATED, mtt.AUTO_MARK_THREADS_READ, mtt.TOPIC_DTYPE, 0 as VERSION, mtt.CREATED, mtt.CREATED_BY, mtt.MODIFIED, mtt.MODIFIED_BY, 'pvt_drafts' as TITLE, 'short-desc' as SHORT_DESCRIPTION, 'ext-desc' as EXTENDED_DESCRIPTION, mtt.TYPE_UUID, mtt.pf_surrogateKey, mtt.USER_ID
                    from MFR_PRIVATE_FORUM_T mpft, MFR_TOPIC_T mtt
                    where mpft.ID = mtt.pf_surrogateKey and mpft.TYPE_UUID = mtt.TYPE_UUID
                    Group By mtt.USER_ID, mtt.pf_surrogateKey) s1
    where s1.c1 = 3;

--MSGCNTR-360
--Hibernate could have missed this index, if this fails, then the index may already be in the table
CREATE INDEX user_type_context_idx ON MFR_PVT_MSG_USR_T ( USER_ID(36), TYPE_UUID(36), CONTEXT_ID(36), READ_STATUS);


-- New column for Email Template service
-- SAK-18532/SAK-19522
alter table EMAIL_TEMPLATE_ITEM add column EMAILFROM text;