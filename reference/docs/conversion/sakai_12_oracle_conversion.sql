-- SAK-30207
CREATE TABLE CONTENTREVIEW_ITEM (
    ID                  NUMBER(19) NOT NULL,
    VERSION             INTEGER NOT NULL,
    PROVIDERID          INTEGER NOT NULL,
    CONTENTID           VARCHAR2(255) NOT NULL,
    USERID              VARCHAR2(255),
    SITEID              VARCHAR2(255),
    TASKID              VARCHAR2(255),
    EXTERNALID          VARCHAR2(255),
    DATEQUEUED          TIMESTAMP NOT NULL,
    DATESUBMITTED       TIMESTAMP,
    DATEREPORTRECEIVED  TIMESTAMP,
    STATUS              NUMBER(19),
    REVIEWSCORE         INTEGER,
    LASTERROR           CLOB,
    RETRYCOUNT          NUMBER(19),
    NEXTRETRYTIME       TIMESTAMP NOT NULL,
    ERRORCODE           INTEGER,
    CONSTRAINT ID PRIMARY KEY (ID),
    CONSTRAINT PROVIDERID UNIQUE (PROVIDERID, CONTENTID)
);
-- END SAK-30207

-- ------------------------------
-- DASHBOARD                -----
-- ------------------------------

create table dash_availability_check 
( id number not null primary key, entity_ref varchar2(255) not null, 
entity_type_id varchar2(255) not null, scheduled_time timestamp(0) not null); 
create sequence dash_availability_check_seq start with 1 increment by 1 nomaxvalue; 
create unique index dash_avail_check_idx on dash_availability_check(entity_ref, scheduled_time); 
create index dash_avail_check_time_idx on dash_availability_check(scheduled_time);

create table dash_calendar_item 
( id number not null primary key, calendar_time timestamp(0) not null, calendar_time_label_key varchar2(40), 
title varchar2(255) not null, 
entity_ref varchar2(255) not null, entity_type number not null, subtype varchar2(255), context_id number not null, 
repeating_event_id number, sequence_num integer ); 
create sequence dash_calendar_item_seq start with 1 increment by 1 nomaxvalue; 
create index dash_cal_time_idx on dash_calendar_item (calendar_time); 
create unique index dash_cal_entity_label_idx on dash_calendar_item (entity_ref, calendar_time_label_key, sequence_num); 
create index dash_cal_entity_idx on dash_calendar_item (entity_ref);

create table dash_calendar_link 
( id number not null primary key, person_id number not null, context_id number not null, 
item_id number not null, hidden number(1,0) default 0, sticky number(1,0) default 0, 
unique (person_id, context_id, item_id) ); 
create sequence dash_calendar_link_seq start with 1 increment by 1 nomaxvalue; 
create index dash_calendar_link_idx on dash_calendar_link (person_id, context_id, item_id, hidden, sticky);
create index dash_calendar_link_item_id_idx on dash_calendar_link (item_id);

create table dash_config ( id number not null primary key, 
property_name varchar2(99) not null, property_value number(10,0) not null ); 
create sequence dash_config_seq start with 1 increment by 1 nomaxvalue;  
create unique index dash_config_name_idx on dash_config(property_name); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_DEFAULT_ITEMS_IN_PANEL', 5); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_DEFAULT_ITEMS_IN_DISCLOSURE', 20); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_DEFAULT_ITEMS_IN_GROUP', 2); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_NEWS_ITEMS_AFTER_WEEKS', 8); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_STARRED_NEWS_ITEMS_AFTER_WEEKS', 26); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_HIDDEN_NEWS_ITEMS_AFTER_WEEKS', 4); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_CALENDAR_ITEMS_AFTER_WEEKS', 2); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_STARRED_CALENDAR_ITEMS_AFTER_WEEKS', 26); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_HIDDEN_CALENDAR_ITEMS_AFTER_WEEKS', 1); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_NEWS_ITEMS_WITH_NO_LINKS', 1); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_REMOVE_CALENDAR_ITEMS_WITH_NO_LINKS', 1); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_DAYS_BETWEEN_HORIZ0N_UPDATES', 1); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_WEEKS_TO_HORIZON', 4); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_MOTD_MODE', 1); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_LOG_MODE_FOR_NAVIGATION_EVENTS', 2); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_LOG_MODE_FOR_ITEM_DETAIL_EVENTS', 2); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_LOG_MODE_FOR_PREFERENCE_EVENTS', 2); 
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_LOG_MODE_FOR_DASH_NAV_EVENTS', 2);
insert into dash_config (id, property_name, property_value) values (dash_config_seq.nextval, 'PROP_LOOP_TIMER_ENABLED', 0);

