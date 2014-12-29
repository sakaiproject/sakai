-- There was no conversion associated with the 2.5.5 release and thus, no 2_5_4-2_5_5 conversion script

-- SAK-14482 Patch mercury and !workspace sites to use sakai.assignment.grades tool
-- update Mercury site
UPDATE SAKAI_SITE_TOOL SET REGISTRATION='sakai.assignment.grades' WHERE REGISTRATION='sakai.assignment' AND SITE_ID='mercury';

-- update !worksite site
UPDATE SAKAI_SITE_TOOL SET REGISTRATION='sakai.assignment.grades' WHERE REGISTRATION='sakai.assignment' AND SITE_ID='!worksite';
