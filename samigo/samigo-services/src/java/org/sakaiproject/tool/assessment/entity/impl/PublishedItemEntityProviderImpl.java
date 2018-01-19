/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.entity.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemTag;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.entity.api.PublishedItemEntityProvider;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.PublishedItemFacade;
import org.sakaiproject.tool.assessment.facade.PublishedItemFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;

/**
 * Entity Provider impl for samigo published questions.
 */
@Slf4j
public class PublishedItemEntityProviderImpl implements PublishedItemEntityProvider,CoreEntityProvider,AutoRegisterEntityProvider {

    private PublishedItemFacadeQueriesAPI publishedItemFacadeQueries;
    private PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries;
    private DeveloperHelperService developerHelperService;
    private SiteService siteService;
    private AssessmentService assessmentService= new AssessmentService();

    public final static String ENTITY_PREFIX = PublishedItemEntityProvider.ENTITY_PREFIX;

    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }


    public boolean entityExists(String ref) {
        try {
            String itemId = getItemIdFromOriginalReference(ref);
            return publishedItemFacadeQueries.itemExists(itemId);
        } catch (Exception e) {
            return false;
        }
    }

    public Object getEntity(EntityReference ref) {
        try {
            if (entityExists(ref.getOriginalReference())) {
                String itemId = getItemIdFromOriginalReference(ref.getOriginalReference());
                PublishedItemFacade item = publishedItemFacadeQueries.getItem(itemId);
                return item;
            }else{
                return null;
            }
        } catch (Exception e) {
            log.warn("No entity found: " + ref );
            return null;
        }
    }

    private String getItemIdFromOriginalReference(String originalReference){
        if (originalReference.contains("/sam_publisheditem/")){
            return originalReference.substring(originalReference.indexOf("/sam_publisheditem/")+19);
        }else if (originalReference.contains(" publishedItemId=")){
            return originalReference.substring(originalReference.indexOf(" publishedItemId=")+17);
        }else{
            return originalReference;
        }

    }

    public Map<String, String> getProperties(String reference) {
        //No properties to get
        Map<String, String> props = new HashMap<String, String>();
        return props;
    }


    public List<String> siteIds(PublishedItemFacade item) {
        Long assessmentId = publishedItemFacadeQueries.getPublishedAssessmentId(item.getItemId());
        List<String> siteIds = new ArrayList<>();
        String siteId = null;
        if (assessmentId > 0) {
            siteId = publishedAssessmentFacadeQueries.getPublishedAssessmentOwner(Long.toString(assessmentId));
        }
        if (siteId != null) {
            siteIds.add(siteId);
        }
        return siteIds;
    }

    public String assessmentId(PublishedItemFacade item) {
        Long assessmentId = publishedItemFacadeQueries.getPublishedAssessmentId(item.getItemId());
        if(assessmentId>0){
            return Long.toString(assessmentId);
        }else {
            return null;
        }

    }


    public List<String> tags(PublishedItemFacade item){
        List<String> tags = new ArrayList<>();
        String itemId = item.getItemIdString();
        //Set itemTagSet = publishedItemFacadeQueries.getItem(itemId).getItemTagSet();
        Set itemTagSet = item.getItemTagSet();
        Iterator iterator = itemTagSet.iterator();
        while(iterator.hasNext()){
            PublishedItemTag tag = (PublishedItemTag)iterator.next();
            if (!(tags.contains(tag.getTagLabel()))){
                tags.add(tag.getTagLabel()+ "(" + tag.getTagCollectionName() + ")");
            }
        }
        return tags;
    }


    public String content(PublishedItemFacade item) {
        String content = "";

        //get question description/text/instructions
        if (item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) {
            if (!(StringUtils.isEmpty(item.getThemeText()))){
                content += item.getThemeText();
            }
            if (!(StringUtils.isEmpty(item.getLeadInText()))){
                content += separator(content) + item.getLeadInText();
            }
        } else {
            if (!(StringUtils.isEmpty(item.getInstruction()))){
                content += item.getInstruction();
            }
        }
        if (!(StringUtils.isEmpty(item.getDescription()))){
            content += separator(content) + item.getDescription();
        }
        //get question answers

        if (item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) {

            if (item.getIsAnswerOptionsSimple()){
                final List<AnswerIfc> emiAnswerOptions = item.getEmiAnswerOptions();
                for ( AnswerIfc answerIfc : emiAnswerOptions ) {
                    if (!(StringUtils.isEmpty(answerIfc.getText()))) {
                        content += separator(content) + answerIfc.getText();
                    }
                }
            }

            if (item.getIsAnswerOptionsRich()){
                if (!(StringUtils.isEmpty(item.getEmiAnswerOptionsRichText()))) {
                    content += separator(content) + item.getEmiAnswerOptionsRichText();
                }
            }

            List<ItemTextIfc> itemTextIfcs = item.getEmiQuestionAnswerCombinations();
            for ( ItemTextIfc itemTextIfc : itemTextIfcs ) {
                if (!(StringUtils.isEmpty(itemTextIfc.getText()))) {
                    content += separator(content) + itemTextIfc.getText();
                }
            }
        }else{
            final List<ItemTextIfc> itemTextArraySorted = item.getItemTextArraySorted();
            for (ItemTextIfc itemTextIfc : itemTextArraySorted) {
                if(!((item.getTypeId().equals(TypeIfc.CALCULATED_QUESTION)))) {
                    if (!(StringUtils.isEmpty(itemTextIfc.getText()))) {
                        content += separator(content) + itemTextIfc.getText();
                    }
                }
                if ((item.getTypeId().equals(TypeIfc.MULTIPLE_CHOICE))
                        || (item.getTypeId().equals(TypeIfc.FILL_IN_BLANK))
                        || (item.getTypeId().equals(TypeIfc.MATCHING))
                        || (item.getTypeId().equals(TypeIfc.FILL_IN_NUMERIC))
                        || (item.getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION))
                        || (item.getTypeId().equals(TypeIfc.MATRIX_CHOICES_SURVEY))
                        || (item.getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS))) {
                    final List<AnswerIfc> answerArraySorted = itemTextIfc.getAnswerArraySorted();
                    for (AnswerIfc answerIfc : answerArraySorted) {
                        if (!(StringUtils.isEmpty(answerIfc.getText()))) {
                            if (!(((item.getTypeId().equals(TypeIfc.MATCHING) && (!(answerIfc.getIsCorrect())))))) {
                                content += separator(content) + answerIfc.getText();
                            }
                        }
                    }
                }
            }
        }

        log.debug("This is the questions content: " + content);
        return content;
    }

    public String separator(String content){
        if (StringUtils.isNotEmpty(content)){
            return ". ";
        }else{
            return "";
        }
    }

    public String[] getHandledOutputFormats() {
        return new String[]{Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[]{Formats.JSON};
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        return null;
    }

    public Object getSampleEntity() {
        return null;
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {

    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {

    }

    public String getPropertyValue(String reference, String name) {
        Map<String, String> props = getProperties(reference);
        return props.get(name);
    }

    public void setPropertyValue(String reference, String name, String value) {

    }

    public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
        this.developerHelperService = developerHelperService;
    }

    public PublishedItemFacadeQueriesAPI getPublishedItemFacadeQueries() {
        return publishedItemFacadeQueries;
    }

    public void setPublishedItemFacadeQueries(PublishedItemFacadeQueriesAPI publishedItemFacadeQueries) {
        this.publishedItemFacadeQueries = publishedItemFacadeQueries;
    }

    public PublishedAssessmentFacadeQueriesAPI getPublishedAssessmentFacadeQueries() {
        return publishedAssessmentFacadeQueries;
    }

    public void setPublishedAssessmentFacadeQueries(PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries) {
        this.publishedAssessmentFacadeQueries = publishedAssessmentFacadeQueries;
    }


    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
