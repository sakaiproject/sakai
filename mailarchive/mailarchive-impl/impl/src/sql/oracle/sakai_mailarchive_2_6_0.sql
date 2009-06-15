ALTER TABLE MAILARCHIVE_MESSAGE ADD (
       SUBJECT           VARCHAR2 (255) default null,
       BODY              CLOB default null
);

CREATE INDEX MAILARCHIVE_SUBJECT_INDEX ON MAILARCHIVE_MESSAGE
(
        SUBJECT
);

-- SAK-16463 fix
alter table MAILARCHIVE_MESSAGE modify XML CLOB;

-- Note after performing this conversion your indexes may be in an invalid state because of the required clob conversion.
-- You may need to run ths following statement, manually execute the generated 'alter indexes' and re-gather statistics on this table
-- There is a randomly named index so it can not be automated.

-- See the 2.6 release notes or SAK-16553 for further details

-- select 'alter index '||index_name||' rebuild online;' from user_indexes where status = 'INVALID' or status = 'UNUSABLE'; 
