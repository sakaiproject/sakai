-- SAK-12883, SAK-12582 - Update existing demo data to match new behavior:
-- assuming that 4 sample academic sessions have been created, treat the
-- middle 2 as "current".
update CM_ACADEMIC_SESSION_T set IS_CURRENT=1 where ENTERPRISE_ID like 'Spring%';
update CM_ACADEMIC_SESSION_T set IS_CURRENT=1 where ENTERPRISE_ID like 'Summer%';
