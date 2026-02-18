/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

package org.sakaiproject.samigo.search;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.entity.impl.ItemEntityProviderImpl;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;

import org.sakaiproject.search.util.HTMLParser;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemContentProducer implements EntityContentProducer, EntityContentProducerEvents {

    @Getter @Setter private EntityManager entityManager = null;
    @Setter EntityProviderManager entityProviderManager;
    AssessmentService assessmentService  = new AssessmentService();

    /**
     * Custom action code for site.upd events.
     * When this action is returned, QuestionElasticSearchIndexBuilder will delete all
     * site items if the site was deleted or softly-deleted.
     * @see org.sakaiproject.samigo.search.QuestionElasticSearchIndexBuilder#validateIndexAction
     */
    public static final Integer ACTION_SITE_DELETE_IF_REMOVED = 100;

    // Map of events to their corresponding search index actions
    private static final Map<String, Integer> EVENT_ACTIONS = Map.of(
            "sam.assessment.saveitem", SearchBuilderItem.ACTION_ADD,
            "sam.assessment.item.delete", SearchBuilderItem.ACTION_DELETE,
            "sam.questionpool.deleteitem", SearchBuilderItem.ACTION_DELETE,
            "sam.assessment.unindexitem", SearchBuilderItem.ACTION_DELETE,
            "site.upd", ACTION_SITE_DELETE_IF_REMOVED
    );

    protected void init() throws Exception {
    }

    @Override
    public Set<String> getTriggerFunctions() {
        return EVENT_ACTIONS.keySet();
    }


    /**
     * Destroy
     */
    protected void destroy()
    {
        log.info("destroy() ItemContentProducer");
    }

    /**
     * {@inheritDoc}
     */
    public boolean canRead(String eventResource) {
        String reference= getReferenceFromEventResource(eventResource);
        
        EntityReference er= new EntityReference("/sam_item/"+reference);

        try {
            ItemEntityProviderImpl qhp= (ItemEntityProviderImpl)entityProviderManager.getProviderByPrefix(er.getPrefix());
            qhp.entityExists(er.getId());
            return true;
        } catch (Exception ex) {
            log.debug("Managed exception getting the item canRead function" + ex.getClass().getName() + " : " + ex.getMessage());
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Integer getAction(Event event) {
        return EVENT_ACTIONS.getOrDefault(event.getEvent(), SearchBuilderItem.ACTION_UNKNOWN);
    }

    /**
     * {@inheritDoc}
     */
    public List getAllContent() {
        List all = new ArrayList<>();
        //Not needed.
        return all;
    }

    public Map<String,Object> getAllFields(String resourceName){
        Map<String,Object> allInfo = new HashMap<>();

        String reference = getReferenceFromEventResource(resourceName);
       
        EntityReference er= new EntityReference("/sam_item/"+reference);
        ItemEntityProviderImpl qhp= (ItemEntityProviderImpl)entityProviderManager.getProviderByPrefix(er.getPrefix());
        try {
            ItemFacade item = (ItemFacade) qhp.getEntity(er);
            List<String> questionPoolIds = qhp.questionPoolIds(item);
            if (questionPoolIds.size()>0){
                allInfo.put("isFromQuestionPool","true");
                allInfo.put("questionPoolId",qhp.questionPoolIds(item));
            }else{
                allInfo.put("isFromQuestionPool","false");
            }
            allInfo.put("questionId", getId(resourceName));
            try {
                allInfo.put("site", qhp.siteIds(item).get(0));
            }catch (Exception ex) {
                allInfo.put("site",null);
            }
            allInfo.put("tags", qhp.tags(item));
            allInfo.put("questionPoolId",qhp.questionPoolIds(item));
            allInfo.put("assessmentId", qhp.assessmentId(item));
            allInfo.put("hash", item.getHash());
            allInfo.put("type", "question");
            allInfo.put("subtype","item");

            allInfo.put("typeId",item.getTypeId().toString());
            if (item.getTypeId() == 14) {
                allInfo.put("qText", item.getThemeText());
            }else{
                allInfo.put("qText", item.getText());
            }

        }catch (Exception ex) {
            log.debug("Managed exception getting the question hash " + ex.getClass().getName() + " : " + ex.getMessage());
        }finally{
            return allInfo;
        }

    }

    /**
     * {@inheritDoc}
     */
    public String getContainer(String reference) {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public String getContent(String eventResource) {
        String reference = getReferenceFromEventResource(eventResource);
        
        EntityReference er= new EntityReference("/sam_item/"+reference);
        ItemEntityProviderImpl qhp= (ItemEntityProviderImpl)entityProviderManager.getProviderByPrefix(er.getPrefix());

        try {
            ItemFacade item = (ItemFacade)qhp.getEntity(er);
            return HTMLParser.stripHtml(qhp.content(item));
        } catch (Exception e) {
            throw new RuntimeException(" Failed to get item content ", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    public Reader getContentReader(String reference) {
        return new StringReader(getContent(reference));
    }

    /**
     * {@inheritDoc}
     */
    public Map getCustomProperties(String eventResource) {
        Map<String, List> customProperties = new HashMap<>();

        try {
            return customProperties;
        }catch (Exception ex){
            log.debug("Managed exception getting the question custom Properties" + ex.getClass().getName() + " : " + ex.getMessage());
            return null;
        }

    }

    public List<String> getQuestionPoolId(String resource) {
        String reference = getReferenceFromEventResource(resource);
        EntityReference er= new EntityReference("/sam_item/"+reference);
        ItemEntityProviderImpl qhp= (ItemEntityProviderImpl)entityProviderManager.getProviderByPrefix(er.getPrefix());

        try {
            ItemFacade item = (ItemFacade) qhp.getEntity(er);
            return qhp.questionPoolIds(item);
        }catch (Exception ex) {
            log.debug("Managed exception getting the question pool id" + ex.getClass().getName() + " : " + ex.getMessage());
            return null;
        }
    }

    public List<String> getTags(String resource) {
        String reference = getReferenceFromEventResource(resource);
        EntityReference er= new EntityReference("/sam_item/"+reference);
        ItemEntityProviderImpl qhp= (ItemEntityProviderImpl)entityProviderManager.getProviderByPrefix(er.getPrefix());

        try {
            ItemFacade item = (ItemFacade) qhp.getEntity(er);
            return qhp.tags(item);
        }catch (Exception ex) {
            log.debug("Managed exception getting the question tags" + ex.getClass().getName() + " : " + ex.getMessage());
            return null;
        }
    }

    public String getHash(String resource) {
        String reference = getReferenceFromEventResource(resource);
        EntityReference er= new EntityReference("/sam_item/"+reference);
        ItemEntityProviderImpl qhp= (ItemEntityProviderImpl)entityProviderManager.getProviderByPrefix(er.getPrefix());

        try {
            ItemFacade item = (ItemFacade) qhp.getEntity(er);
            return item.getHash();
        }catch (Exception ex) {
            log.debug("Managed exception getting the question hash " + ex.getClass().getName() + " : " + ex.getMessage());
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getCustomRDF(String ref) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getId(String reference) {
        try {
            return "/sam_item/"+getReferenceFromEventResource(reference);
        } catch ( Exception ex ) {
            return "";
        }
    }

    /**
     * {@inheritDoc}
     */
    public List getSiteContent(String context) {
        List<Long> questionIdList = new ArrayList<Long>();

        List<AssessmentData> assessmentsList = assessmentService.getAllActiveAssessmentsbyAgent(context);
        for (AssessmentData assessmentData:assessmentsList){
            List<Long> assessmentQuestionIdsList = assessmentService.getQuestionsIdList(assessmentData.getAssessmentId());
            questionIdList.addAll(assessmentQuestionIdsList);
        }
        return questionIdList;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator getSiteContentIterator(final String context) {

        return getSiteContent(context).iterator();

    }


    /**
     * {@inheritDoc}
     */
    public String getSiteId(String resource) {
        String reference = getReferenceFromEventResource(resource);
        EntityReference er= new EntityReference("/sam_item/"+reference);
        ItemEntityProviderImpl qhp= (ItemEntityProviderImpl)entityProviderManager.getProviderByPrefix(er.getPrefix());

        try {
            ItemFacade item = (ItemFacade) qhp.getEntity(er);
            return qhp.siteIds(item).get(0);
        }catch (Exception ex) {
            log.debug("Managed exception getting the question site id" + ex.getClass().getName() + " : " + ex.getMessage());
            return null;
        }
    }

    public String getAssessmentId(String resource){
        String reference = getReferenceFromEventResource(resource);
        EntityReference er= new EntityReference("/sam_item/"+reference);
        ItemEntityProviderImpl qhp= (ItemEntityProviderImpl)entityProviderManager.getProviderByPrefix(er.getPrefix());
        try {
            ItemFacade item = (ItemFacade) qhp.getEntity(er);
            return qhp.assessmentId(item);
        }catch (Exception ex) {
            log.debug("Managed exception getting the question origin" + ex.getClass().getName() + " : " + ex.getMessage());
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getSubType(String reference) {
        return "item";
    }

    /**
     * {@inheritDoc}
     */
    public String getTitle(String reference) {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public String getTool() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public String getType(String reference) {
        return "question";
    }

    /**
     * {@inheritDoc}
     */
    public String getUrl(String reference) {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isContentFromReader(String reference) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isForIndex(String resource) {
        String reference = getReferenceFromEventResource(resource);
        //Basically is a true always... but in case the reference is not valid let's maintain this.
        EntityReference er= new EntityReference("/sam_item/"+reference);
        ItemEntityProviderImpl qhp= (ItemEntityProviderImpl)entityProviderManager.getProviderByPrefix(er.getPrefix());

        try {
            return qhp.entityExists(reference);
        } catch (Exception ex) {
            log.debug("Managed exception in isForIndex" + ex.getClass().getName() + " : " + ex.getMessage());
            return false;
        }

    }

    /**
     * {@inheritDoc}
     */
    public boolean matches(String resource) {
        String reference = getReferenceFromEventResource(resource);
        EntityReference er= new EntityReference("/sam_item/"+reference);
        ItemEntityProviderImpl qhp= (ItemEntityProviderImpl)entityProviderManager.getProviderByPrefix(er.getPrefix());

        return qhp.entityExists(reference);

    }

    /**
     * {@inheritDoc}
     */
    public boolean matches(Event event) {
        return EVENT_ACTIONS.containsKey(event.getEvent());
    }

    private String getReferenceFromEventResource(String resource){
        String reference;
        if (resource.indexOf(" itemId=")==-1){
            if (resource.indexOf("/sam_item/")==-1){
                reference = resource;
            }else {
                reference = resource.substring(resource.indexOf("/sam_item/") + 10);
            }
        }else{
            reference = resource.substring(resource.indexOf(" itemId=") + 8);
        }
        return reference;
    }

}
