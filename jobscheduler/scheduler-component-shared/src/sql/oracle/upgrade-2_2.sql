--
-- drop tables that are no longer used
--
drop table qrtz_job_listeners;
drop table qrtz_trigger_listeners;

--
-- drop columns that are no longer used
--
alter table qrtz_job_details drop column is_volatile;
alter table qrtz_triggers drop column is_volatile;
alter table qrtz_fired_triggers drop column is_volatile;

--
-- add new columns that replace the 'is_stateful' column
--
alter table qrtz_job_details add is_nonconcurrent VARCHAR2(1);
alter table qrtz_job_details add is_update_data VARCHAR2(1);
update qrtz_job_details set is_nonconcurrent = is_stateful;
update qrtz_job_details set is_update_data = is_stateful;
alter table qrtz_job_details drop column is_stateful;
alter table qrtz_fired_triggers add is_nonconcurrent VARCHAR2(1);
alter table qrtz_fired_triggers add is_update_data VARCHAR2(1);
update qrtz_fired_triggers set is_nonconcurrent = is_stateful;
update qrtz_fired_triggers set is_update_data = is_stateful;
alter table qrtz_fired_triggers drop column is_stateful;

--
-- add new 'sched_name' column to all tables
--
alter table qrtz_blob_triggers add sched_name varchar2(120) DEFAULT 'QuartzScheduler' NOT NULL;
alter table qrtz_calendars add sched_name varchar2(120) DEFAULT 'QuartzScheduler' NOT NULL;
alter table qrtz_cron_triggers add sched_name varchar2(120) DEFAULT 'QuartzScheduler' NOT NULL;
alter table qrtz_fired_triggers add sched_name varchar2(120) DEFAULT 'QuartzScheduler' NOT NULL;
alter table qrtz_job_details add sched_name varchar2(120) DEFAULT 'QuartzScheduler' NOT NULL;
alter table qrtz_locks add sched_name varchar2(120) DEFAULT 'QuartzScheduler' NOT NULL;
alter table qrtz_paused_trigger_grps add sched_name varchar2(120) DEFAULT 'QuartzScheduler' NOT NULL;
alter table qrtz_scheduler_state add sched_name varchar2(120) DEFAULT 'QuartzScheduler' NOT NULL;
alter table qrtz_simple_triggers add sched_name varchar2(120) DEFAULT 'QuartzScheduler' NOT NULL;
alter table qrtz_triggers add sched_name varchar2(120) DEFAULT 'QuartzScheduler' NOT NULL;

--
-- add new 'sched_time' column to all tables
--
alter table qrtz_fired_triggers add sched_time NUMBER(13) NOT NULL;

--
-- drop all foreign key constraints, so that we can define new ones
--

-- alter table qrtz_triggers drop constraint qrtz_triggers_ibfk_1;
declare
fkey varchar2(255);
begin
    select c.constraint_name into fkey
    from all_constraints c
    inner join all_constraints c2 on c.r_constraint_name = c2.constraint_name
    where c.table_name = 'QRTZ_TRIGGERS' AND c.constraint_type = 'R';

    execute immediate 'alter table qrtz_triggers drop constraint ' || fkey;
end;

-- alter table qrtz_blob_triggers drop constraint qrtz_blob_triggers_ibfk_1;
declare
fkey varchar2(255);
begin
    select c.constraint_name into fkey
    from all_constraints c
    inner join all_constraints c2 on c.r_constraint_name = c2.constraint_name
    where c.table_name = 'QRTZ_BLOB_TRIGGERS' AND c.constraint_type = 'R';

    execute immediate 'alter table qrtz_blob_triggers drop constraint ' || fkey;
end;

-- alter table qrtz_simple_triggers drop constraint qrtz_simple_triggers_ibfk_1;
declare
fkey varchar2(255);
begin
    select c.constraint_name into fkey
    from all_constraints c
    inner join all_constraints c2 on c.r_constraint_name = c2.constraint_name
    where c.table_name = 'QRTZ_SIMPLE_TRIGGERS' AND c.constraint_type = 'R';

    execute immediate 'alter table qrtz_simple_triggers drop constraint ' || fkey;
end;

-- alter table qrtz_cron_triggers drop constraint qrtz_cron_triggers_ibfk_1;
declare
fkey varchar2(255);
begin
    select c.constraint_name into fkey
    from all_constraints c
    inner join all_constraints c2 on c.r_constraint_name = c2.constraint_name
    where c.table_name = 'QRTZ_CRON_TRIGGERS' AND c.constraint_type = 'R';

    execute immediate 'alter table qrtz_cron_triggers drop constraint ' || fkey;
end;


