/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.beans.RubricTransferBean;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class RubricContentProducer implements EntityContentProducer {

    private RubricsService rubricsService;
    private SearchIndexBuilder searchIndexBuilder;
    private ServerConfigurationService serverConfigurationService;
    private SearchService searchService;
    private SiteService siteService;

    private List<String> addingEvents = new ArrayList<>();
    private List<String> removingEvents = new ArrayList<>();

    public void init() {
        if (searchIndexBuilder != null && searchService != null && serverConfigurationService.getBoolean("search.enable", false)) {
            addingEvents.add("rubric.create");
            addingEvents.add("rubric.update");
            removingEvents.add("rubric.delete");
            addingEvents.forEach(e -> searchService.registerFunction(e));
            removingEvents.forEach(e -> searchService.registerFunction(e));
            searchIndexBuilder.registerEntityContentProducer(this);
        }
    }


    private Optional<RubricTransferBean> getRubric(String ref) {
        try {
            // Extract rubric ID from reference like "/rubrics/rubric/123"
            String[] parts = ref.split("/");
            if (parts.length >= 3) {
                Long rubricId = Long.valueOf(parts[parts.length - 1]);
                return rubricsService.getRubric(rubricId);
            }
        } catch (Exception e) {
            log.error("Error parsing rubric reference: {}", ref, e);
        }
        return Optional.empty();
    }

    public String getContent(String ref) {
        Optional<RubricTransferBean> opRubric = getRubric(ref);
        return opRubric.map(RubricTransferBean::getTitle).orElse("");
    }

    public String getTitle(String ref) {
        Optional<RubricTransferBean> opRubric = getRubric(ref);
        return opRubric.map(RubricTransferBean::getTitle).orElse("");
    }

    public String getUrl(String ref) {
        Optional<RubricTransferBean> opRubric = getRubric(ref);
        if (opRubric.isPresent()) {
            RubricTransferBean rubric = opRubric.get();
            String siteId = rubric.getOwnerId();

            try {
                Site site = siteService.getSite(siteId);
                ToolConfiguration toolConfig = site.getToolForCommonId("sakai.rubrics");
                if (toolConfig != null) {
                    return serverConfigurationService.getPortalUrl()
                            + "/directtool/"
                            + toolConfig.getId()
                            + "?rubricId="
                            + rubric.getId()
                            + "&panel=Main&sakai_action=doView_rubric";
                }
            } catch (Exception e) {
                log.error("Failed to get deep link for context {} and rubric {}. Returning empty string.", siteId, ref, e);
            }
        }
        return "";
    }

    public boolean matches(String ref) {
        return ref.startsWith(RubricsService.REFERENCE_ROOT);
    }

    public Integer getAction(Event event) {
        String evt = event.getEvent();
        if (addingEvents.contains(evt)) return SearchBuilderItem.ACTION_ADD;
        if (removingEvents.contains(evt)) return SearchBuilderItem.ACTION_DELETE;
        return SearchBuilderItem.ACTION_UNKNOWN;
    }

    public boolean matches(Event event) {
        String evt = event.getEvent();
        return addingEvents.contains(evt) || removingEvents.contains(evt);
    }

    public String getTool() {
        return "rubrics";
    }

    public String getSiteId(String ref) {
        return getRubric(ref).map(RubricTransferBean::getOwnerId).orElse(null);
    }

    public Iterator<String> getSiteContentIterator(String context) {
        List<String> rv = new ArrayList<>();
        try {
            List<RubricTransferBean> rubrics = rubricsService.getRubricsForSite(context);
            rv = rubrics.stream()
                    .map(r -> RubricsService.REFERENCE_ROOT + "/rubric/" + r.getId())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Error getting site content for context: {}", context, e);
        }
        return rv.iterator();
    }


    public boolean canRead(String ref) {
        if (!matches(ref)) {
            return false;
        }

        Optional<RubricTransferBean> opRubric = getRubric(ref);
        if (opRubric.isPresent()) {
            RubricTransferBean rubric = opRubric.get();
            String siteId = rubric.getOwnerId();

            // Check if user can access the site
            try {
                siteService.getSiteVisit(siteId);
                // Don't index draft rubrics unless user has special permissions
                return !rubric.getDraft();
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }


    public String getId(String ref) {
        String[] parts = ref.split("/");
        return parts.length >= 3 ? parts[parts.length - 1] : null;
    }

    public String getType(String ref) {
        return "rubric";
    }


    public String getContainer(String ref) {
        return getSiteId(ref);
    }
}