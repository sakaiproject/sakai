-- SAK-12883, SAK-12582 - Allow control over which academic sessions are
-- considered current; support more than one current academic session
alter table CM_ACADEMIC_SESSION_T add column IS_CURRENT bit default 0 not null;