create table dash_context 
( id number not null primary key, context_id varchar2(255) not null, 
context_url varchar2(1024) not null, context_title varchar2(255) not null ); 
create sequence dash_context_seq start with 1 increment by 1 nomaxvalue; 
create unique index dash_context_idx on dash_context (context_id);

create table dash_event (event_id number not null primary key, event_date timestamp with time zone, 
event varchar2 (32), ref varchar2 (255), context varchar2 (255), session_id varchar2 (163), event_code varchar2 (1)); 
--create unique index dash_event_index on dash_event (event_id asc); 
create sequence dash_event_seq start with 1 increment by 1 nomaxvalue;

create table dash_news_item ( id number not null primary key, 
news_time timestamp(0) not null, news_time_label_key varchar2(40), title varchar2(255) not null, 
entity_ref varchar2(255) not null, 
entity_type number not null, subtype varchar2(255), context_id number not null, grouping_id varchar2(90) ); 
create sequence dash_news_item_seq start with 1 increment by 1 nomaxvalue; 
create index dash_news_time_idx on dash_news_item (news_time); 
create index dash_news_grouping_idx on dash_news_item (grouping_id); 
create unique index dash_news_entity_idx on dash_news_item (entity_ref);

create table dash_news_link 
( id number not null primary key, person_id number not null, context_id number not null, 
item_id number not null, hidden number(1,0) default 0, sticky number(1,0) default 0, unique (person_id, context_id, item_id) ); 
create sequence dash_news_link_seq start with 1 increment by 1 nomaxvalue; 
create index dash_news_link_idx on dash_news_link (person_id, context_id, item_id, hidden, sticky);
create index dash_news_link_item_id_idx on dash_news_link (item_id);

create table dash_person 
( id number not null primary key,user_id varchar2(99) not null, sakai_id varchar2(99) ); 
create sequence dash_person_seq start with 1 increment by 1 nomaxvalue; 
create unique index dash_person_user_id_idx on dash_person (user_id); 
create unique index dash_person_sakai_id_idx on dash_person (sakai_id);

create table dash_repeating_event (id number not null primary key, 
first_time timestamp(0) not null, last_time timestamp(0), frequency varchar2(40) not null, max_count integer, 
calendar_time_label_key varchar2(40), title varchar2(255) not null, 
entity_ref varchar2(255) not null, subtype varchar2(255), entity_type number not null, context_id number not null ); 
create sequence dash_repeating_event_seq start with 1 increment by 1 nomaxvalue; 
create index dash_repeating_event_first_idx on dash_repeating_event (first_time); 
create index dash_repeating_event_last_idx on dash_repeating_event (last_time);

create table dash_sourcetype 
( id number not null primary key, identifier varchar2(255) not null ); 
create sequence dash_sourcetype_seq start with 1 increment by 1 nomaxvalue; 
create unique index dash_source_idx on dash_sourcetype (identifier);

create table dash_task_lock
( id number not null primary key, 
task varchar2(255) not null, 
server_id varchar2(255) not null, 
claim_time timestamp(9), 
last_update timestamp(9), 
has_lock number(1,0) default 0); 
create sequence dash_task_lock_seq start with 1 increment by 1 nomaxvalue; 
create index dash_lock_ct_idx on dash_task_lock (claim_time); 
create unique index dash_lock_ts_idx on dash_task_lock (task, server_id);

--
-- SAK-27929 Add Dashboard to default !user site
--

INSERT INTO SAKAI_SITE_PAGE VALUES('!user-99', '!user', 'Dashboard', '0', 0, '0' );
INSERT INTO SAKAI_SITE_TOOL VALUES('!user-999', '!user-99', '!user', 'sakai.dashboard', 1, 'Dashboard', NULL );

