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
import java.util.Map;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.time.Instant;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
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

    @Override
    @Transactional(readOnly = true)
    public long countToolLinks(ToolLinkFilter filter) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<LtiContent> content = query.from(LtiContent.class);
        query.select(cb.count(content)).where(toolLinkPredicates(cb, content, filter));
        return sessionFactory.getCurrentSession().createQuery(query).getSingleResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findToolLinkSites(ToolLinkFilter filter) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<LtiContent> content = query.from(LtiContent.class);
        query.select(content.get("siteId")).distinct(true).where(toolLinkPredicates(cb, content, filter));
        return sessionFactory.getCurrentSession().createQuery(query).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LtiContent> findToolLinks(ToolLinkFilter filter, String sortField, boolean ascending, int start, int length) {
        if (start < 0 || length < 1 || length > 200) {
            throw new IllegalArgumentException("Invalid Tool Links page");
        }
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<LtiContent> query = cb.createQuery(LtiContent.class);
        Root<LtiContent> content = query.from(LtiContent.class);
        query.select(content).where(toolLinkPredicates(cb, content, filter));
        Expression<?> sort;
        if (filter.siteOrder() != null) {
            CriteriaBuilder.Case<Integer> rank = cb.selectCase();
            filter.siteOrder().forEach((site, value) -> rank.when(site == null
                    ? cb.isNull(content.get("siteId")) : cb.equal(content.get("siteId"), site), value));
            sort = rank.otherwise(filter.siteOrder().size());
        } else {
            sort = switch (sortField) {
                case "title" -> cb.lower(content.get("title"));
                case "created_at" -> content.get("createdAt");
                case "searchURL" -> cb.lower(cb.concat(cb.coalesce(content.get("launch"), ""),
                        cb.coalesce(content.join("tool", JoinType.LEFT).get("launch"), "")));
                default -> throw new IllegalArgumentException("Unsupported Tool Links column");
            };
        }
        Expression<Integer> missing = cb.<Integer>selectCase().when(cb.isNull(sort), 1).otherwise(0);
        query.orderBy(ascending ? cb.asc(missing) : cb.desc(missing),
                ascending ? cb.asc(sort) : cb.desc(sort), cb.asc(content.get("id")));
        return sessionFactory.getCurrentSession().createQuery(query)
                .setFirstResult(start).setMaxResults(length).getResultList();
    }

    private Predicate[] toolLinkPredicates(CriteriaBuilder cb, Root<LtiContent> content, ToolLinkFilter filter) {
        List<Predicate> predicates = new ArrayList<>();
        if (!filter.admin()) {
            predicates.add(cb.or(cb.equal(content.get("siteId"), filter.siteId()), cb.isNull(content.get("siteId"))));
        }
        if (filter.toolId() != null) {
            predicates.add(cb.equal(content.get("tool").get("id"), filter.toolId()));
        }
        if (filter.matchingSites() != null) {
            predicates.add(filter.matchingSites().isEmpty() ? cb.disjunction() : content.get("siteId").in(filter.matchingSites()));
        }
        for (Map.Entry<String, String> entry : filter.text().entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isEmpty()) continue;
            String pattern = "%" + value.toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
            switch (entry.getKey()) {
                case "title" -> predicates.add(cb.like(cb.lower(content.get("title")), pattern, '!'));
                case "searchURL" -> {
                    Join<LtiContent, LtiTool> tool = content.join("tool", JoinType.LEFT);
                    predicates.add(cb.or(cb.like(cb.lower(content.get("launch")), pattern, '!'),
                            cb.like(cb.lower(tool.get("launch")), pattern, '!')));
                }
                default -> throw new IllegalArgumentException("Unsupported Tool Links filter");
            }
        }
        if (filter.date() != null) {
            Expression<Instant> created = content.get("createdAt");
            predicates.add(switch (filter.dateOperator()) {
                case '<' -> cb.lessThan(created, filter.date());
                case '>' -> cb.greaterThan(created, filter.date());
                default -> cb.and(cb.greaterThanOrEqualTo(created, filter.date()),
                        cb.lessThan(created, filter.date().plusSeconds(1)));
            });
        }
        return predicates.toArray(new Predicate[0]);
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
        query.multiselect(content.get("tool").get("id"), cb.countDistinct(content.get("id")), cb.countDistinct(content.get("siteId")));
        query.groupBy(content.get("tool").get("id"));
        return session.createQuery(query).list();
    }

    @Transactional
    public int reassignTool(Long currentToolId, Long newToolId, String siteId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaUpdate<LtiContent> update = cb.createCriteriaUpdate(LtiContent.class);
        Root<LtiContent> content = update.from(LtiContent.class);
        update.set(content.get("tool"), session.byId(LtiTool.class).getReference(newToolId));
        update.set(content.get("updatedAt"), Instant.now());
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
}
