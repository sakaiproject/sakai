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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
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

    // TODO: Hibernate 6 supports CTEs natively in HQL/Criteria (JpaCriteriaQuery#with / CteCriteria).
    // These recursive-CTE native queries are the only native SQL in this repository; migrate them to
    // the Criteria CTE API on the Hibernate 6 upgrade so the data layer is fully native-SQL free.
    // This also resolves the Oracle "WITH RECURSIVE" syntax incompatibility (Oracle omits the keyword).
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

    // Batch, origin-tracking variants: each row is (ORIGIN node id, related node id) so a single
    // query resolves the closure for many origin nodes at once.
    private static final String ANCESTOR_IDS_BATCH_CTE =
        "WITH RECURSIVE ancestors (ORIGIN, ID) AS (" +
        "  SELECT NODE_ID, PARENT_NODE_ID FROM HIERARCHY_NODE_PARENTS WHERE NODE_ID IN (:nodeIds) " +
        "  UNION ALL " +
        "  SELECT a.ORIGIN, p.PARENT_NODE_ID FROM HIERARCHY_NODE_PARENTS p " +
        "  INNER JOIN ancestors a ON p.NODE_ID = a.ID" +
        ") SELECT ORIGIN, ID FROM ancestors";

    private static final String DESCENDANT_IDS_BATCH_CTE =
        "WITH RECURSIVE descendants (ORIGIN, ID) AS (" +
        "  SELECT PARENT_NODE_ID, NODE_ID FROM HIERARCHY_NODE_PARENTS WHERE PARENT_NODE_ID IN (:nodeIds) " +
        "  UNION ALL " +
        "  SELECT d.ORIGIN, p.NODE_ID FROM HIERARCHY_NODE_PARENTS p " +
        "  INNER JOIN descendants d ON p.PARENT_NODE_ID = d.ID" +
        ") SELECT ORIGIN, ID FROM descendants";

    private static final String DIRECT_PARENT_IDS_BATCH =
        "SELECT NODE_ID, PARENT_NODE_ID FROM HIERARCHY_NODE_PARENTS WHERE NODE_ID IN (:nodeIds)";

    private static final String DIRECT_CHILD_IDS_BATCH =
        "SELECT PARENT_NODE_ID, NODE_ID FROM HIERARCHY_NODE_PARENTS WHERE PARENT_NODE_ID IN (:nodeIds)";

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
    public List<HierarchyNode> findByHierarchyIdAndTitleInAndIsDisabled(String hierarchyId, Collection<String> titles, Boolean isDisabled) {
        if (hierarchyId == null || titles == null || titles.isEmpty() || isDisabled == null) return List.of();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNode> query = cb.createQuery(HierarchyNode.class);
        Root<HierarchyNode> root = query.from(HierarchyNode.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get("hierarchyId"), hierarchyId),
                        cb.equal(root.get("isDisabled"), isDisabled),
                        HibernateCriterionUtils.PredicateInSplitter(cb, root.get("title"), titles)));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNode> findChildlessByHierarchyIdAndTitleNotLike(String hierarchyId, String titlePattern) {
        if (hierarchyId == null || titlePattern == null) return List.of();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNode> query = cb.createQuery(HierarchyNode.class);
        Root<HierarchyNode> root = query.from(HierarchyNode.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get("hierarchyId"), hierarchyId),
                        cb.equal(root.get("isRootNode"), Boolean.FALSE),
                        cb.equal(root.get("isDisabled"), Boolean.FALSE),
                        cb.notLike(root.get("title"), titlePattern),
                        cb.isEmpty(root.get("children"))));

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
    @Transactional(readOnly = true)
    public Map<Long, Set<Long>> findAncestorIdsByNodeIds(Collection<Long> nodeIds) {
        return queryIdPairs(ANCESTOR_IDS_BATCH_CTE, nodeIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Set<Long>> findDescendantIdsByNodeIds(Collection<Long> nodeIds) {
        return queryIdPairs(DESCENDANT_IDS_BATCH_CTE, nodeIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Set<Long>> findDirectParentIdsByNodeIds(Collection<Long> nodeIds) {
        return queryIdPairs(DIRECT_PARENT_IDS_BATCH, nodeIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Set<Long>> findDirectChildIdsByNodeIds(Collection<Long> nodeIds) {
        return queryIdPairs(DIRECT_CHILD_IDS_BATCH, nodeIds);
    }

    /**
     * Runs a native query that returns (origin id, related id) pairs and groups it into a map keyed
     * by origin id. The id collection is chunked to stay within the SQL parameter-list limit.
     */
    @SuppressWarnings("unchecked")
    private Map<Long, Set<Long>> queryIdPairs(String sql, Collection<Long> nodeIds) {
        Map<Long, Set<Long>> result = new HashMap<>();
        if (nodeIds == null || nodeIds.isEmpty()) return result;
        List<Long> ids = new ArrayList<>(nodeIds);
        int chunkSize = HibernateCriterionUtils.MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST;
        for (int i = 0; i < ids.size(); i += chunkSize) {
            List<Long> chunk = ids.subList(i, Math.min(i + chunkSize, ids.size()));
            List<Object[]> rows = sessionFactory.getCurrentSession()
                    .createNativeQuery(sql)
                    .setParameterList("nodeIds", chunk)
                    .getResultList();
            for (Object[] row : rows) {
                Long origin = ((Number) row[0]).longValue();
                Long related = ((Number) row[1]).longValue();
                result.computeIfAbsent(origin, k -> new HashSet<>()).add(related);
            }
        }
        return result;
    }
}
