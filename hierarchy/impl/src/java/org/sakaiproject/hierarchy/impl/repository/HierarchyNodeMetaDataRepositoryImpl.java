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

import org.sakaiproject.hierarchy.api.repository.HierarchyNodeMetaDataRepository;
import org.sakaiproject.hierarchy.dao.model.HierarchyNodeMetaData;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class HierarchyNodeMetaDataRepositoryImpl extends SpringCrudRepositoryImpl<HierarchyNodeMetaData, Long> implements HierarchyNodeMetaDataRepository {

    @Override
    @Transactional(readOnly = true)
    public long countByHierarchyId(String hierarchyId) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<HierarchyNodeMetaData> m = cq.from(HierarchyNodeMetaData.class);
        cq.select(cb.count(m)).where(cb.equal(m.get("hierarchyId"), hierarchyId));
        Long count = session.createQuery(cq).uniqueResult();
        return count == null ? 0L : count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodeMetaData> findByHierarchyId(String hierarchyId) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodeMetaData> cq = cb.createQuery(HierarchyNodeMetaData.class);
        Root<HierarchyNodeMetaData> m = cq.from(HierarchyNodeMetaData.class);
        cq.where(cb.equal(m.get("hierarchyId"), hierarchyId));
        return session.createQuery(cq).list();
    }

    @Override
    @Transactional(readOnly = true)
    public HierarchyNodeMetaData findRootByHierarchyId(String hierarchyId) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodeMetaData> cq = cb.createQuery(HierarchyNodeMetaData.class);
        Root<HierarchyNodeMetaData> m = cq.from(HierarchyNodeMetaData.class);
        cq.where(cb.and(cb.equal(m.get("hierarchyId"), hierarchyId), cb.isTrue(m.get("isRootNode"))));
        return session.createQuery(cq).uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public HierarchyNodeMetaData findByNodeId(Long nodeId) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodeMetaData> cq = cb.createQuery(HierarchyNodeMetaData.class);
        Root<HierarchyNodeMetaData> m = cq.from(HierarchyNodeMetaData.class);
        cq.where(cb.equal(m.get("node").get("id"), nodeId));
        return session.createQuery(cq).uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodeMetaData> findByNodeIds(List<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) return List.of();
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodeMetaData> cq = cb.createQuery(HierarchyNodeMetaData.class);
        Root<HierarchyNodeMetaData> m = cq.from(HierarchyNodeMetaData.class);
        cq.where(m.get("node").get("id").in(nodeIds));
        return session.createQuery(cq).list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyNodeMetaData> findByHierarchyIdAndPermTokenOrdered(String hierarchyId, String permToken) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyNodeMetaData> cq = cb.createQuery(HierarchyNodeMetaData.class);
        Root<HierarchyNodeMetaData> m = cq.from(HierarchyNodeMetaData.class);
        cq.where(cb.and(cb.equal(m.get("hierarchyId"), hierarchyId), cb.equal(m.get("permToken"), permToken)))
          .orderBy(cb.asc(m.get("node").get("id")));
        return session.createQuery(cq).list();
    }
}

