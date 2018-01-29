/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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

package org.sakaiproject.tool.gradebook.business.impl;

import org.sakaiproject.scoringservice.api.ScoringAgent;
import org.sakaiproject.scoringservice.api.ScoringComponent;
import org.sakaiproject.scoringservice.api.ScoringService;
import org.sakaiproject.tool.gradebook.business.GradebookScoringAgentManager;

/**
 * Manages Gradebook's integration with a ScoringAgent
 */
public class GradebookScoringAgentManagerImpl implements GradebookScoringAgentManager {
	
	private static String DEFAULT_IMAGE = "/../library/image/silk/report_edit.png";
	
	private ScoringService scoringService;
	public void setScoringService(ScoringService scoringService) {
		this.scoringService = scoringService;
	}
	
	public boolean isScoringAgentEnabledForGradebook(String gradebookUid) {
		boolean enabled = false;
		ScoringAgent scoringAgent = scoringService.getDefaultScoringAgent();
		if (scoringAgent != null) {
			enabled = scoringAgent.isEnabled(gradebookUid, null);
		}
		
    	return enabled;
	}
	
	public String getScoringAgentName() {
		return scoringService.getDefaultScoringAgent().getName();
	}
	
	public boolean isScoringComponentEnabledForGbItem(String gradebookUid, Long gradebookItemId) {
		ScoringAgent scoringAgent = scoringService.getDefaultScoringAgent();
    	ScoringComponent component = scoringService.getScoringComponent(
    			scoringAgent.getAgentId(), gradebookUid, getString(gradebookItemId));
    	return component != null;
	}

	
	public String getScoringComponentName(String gradebookUid, Long gradebookItemId) {
		String componentName = null;
		
		ScoringAgent scoringAgent = scoringService.getDefaultScoringAgent();
    	ScoringComponent component = scoringService.getScoringComponent(
    			scoringAgent.getAgentId(), gradebookUid, getString(gradebookItemId));
	
		if (component != null) {
			componentName = component.getName();
		}

    	return componentName;
	}
	
	public String getScoringComponentUrl(String gradebookUid, Long gradebookItemId) {
		ScoringAgent scoringAgent = scoringService.getDefaultScoringAgent();
		return scoringAgent.getScoringComponentLaunchUrl(gradebookUid, getString(gradebookItemId));
	}

	
	public String getScoreStudentUrl(String gradebookUid, Long gradebookItemId,
			String studentUid) {
		ScoringAgent scoringAgent = scoringService.getDefaultScoringAgent();
        return scoringAgent.getScoreLaunchUrl(gradebookUid, getString(gradebookItemId), studentUid);
	}

	
	public String getScoreAllUrl(String gradebookUid, Long gradebookItemId) {
		ScoringAgent scoringAgent = scoringService.getDefaultScoringAgent();
        return scoringAgent.getScoreLaunchUrl(gradebookUid, getString(gradebookItemId));
	}

	
	public String getViewStudentScoreUrl(String gradebookUid,
			Long gradebookItemId, String studentUid) {
		ScoringAgent scoringAgent = scoringService.getDefaultScoringAgent();
        return scoringAgent.getViewScoreLaunchUrl(gradebookUid, getString(gradebookItemId), studentUid);
	}
	
	/**
	 * 
	 * @param longValue
	 * @return convenience method to return the String representation of
	 * the given Long
	 */
	private String getString(Long longValue) {
		return longValue == null ? null : Long.toString(longValue);
	}

	public String getScoringAgentImageRef() {
		String imageRef = scoringService.getDefaultScoringAgent().getImageReference();
		if (imageRef == null) {
			imageRef = DEFAULT_IMAGE;
		}
		
		return imageRef;
	}
	
	public String getScoresUrl(String gradebookUid, Long gradebookItemId) {
		return scoringService.getDefaultScoringAgent().getScoresUrl(gradebookUid, getString(gradebookItemId)) + GRADEBOOK_PARAM; 
	}
	
	public String getScoreUrl(String gradebookUid, Long gradebookItemId, String studentUid) {
		return scoringService.getDefaultScoringAgent().getScoreUrl(gradebookUid, getString(gradebookItemId), studentUid) + GRADEBOOK_PARAM;
	}
	
	public String getStudentScoresUrl(String gradebookUid, String studentUid) {
		return scoringService.getDefaultScoringAgent().getStudentScoresUrl(gradebookUid, studentUid);
	}
    
}
