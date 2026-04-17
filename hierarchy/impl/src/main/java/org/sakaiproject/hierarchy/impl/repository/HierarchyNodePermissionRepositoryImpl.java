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
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.sakaiproject.hierarchy.model.HierarchyNodePermission;
import org.sakaiproject.hierarchy.repository.HierarchyNodePermissionRepository;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public class HierarchyNodePermissionRepositoryImpl
        extends SpringCrudRepositoryImpl<HierarchyNodePermission, Long>
        implements HierarchyNodePermissionRepository {

    @Override
    @Transactional(readOnly = true)
    public Optional<HierarchyNodePermission> findByUserIdAndNodeIdAndPermission(String userId, String nodeId, String permission) {
        if (userId == null || nodeId == null || permission == null) return Optional.empty();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> query = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> root = query.from(HierarchyNodePermission.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get("userId"), userId),
                        cb.equal(root.get("nodeId"), nodeId),
                        cb.equal(root.get("permission"), permission)));

        HierarchyNodePermission result = sessionFactory.getCurrentSession()
                .createQuery(query)
                .uniqueResult();

        return Optional.ofNullable(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodePermission> findByUserIdAndPermission(String userId, String permission) {
        if (userId == null || permission == null) return List.of();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> query = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> root = query.from(HierarchyNodePermission.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get("userId"), userId),
                        cb.equal(root.get("permission"), permission)));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodePermission> findByNodeIdIn(Collection<String> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) return List.of();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> query = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> root = query.from(HierarchyNodePermission.class);

        query.select(root)
                .where(HibernateCriterionUtils.PredicateInSplitter(cb, root.get("nodeId"), nodeIds));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodePermission> findByUserIdIn(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> query = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> root = query.from(HierarchyNodePermission.class);

        query.select(root)
                .where(HibernateCriterionUtils.PredicateInSplitter(cb, root.get("userId"), userIds));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodePermission> findByUserIdAndPermissionAndNodeIdIn(String userId, String permission, Collection<String> nodeIds) {
        if (userId == null || permission == null || nodeIds == null || nodeIds.isEmpty()) return List.of();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> query = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> root = query.from(HierarchyNodePermission.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get("userId"), userId),
                        cb.equal(root.get("permission"), permission),
                        HibernateCriterionUtils.PredicateInSplitter(cb, root.get("nodeId"), nodeIds)));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodePermission> findByPermissionAndNodeIdIn(String permission, Collection<String> nodeIds) {
        if (permission == null || nodeIds == null || nodeIds.isEmpty()) return List.of();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> query = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> root = query.from(HierarchyNodePermission.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get("permission"), permission),
                        HibernateCriterionUtils.PredicateInSplitter(cb, root.get("nodeId"), nodeIds)));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodePermission> findByUserIdAndNodeIdIn(String userId, Collection<String> nodeIds) {
        if (userId == null || nodeIds == null || nodeIds.isEmpty()) return List.of();

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> query = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> root = query.from(HierarchyNodePermission.class);

        query.select(root)
                .where(cb.and(
                        cb.equal(root.get("userId"), userId),
                        HibernateCriterionUtils.PredicateInSplitter(cb, root.get("nodeId"), nodeIds)));

        return sessionFactory.getCurrentSession()
                .createQuery(query)
                .getResultList();
    }
}
