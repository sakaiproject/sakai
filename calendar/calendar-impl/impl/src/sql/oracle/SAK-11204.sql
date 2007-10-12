-- Force this to happen and report problems bu having a non failing SQL statement first
-- as of 2.5 this mod is in the autoDDL script, but has been left there to allow DBA's to selectively
-- do the update


ALTER TABLE CALENDAR_EVENT ADD (RANGE_START INTEGER);
ALTER TABLE CALENDAR_EVENT ADD (RANGE_END INTEGER);

CREATE INDEX CALENDAR_EVENT_RSTART ON CALENDAR_EVENT(RANGE_START);
CREATE INDEX CALENDAR_EVENT_REND ON CALENDAR_EVENT(RANGE_END);

