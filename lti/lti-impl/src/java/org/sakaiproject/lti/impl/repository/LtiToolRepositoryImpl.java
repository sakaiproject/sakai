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
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.hibernate.Session;

import org.springframework.transaction.annotation.Transactional;

import org.sakaiproject.lti.api.model.LtiTool;
import org.sakaiproject.lti.api.model.LtiToolSite;
import org.sakaiproject.lti.api.repository.LtiToolRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

public class LtiToolRepositoryImpl extends SpringCrudRepositoryImpl<LtiTool, Long> implements LtiToolRepository {

    @Transactional(readOnly = true)
    public List<LtiTool> findBySiteId(String siteId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiTool> query = cb.createQuery(LtiTool.class);
        Root<LtiTool> tool = query.from(LtiTool.class);
        query.where(cb.equal(tool.get("siteId"), siteId));
        return session.createQuery(query).list();
    }

    @Transactional(readOnly = true)
    public Optional<LtiTool> findVisibleTool(Long id, String siteId, boolean isAdmin) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiTool> query = cb.createQuery(LtiTool.class);
        Root<LtiTool> tool = query.from(LtiTool.class);

        Predicate byId = cb.equal(tool.get("id"), id);

        if (isAdmin) {
            query.where(byId);
        } else {
            // Owned by the requesting site
            Predicate ownSite = cb.equal(tool.get("siteId"), siteId);
            // Globally available (system) tool that is not stealthed
            Predicate globalVisible = cb.and(
                    cb.isNull(tool.get("siteId")),
                    cb.notEqual(tool.get("visible"), 1));
            // Deployed to the requesting site via lti_tool_site
            Subquery<Long> deployed = query.subquery(Long.class);
            Root<LtiToolSite> toolSite = deployed.from(LtiToolSite.class);
            deployed.select(toolSite.get("tool").get("id"))
                    .where(cb.equal(toolSite.get("siteId"), siteId));

            query.where(cb.and(byId, cb.or(ownSite, globalVisible, tool.get("id").in(deployed))));
        }

        return session.createQuery(query).uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<LtiTool> findVisibleTools(String siteId, boolean isAdmin, boolean includeStealthed, boolean includeLaunchable) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<LtiTool> query = cb.createQuery(LtiTool.class);
        Root<LtiTool> tool = query.from(LtiTool.class);

        if (!isAdmin) {
            // Owned by the requesting site
            Predicate ownSite = cb.equal(tool.get("siteId"), siteId);

            // Globally available (system) tools
            Predicate globalVisible;
            if (includeStealthed) {
                globalVisible = cb.isNull(tool.get("siteId"));
            } else {
                globalVisible = cb.and(
                        cb.isNull(tool.get("siteId")),
                        cb.notEqual(tool.get("visible"), 1));
            }

            if (includeLaunchable) {
                // Deployed to the requesting site via lti_tool_site
                Subquery<Long> deployed = query.subquery(Long.class);
                Root<LtiToolSite> toolSite = deployed.from(LtiToolSite.class);
                deployed.select(toolSite.get("tool").get("id"))
                        .where(cb.equal(toolSite.get("siteId"), siteId));
                query.where(cb.or(ownSite, globalVisible, tool.get("id").in(deployed)));
            } else {
                query.where(cb.or(ownSite, globalVisible));
            }
        }

        query.orderBy(cb.asc(tool.get("id")));
        return session.createQuery(query).list();
    }
}
