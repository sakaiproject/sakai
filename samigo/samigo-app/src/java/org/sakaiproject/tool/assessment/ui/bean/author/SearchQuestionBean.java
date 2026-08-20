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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.question.QuestionSearchResult;
import org.sakaiproject.tool.assessment.services.question.QuestionSearchService;

import lombok.extern.slf4j.Slf4j;
import net.htmlparser.jericho.Source;

@Slf4j
@ManagedBean(name="searchQuestionBean")
@SessionScoped
public class SearchQuestionBean implements Serializable {

    private static final String EDIT_POOL = "editPool";
    private static final String EDIT_ASSESSMENT = "editAssessment";

    private static final TagService tagService = (TagService) ComponentManager.get(TagService.class);
    private static final QuestionSearchService questionSearchService = (QuestionSearchService) ComponentManager.get(QuestionSearchService.class);
    private static final ServerConfigurationService serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);

    private final ItemService itemService = new ItemService();

    private String selectedSection;
    private String selectedQuestionPool;
    private boolean comesFromPool;
    private String outcome;
    // Selected question IDs from search results to be copied to a pool or assessment
    private String[] destItems = {};
    private Map<String, QuestionSearchResult> results;
    private boolean showTags;
    private String tagDisabled;
    private String textToSearch;
    private String[] tagToSearch;
    private String tagToSearchLabel;
    private String lastSearchType;
    private Map<String, Boolean> questionsIOwn = new HashMap<>();
    private Map<String, String> titleCache = new HashMap<>();

    public SearchQuestionBean() {
        results = new HashMap<>();
        textToSearch = "";
        tagToSearch = null;
        tagToSearchLabel = "";
        showTags = serverConfigurationService.getBoolean("samigo.author.usetags", false);
        tagDisabled = showTags ? "" : "style=display:none";
    }

    public void searchQuestionsByTag(String[] tagList, boolean andOption) {
        questionsIOwn.clear();
        titleCache.clear();
        results = new HashMap<>();

        List<String> tagLabelsForSearch = new ArrayList<>();
        StringBuilder tagsLabelsDisplay = new StringBuilder();

        if (tagList != null) {
            boolean first = true;
            for (String tagId : tagList) {
                Optional<Tag> tagOpt = tagService.getTags().getForId(tagId);
                if (tagOpt.isPresent()) {
                    Tag tag = tagOpt.get();
                    String fullLabel = tag.getTagLabel() + "(" + tag.getCollectionName() + ")";
                    tagLabelsForSearch.add(fullLabel);
                    if (!first) {
                        tagsLabelsDisplay.append(",");
                    }
                    tagsLabelsDisplay.append(" ").append(fullLabel);
                    first = false;
                }
            }
        }
        tagToSearch = tagList;
        tagToSearchLabel = tagsLabelsDisplay.toString();

        List<QuestionSearchResult> searchResults = questionSearchService.searchByTags(tagLabelsForSearch, andOption);
        for (QuestionSearchResult qsr : searchResults) {
            results.put(qsr.getId(), qsr);
        }
        textToSearch = "";
    }

    public void searchQuestionsByText(boolean andOption) {
        questionsIOwn.clear();
        titleCache.clear();
        results = new HashMap<>();

        Source parseSearchTerms = new Source(textToSearch);
        textToSearch = parseSearchTerms.getTextExtractor().toString();

        List<QuestionSearchResult> searchResults = questionSearchService.searchByText(textToSearch, andOption);
        for (QuestionSearchResult qsr : searchResults) {
            results.put(qsr.getId(), qsr);
        }
        tagToSearch = null;
        tagToSearchLabel = "";
    }

    public boolean userOwns(String questionId) {
        return questionsIOwn.computeIfAbsent(questionId, questionSearchService::userOwnsQuestion);
    }

    public List<String> originFull(String hash) {
        return questionSearchService.getQuestionOrigins(hash, titleCache);
    }

    public String getOriginDisplay(QuestionSearchResult result) {
        return questionSearchService.getOriginDisplay(result, titleCache);
    }

    public ItemFacade getItem(String itemId) {
        return itemService.getItem(itemId);
    }

    public ItemDataIfc getData(String itemId) {
        ItemFacade item = itemService.getItem(itemId);
        if (item == null) {
            return null;
        }
        return item.getData();
    }

    public String getSelectedSection() { return selectedSection; }
    public void setSelectedSection(String pstr) { selectedSection = pstr; }

    public String[] getDestItems() { return destItems; }
    public void setDestItems(String[] pPool) { destItems = pPool; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String param) { outcome = param; }

    public Map<String, QuestionSearchResult> getResults() { return results; }
    public void setResults(Map<String, QuestionSearchResult> list) { results = list; }

    public int getResultsSize() { return results.size(); }

    public boolean getShowTags() { return showTags; }
    public void setShowTags(boolean showTags) { this.showTags = showTags; }

    public String getSelectedQuestionPool() { return selectedQuestionPool; }
    public void setSelectedQuestionPool(String selectedQuestionPool) { this.selectedQuestionPool = selectedQuestionPool; }

    public boolean isComesFromPool() { return comesFromPool; }
    public void setComesFromPool(boolean comesFromPool) { this.comesFromPool = comesFromPool; }

    public String getTextToSearch() { return textToSearch; }
    public void setTextToSearch(String textToSearch) { this.textToSearch = textToSearch; }

    public String[] getTagToSearch() { return tagToSearch; }
    public void setTagToSearch(String[] tagToSearch) { this.tagToSearch = tagToSearch; }

    public String getTagToSearchLabel() { return tagToSearchLabel; }
    public void setTagToSearchLabel(String tagToSearchLabel) { this.tagToSearchLabel = tagToSearchLabel; }

    public String getTagDisabled() { return tagDisabled; }
    public void setTagDisabled(String tagDisabled) { this.tagDisabled = tagDisabled; }

    public String getLastSearchType() { return lastSearchType; }
    public void setLastSearchType(String lastSearchType) { this.lastSearchType = lastSearchType; }

    public String doit() {
        return outcome;
    }

    public String cancelSearchQuestion() {
        results = new HashMap<>();
        titleCache.clear();
        questionsIOwn.clear();
        textToSearch = "";
        tagToSearch = null;
        tagToSearchLabel = "";
        destItems = new String[]{};
        outcome = comesFromPool ? EDIT_POOL : EDIT_ASSESSMENT;
        return outcome;
    }
}
