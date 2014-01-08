-- --------------------------------------------------------------------------------------------------------------------------------------

-- SAK-6780 added SQL update scripts to add new tables and alter existing tables to support selective release and spreadsheet upload

-- Gradebook table changes between Sakai 2.3.0 and 2.3.1.

-- Add selective release support

alter table GB_GRADABLE_OBJECT_T add (RELEASED NUMBER(1,0));
update GB_GRADABLE_OBJECT_T set RELEASED=1 where RELEASED is NULL;

-- --------------------------------------------------------------------------------------------------------------------------------------

-- -------------------------------------------------------------------------------------------------------------------------------------
-- new content.hidden permission
-- --------------------------------------------------------------------------------------------------------------------------------------

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'content.hidden');

-- GENERIC template
-- maintain role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'));

-- COURSE SITE template
-- Instructor and Teaching Assistant roles
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'));


-- PORTFOLIO SITE template
-- CIG Coordinateor role
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'));

-- PORTFOLIO ADMIN SITE template
-- Program Coordinator and Program Admin roles
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.hidden'));


-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new content.hidden permission into existing realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

-- These are for the site templates

INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','content.hidden');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','content.hidden');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','content.hidden');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','content.hidden');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Program Coordinator','content.hidden');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Program Admin','content.hidden');

-- lookup the role and function numbers
create table PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
insert into PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
select SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
join SAKAI_REALM_ROLE SRR on (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
join SAKAI_REALM_FUNCTION SRF on (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper")
insert into SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
select
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
from
    (select distinct SRRF.REALM_KEY, SRRF.ROLE_KEY from SAKAI_REALM_RL_FN SRRF) SRRFD
    join PERMISSIONS_TEMP TMP on (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    join SAKAI_REALM SR on (SRRFD.REALM_KEY = SR.REALM_KEY)
    where SR.REALM_ID != '!site.helper'
    and not exists (
        select 1
            from SAKAI_REALM_RL_FN SRRFI
            where SRRFI.REALM_KEY=SRRFD.REALM_KEY and SRRFI.ROLE_KEY=SRRFD.ROLE_KEY and  SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables
drop table PERMISSIONS_TEMP;
drop table PERMISSIONS_SRC_TEMP;