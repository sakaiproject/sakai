/*
 * Copyright (c) 2025 The Apereo Foundation
 * Licensed under the ECL-2.0 license.
 */
package org.sakaiproject.hierarchy.impl.repository;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import org.sakaiproject.hierarchy.api.repository.HierarchyNodePermissionRepository;
import org.sakaiproject.hierarchy.dao.model.HierarchyNodePermission;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class HierarchyNodePermissionRepositoryImpl extends SpringCrudRepositoryImpl<HierarchyNodePermission, Long> implements HierarchyNodePermissionRepository {

    @Override
    @Transactional(readOnly = true)
    public HierarchyNodePermission findByUserIdAndNodeIdAndPermission(String userId, String nodeId, String permission) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> cq = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> p = cq.from(HierarchyNodePermission.class);
        cq.where(cb.and(cb.equal(p.get("userId"), userId), cb.equal(p.get("nodeId"), nodeId), cb.equal(p.get("permission"), permission)));
        return session.createQuery(cq).uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodePermission> findByUserIdAndPermission(String userId, String permission) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> cq = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> p = cq.from(HierarchyNodePermission.class);
        cq.where(cb.and(cb.equal(p.get("userId"), userId), cb.equal(p.get("permission"), permission)));
        return session.createQuery(cq).list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodePermission> findByUserIdAndPermissionAndNodeIds(String userId, String permission, List<String> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) return List.of();
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> cq = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> p = cq.from(HierarchyNodePermission.class);
        cq.where(cb.and(cb.equal(p.get("userId"), userId), cb.equal(p.get("permission"), permission), p.get("nodeId").in(nodeIds)));
        return session.createQuery(cq).list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodePermission> findByPermissionAndNodeIds(String permission, List<String> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) return List.of();
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> cq = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> p = cq.from(HierarchyNodePermission.class);
        cq.where(cb.and(cb.equal(p.get("permission"), permission), p.get("nodeId").in(nodeIds)));
        return session.createQuery(cq).list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodePermission> findByUserIdAndNodeIds(String userId, List<String> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) return List.of();
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> cq = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> p = cq.from(HierarchyNodePermission.class);
        cq.where(cb.and(cb.equal(p.get("userId"), userId), p.get("nodeId").in(nodeIds)));
        return session.createQuery(cq).list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodePermission> findByNodeIdIn(List<String> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) return List.of();
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> cq = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> p = cq.from(HierarchyNodePermission.class);
        cq.where(p.get("nodeId").in(nodeIds));
        return session.createQuery(cq).list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodePermission> findByUserIdIn(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodePermission> cq = cb.createQuery(HierarchyNodePermission.class);
        Root<HierarchyNodePermission> p = cq.from(HierarchyNodePermission.class);
        cq.where(p.get("userId").in(userIds));
        return session.createQuery(cq).list();
    }
}
