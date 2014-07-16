-- SAK-26633 Roster2 replaces Roster
UPDATE SAKAI_SITE_TOOL SET REGISTRATION = 'sakai.site.roster2' WHERE REGISTRATION = 'sakai.site.roster'; 
-- End SAK-26633