--
-- SAK-31641 Switch from INTs to VARCHARs in Oauth
--
ALTER TABLE oauth_accessors
MODIFY (
  status VARCHAR2(255)
, type VARCHAR2(255)
);

UPDATE oauth_accessors SET status = CASE
  WHEN status = 0 THEN 'VALID'
  WHEN status = 1 THEN 'REVOKED'
  WHEN status = 2 THEN 'EXPIRED'
END;

UPDATE oauth_accessors SET type = CASE
  WHEN type = 0 THEN 'REQUEST'
  WHEN type = 1 THEN 'REQUEST_AUTHORISING'
  WHEN type = 2 THEN 'REQUEST_AUTHORISED'
  WHEN type = 3 THEN 'ACCESS'
END;

--
-- SAK-31636 Rename existing 'Home' tools
--

update sakai_site_page set title = 'Overview' where title = 'Home';

--
-- SAK-31563
--

-- Add new user_id columns and their corresponding indexes
ALTER TABLE pasystem_popup_assign ADD user_id varchar2(99);
ALTER TABLE pasystem_popup_dismissed ADD user_id varchar2(99);
ALTER TABLE pasystem_banner_dismissed ADD user_id varchar2(99);

CREATE INDEX popup_assign_lower_user_id on pasystem_popup_assign (user_id);
CREATE INDEX popup_dismissed_lower_user_id on pasystem_popup_dismissed (user_id);
CREATE INDEX banner_dismissed_user_id on pasystem_banner_dismissed (user_id);

-- Map existing EIDs to their corresponding user IDs
update pasystem_popup_assign popup set user_id = (select user_id from sakai_user_id_map map where popup.user_eid = map.eid);
update pasystem_popup_dismissed popup set user_id = (select user_id from sakai_user_id_map map where popup.user_eid = map.eid);
update pasystem_banner_dismissed banner set user_id = (select user_id from sakai_user_id_map map where banner.user_eid = map.eid);

-- Any rows that couldn't be mapped are dropped (there shouldn't
-- really be any, but if there are those users were already being
-- ignored when identified by EID)
DELETE FROM pasystem_popup_assign WHERE user_id is null;
DELETE FROM pasystem_popup_dismissed WHERE user_id is null;
DELETE FROM pasystem_banner_dismissed WHERE user_id is null;

-- Enforce NULL checks on the new columns
ALTER TABLE pasystem_popup_assign MODIFY (user_id NOT NULL);
ALTER TABLE pasystem_popup_dismissed MODIFY (user_id NOT NULL);
ALTER TABLE pasystem_banner_dismissed MODIFY (user_id NOT NULL);

-- Reintroduce unique constraints for the new column
ALTER TABLE pasystem_popup_dismissed drop CONSTRAINT popup_dismissed_unique;
ALTER TABLE pasystem_popup_dismissed add CONSTRAINT popup_dismissed_unique UNIQUE (user_id, state, uuid);

ALTER TABLE pasystem_banner_dismissed drop CONSTRAINT banner_dismissed_unique;
ALTER TABLE pasystem_banner_dismissed add CONSTRAINT banner_dismissed_unique UNIQUE (user_id, state, uuid);

-- Drop the old columns
ALTER TABLE pasystem_popup_assign DROP COLUMN user_eid;
ALTER TABLE pasystem_popup_dismissed DROP COLUMN user_eid;
ALTER TABLE pasystem_banner_dismissed DROP COLUMN user_eid;

--
-- SAK-31840 drop defaults as its now managed in the POJO
--
ALTER TABLE GB_GRADABLE_OBJECT_T MODIFY IS_EXTRA_CREDIT DEFAULT NULL;
ALTER TABLE GB_GRADABLE_OBJECT_T MODIFY HIDE_IN_ALL_GRADES_TABLE DEFAULT NULL;

