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

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.services.question.QuestionSearchException;
import org.sakaiproject.tool.assessment.services.question.QuestionSearchResult;
import org.sakaiproject.tool.assessment.services.question.QuestionSearchService;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import lombok.extern.slf4j.Slf4j;
import net.htmlparser.jericho.Source;

/* For author: Search Questions backing bean. */
@Slf4j
@ManagedBean(name="searchQuestionBean")
@SessionScoped
public class SearchQuestionBean implements Serializable {

    private String selectedSection;
    private String selectedQuestionPool;
    private boolean comesFromPool;
    private String outcome;
    private String[] destItems = {};
    private HashMap<String, QuestionSearchResult> results;
    private static final String EDIT_POOL = "editPool";
    private static final String EDIT_ASSESSMENT = "editAssessment";
    private int resultsSize;
    private boolean showTags;
    private String tagDisabled;
    private String textToSearch;
    private String[] tagToSearch;

    // Services
    private static final TagService tagService = (TagService) ComponentManager.get(TagService.class);
    private static final QuestionSearchService questionSearchService = (QuestionSearchService) ComponentManager.get(QuestionSearchService.class);
    private static final ServerConfigurationService serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
    private static final SiteService siteService = (SiteService) ComponentManager.get(SiteService.class);

    private ItemService itemService = new ItemService();
    private QuestionPoolService questionPoolService = new QuestionPoolService();
    private AssessmentService assessmentService = new AssessmentService();

    private String tagToSearchLabel;
    private String lastSearchType;

    // Session-scoped caches (cleared on new search) - avoids memory leak in singleton service
    private Map<String, Boolean> questionsIOwn = new HashMap<>();
    private Map<String, String> titleCache = new HashMap<>();

    public SearchQuestionBean() {
        results = new HashMap<>();
        setResultsSize(0);
        setTextToSearch("");
        setTagToSearch(null);
        setTagToSearchLabel("");
        setShowTags(serverConfigurationService.getBoolean("samigo.author.usetags", false));
        if (getShowTags()) {
            setTagDisabled("");
        } else {
            setTagDisabled("style=display:none");
        }
    }

