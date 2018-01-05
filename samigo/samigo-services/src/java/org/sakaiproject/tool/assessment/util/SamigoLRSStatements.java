/**
 * Copyright (c) 2005-2016 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.util;

import java.util.HashMap;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Actor;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Object;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Result;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Verb.SAKAI_VERB;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/* 
 * Class that holds custom code for generating LRS_Statements that contain special samigo Metadata
 */
public class SamigoLRSStatements {
    private static final ServerConfigurationService serverConfigurationService = (ServerConfigurationService) ComponentManager.get( ServerConfigurationService.class );
    private static final UserDirectoryService userDirectoryService = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);

    
    public static LRS_Statement getStatementForTakeAssessment(String assessmentTitle, boolean pastDue, String releaseTo, boolean isViaURL) {
    	StringBuffer lrssMetaInfo = new StringBuffer("Assesment: " + assessmentTitle);
    	lrssMetaInfo.append(", Past Due?: " + pastDue);
    	if (isViaURL) {
    		lrssMetaInfo.append(", Assesment taken via URL.");
    	}
    	
    	lrssMetaInfo.append(", Release to:" + AgentFacade.getCurrentSiteId());
    	
        String url = serverConfigurationService.getPortalUrl();
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.attempted);
        LRS_Object lrsObject = new LRS_Object(url + "/assessment", "attempted-assessment");
        HashMap<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("en-US", "User attempted assessment");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<String, String>();
        descMap.put("en-US", "User attempted assessment: " + lrssMetaInfo);
        lrsObject.setDescription(descMap);
        return new LRS_Statement(null, verb, lrsObject);
    }
    
    public static LRS_Statement getStatementForGradedAssessment(AssessmentGradingData gradingData, PublishedAssessmentFacade publishedAssessment) {
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.scored);
        LRS_Object lrsObject = new LRS_Object(serverConfigurationService.getPortalUrl() + "/assessment", "received-grade-assessment");
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("en-US", "User received a grade");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<>();
        String userId = gradingData.getAgentId();
        String userIdLabel = "User Id";
        try {
        	userId = userDirectoryService.getUserEid(gradingData.getAgentId());
        	userIdLabel = "User Eid";
        } catch (UserNotDefinedException e) {
        	//This is fine as userId is set by default
        }
       
        descMap.put("en-US", "User received a grade for their assessment: " + publishedAssessment.getTitle() +
        		"; " + userIdLabel + ": " + userId + 
        		"; Release To: "+ AgentFacade.getCurrentSiteId() + 
        		"; Submitted: " + (gradingData.getIsLate() ? "late" : "on time"));
        lrsObject.setDescription(descMap);
        LRS_Statement statement = new LRS_Statement(null, verb, lrsObject, getLRS_Result(gradingData, publishedAssessment), null);
        return statement;
	}

    public static LRS_Statement getStatementForTotalScoreUpdate(AssessmentGradingData gradingData, PublishedAssessmentData publishedAssessment) {
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.scored);
        LRS_Object lrsObject = new LRS_Object(serverConfigurationService.getPortalUrl() + "/assessment", "total-score-update");
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("en-US", "Total score updated");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<>();
        String userId = gradingData.getAgentId();
        String userIdLabel = "User Id";
        try {
        	userId = userDirectoryService.getUserEid(gradingData.getAgentId());
        	userIdLabel = "User Eid";
        } catch (UserNotDefinedException e) {
        	//This is fine as userId is set by default
        }

        descMap.put("en-US", "Total score updated for Assessment Title: " + publishedAssessment.getTitle() + 
        		"; " + userIdLabel + ": " + userId + 
        		"; Release To: "+ AgentFacade.getCurrentSiteId() + 
        		"; Submitted: "+ (gradingData.getIsLate() ? "late" : "on time"));
        lrsObject.setDescription(descMap);
        LRS_Statement statement = new LRS_Statement(null, verb, lrsObject, getLRS_Result(gradingData, publishedAssessment), null);
        return statement;
    }

    public static LRS_Statement getStatementForStudentScoreUpdate(AssessmentGradingData gradingData, PublishedAssessmentData publishedAssessment) {
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.scored);
        LRS_Object lrsObject = new LRS_Object(serverConfigurationService.getPortalUrl() + "/assessment", "student-score-update");
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("en-US", "Student score updated");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<>();
        String userId = gradingData.getAgentId();
        String userIdLabel = "User Id";
        try {
        	userId = userDirectoryService.getUserEid(gradingData.getAgentId());
        	userIdLabel = "User Eid";
        	
        } catch (UserNotDefinedException e) {
        	//This is fine as userId is set by default
        }

        descMap.put("en-US", "Student score updated for: " + publishedAssessment.getTitle() + 
        		"; " + userIdLabel + ": " + userId + 
        		"; Release To: "+ AgentFacade.getCurrentSiteId() + 
        		"; Submitted: " + (gradingData.getIsLate() ? "late" : "on time"));
        lrsObject.setDescription(descMap);
        LRS_Statement statement = new LRS_Statement(null, verb, lrsObject, getLRS_Result(gradingData, publishedAssessment), null);
        return statement;
    }

    public static LRS_Statement getStatementForQuestionScoreUpdate(AssessmentGradingData gradingData, PublishedAssessmentData publishedAssessment, double newAutoScore, double oldAutoScore) {
        LRS_Verb verb = new LRS_Verb(SAKAI_VERB.scored);
        LRS_Object lrsObject = new LRS_Object(serverConfigurationService.getPortalUrl() + "/assessment", "question-score-update");
        HashMap<String, String> nameMap = new HashMap<>();
        nameMap.put("en-US", "Question score updated");
        lrsObject.setActivityName(nameMap);
        HashMap<String, String> descMap = new HashMap<>();
        String userId = gradingData.getAgentId();
        String userIdLabel = "User Id";
        try {
        	userId = userDirectoryService.getUserEid(gradingData.getAgentId());
        	userIdLabel = "User Eid";
        	
        } catch (UserNotDefinedException e) {
        	//This is fine as userId is set by default
        }

        descMap.put("en-US", "Student score updated for: " + publishedAssessment.getTitle() + 
        		"; " + userIdLabel + ": " + userId + 
        		"; Release To: "+ AgentFacade.getCurrentSiteId() + 
        		"; Submitted: " + (gradingData.getIsLate() ? "late" : "on time") +
        		"; Old Auto Score: " + oldAutoScore +
        		"; New Auto Score: " + newAutoScore);
        lrsObject.setDescription(descMap);
        LRS_Statement statement = new LRS_Statement(null, verb, lrsObject, getLRS_Result(gradingData, publishedAssessment), null);
        return statement;
    }
    
    private static LRS_Result getLRS_Result(AssessmentGradingData gradingData, PublishedAssessmentIfc publishedAssessment) {
        double score = gradingData.getFinalScore();
        LRS_Result result = new LRS_Result(score, 0.0, publishedAssessment.getTotalScore(), null);
        result.setCompletion(true);
        return result;
    }
    
}