--
-- add all primary and foreign key constraints, based on new columns
--
alter table qrtz_job_details drop primary key;
alter table qrtz_job_details add primary key (sched_name, job_name, job_group);
alter table qrtz_triggers drop primary key;
alter table qrtz_triggers add primary key (sched_name, trigger_name, trigger_group);
alter table qrtz_triggers add foreign key (sched_name, job_name, job_group) references qrtz_job_details(sched_name, job_name, job_group);
alter table qrtz_blob_triggers drop primary key;
alter table qrtz_blob_triggers add primary key (sched_name, trigger_name, trigger_group);
alter table qrtz_blob_triggers add foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers(sched_name, trigger_name, trigger_group);
alter table qrtz_cron_triggers drop primary key;
alter table qrtz_cron_triggers add primary key (sched_name, trigger_name, trigger_group);
alter table qrtz_cron_triggers add foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers(sched_name, trigger_name, trigger_group);
alter table qrtz_simple_triggers drop primary key;
alter table qrtz_simple_triggers add primary key (sched_name, trigger_name, trigger_group);
alter table qrtz_simple_triggers add foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers(sched_name, trigger_name, trigger_group);
alter table qrtz_fired_triggers drop primary key;
alter table qrtz_fired_triggers add primary key (sched_name, entry_id);
alter table qrtz_calendars drop primary key;
alter table qrtz_calendars add primary key (sched_name, calendar_name);
alter table qrtz_locks drop primary key;
alter table qrtz_locks add primary key (sched_name, lock_name);
alter table qrtz_paused_trigger_grps drop primary key;
alter table qrtz_paused_trigger_grps add primary key (sched_name, trigger_group);
alter table qrtz_scheduler_state drop primary key;
alter table qrtz_scheduler_state add primary key (sched_name, instance_name);

--
-- add new simprop_triggers table
--
CREATE TABLE qrtz_simprop_triggers
  (
    SCHED_NAME VARCHAR2(120) NOT NULL,
    TRIGGER_NAME VARCHAR2(200) NOT NULL,
    TRIGGER_GROUP VARCHAR2(200) NOT NULL,
    STR_PROP_1 VARCHAR2(512) NULL,
    STR_PROP_2 VARCHAR2(512) NULL,
    STR_PROP_3 VARCHAR2(512) NULL,
    INT_PROP_1 NUMBER(10) NULL,
    INT_PROP_2 NUMBER(10) NULL,
    LONG_PROP_1 NUMBER(13) NULL,
    LONG_PROP_2 NUMBER(13) NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 VARCHAR2(1) NULL,
    BOOL_PROP_2 VARCHAR2(1) NULL,
    CONSTRAINT QRTZ_SIMPROP_TRIG_PK PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT QRTZ_SIMPROP_TRIG_TO_TRIG_FK FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
      REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

--
-- create indexes for faster queries
--

-- start by dropping everything we're about to create. (some of these
-- will fail because they never existed, but that's OK)
drop index idx_qrtz_j_req_recovery;
drop index idx_qrtz_j_grp;
drop index idx_qrtz_t_j;
drop index idx_qrtz_t_jg;
drop index idx_qrtz_t_c;
drop index idx_qrtz_t_g;
drop index idx_qrtz_t_state;
drop index idx_qrtz_t_n_state;
drop index idx_qrtz_t_n_g_state;
drop index idx_qrtz_t_next_fire_time;
drop index idx_qrtz_t_nft_st;
drop index idx_qrtz_t_nft_misfire;
drop index idx_qrtz_t_nft_st_misfire;
drop index idx_qrtz_t_nft_st_misfire_grp;
drop index idx_qrtz_ft_trig_inst_name;
drop index idx_qrtz_ft_inst_job_req_rcvry;
drop index idx_qrtz_ft_j_g;
drop index idx_qrtz_ft_jg;
drop index idx_qrtz_ft_t_g;
drop index idx_qrtz_ft_tg;

create index idx_qrtz_j_req_recovery on qrtz_job_details(SCHED_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_j_grp on qrtz_job_details(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_t_j on qrtz_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_t_jg on qrtz_triggers(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_t_c on qrtz_triggers(SCHED_NAME,CALENDAR_NAME);
create index idx_qrtz_t_g on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP);
create index idx_qrtz_t_state on qrtz_triggers(SCHED_NAME,TRIGGER_STATE);
create index idx_qrtz_t_n_state on qrtz_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_n_g_state on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_next_fire_time on qrtz_triggers(SCHED_NAME,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st on qrtz_triggers(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
create index idx_qrtz_t_nft_st_misfire_grp on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_ft_trig_inst_name on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME);
create index idx_qrtz_ft_inst_job_req_rcvry on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_ft_j_g on qrtz_fired_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_ft_jg on qrtz_fired_triggers(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_ft_t_g on qrtz_fired_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
create index idx_qrtz_ft_tg on qrtz_fired_triggers(SCHED_NAME,TRIGGER_GROUP);
