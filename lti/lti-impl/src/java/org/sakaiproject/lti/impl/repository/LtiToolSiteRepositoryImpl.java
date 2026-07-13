/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.lti.impl.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.lti.api.model.LtiTool;
import org.sakaiproject.lti.api.model.LtiToolSite;
import org.sakaiproject.lti.api.repository.LtiToolSiteRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class LtiToolSiteRepositoryImpl extends SpringCrudRepositoryImpl<LtiToolSite, Long> implements LtiToolSiteRepository {

    @Transactional(readOnly = true)
    public List<LtiToolSite> findBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiToolSite> query = cb.createQuery(LtiToolSite.class);
        Root<LtiToolSite> toolSite = query.from(LtiToolSite.class);
        query.where(cb.equal(toolSite.get("siteId"), siteId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<LtiToolSite> findByTool_Id(Long toolId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiToolSite> query = cb.createQuery(LtiToolSite.class);
        Root<LtiToolSite> toolSite = query.from(LtiToolSite.class);
        Join<LtiToolSite, LtiTool> tool = toolSite.join("tool");
        query.where(cb.equal(tool.get("id"), toolId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<LtiToolSite> findVisibleToolSite(Long id, String siteId, boolean isAdmin) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiToolSite> query = cb.createQuery(LtiToolSite.class);
        Root<LtiToolSite> toolSite = query.from(LtiToolSite.class);

        Predicate byId = cb.equal(toolSite.get("id"), id);

        if (isAdmin) {
            query.where(byId);
        } else {
            // Owned by the requesting site or globally available (no site)
            Predicate visible = cb.or(
                    cb.equal(toolSite.get("siteId"), siteId),
                    cb.isNull(toolSite.get("siteId")));
            query.where(cb.and(byId, visible));
        }

        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<LtiToolSite> findVisibleToolSites(String siteId, boolean isAdmin) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiToolSite> query = cb.createQuery(LtiToolSite.class);
        Root<LtiToolSite> toolSite = query.from(LtiToolSite.class);
        if (!isAdmin) {
            // Owned by the requesting site or globally available (no site)
            query.where(cb.or(
                    cb.equal(toolSite.get("siteId"), siteId),
                    cb.isNull(toolSite.get("siteId"))));
        }
        query.orderBy(cb.asc(toolSite.get("id")));
        return session.createQuery(query).list();
    }

    @Transactional
    public int deleteBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<LtiToolSite> delete = cb.createCriteriaDelete(LtiToolSite.class);
        Root<LtiToolSite> toolSite = delete.from(LtiToolSite.class);
        delete.where(cb.equal(toolSite.get("siteId"), siteId));
        return session.createQuery(delete).executeUpdate();
    }

    @Transactional
    public int deleteByTool_Id(Long toolId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<LtiToolSite> delete = cb.createCriteriaDelete(LtiToolSite.class);
        Root<LtiToolSite> toolSite = delete.from(LtiToolSite.class);
        delete.where(cb.equal(toolSite.get("tool").get("id"), toolId));
        return session.createQuery(delete).executeUpdate();
    }
}
