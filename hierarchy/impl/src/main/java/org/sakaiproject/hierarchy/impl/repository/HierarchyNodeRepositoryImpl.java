/**
 * Copyright (c) 2007-2024 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.hierarchy.impl.repository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.hierarchy.repository.HierarchyNodeRepository;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
public class HierarchyNodeRepositoryImpl
        extends SpringCrudRepositoryImpl<HierarchyNode, Long>
        implements HierarchyNodeRepository {

    private static final String ANCESTORS_IDS_CTE =
        "WITH RECURSIVE ancestors (ID) AS (" +
        "  SELECT PARENT_NODE_ID FROM HIERARCHY_NODE_PARENTS WHERE NODE_ID = :nodeId " +
        "  UNION ALL " +
        "  SELECT p.PARENT_NODE_ID FROM HIERARCHY_NODE_PARENTS p " +
        "  INNER JOIN ancestors a ON p.NODE_ID = a.ID" +
        ") SELECT ID FROM ancestors";

    private static final String DESCENDANTS_IDS_CTE =
        "WITH RECURSIVE descendants (ID) AS (" +
        "  SELECT NODE_ID FROM HIERARCHY_NODE_PARENTS WHERE PARENT_NODE_ID = :nodeId " +
        "  UNION ALL " +
        "  SELECT p.NODE_ID FROM HIERARCHY_NODE_PARENTS p " +
        "  INNER JOIN descendants d ON p.PARENT_NODE_ID = d.ID" +
        ") SELECT ID FROM descendants";

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNode> findByIdIn(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNode> query = cb.createQuery(HierarchyNode.class);
        Root<HierarchyNode> root = query.from(HierarchyNode.class);

        query.select(root)
                .where(HibernateCriterionUtils.PredicateInSplitter(cb, root.get("id"), ids));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByHierarchyId(String hierarchyId) {
        if (hierarchyId == null) return 0L;

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<HierarchyNode> root = query.from(HierarchyNode.class);

        query.select(cb.count(root))
                .where(cb.equal(root.get("hierarchyId"), hierarchyId));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNode> findByHierarchyId(String hierarchyId) {
        if (hierarchyId == null) return List.of();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNode> query = cb.createQuery(HierarchyNode.class);
        Root<HierarchyNode> root = query.from(HierarchyNode.class);

        query.select(root)
                .where(cb.equal(root.get("hierarchyId"), hierarchyId));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HierarchyNode> findByHierarchyIdAndIsRootNode(String hierarchyId, Boolean isRootNode) {
        if (hierarchyId == null || isRootNode == null) return Optional.empty();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNode> query = cb.createQuery(HierarchyNode.class);
        Root<HierarchyNode> root = query.from(HierarchyNode.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get("hierarchyId"), hierarchyId),
                        cb.equal(root.get("isRootNode"), isRootNode)));

        HierarchyNode result = sessionFactory.getCurrentSession()
                .createQuery(query)
                .uniqueResult();

        return Optional.ofNullable(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNode> findByHierarchyIdAndPermToken(String hierarchyId, String permToken) {
        if (hierarchyId == null || permToken == null) return List.of();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNode> query = cb.createQuery(HierarchyNode.class);
        Root<HierarchyNode> root = query.from(HierarchyNode.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get("hierarchyId"), hierarchyId),
                        cb.equal(root.get("permToken"), permToken)))
                .orderBy(cb.asc(root.get("id")));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByIsDisabledIsNull() {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<HierarchyNode> root = query.from(HierarchyNode.class);

        query.select(cb.count(root))
                .where(root.get("isDisabled").isNull());

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .uniqueResult();
    }

    @Override
    public void fixupDatabase() {
        long count = countByIsDisabledIsNull();
        if (count > 0) {
            CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
            CriteriaUpdate<HierarchyNode> update = cb.createCriteriaUpdate(HierarchyNode.class);
            Root<HierarchyNode> root = update.from(HierarchyNode.class);
            update.set(root.get("isDisabled"), Boolean.FALSE)
                    .where(root.get("isDisabled").isNull());
            int updated = sessionFactory.getCurrentSession().createQuery(update).executeUpdate();
            log.info("Updated {} HierarchyNode.isDisabled fields from null to false", updated);
        }
        migrateEncodedParentIds();
    }

    @SuppressWarnings("unchecked")
    private void migrateEncodedParentIds() {
        // Check whether old encoded-string column still exists using JDBC metadata (avoids SQL error logging)
        boolean hasOldColumn = Boolean.TRUE.equals(sessionFactory.getCurrentSession().doReturningWork(conn -> {
            try (java.sql.ResultSet rs = conn.getMetaData().getColumns(null, null, "HIERARCHY_NODE", "DIRECTPARENTIDS")) {
                if (rs.next()) return true;
            }
            // Some databases store names in lowercase; try that too
            try (java.sql.ResultSet rs = conn.getMetaData().getColumns(null, null, "hierarchy_node", "directparentids")) {
                return rs.next();
            }
        }));

        if (!hasOldColumn) return;

        List<Object[]> rows = sessionFactory.getCurrentSession()
                .createNativeQuery("SELECT ID, DIRECTPARENTIDS FROM HIERARCHY_NODE" +
                                   " WHERE DIRECTPARENTIDS IS NOT NULL AND DIRECTPARENTIDS <> ''")
                .getResultList();

        int migrated = 0;
        for (Object[] row : rows) {
            Long nodeId = ((Number) row[0]).longValue();
            String encoded = (String) row[1];
            if (encoded == null || encoded.isEmpty()) continue;
            for (String part : encoded.split(":")) {
                if (part.isEmpty()) continue;
                try {
                    Long parentId = Long.parseLong(part);
                    sessionFactory.getCurrentSession()
                            .createNativeQuery("INSERT INTO HIERARCHY_NODE_PARENTS" +
                                               " (NODE_ID, PARENT_NODE_ID) VALUES (:n, :p)")
                            .setParameter("n", nodeId)
                            .setParameter("p", parentId)
                            .executeUpdate();
                    migrated++;
                } catch (Exception ex) {
                    log.warn("Skipping parent '{}' for node {} during migration: {}", part, nodeId, ex.getMessage());
                }
            }
        }
        log.info("Migrated {} parent relationships from encoded strings to HIERARCHY_NODE_PARENTS", migrated);

        // Drop obsolete columns
        for (String col : new String[]{"DIRECTPARENTIDS", "PARENTIDS", "DIRECTCHILDIDS", "CHILDIDS"}) {
            try {
                sessionFactory.getCurrentSession()
                        .createNativeQuery("ALTER TABLE HIERARCHY_NODE DROP COLUMN " + col)
                        .executeUpdate();
            } catch (Exception ex) {
                log.debug("Could not drop column {} (may not exist): {}", col, ex.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Set<HierarchyNode> findAllAncestors(Long nodeId) {
        List<Object> ids = sessionFactory.getCurrentSession()
                .createNativeQuery(ANCESTORS_IDS_CTE)
                .setParameter("nodeId", nodeId)
                .getResultList();
        if (ids.isEmpty()) return new HashSet<>();
        List<Long> longIds = ids.stream()
                .map(o -> ((Number) o).longValue())
                .collect(Collectors.toList());
        return new HashSet<>(findByIdIn(longIds));
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Set<HierarchyNode> findAllDescendants(Long nodeId) {
        List<Object> ids = sessionFactory.getCurrentSession()
                .createNativeQuery(DESCENDANTS_IDS_CTE)
                .setParameter("nodeId", nodeId)
                .getResultList();
        if (ids.isEmpty()) return new HashSet<>();
        List<Long> longIds = ids.stream()
                .map(o -> ((Number) o).longValue())
                .collect(Collectors.toList());
        return new HashSet<>(findByIdIn(longIds));
    }

    @Override
    public void deleteRelationsByNodeIds(Collection<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) return;
        String inClause = nodeIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        sessionFactory.getCurrentSession()
                .createNativeQuery("DELETE FROM HIERARCHY_NODE_PARENTS WHERE NODE_ID IN (" + inClause +
                                   ") OR PARENT_NODE_ID IN (" + inClause + ")")
                .executeUpdate();
    }
}
