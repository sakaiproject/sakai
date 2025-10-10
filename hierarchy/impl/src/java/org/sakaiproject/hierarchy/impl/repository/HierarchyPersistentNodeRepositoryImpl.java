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

import org.sakaiproject.hierarchy.api.repository.HierarchyPersistentNodeRepository;
import org.sakaiproject.hierarchy.dao.model.HierarchyPersistentNode;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class HierarchyPersistentNodeRepositoryImpl extends SpringCrudRepositoryImpl<HierarchyPersistentNode, Long> implements HierarchyPersistentNodeRepository {

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyPersistentNode> findByIdIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<HierarchyPersistentNode> cq = cb.createQuery(HierarchyPersistentNode.class);
        Root<HierarchyPersistentNode> n = cq.from(HierarchyPersistentNode.class);
        cq.where(n.get("id").in(ids));
        return session.createQuery(cq).list();
    }
}

