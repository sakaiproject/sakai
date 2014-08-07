-- SAK-26633 Roster2 replaces Roster
UPDATE SAKAI_SITE_TOOL SET REGISTRATION = 'sakai.site.roster2' WHERE REGISTRATION = 'sakai.site.roster'; 
-- End SAK-26633

-- SAK-27775 Missing new column added in SAK-22187
ALTER TABLE scheduler_trigger_events ADD serverId varchar2(255);
CREATE INDEX schdulr_trggr_vnts_eventTime ON scheduler_trigger_events(eventTime);
-- End SAK-27775 
