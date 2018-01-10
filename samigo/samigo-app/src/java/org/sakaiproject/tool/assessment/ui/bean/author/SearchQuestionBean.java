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
import javax.faces.context.FacesContext;

import net.htmlparser.jericho.Source;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

@Slf4j
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
    private static final SearchService searchService= (SearchService) ComponentManager.get( SearchService.class );
    private static final SiteService siteService= (SiteService) ComponentManager.get( SiteService.class );
    private static final ServerConfigurationService serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
    private ItemService itemService = new ItemService();
    private ItemFacadeQueriesAPI itemFacadeQueries;
    private AssessmentService assessmentService = new AssessmentService();
    private QuestionPoolService questionPoolService = new QuestionPoolService();
    private String tagToSearchLabel;
    private String lastSearchType;

    //This cache will avoid calling multiple times the services to know the name of sites, and assessments.
    //It will live during the Bean live... so it will be useful for preview too.
    private HashMap<String,String> qpTitlesCache = new HashMap<String,String>();
    private HashMap<String,String> assessmentTitlesCache = new HashMap<String,String>();
    private HashMap<String,String> siteTitlesCache = new HashMap<String,String>();
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

        HashMap<String,ItemSearchResult> resultsTemp = new HashMap<>();
        questionsIOwn.clear();
        setResults(new HashMap<>());
        setResultsSize(0);
        HashMap<String,String> additionalSearchInformation = new HashMap<String,String>();
        additionalSearchInformation.put("group","hash");
        additionalSearchInformation.put("scope","own");
        additionalSearchInformation.put("subtype","item");
        if (andOption) {
            additionalSearchInformation.put("logic","and");
        }else{
            additionalSearchInformation.put("logic","or");
        }

        String tagsLabels ="";
        if (tagList!=null) {
            Boolean more = false;
            int i=1;
            for (String s : tagList) {

                if (more) {
                    tagsLabels += ",";
                }
                if (tagService.getTags().getForId(s).isPresent()) {
                    Tag tag = tagService.getTags().getForId(s).get();
                    String tagLabel = tag.getTagLabel();
                    String tagCollectionName = tag.getCollectionName();
                    tagsLabels += " " + tagLabel + "(" + tagCollectionName + ")";
                    additionalSearchInformation.put("tag_" + i,tagLabel + "(" + tagCollectionName + ")");
                    more = true;
                }
                i++;
            }
        }
        setTagToSearch(tagList);
        setTagToSearchLabel(tagsLabels);

        try {

            SearchResponse sr = searchService.searchResponse("", null, 0, 0, "questions",additionalSearchInformation);
            log.debug("This is the search response: " + sr.toString());
            Terms dedup = sr.getAggregations().get("dedup");

            for (Terms.Bucket entry : dedup.getBuckets()) {
                String key = entry.getKey();                    // bucket key
                long docCount = entry.getDocCount();            // Doc count
                log.debug("key [{"+key+"}], doc_count [{"+docCount+"}]");

                TopHits topHits = entry.getAggregations().get("dedup_docs");
                for (SearchHit hit : topHits.getHits().getHits()) {
                    log.debug(" -> id [{"+hit.getId()+"}]");
                    String typeId = hit.field("typeId").getValue();
                    String qText = hit.field("qText").getValue();
                    Set<String> tagsSet = getTagsFromString(hit.field("tags").getValues());
                    ItemSearchResult itemTemp = new ItemSearchResult(hit.getId(),typeId,qText,tagsSet,origin(hit));
                    resultsTemp.put(hit.getId(),itemTemp);
                }
            }

        }catch (org.sakaiproject.search.api.InvalidSearchQueryException ex){
            log.info("Error in the search: " + ex.getMessage());
            String publish_error= ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","tag_tags_error2");
            FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(publish_error));
        }

        for (String q : resultsTemp.keySet()) {
            try {
                if (q.startsWith("/sam_item/")) {
                    ItemSearchResult item = resultsTemp.get(q);
                    item.setIdString(q.substring(10));
                    results.put(q.substring(10), item);
                }
            }catch (Exception ex){
                log.debug("Error finding the indexed question: " + q);
            }
        }

        setResults(results);
        if (results==null) {
            setResultsSize(0);
        }else{
            setResultsSize(results.size());
        }
        setTextToSearch("");

    }

    public void searchQuestionsByText (boolean andOption){
        HashMap<String,ItemSearchResult> resultsTemp = new HashMap<>();
        questionsIOwn.clear();
        setResults(new HashMap<>());
        setResultsSize(0);
        HashMap<String,String> additionalSearchInformation = new HashMap<String,String>();
        additionalSearchInformation.put("group","hash");
        additionalSearchInformation.put("scope","own");
        additionalSearchInformation.put("subtype","item");
        if (andOption) {
            additionalSearchInformation.put("logic","and");
        }else{
            additionalSearchInformation.put("logic","or");
        }
        String textToSearch =getTextToSearch();
        //Let's remove the HTML code to search only by the real text.
        Source parseSearchTerms = new Source(textToSearch);
        textToSearch = parseSearchTerms.getTextExtractor().toString();
        this.setTextToSearch(textToSearch);

        try {
            SearchResponse sr = searchService.searchResponse(textToSearch, null, 0, 0, "questions", additionalSearchInformation);
            log.debug("This is the search repsonse: " + sr.toString());
            Terms dedup = sr.getAggregations().get("dedup");
            // For each entry
            for (Terms.Bucket entry : dedup.getBuckets()) {

                String key = entry.getKey();                    // bucket key
                long docCount = entry.getDocCount();            // Doc count
                log.debug("key [{"+key+"}], doc_count [{"+docCount+"}]");

                // We ask for top_hits for each bucket
                TopHits topHits = entry.getAggregations().get("dedup_docs");
                for (SearchHit hit : topHits.getHits().getHits()) {
                    log.debug(" -> id [{"+hit.getId()+"}]");
                    String typeId = hit.field("typeId").getValue();
                    String qText = hit.field("qText").getValue();
                    Set<String> tagsSet = getTagsFromString(hit.field("tags").getValues());
                    ItemSearchResult itemTemp = new ItemSearchResult(hit.getId(),typeId,qText,tagsSet,origin(hit));
                    resultsTemp.put(hit.getId(),itemTemp);
                    //resultIds.put(hit.getId(),origin(hit));
                }
            }


        }catch (org.sakaiproject.search.api.InvalidSearchQueryException ex){
            log.info("Error in the search: " + ex.getMessage());
            String publish_error= ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","tag_text_error3");
            FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(publish_error));
        }catch (java.lang.NullPointerException e){
            log.info("Error in the search: " + e.getMessage());
            String publish_error= ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","tag_text_error2");
            FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(publish_error));
        }


        for (String q : resultsTemp.keySet()) {
            try {
                if (q.startsWith("/sam_item/")) {
                    ItemSearchResult item = resultsTemp.get(q);
                    item.setIdString(q.substring(10));
                    results.put(q.substring(10), item);
                }
            }catch (Exception ex){
                log.debug("Error finding the indexed question: " + q);
            }
        }
        setResults(results);
        if (results==null) {
            setResultsSize(0);
        }else{
            setResultsSize(results.size());
        }
        setTagToSearch(null);
        setTagToSearchLabel("");

    }


    private String origin(SearchHit hit){
        try {
            if ((hit.field("questionPoolId") != null) && (hit.field("questionPoolId").value()!=null)) {
                //First we check if we have the question pool title in the temporal cache
                if (qpTitlesCache.containsKey(hit.field("questionPoolId").value())){
                     return qpTitlesCache.get(hit.field("questionPoolId").value());
                }else {
                    //If not... we retrieve the title and add it to the cache map.
                    //We will do the same with the assessmentTitle and the siteTitle
                    String qpTitle = questionPoolService.getPool(Long.parseLong(hit.field("questionPoolId").value()), AgentFacade.getAgentString()).getTitle();
                    qpTitlesCache.put(hit.field("questionPoolId").value(),qpTitle);
                    return qpTitle;
                }
            }else if ((hit.field("assessmentId") != null) && (hit.field("assessmentId").value() != null) && (hit.field("site") != null) && (hit.field("site").value() != null)) {
                    String assessmentTitle="";
                    String siteTitle="";
                    if (assessmentTitlesCache.containsKey(hit.field("assessmentId").value())) {
                        assessmentTitle = assessmentTitlesCache.get(hit.field("assessmentId").value());
                    }else{
                        assessmentTitle = assessmentService.getAssessment(hit.field("assessmentId").value().toString()).getTitle();
                        assessmentTitlesCache.put(hit.field("assessmentId").value(),assessmentTitle);
                    }
                    if (siteTitlesCache.containsKey(hit.field("site").value())) {
                        siteTitle = siteTitlesCache.get(hit.field("site").value());
                    }else{
                        siteTitle = siteService.getSite(hit.field("site").value().toString()).getTitle();
                        siteTitlesCache.put(hit.field("site").value(),siteTitle);
                    }

                    return siteTitle + " : " + assessmentTitle;
            }else {
                return "";
            }
        }catch(Exception ex){
            //Maybe a question is orphan and has not assessment or question pool. In that case we just return empty string
            return "";
        }
    }

    private Set<String> getTagsFromString(List tags){
        Set<String> tagSet = new HashSet<>();
        Iterator<String> iterator = tags.iterator();
        while (iterator.hasNext()){
            tagSet.add(iterator.next().toString());
        }
        return tagSet;
    }


    public boolean userOwns(String questionId){
        // To know if the actual user owns a question... just search for it with the "own" parameter.
        // If results length is 0... then you don't own it :)
        // This is needed to avoid the view of not owned question by someone that
        // tries to preview a question (for instance calling the jsf directly).
        // To improve performance, we will store a "search-duration" cache of this,
        // so we don't call the search service once we know that result.

        if (questionsIOwn.containsKey(questionId)){
            if (questionsIOwn.get(questionId)){
                return true;
            }else{
                return false;
            }
        }else {

            HashMap<String, String> additionalSearchInformation = new HashMap<String, String>();
            additionalSearchInformation.put("scope", "own");
            additionalSearchInformation.put("subtype", "item");
            additionalSearchInformation.put("questionId", questionId);

            try {

                SearchResponse sr = searchService.searchResponse("", null, 0, 1, "questions", additionalSearchInformation);
                if (sr.getHits().totalHits() < 1) {
                    questionsIOwn.put(questionId, Boolean.FALSE);
                    return false;
                } else {
                    questionsIOwn.put(questionId, Boolean.TRUE);
                    return true;
                }

            } catch (Exception ex) {
                //Failure searching. It should not happen, Let's return false.
                log.warn("Failure calculating permissions to preview");
                return false;
            }
        }
    }

    public List<String> originFull(String hash){
        //We will return all the origins based in a hash.
        List<String> origins = new ArrayList<>();

        HashMap<String,String> additionalSearchInformation = new HashMap<String,String>();
        additionalSearchInformation.put("scope","own");
        additionalSearchInformation.put("subtype","item");
        additionalSearchInformation.put("hash",hash);
        try {

            SearchResponse sr = searchService.searchResponse("", null, 0, 1000, "questions", additionalSearchInformation);

            for (SearchHit hit : sr.getHits()) {
                origins.add(origin(hit));
            }

        }catch (Exception ex){
            //Failure searching. It should not happen, Let's return an empty List.
            log.warn("Failure searching for a question hash");
            return new ArrayList<>();
        }
        return origins;

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

    public ItemFacadeQueriesAPI getItemFacadeQueries() {
        return itemFacadeQueries;
    }

    public void setItemFacadeQueries(
            ItemFacadeQueriesAPI itemFacadeQueries) {
        this.itemFacadeQueries = itemFacadeQueries;
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
