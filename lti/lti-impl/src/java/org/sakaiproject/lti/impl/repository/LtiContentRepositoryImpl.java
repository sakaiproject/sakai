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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.sql.Timestamp;
import java.util.Optional;
import java.time.Instant;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.dialect.Oracle8iDialect;

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
        return ((Number) toolLinksQuery(filter, "count(*)", null, true).uniqueResult()).longValue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LtiContent> findToolLinks(ToolLinkFilter filter, String sortField, boolean ascending, int start, int length) {
        if (start < 0 || length < 1 || length > 200) {
            throw new IllegalArgumentException("Invalid Tool Links page");
        }
        return this.<LtiContent>toolLinksQuery(filter, "c.*", sortField, ascending).addEntity(LtiContent.class)
                .setFirstResult(start).setMaxResults(length).list();
    }

    private <T> NativeQuery<T> toolLinksQuery(ToolLinkFilter filter, String select, String sortField, boolean ascending) {
        StringBuilder sql = new StringBuilder("select ").append(select).append(" from lti_content c");
        if ("searchURL".equals(sortField) || filter.text().containsKey("searchURL")) {
            sql.append(" left join lti_tools t on t.id = c.tool_id");
        }
        Map<String, Object> parameters = new LinkedHashMap<>();
        if ("SITE_TITLE".equals(sortField) || filter.text().containsKey("SITE_TITLE")) {
            sql.append(" left join SAKAI_SITE s on s.SITE_ID = c.SITE_ID");
        }
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("SITE_CONTACT_NAME", "contact-name");
        properties.put("SITE_CONTACT_EMAIL", "contact-email");
        properties.put("ATTRIBUTION", filter.attributionProperty());
        for (Map.Entry<String, String> property : properties.entrySet()) {
            String field = property.getKey();
            if (field.equals(sortField) || filter.text().containsKey(field)) {
                sql.append(" left join SAKAI_SITE_PROPERTY ").append(field)
                        .append(" on ").append(field).append(".SITE_ID = c.SITE_ID and ")
                        .append(field).append(".NAME = :").append(field);
                parameters.put(field, property.getValue());
            }
        }
        sql.append(" where 1 = 1");
        if (!filter.admin()) {
            sql.append(" and (c.SITE_ID = :siteId or c.SITE_ID is null)");
            parameters.put("siteId", filter.siteId());
        }
        if (filter.toolId() != null) {
            sql.append(" and c.tool_id = :toolId");
            parameters.put("toolId", filter.toolId());
        }
        for (Map.Entry<String, String> entry : filter.text().entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isEmpty()) continue;
            String parameter = "search" + parameters.size();
            // Escape LIKE syntax so searches remain literal substring matches.
            parameters.put(parameter, "%" + value.toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%");
            String like = " like :" + parameter + " escape '!'";
            if ("searchURL".equals(entry.getKey())) {
                sql.append(" and (lower(c.launch)").append(like).append(" or lower(t.launch)").append(like).append(")");
            } else {
                sql.append(" and lower(").append(toolLinkColumn(entry.getKey())).append(")").append(like);
            }
        }
        if (filter.date() != null) {
            parameters.put("created", Timestamp.from(filter.date()));
            if (filter.dateOperator() == '<' || filter.dateOperator() == '>') {
                sql.append(" and c.created_at ").append(filter.dateOperator()).append(" :created");
            } else {
                sql.append(" and c.created_at >= :created and c.created_at < :createdEnd");
                parameters.put("createdEnd", Timestamp.from(filter.date().plusSeconds(1)));
            }
        }
        if (sortField != null) {
            String column = toolLinkColumn(sortField);
            if (properties.containsKey(sortField)
                    && sessionFactory.unwrap(SessionFactoryImplementor.class).getJdbcServices().getDialect() instanceof Oracle8iDialect) {
                // Oracle cannot order CLOBs directly; property ordering uses the first 4000 characters.
                column = "dbms_lob.substr(" + column + ", 4000, 1)";
            }
            String direction = ascending ? " asc" : " desc";
            sql.append(" order by case when ").append(column).append(" is null then 1 else 0 end").append(direction)
                    .append(", ").append("created_at".equals(sortField) ? column : "lower(" + column + ")")
                    .append(direction).append(", c.id asc");
        }
        NativeQuery<T> query = sessionFactory.getCurrentSession().createNativeQuery(sql.toString());
        query.addSynchronizedEntityClass(LtiContent.class).addSynchronizedEntityClass(LtiTool.class);
        parameters.forEach(query::setParameter);
        return query;
    }

    private String toolLinkColumn(String field) {
        return switch (field) {
            case "title" -> "c.title";
            case "searchURL" -> "concat(coalesce(c.launch, ''), coalesce(t.launch, ''))";
            case "created_at" -> "c.created_at";
            case "SITE_TITLE" -> "s.TITLE";
            case "SITE_CONTACT_NAME", "SITE_CONTACT_EMAIL", "ATTRIBUTION" -> field + ".VALUE";
            default -> throw new IllegalArgumentException("Unsupported Tool Links column");
        };
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
