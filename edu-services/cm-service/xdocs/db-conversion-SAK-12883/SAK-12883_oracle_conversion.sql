-- SAK-12883, SAK-12582 - Allow control over which academic sessions are
-- considered current; support more than one current academic session
alter table CM_ACADEMIC_SESSION_T add IS_CURRENT number(1,0) default 0 not null;

-- WARNING: This simply emulates the old runtime behavior. It is strongly
-- recommended that you decide which terms should be treated as current
-- and edit this script accordingly!
update CM_ACADEMIC_SESSION_T set IS_CURRENT=1 where SYSDATE >= START_DATE and SYSDATE <= END_DATE;
