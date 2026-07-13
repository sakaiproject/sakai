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
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.hibernate.Session;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.lti.api.model.LtiContent;
import org.sakaiproject.lti.api.model.LtiTool;
import org.sakaiproject.lti.api.model.LtiToolSite;
import org.sakaiproject.lti.api.repository.LtiContentRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class LtiContentRepositoryImpl extends SpringCrudRepositoryImpl<LtiContent, Long> implements LtiContentRepository {

    @Transactional(readOnly = true)
    public List<LtiContent> findBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiContent> query = cb.createQuery(LtiContent.class);
        Root<LtiContent> content = query.from(LtiContent.class);
        query.where(cb.equal(content.get("siteId"), siteId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<LtiContent> findByTool_Id(Long toolId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiContent> query = cb.createQuery(LtiContent.class);
        Root<LtiContent> content = query.from(LtiContent.class);
        Join<LtiContent, LtiTool> tool = content.join("tool");
        query.where(cb.equal(tool.get("id"), toolId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<LtiContent> findVisibleContent(Long id, String siteId, boolean isAdmin) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiContent> query = cb.createQuery(LtiContent.class);
        Root<LtiContent> content = query.from(LtiContent.class);

        Predicate byId = cb.equal(content.get("id"), id);

        if (isAdmin) {
            query.where(byId);
        } else {
            // Owned by the requesting site or globally available (no site)
            Predicate visible = cb.or(
                    cb.equal(content.get("siteId"), siteId),
                    cb.isNull(content.get("siteId")));
            query.where(cb.and(byId, visible));
        }

        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<LtiContent> findVisibleContents(String siteId, boolean isAdmin) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiContent> query = cb.createQuery(LtiContent.class);
        Root<LtiContent> content = query.from(LtiContent.class);
        // Eagerly fetch the tool so tool.launch can be read after the session closes
        content.fetch("tool", JoinType.LEFT);
        query.select(content);
        if (!isAdmin) {
            // Owned by the requesting site or globally available (no site)
            query.where(cb.or(
                    cb.equal(content.get("siteId"), siteId),
                    cb.isNull(content.get("siteId"))));
        }
        query.orderBy(cb.asc(content.get("id")));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public List<Long[]> countContentsByTool() {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long[]> query = cb.createQuery(Long[].class);
        Root<LtiContent> content = query.from(LtiContent.class);
        Join<LtiContent, LtiTool> tool = content.join("tool");
        query.multiselect(tool.get("id"), cb.countDistinct(content.get("id")), cb.countDistinct(content.get("siteId")));
        query.groupBy(tool.get("id"));
        return session.createQuery(query).list();
    }

    @Transactional
    public int reassignTool(Long currentToolId, Long newToolId, String siteId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<LtiContent> update = cb.createCriteriaUpdate(LtiContent.class);
        Root<LtiContent> content = update.from(LtiContent.class);
        update.set(content.get("tool"), session.byId(LtiTool.class).getReference(newToolId));
        Predicate where = cb.equal(content.get("tool").get("id"), currentToolId);
        if (siteId != null) {
            where = cb.and(where, cb.equal(content.get("siteId"), siteId));
        }
        update.where(where);
        return session.createQuery(update).executeUpdate();
    }

    @Transactional(readOnly = true)
    public List<String> findSitesNeedingDeployment(Long toolId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<LtiContent> content = query.from(LtiContent.class);

        // NOT EXISTS: no lti_tool_site deployment for this tool in the content's site
        Subquery<Long> deployed = query.subquery(Long.class);
        Root<LtiToolSite> toolSite = deployed.from(LtiToolSite.class);
        deployed.select(cb.literal(1L));
        deployed.where(cb.and(
                cb.equal(toolSite.get("tool").get("id"), toolId),
                cb.equal(toolSite.get("siteId"), content.get("siteId"))));

        query.select(content.get("siteId")).distinct(true);
        query.where(cb.and(
                cb.equal(content.get("tool").get("id"), toolId),
                cb.isNotNull(content.get("siteId")),
                cb.not(cb.exists(deployed))));
        return session.createQuery(query).list();
    }

    @Transactional
    public int deleteBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaDelete<LtiContent> delete = cb.createCriteriaDelete(LtiContent.class);
        Root<LtiContent> content = delete.from(LtiContent.class);
        delete.where(cb.equal(content.get("siteId"), siteId));
        return session.createQuery(delete).executeUpdate();
    }
}
