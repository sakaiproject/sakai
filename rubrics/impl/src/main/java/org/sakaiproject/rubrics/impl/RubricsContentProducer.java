/**********************************************************************************
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.model.Criterion;
import org.sakaiproject.rubrics.api.model.Rating;
import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.repository.RubricRepository;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Indexes site rubrics (title, criteria and ratings) for Sakai search. Rubrics are an authoring tool,
 * so only rubrics editors in the owning site can see them in search results.
 *
 * References have the form {@code /rubrics/{siteId}/{rubricId}}, see {@link RubricsService#rubricReference}.
 */
@Slf4j
@Setter
public class RubricsContentProducer implements EntityContentProducer, EntityContentProducerEvents {

    private static final String REFERENCE_PREFIX = RubricsService.REFERENCE_ROOT + Entity.SEPARATOR;

    private static final Map<String, Integer> EVENT_ACTIONS = Map.of(
            RubricsConstants.EVENT_RUBRIC_CREATED, SearchBuilderItem.ACTION_ADD,
            RubricsConstants.EVENT_RUBRIC_UPDATED, SearchBuilderItem.ACTION_ADD,
            RubricsConstants.EVENT_RUBRIC_DELETED, SearchBuilderItem.ACTION_DELETE);

    private RubricRepository rubricRepository;
    private SearchIndexBuilder searchIndexBuilder;
    private SecurityService securityService;
    private ServerConfigurationService serverConfigurationService;
    private SiteService siteService;
    // Criteria and ratings load lazily, and the indexer calls us outside of any request transaction
    private TransactionTemplate transactionTemplate;

    public void init() {
        if (serverConfigurationService.getBoolean("search.enable", false)) {
            searchIndexBuilder.registerEntityContentProducer(this);
        }
    }

    @Override
    public Set<String> getTriggerFunctions() {
        return EVENT_ACTIONS.keySet();
    }

    @Override
    public boolean matches(String reference) {
        return parse(reference).isPresent();
    }

    @Override
    public boolean matches(Event event) {
        return EVENT_ACTIONS.containsKey(event.getEvent());
    }

    @Override
    public Integer getAction(Event event) {
        return EVENT_ACTIONS.getOrDefault(event.getEvent(), SearchBuilderItem.ACTION_UNKNOWN);
    }

    @Override
    public boolean canRead(String reference) {
        return parse(reference)
                .map(ref -> securityService.unlock(RubricsConstants.RBCS_PERMISSIONS_EDITOR, siteService.siteReference(ref.siteId())))
                .orElse(false);
    }

    @Override
    public String getTitle(String reference) {
        return withRubric(reference, Rubric::getTitle).orElse("");
    }

    @Override
    public String getContent(String reference) {
        return withRubric(reference, RubricsContentProducer::content).orElse("");
    }

    @Override
    public String getUrl(String reference) {
        return parse(reference).map(ref -> {
            try {
                ToolConfiguration tool = siteService.getSite(ref.siteId()).getToolForCommonId(RubricsConstants.RBCS_TOOL);
                if (tool != null) {
                    // The rubrics tool has no per rubric view, so link to the site's rubrics list
                    return serverConfigurationService.getPortalUrl() + "/site/" + ref.siteId() + "/tool/" + tool.getId();
                }
            } catch (Exception e) {
                log.debug("Could not build url for rubric {}: {}", reference, e.toString());
            }
            return "";
        }).orElse("");
    }

    @Override
    public String getTool() {
        return "rubrics";
    }

    @Override
    public String getType(String reference) {
        return "rubric";
    }

    @Override
    public String getId(String reference) {
        return parse(reference).map(ref -> ref.rubricId().toString()).orElse("");
    }

    @Override
    public String getSiteId(String reference) {
        return parse(reference).map(ref -> ref.siteId()).orElse(null);
    }

    @Override
    public String getContainer(String reference) {
        return parse(reference).map(ref -> ref.siteId()).orElse("");
    }

    @Override
    public Iterator<String> getSiteContentIterator(String siteId) {
        return transactionTemplate.execute(status -> rubricRepository.findByOwnerId(siteId).stream()
                .filter(RubricsContentProducer::isListed)
                .map(rubric -> RubricsService.rubricReference(siteId, rubric.getId()))
                .toList()
                .iterator());
    }

    /**
     * Applies the function to the referenced rubric. Nothing is returned for ad hoc rubrics, so the indexer,
     * which drops documents without content, never indexes them.
     */
    private <T> Optional<T> withRubric(String reference, Function<Rubric, T> function) {
        return parse(reference).flatMap(ref -> transactionTemplate.execute(status ->
                rubricRepository.findById(ref.rubricId())
                        .filter(rubric -> ref.siteId().equals(rubric.getOwnerId()))
                        .filter(RubricsContentProducer::isListed)
                        .map(function)));
    }

    // Ad hoc rubrics belong to a single item and are never listed in the rubrics tool
    private static boolean isListed(Rubric rubric) {
        return !Boolean.TRUE.equals(rubric.getAdhoc());
    }

    /** The rubric title followed by the text of every criterion and rating. The indexer strips any markup. */
    private static String content(Rubric rubric) {
        StringBuilder sb = new StringBuilder();
        append(sb, rubric.getTitle());
        for (Criterion criterion : rubric.getCriteria()) {
            append(sb, criterion.getTitle());
            append(sb, criterion.getDescription());
            for (Rating rating : criterion.getRatings()) {
                append(sb, rating.getTitle());
                append(sb, rating.getDescription());
            }
        }
        return sb.toString().trim();
    }

    private static void append(StringBuilder sb, String text) {
        if (StringUtils.isNotBlank(text)) {
            sb.append(text).append(' ');
        }
    }

    private static Optional<RubricRef> parse(String reference) {
        if (reference == null || !reference.startsWith(REFERENCE_PREFIX)) {
            return Optional.empty();
        }
        String[] parts = reference.substring(REFERENCE_PREFIX.length()).split(Entity.SEPARATOR);
        if (parts.length != 2 || parts[0].isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new RubricRef(parts[0], Long.valueOf(parts[1])));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private record RubricRef(String siteId, Long rubricId) { }
}