    public void searchQuestionsByTag(String[] tagList, boolean andOption) {
        // Clear caches for new search
        questionsIOwn.clear();
        titleCache.clear();
        setResults(new HashMap<>());
        setResultsSize(0);

        // Build tag labels for display AND for search (single lookup)
        List<String> tagLabelsForSearch = new ArrayList<>();
        StringBuilder tagsLabelsDisplay = new StringBuilder();

        if (tagList != null) {
            boolean first = true;
            for (String tagId : tagList) {
                if (tagService.getTags().getForId(tagId).isPresent()) {
                    Tag tag = tagService.getTags().getForId(tagId).get();
                    String tagLabel = tag.getTagLabel();
                    String tagCollectionName = tag.getCollectionName();
                    String fullLabel = tagLabel + "(" + tagCollectionName + ")";

                    // For search
                    tagLabelsForSearch.add(fullLabel);

                    // For display
                    if (!first) {
                        tagsLabelsDisplay.append(",");
                    }
                    tagsLabelsDisplay.append(" ").append(fullLabel);
                    first = false;
                }
            }
        }
        setTagToSearch(tagList);
        setTagToSearchLabel(tagsLabelsDisplay.toString());

        try {
            // Use the service to search (passing labels, not IDs)
            List<QuestionSearchResult> searchResults = questionSearchService.searchByTags(tagLabelsForSearch, andOption);

            // Store results directly (no conversion needed)
            for (QuestionSearchResult qsr : searchResults) {
                results.put(qsr.getId(), qsr);
            }

            setResults(results);
            setResultsSize(results.size());

        } catch (QuestionSearchException ex) {
            log.warn("Error searching questions by tags: {}", ex.getMessage());
            String errorMsg = ContextUtil.getLocalizedString(
                "org.sakaiproject.tool.assessment.bundle.AuthorMessages", "tag_tags_error2");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg, null));
        }

        setTextToSearch("");
    }

    public void searchQuestionsByText(boolean andOption) {
        // Clear caches for new search
        questionsIOwn.clear();
        titleCache.clear();
        setResults(new HashMap<>());
        setResultsSize(0);

        String searchText = getTextToSearch();
        // Remove HTML code to search only by the real text
        Source parseSearchTerms = new Source(searchText);
        searchText = parseSearchTerms.getTextExtractor().toString();
        this.setTextToSearch(searchText);

        try {
            // Use the service to search
            List<QuestionSearchResult> searchResults = questionSearchService.searchByText(searchText, andOption);

            // Store results directly (no conversion needed)
            for (QuestionSearchResult qsr : searchResults) {
                results.put(qsr.getId(), qsr);
            }

            setResults(results);
            setResultsSize(results.size());

        } catch (QuestionSearchException ex) {
            log.warn("Error searching questions by text: {}", ex.getMessage());
            String errorMsg = ContextUtil.getLocalizedString(
                "org.sakaiproject.tool.assessment.bundle.AuthorMessages", "tag_tags_error2");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg, null));
        }

        setTagToSearch(null);
        setTagToSearchLabel("");
    }


    public boolean userOwns(String questionId) {
        // To know if the actual user owns a question... just search for it with the "own" parameter.
        // If results length is 0... then you don't own it :)
        // This is needed to avoid the view of not owned question by someone that
        // tries to preview a question (for instance calling the jsf directly).
        // To improve performance, we will store a "search-duration" cache of this,
        // so we don't call the search service once we know that result.

        if (questionsIOwn.containsKey(questionId)) {
            return questionsIOwn.get(questionId);
        } else {
            boolean owns = questionSearchService.userOwnsQuestion(questionId);
            questionsIOwn.put(questionId, owns);
            return owns;
        }
    }

    public List<String> originFull(String hash) {
        // We will return all the origins based on a hash, using session-scoped cache
        return questionSearchService.getQuestionOrigins(hash, titleCache);
    }

    /**
     * Get the display origin for a question result.
     * Resolves the origin lazily using session-scoped cache.
     */
    public String getOriginDisplay(QuestionSearchResult result) {
        if (result == null) {
            return "";
        }

        try {
            if (result.isFromQuestionPool()) {
                String cacheKey = "qp:" + result.getQuestionPoolId();
                if (titleCache.containsKey(cacheKey)) {
                    return titleCache.get(cacheKey);
                }
                String title = questionPoolService.getPool(
                    Long.parseLong(result.getQuestionPoolId()), AgentFacade.getAgentString()).getTitle();
                titleCache.put(cacheKey, title);
                return title;
            }

            if (result.isFromAssessment()) {
                String siteCacheKey = "site:" + result.getSiteId();
                String assessmentCacheKey = "assessment:" + result.getAssessmentId();

                String siteTitle;
                if (titleCache.containsKey(siteCacheKey)) {
                    siteTitle = titleCache.get(siteCacheKey);
                } else {
                    siteTitle = siteService.getSite(result.getSiteId()).getTitle();
                    titleCache.put(siteCacheKey, siteTitle);
                }

                String assessmentTitle;
                if (titleCache.containsKey(assessmentCacheKey)) {
                    assessmentTitle = titleCache.get(assessmentCacheKey);
                } else {
                    assessmentTitle = assessmentService.getAssessment(result.getAssessmentId()).getTitle();
                    titleCache.put(assessmentCacheKey, assessmentTitle);
                }

                return siteTitle + " : " + assessmentTitle;
            }
        } catch (Exception ex) {
            log.debug("Could not resolve origin for question {}: {}", result.getId(), ex.getMessage());
        }

        return "";
    }

    public ItemFacade getItem(String itemId){
        ItemService itemService = new ItemService();
        return itemService.getItem(itemId);
    }

    public ItemDataIfc getData(String itemId){
        ItemService itemService = new ItemService();
        return itemService.getItem(itemId).getData();
    }

    public String getSelectedSection() {return selectedSection;}

    public void setSelectedSection(String pstr) {selectedSection= pstr;}

    public void setDestItems(String[] pPool) {destItems= pPool;}

    public String[] getDestItems() {return destItems;}

    public String getOutcome() {return outcome;}

    public void setOutcome(String param) {this.outcome= param;}

    public void setResults(HashMap<String, QuestionSearchResult> list) { results = list; }

    public HashMap<String, QuestionSearchResult> getResults() { return results; }

    public int getResultsSize() { return resultsSize; }

    public void setResultsSize(int resultSize) { this.resultsSize = resultSize; }

    public boolean getShowTags() {return showTags;}

    public void setShowTags(boolean showTags) {this.showTags = showTags;}

    public String getSelectedQuestionPool() {return selectedQuestionPool;}

    public void setSelectedQuestionPool(String selectedQuestionPool) {this.selectedQuestionPool = selectedQuestionPool;}

    public boolean isComesFromPool() {return comesFromPool;}

    public void setComesFromPool(boolean comesFromPool) {this.comesFromPool = comesFromPool;}

    public String getTextToSearch() {return textToSearch;}

    public void setTextToSearch(String textToSearch) {this.textToSearch = textToSearch;}

    public String[] getTagToSearch() {return tagToSearch;}

    public void setTagToSearch(String[] tagToSearch) {this.tagToSearch = tagToSearch;}

    public String getTagToSearchLabel() {return tagToSearchLabel;}

    public void setTagToSearchLabel(String tagToSearchLabel) {this.tagToSearchLabel = tagToSearchLabel;}

    public String getTagDisabled() {return tagDisabled;}

    public void setTagDisabled(String tagDisabled) {this.tagDisabled = tagDisabled;}

    public String getLastSearchType() {return lastSearchType;}

    public void setLastSearchType(String lastSearchType) {this.lastSearchType = lastSearchType;}

    public String doit() {
        return outcome;
    }

    public String cancelSearchQuestion() {
        results = new HashMap<>();
        titleCache.clear();
        questionsIOwn.clear();
        setResultsSize(0);
        setTextToSearch("");
        setTagToSearch(null);
        setTagToSearchLabel("");
        String[] emptyArr = {};
        setDestItems(emptyArr);
        if (comesFromPool) {
            setOutcome(EDIT_POOL);
        } else {
            setOutcome(EDIT_ASSESSMENT);
        }
        return getOutcome();
    }
}
