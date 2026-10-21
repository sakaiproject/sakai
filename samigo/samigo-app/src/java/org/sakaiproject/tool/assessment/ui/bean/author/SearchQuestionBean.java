/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.bean.SessionScoped;
import jakarta.faces.context.FacesContext;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.samigo.api.search.QuestionSearchException;
import org.sakaiproject.samigo.api.search.QuestionSearchResult;
import org.sakaiproject.samigo.api.search.QuestionSearchService;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.htmlparser.jericho.Source;

/* For author: Search Questions backing bean. */
@Slf4j
@ManagedBean(name="searchQuestionBean")
@SessionScoped
public class SearchQuestionBean implements Serializable {

    private static final String EDIT_POOL = "editPool";
    private static final String EDIT_ASSESSMENT = "editAssessment";
    private static final String AUTHOR_MESSAGES = "org.sakaiproject.tool.assessment.bundle.AuthorMessages";

    private static final TagService tagService = ComponentManager.get(TagService.class);
    private static final QuestionSearchService questionSearchService = ComponentManager.get(QuestionSearchService.class);
    private static final ServerConfigurationService serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);

    private final ItemService itemService = new ItemService();

    @Getter @Setter private String selectedSection;
    @Getter @Setter private String selectedQuestionPool;
    @Getter @Setter private boolean comesFromPool;
    @Getter @Setter private String outcome;
    // ids of the search results selected for import
    @Getter @Setter private String[] destItems = {};
    // keyed by item id, in the order returned by the search
    @Getter private Map<String, QuestionSearchResult> results = new LinkedHashMap<>();
    @Getter private final boolean showTags;
    @Getter private final String tagDisabled;
    @Getter @Setter private String textToSearch = "";
    @Getter @Setter private String[] tagToSearch;
    @Getter @Setter private String tagToSearchLabel = "";
    @Getter @Setter private String lastSearchType;

    // Preview checks access for every rendered question, so remember the answers until the next search
    private final Map<String, Boolean> accessibleQuestions = new HashMap<>();

    public SearchQuestionBean() {
        showTags = serverConfigurationService.getBoolean("samigo.author.usetags", serverConfigurationService.getBoolean("tagservice.enable.integrations", true));
        tagDisabled = showTags ? "" : "style=display:none";
    }

    public void searchQuestionsByTag(String[] tagIds, boolean matchAll) {
        List<String> tags = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        if (tagIds != null) {
            for (String tagId : tagIds) {
                tagService.getTag(tagId).map(SearchQuestionBean::indexedTagValue).ifPresent(tag -> {
                    tags.add(tag);
                    labels.add(" " + tag);
                });
            }
        }
        tagToSearch = tagIds;
        tagToSearchLabel = String.join(",", labels);
        textToSearch = "";

        runSearch(() -> questionSearchService.searchByTags(tags, matchAll), "tag_text_error3");
    }

    public void searchQuestionsByText(boolean matchAll) {
        // Search only the visible text, not the HTML
        textToSearch = new Source(textToSearch).getTextExtractor().toString();
        tagToSearch = null;
        tagToSearchLabel = "";

        runSearch(() -> questionSearchService.searchByText(textToSearch, matchAll), "tag_text_error2");
    }

    private void runSearch(Supplier<List<QuestionSearchResult>> search, String errorMessageKey) {
        results = new LinkedHashMap<>();
        accessibleQuestions.clear();
        try {
            search.get().forEach(result -> results.put(result.getId(), result));
        } catch (QuestionSearchException e) {
            log.info("Question search failed: {}", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(ContextUtil.getLocalizedString(AUTHOR_MESSAGES, errorMessageKey)));
        }
    }

    // Tags are indexed as label(collection)
    private static String indexedTagValue(Tag tag) {
        return tag.getTagLabel() + "(" + tag.getCollectionName() + ")";
    }

    /**
     * Guards the preview, so a question outside the user's search scope can't be shown by calling the page directly.
     */
    public boolean userOwns(String questionId) {
        return accessibleQuestions.computeIfAbsent(questionId, questionSearchService::canAccessQuestion);
    }

    public List<String> originFull(String hash) {
        return questionSearchService.getOrigins(hash);
    }

    public ItemFacade getItem(String itemId) {
        return itemService.getItem(itemId);
    }

    public ItemDataIfc getData(String itemId) {
        ItemFacade item = itemService.getItem(itemId);
        return item != null ? item.getData() : null;
    }

    public int getResultsSize() {
        return results.size();
    }

    public String doit() {
        return outcome;
    }

    /** Forgets the current search, its results and the selection. */
    public void clearSearch() {
        results = new LinkedHashMap<>();
        accessibleQuestions.clear();
        textToSearch = "";
        tagToSearch = null;
        tagToSearchLabel = "";
        destItems = new String[] {};
    }

    public String cancelSearchQuestion() {
        clearSearch();
        outcome = comesFromPool ? EDIT_POOL : EDIT_ASSESSMENT;
        return outcome;
    }
}
