-- SAK-8780, SAK-7452 - Add SESSION_ACTIVE flag to explicitly indicate when
-- a session is active rather than relying on SESSION_START and SESSION_END
-- having the same value.

-- It is important that the new column have a null value by default,
-- since that allows for an extremely efficient Oracle index.
alter table SAKAI_SESSION add column SESSION_ACTIVE number(1,0);
create index SESSION_ACTIVE_IE on SAKAI_SESSION (SESSION_ACTIVE);
