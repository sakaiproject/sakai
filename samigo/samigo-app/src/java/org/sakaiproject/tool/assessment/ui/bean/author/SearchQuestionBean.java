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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
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
public class SearchQuestionBean   implements Serializable {

    private String selectedSection;

    private String selectedQuestionPool;
    private boolean comesFromPool;
    private String outcome;
    private String[] destItems = {  }; // items to delete
    private HashMap<String,ItemSearchResult> results;
    private final static String EDIT_POOL = "editPool";
    private final static String EDIT_ASSESSMENT = "editAssessment";
    private int resultsSize;
    private boolean showTags;
    private String tagDisabled;
    private String textToSearch;
    private String[] tagToSearch;
    private static final TagService tagService= (TagService) ComponentManager.get( TagService.class );
    private static final QuestionSearchService questionSearchService = (QuestionSearchService) ComponentManager.get(QuestionSearchService.class);
    private static final ServerConfigurationService serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
    private ItemService itemService = new ItemService();
    private String tagToSearchLabel;
    private String lastSearchType;

    // Cache for userOwns() checks to avoid repeated searches
    private HashMap<String,Boolean> questionsIOwn = new HashMap<String,Boolean>();

    public SearchQuestionBean() {

        results = new HashMap<>();
        setResultsSize(0);
        setTextToSearch("");
        setTagToSearch(null);
        setTagToSearchLabel("");
        setShowTags(serverConfigurationService.getBoolean("samigo.author.usetags",false));
        if (getShowTags()){
            setTagDisabled("");
        }else{
            setTagDisabled("style=display:none");
        }


    }

    public void searchQuestionsByTag (String[] tagList, boolean andOption){

        questionsIOwn.clear();
        setResults(new HashMap<>());
        setResultsSize(0);

        // Build tag labels for display
        String tagsLabels ="";
        if (tagList!=null) {
            Boolean more = false;
            for (String s : tagList) {
                if (more) {
                    tagsLabels += ",";
                }
                if (tagService.getTags().getForId(s).isPresent()) {
                    Tag tag = tagService.getTags().getForId(s).get();
                    String tagLabel = tag.getTagLabel();
                    String tagCollectionName = tag.getCollectionName();
                    tagsLabels += " " + tagLabel + "(" + tagCollectionName + ")";
                    more = true;
                }
            }
        }
        setTagToSearch(tagList);
        setTagToSearchLabel(tagsLabels);

        // Use the service to search
        List<QuestionSearchResult> searchResults = questionSearchService.searchByTags(tagList, andOption);

        // Convert to ItemSearchResult and filter by /sam_item/ prefix
        for (QuestionSearchResult qsr : searchResults) {
            try {
                if (qsr.getId().startsWith("/sam_item/")) {
                    String itemId = qsr.getId().substring(10);
                    ItemSearchResult item = new ItemSearchResult(
                        itemId,
                        qsr.getTypeId(),
                        qsr.getQuestionText(),
                        qsr.getTags(),
                        qsr.getOrigin()
                    );
                    results.put(itemId, item);
                }
            } catch (Exception ex) {
                log.debug("Error processing search result: " + qsr.getId());
            }
        }

        setResults(results);
        setResultsSize(results.size());
        setTextToSearch("");

    }

    public void searchQuestionsByText (boolean andOption){
        questionsIOwn.clear();
        setResults(new HashMap<>());
        setResultsSize(0);

        String textToSearch = getTextToSearch();
        // Remove HTML code to search only by the real text
        Source parseSearchTerms = new Source(textToSearch);
        textToSearch = parseSearchTerms.getTextExtractor().toString();
        this.setTextToSearch(textToSearch);

        // Use the service to search
        List<QuestionSearchResult> searchResults = questionSearchService.searchByText(textToSearch, andOption);

        // Convert to ItemSearchResult and filter by /sam_item/ prefix
        for (QuestionSearchResult qsr : searchResults) {
            try {
                if (qsr.getId().startsWith("/sam_item/")) {
                    String itemId = qsr.getId().substring(10);
                    ItemSearchResult item = new ItemSearchResult(
                        itemId,
                        qsr.getTypeId(),
                        qsr.getQuestionText(),
                        qsr.getTags(),
                        qsr.getOrigin()
                    );
                    results.put(itemId, item);
                }
            } catch (Exception ex) {
                log.debug("Error processing search result: " + qsr.getId());
            }
        }

        setResults(results);
        setResultsSize(results.size());
        setTagToSearch(null);
        setTagToSearchLabel("");

    }


    public boolean userOwns(String questionId){
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

    public List<String> originFull(String hash){
        // We will return all the origins based on a hash
        return questionSearchService.getQuestionOrigins(hash);
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

    public void setResults(HashMap<String, ItemSearchResult> list) { results = list; }

    public HashMap<String, ItemSearchResult> getResults() { return results; }

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

    public String doit(){
        return outcome;
    }

    public String cancelSearchQuestion() {
        results = new HashMap<>();
        setResultsSize(0);
        setTextToSearch("");
        setTagToSearch(null);
        setTagToSearchLabel("");
        String[] emptyArr = {};
        setDestItems(emptyArr);
        if (comesFromPool){
            setOutcome(EDIT_POOL);
        }else{
            setOutcome(EDIT_ASSESSMENT);
        }
        return getOutcome();
    }

    public class ItemSearchResult {

        private String idString;
        private String typeId;
        private String qText;
        private Set<String> tagSet;
        private String Origin;



        public String getIdString() {
            return idString;
        }

        public void setIdString(String idString) {
            this.idString = idString;
        }

        public String getTypeId() {
            return typeId;
        }

        public void setType(String typeId) {
            this.typeId = typeId;
        }

        public String getqText() {
            return qText;
        }

        public void setqText(String qText) {
            this.qText = qText;
        }

        public Set<String> getTagSet() {
            return tagSet;
        }

        public void setTagSet(Set<String> tagSet) {
            this.tagSet = tagSet;
        }

        public String getOrigin() {
            return Origin;
        }

        public void setOrigin(String origin) {
            Origin = origin;
        }

        public ItemSearchResult(String idString, String typeId, String qText, Set<String> tagSet, String origin) {
            this.idString = idString;
            this.typeId = typeId;
            this.qText = qText;
            this.tagSet = tagSet;
            Origin = origin;
        }

    }

}
