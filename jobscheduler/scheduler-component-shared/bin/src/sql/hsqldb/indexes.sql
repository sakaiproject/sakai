-- Creates the index on event time as all searches sort on this.
-- Index name remains < 30 characters for Oracle.
CREATE INDEX schdulr_trggr_vnts_eventTime ON scheduler_trigger_events(eventTime);