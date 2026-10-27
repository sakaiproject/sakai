/**
 * Copyright (c) 2015, The Apereo Foundation
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

package org.sakaiproject.samigo.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.util.GradebookAssociationHelper;

@Slf4j
public class SamigoObserver implements Observer {

  @Setter
  private AuthzQueriesFacadeAPI authzQueriesFacade;

    public void init() {
        log.info("init()");
        eventTrackingService.addLocalObserver(this);
    }

    public void destroy(){
        log.info("destroy");
        eventTrackingService.deleteObserver(this);
    }

    public void update(Observable arg0, Object arg) {
        if (!(arg instanceof Event)){
            return;
        }

        Event event = (Event) arg;
        String eventType = event.getEvent();

        if(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_NOTI.equals(eventType)) {
            log.debug("Assessment Submitted Event");
            String hashMapString = event.getResource();
            Map<String, Object> notiValues =  stringToHashMap(hashMapString);
            samigoETSProvider.notify(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED, notiValues, event);
        } else if(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_AUTO.equals(eventType)){
            log.debug("Assessment Auto Submitted Event");
            String hashMapString = event.getResource();
            Map<String, Object> notiValues =  stringToHashMap(hashMapString);
            samigoETSProvider.notify(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_AUTO, notiValues, event);
        } else if(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_TIMER_THREAD.equals(eventType)){
            log.debug("Assessment Timed Submitted Event");
            String hashMapString = event.getResource();
            Map<String, Object> notiValues = stringToHashMap(hashMapString);
            samigoETSProvider.notify(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_TIMER_THREAD, notiValues, event);
        } else if (SamigoConstants.AUTHZ_GROUP_DELETED.equals(eventType)) {
            authzQueriesFacade.hardDeleteAuthzData(event.getResource());
        } else if (SamigoConstants.EVENT_GRADEBOOK_DELETE_ITEM.equals(eventType)) {
            handleGradebookItemDeleted(event.getResource());
        }
    }

    /**
     * Resource format: {@code /gradebookng/{gradebookId}/{assignmentId}/{role}}.
     */
    void handleGradebookItemDeleted(String resource) {
        if (StringUtils.isBlank(resource)) {
            return;
        }
        String[] parts = StringUtils.split(resource, '/');
        // parts: ["gradebookng", gradebookId, assignmentId, role]
        if (parts.length < 3 || !"gradebookng".equals(parts[0])) {
            log.warn("Unexpected gradebook.deleteItem resource format: {}", resource);
            return;
        }
        String assignmentId = parts[2];
        log.debug("Gradebook item {} deleted; clearing Samigo TO_SELECTED links", assignmentId);
        unlinkGradebookItemFromDraftAssessments(assignmentId);
        unlinkGradebookItemFromPublishedAssessments(assignmentId);
    }

    private void unlinkGradebookItemFromDraftAssessments(String assignmentId) {
        AssessmentService assessmentService = new AssessmentService();
        List<Long> assessmentIds = PersistenceService.getInstance().getAssessmentFacadeQueries()
                .findAssessmentIdsLinkedToGradebookItem(assignmentId);
        for (Long assessmentId : assessmentIds) {
            AssessmentFacade facade = assessmentService.getAssessment(assessmentId.toString());
            if (facade == null || facade.getEvaluationModel() == null) {
                continue;
            }
            if (unlinkSelectedGradebookItem(facade.getEvaluationModel(),
                    facade.getAssessmentToGradebookNameMetaData(),
                    facade::updateAssessmentToGradebookNameMetaData,
                    assignmentId)) {
                assessmentService.saveAssessment(facade);
                log.debug("Cleared deleted gradebook item {} from draft assessment {}", assignmentId, assessmentId);
            }
        }
    }

    private void unlinkGradebookItemFromPublishedAssessments(String assignmentId) {
        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
        List<Long> publishedIds = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries()
                .findPublishedAssessmentIdsLinkedToGradebookItem(assignmentId);
        for (Long publishedId : publishedIds) {
            PublishedAssessmentFacade facade = publishedAssessmentService.getPublishedAssessment(publishedId.toString());
            if (facade == null || facade.getEvaluationModel() == null) {
                continue;
            }
            if (unlinkSelectedGradebookItem(facade.getEvaluationModel(),
                    facade.getAssessmentToGradebookNameMetaData(),
                    facade::updateAssessmentToGradebookNameMetaData,
                    assignmentId)) {
                publishedAssessmentService.saveAssessment(facade);
                log.debug("Cleared deleted gradebook item {} from published assessment {}", assignmentId, publishedId);
            }
        }
    }

    /**
     * @return true if evaluation/metadata were changed
     */
    private boolean unlinkSelectedGradebookItem(EvaluationModelIfc evaluation, String currentIds,
            java.util.function.Consumer<String> updateMetadata, String assignmentId) {
        if (!EvaluationModelIfc.TO_SELECTED_GRADEBOOK.toString().equals(evaluation.getToGradeBook())) {
            return false;
        }
        if (!GradebookAssociationHelper.parseItemIds(currentIds).contains(assignmentId)) {
            return false;
        }
        String remaining = GradebookAssociationHelper.removeItemId(currentIds, assignmentId);
        if (StringUtils.isBlank(remaining)) {
            evaluation.setToGradeBook(EvaluationModelIfc.NOT_TO_GRADEBOOK.toString());
        }
        updateMetadata.accept(remaining);
        return true;
    }

    /*
     * stringToHashMap
     * Derived from http://stackoverflow.com/a/26486046
     */
    private Map<String, Object> stringToHashMap(String hashMapString){
        Map<String, Object> map = new HashMap<>();

        hashMapString = StringUtils.substringBetween(hashMapString, "{", "}");           //remove curly brackets
        String[] keyValuePairs = hashMapString.split(",");              //split the string to create key-value pairs

        for(String pair : keyValuePairs)                        //iterate over the pairs
        {
            String[] entry = pair.split("=");                   //split the pairs to get key and value
            String key = StringUtils.trim(entry[0]);
            if(entry.length == 2) {
                if (key.equals("assessmentGradingID") || key.equals("publishedAssessmentID")) {
                    map.put(key, Long.valueOf(StringUtils.trim(entry[1])));
                } else {
                    map.put(key, String.valueOf(StringUtils.trim(entry[1])));          //add them to the hashmap and trim whitespaces
                }
            } else{
                map.put(key, "");
            }
        }

        return map;
    }

    @Setter
    private EventTrackingService    eventTrackingService;

    @Setter
    private SamigoETSProviderImpl   samigoETSProvider;
}
