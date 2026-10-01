-- SAK-52046: run once, with Sakai stopped, after the Tags/Taggable merger conversion.
-- Back up the database first. Unscoped imported/admin collections remain global.
ALTER TABLE tagservice_collection ADD siteid VARCHAR(100);
ALTER TABLE tagservice_collection DROP CONSTRAINT name_UNIQUE;
ALTER TABLE tagservice_collection ADD CONSTRAINT tagservice_site_name UNIQUE (siteid, name);
CREATE INDEX tagservice_collection_siteid ON tagservice_collection(siteid);

UPDATE tagservice_collection SET siteid = tagcollectionid
WHERE EXISTS (SELECT 1 FROM SAKAI_SITE s WHERE s.SITE_ID = tagcollectionid);
UPDATE tagservice_collection SET siteid = '~' || tagcollectionid
WHERE siteid IS NULL AND EXISTS (SELECT 1 FROM SAKAI_USER_ID_MAP u WHERE u.USER_ID = tagcollectionid);

-- Empty external-source identifiers are absent, allowing ordinary collections to coexist.
UPDATE tagservice_collection SET externalsourcename = NULL WHERE TRIM(externalsourcename) = '';

-- Clone personal Messages tags into each site that actually uses them.
-- Retain originals: question pools and questions may still refer to those IDs.
CREATE TABLE SAK52046_MESSAGE_TAGS (
    old_tag_id VARCHAR(36) NOT NULL,
    site_id VARCHAR(99) NOT NULL,
    new_tag_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (old_tag_id, site_id)
);
INSERT INTO SAK52046_MESSAGE_TAGS (old_tag_id, site_id, new_tag_id)
SELECT old_tag_id, site_id, CAST(UUID() AS VARCHAR(36)) FROM (
    SELECT DISTINCT t.tagid AS old_tag_id, r.CONTEXT_ID AS site_id
    FROM tagservice_tag t
    JOIN tagservice_collection c ON c.tagcollectionid = t.tagcollectionid
    JOIN tagservice_tagassociation a ON a.tag_id = t.tagid
    JOIN MFR_PVT_MSG_USR_T r ON a.item_id = CAST(r.messageSurrogateKey AS VARCHAR(255))
    JOIN SAKAI_SITE s ON s.SITE_ID = r.CONTEXT_ID
    WHERE c.siteid LIKE '~%'
) message_tags;

INSERT INTO tagservice_collection (tagcollectionid, siteid, name, description)
SELECT DISTINCT m.site_id, m.site_id, m.site_id, 'Site tags'
FROM SAK52046_MESSAGE_TAGS m
WHERE NOT EXISTS (SELECT 1 FROM tagservice_collection c WHERE c.tagcollectionid = m.site_id);

INSERT INTO tagservice_tag (tagid, tagcollectionid, externalid, taglabel, description, alternativelabels, createdby, creationdate, externalcreation, externalcreationdate, externalupdate, lastmodifiedby, lastmodificationdate, lastupdatedateinexternalsystem, parentid, externalhierarchycode, externaltype, data)
SELECT m.new_tag_id, m.site_id, t.externalid, t.taglabel, t.description, t.alternativelabels, t.createdby, t.creationdate, t.externalcreation, t.externalcreationdate, t.externalupdate, t.lastmodifiedby, t.lastmodificationdate, t.lastupdatedateinexternalsystem, t.parentid, t.externalhierarchycode, t.externaltype, t.data
FROM SAK52046_MESSAGE_TAGS m JOIN tagservice_tag t ON t.tagid = m.old_tag_id;

-- Build distinct pairs before assigning IDs: a message can have several recipients.
INSERT INTO tagservice_tagassociation (id, tag_id, item_id)
SELECT CAST(UUID() AS VARCHAR(36)), new_tag_id, item_id FROM (
    SELECT DISTINCT m.new_tag_id, a.item_id
    FROM SAK52046_MESSAGE_TAGS m
    JOIN tagservice_tagassociation a ON a.tag_id = m.old_tag_id
    JOIN MFR_PVT_MSG_USR_T r ON a.item_id = CAST(r.messageSurrogateKey AS VARCHAR(255)) AND r.CONTEXT_ID = m.site_id
) message_associations;

DELETE FROM tagservice_tagassociation a WHERE EXISTS (
    SELECT 1 FROM SAK52046_MESSAGE_TAGS m JOIN MFR_PVT_MSG_USR_T r ON r.CONTEXT_ID = m.site_id
    WHERE m.old_tag_id = a.tag_id AND a.item_id = CAST(r.messageSurrogateKey AS VARCHAR(255))
);
DROP TABLE SAK52046_MESSAGE_TAGS;

-- Add Tags to existing Admin Workspaces that do not already contain it.
INSERT INTO SAKAI_SITE_PAGE (PAGE_ID, SITE_ID, TITLE, LAYOUT, SITE_ORDER, POPUP)
SELECT '!admin-52046', '!admin', 'Tags', '0', COALESCE(MAX(SITE_ORDER), 0) + 1, '0'
FROM SAKAI_SITE_PAGE
WHERE SITE_ID = '!admin'
HAVING NOT EXISTS (SELECT 1 FROM SAKAI_SITE_TOOL WHERE SITE_ID = '!admin' AND REGISTRATION = 'sakai.tagservice');
INSERT INTO SAKAI_SITE_TOOL (TOOL_ID, PAGE_ID, SITE_ID, REGISTRATION, PAGE_ORDER, TITLE, LAYOUT_HINTS)
SELECT '!admin-52046', '!admin-52046', '!admin', 'sakai.tagservice', 1, 'Tags', NULL FROM (VALUES(0))
WHERE EXISTS (SELECT 1 FROM SAKAI_SITE_PAGE WHERE PAGE_ID = '!admin-52046')
AND NOT EXISTS (SELECT 1 FROM SAKAI_SITE_TOOL WHERE SITE_ID = '!admin' AND REGISTRATION = 'sakai.tagservice');
