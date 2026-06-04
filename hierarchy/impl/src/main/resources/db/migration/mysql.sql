ALTER TABLE HIERARCHY_NODE ADD COLUMN HIERARCHYID VARCHAR(255);
ALTER TABLE HIERARCHY_NODE ADD COLUMN ISROOTNODE TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE HIERARCHY_NODE ADD COLUMN OWNERID VARCHAR(99);
ALTER TABLE HIERARCHY_NODE ADD COLUMN TITLE VARCHAR(255);
ALTER TABLE HIERARCHY_NODE ADD COLUMN DESCRIPTION LONGTEXT;
ALTER TABLE HIERARCHY_NODE ADD COLUMN PERMTOKEN VARCHAR(255);
ALTER TABLE HIERARCHY_NODE ADD COLUMN ISDISABLED TINYINT(1) NOT NULL DEFAULT 0;
UPDATE HIERARCHY_NODE n JOIN HIERARCHY_NODE_META m ON n.ID = m.ID SET n.HIERARCHYID = m.HIERARCHYID, n.ISROOTNODE = m.ISROOTNODE, n.OWNERID = m.OWNERID, n.TITLE = m.TITLE, n.DESCRIPTION = m.DESCRIPTION, n.PERMTOKEN = m.PERMTOKEN, n.ISDISABLED = COALESCE(m.ISDISABLED, 0);
DROP TABLE HIERARCHY_NODE_META;

CREATE INDEX IF NOT EXISTS IDX_HN_HIERARCHYID ON HIERARCHY_NODE (HIERARCHYID);

-- JPA @ManyToMany join table replacing encoded-string parent/child columns
CREATE TABLE IF NOT EXISTS HIERARCHY_NODE_PARENTS (
    NODE_ID        BIGINT NOT NULL,
    PARENT_NODE_ID BIGINT NOT NULL,
    CONSTRAINT FK_HNP_NODE   FOREIGN KEY (NODE_ID)        REFERENCES HIERARCHY_NODE(ID),
    CONSTRAINT FK_HNP_PARENT FOREIGN KEY (PARENT_NODE_ID) REFERENCES HIERARCHY_NODE(ID),
    PRIMARY KEY (NODE_ID, PARENT_NODE_ID)
);
CREATE INDEX IF NOT EXISTS IDX_HNP_PARENT_NODE_ID ON HIERARCHY_NODE_PARENTS (PARENT_NODE_ID);

-- Migrate encoded child IDs to HIERARCHY_NODE_PARENTS (upgrade from pre-JPA schema).
-- We derive the parent/child links from the parent-side DIRECTCHILDIDS rather than the
-- child-side DIRECTPARENTIDS. The pre-JPA removeNode only cleaned up the parent's
-- DIRECTCHILDIDS when a node was removed and left the removed node's own DIRECTPARENTIDS
-- pointing at its former parent. DIRECTCHILDIDS therefore reflects the live tree (and is what
-- the old top-down traversal read), so sourcing from it avoids resurrecting orphaned nodes.
DROP PROCEDURE IF EXISTS hierarchy_migrate_parents;
CREATE PROCEDURE hierarchy_migrate_parents()
BEGIN
    IF EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = 'HIERARCHY_NODE'
          AND COLUMN_NAME  = 'DIRECTCHILDIDS'
    ) THEN
        -- The split CTE recurses once per child segment, so its depth equals the largest
        -- number of direct children on a single node. Raise the limit (default 1000) so nodes
        -- with very high fan-out (e.g. a department with thousands of sites) don't abort the migration.
        SET SESSION cte_max_recursion_depth = 1000000;
        INSERT IGNORE INTO HIERARCHY_NODE_PARENTS (NODE_ID, PARENT_NODE_ID)
        WITH RECURSIVE split(parent_id, rest) AS (
            SELECT ID, CONCAT(TRIM(BOTH ':' FROM DIRECTCHILDIDS), ':')
            FROM   HIERARCHY_NODE
            WHERE  DIRECTCHILDIDS IS NOT NULL
              AND  TRIM(BOTH ':' FROM DIRECTCHILDIDS) != ''
            UNION ALL
            SELECT parent_id, SUBSTRING(rest, LOCATE(':', rest) + 1)
            FROM   split
            WHERE  LOCATE(':', rest) > 0
        )
        SELECT CAST(SUBSTRING_INDEX(rest, ':', 1) AS UNSIGNED), parent_id
        FROM   split
        WHERE  LOCATE(':', rest) > 0
          AND  SUBSTRING_INDEX(rest, ':', 1) REGEXP '^[0-9]+$';

        ALTER TABLE HIERARCHY_NODE DROP COLUMN DIRECTPARENTIDS;
        ALTER TABLE HIERARCHY_NODE DROP COLUMN PARENTIDS;
        ALTER TABLE HIERARCHY_NODE DROP COLUMN DIRECTCHILDIDS;
        ALTER TABLE HIERARCHY_NODE DROP COLUMN CHILDIDS;
    END IF;
END;
CALL hierarchy_migrate_parents();
DROP PROCEDURE IF EXISTS hierarchy_migrate_parents;
