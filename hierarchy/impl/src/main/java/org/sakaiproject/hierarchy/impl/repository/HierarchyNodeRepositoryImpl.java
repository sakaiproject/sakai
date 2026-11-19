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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

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

    private static final String ANCESTORS_IDS_CTE = """
        with ancestors as (
            select p.id as id from HierarchyNode n join n.parents p where n.id = :nodeId
            union all
            select p.id as id from ancestors a join HierarchyNode n on n.id = a.id join n.parents p
        )
        select a.id from ancestors a
        """;

    private static final String DESCENDANTS_IDS_CTE = """
        with descendants as (
            select c.id as id from HierarchyNode n join n.children c where n.id = :nodeId
            union all
            select c.id as id from descendants d join HierarchyNode n on n.id = d.id join n.children c
        )
        select d.id from descendants d
        """;

    // Batch, origin-tracking variants: each row is (origin node id, related node id) so a single
    // query resolves the closure for many origin nodes at once.
    private static final String ANCESTOR_IDS_BATCH_CTE = """
        with ancestors as (
            select n.id as origin, p.id as id from HierarchyNode n join n.parents p where n.id in (:nodeIds)
            union all
            select a.origin as origin, p.id as id
            from ancestors a join HierarchyNode n on n.id = a.id join n.parents p
        )
        select a.origin, a.id from ancestors a
        """;

    private static final String DESCENDANT_IDS_BATCH_CTE = """
        with descendants as (
            select n.id as origin, c.id as id from HierarchyNode n join n.children c where n.id in (:nodeIds)
            union all
            select d.origin as origin, c.id as id
            from descendants d join HierarchyNode n on n.id = d.id join n.children c
        )
        select d.origin, d.id from descendants d
        """;

    private static final String DIRECT_PARENT_IDS_BATCH =
        "select n.id, p.id from HierarchyNode n join n.parents p where n.id in (:nodeIds)";

    private static final String DIRECT_CHILD_IDS_BATCH =
        "select n.id, c.id from HierarchyNode n join n.children c where n.id in (:nodeIds)";

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
    public Set<HierarchyNode> findAllAncestors(Long nodeId) {
        List<Long> ids = sessionFactory.getCurrentSession()
                .createQuery(ANCESTORS_IDS_CTE, Long.class)
                .setParameter("nodeId", nodeId)
                .getResultList();
        if (ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(findByIdIn(ids));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<HierarchyNode> findAllDescendants(Long nodeId) {
        List<Long> ids = sessionFactory.getCurrentSession()
                .createQuery(DESCENDANTS_IDS_CTE, Long.class)
                .setParameter("nodeId", nodeId)
                .getResultList();
        if (ids.isEmpty()) return new HashSet<>();
        return new HashSet<>(findByIdIn(ids));
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
     * Runs an HQL query that returns (origin id, related id) pairs and groups it into a map keyed
     * by origin id. The id collection is chunked to stay within the SQL parameter-list limit.
     */
    private Map<Long, Set<Long>> queryIdPairs(String hql, Collection<Long> nodeIds) {
        Map<Long, Set<Long>> result = new HashMap<>();
        if (nodeIds == null || nodeIds.isEmpty()) return result;
        List<Long> ids = new ArrayList<>(nodeIds);
        int chunkSize = HibernateCriterionUtils.MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST;
        for (int i = 0; i < ids.size(); i += chunkSize) {
            List<Long> chunk = ids.subList(i, Math.min(i + chunkSize, ids.size()));
            List<Object[]> rows = sessionFactory.getCurrentSession()
                    .createQuery(hql, Object[].class)
                    .setParameterList("nodeIds", chunk)
                    .getResultList();
            for (Object[] row : rows) {
                Long origin = (Long) row[0];
                Long related = (Long) row[1];
                result.computeIfAbsent(origin, k -> new HashSet<>()).add(related);
            }
        }
        return result;
    }
}