-- BEGIN SAK-31819 Remove the old ScheduledInvocationManager job as it's not present in Sakai 12.
DELETE FROM QRTZ_SIMPLE_TRIGGERS WHERE TRIGGER_NAME='org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner';
DELETE FROM QRTZ_TRIGGERS WHERE TRIGGER_NAME='org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner';
-- This one is the actual job that the triggers were trying to run
DELETE FROM QRTZ_JOB_DETAILS WHERE JOB_NAME='org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner';
-- END SAK-31819

-- BEGIN SAK-15708 avoid duplicate rows
CREATE TABLE SAKAI_POSTEM_STUDENT_DUPES (
  id number not null,
  username varchar2(99),
  surrogate_key number
);
INSERT INTO SAKAI_POSTEM_STUDENT_DUPES SELECT MAX(id), username, surrogate_key FROM SAKAI_POSTEM_STUDENT GROUP BY username, surrogate_key HAVING count(id) > 1;
DELETE FROM SAKAI_POSTEM_STUDENT_GRADES WHERE student_id IN (SELECT id FROM SAKAI_POSTEM_STUDENT_DUPES);
DELETE FROM SAKAI_POSTEM_STUDENT WHERE id IN (SELECT id FROM SAKAI_POSTEM_STUDENT_DUPES);
DROP TABLE SAKAI_POSTEM_STUDENT_DUPES;

DROP INDEX POSTEM_STUDENT_USERNAME_I;
ALTER TABLE SAKAI_POSTEM_STUDENT MODIFY ( "USERNAME" VARCHAR2(99 CHAR) ) ;
CREATE UNIQUE INDEX POSTEM_USERNAME_SURROGATE ON SAKAI_POSTEM_STUDENT ("USERNAME" ASC, "SURROGATE_KEY" ASC);
-- END SAK-15708

-- #3431 Add final grade mode setting
ALTER TABLE gb_gradebook_t ADD final_grade_mode NUMBER(1,0) DEFAULT '0' NOT NULL;

-- BEGIN 3432 Grade Points Grading Scale
-- add the new grading scale
INSERT INTO gb_grading_scale_t (id, object_type_id, version, scale_uid, name, unavailable)
VALUES (gb_grading_scale_s.nextval, 0, 0, 'GradePointsMapping', 'Grade Points', 0);

-- add the grade ordering
INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'A (4.0)', 0);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'A- (3.67)', 1);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'B+ (3.33)', 2);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'B (3.0)', 3);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'B- (2.67)', 4);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'C+ (2.33)', 5);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'C (2.0)', 6);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'C- (1.67)', 7);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'D (1.0)', 8);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'F (0)', 9);

-- add the percent mapping
INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 100, 'A (4.0)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 90, 'A- (3.67)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 87, 'B+ (3.33)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 83, 'B (3.0)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 80, 'B- (2.67)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 77, 'C+ (2.33)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 73, 'C (2.0)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 70, 'C- (1.67)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 67, 'D (1.0)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 0, 'F (0)');

-- add the new scale to all existing gradebook sites
INSERT INTO gb_grade_map_t (id, object_type_id, version, gradebook_id, gb_grading_scale_t)
SELECT 
  gb_grade_mapping_s.nextval
, 0
, 0
, gb.id
, gs.id
FROM gb_gradebook_t gb
JOIN gb_grading_scale_t gs
  ON gs.scale_uid = 'GradePointsMapping';
-- END 3432

-- SAM-1129 Change the column DESCRIPTION of SAM_QUESTIONPOOL_T from VARCHAR2(255) to CLOB

ALTER TABLE SAM_QUESTIONPOOL_T ADD DESCRIPTION_COPY VARCHAR2(255);
UPDATE SAM_QUESTIONPOOL_T SET DESCRIPTION_COPY = DESCRIPTION;

UPDATE SAM_QUESTIONPOOL_T SET DESCRIPTION = NULL;
ALTER TABLE SAM_QUESTIONPOOL_T MODIFY DESCRIPTION LONG;
ALTER TABLE SAM_QUESTIONPOOL_T MODIFY DESCRIPTION CLOB;
UPDATE SAM_QUESTIONPOOL_T SET DESCRIPTION = DESCRIPTION_COPY;

ALTER TABLE SAM_QUESTIONPOOL_T DROP COLUMN DESCRIPTION_COPY;
