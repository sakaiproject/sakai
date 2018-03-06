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

package org.sakaiproject.tool.gradebook.business;


/**
 * Manages Gradebook interaction with an external ScoringAgent
 *
 */
public interface GradebookScoringAgentManager {
	
	/**
	 * append this param to the end of the url calls to the ScoringAgent
	 * to identify the Gradebook tool (vs Gradebook2)
	 */
	public static String GRADEBOOK_PARAM = "&t=gb";
    
	/**
	 * 
	 * @param gradebookUid 
	 * @return true if a ScoringAgent is enabled for this gradebook
	 */
	public boolean isScoringAgentEnabledForGradebook(String gradebookUid);
	
	/**
	 * 
	 * @return the name of the ScoringAgent for display in the UI
	 */
	public String getScoringAgentName();
	
	/**
	 * 
	 * @param gradebookUid
	 * @param gradebookItemId
	 * @return true if a ScoringComponent is associated with the given
	 * gradebook item
	 */
	public boolean isScoringComponentEnabledForGbItem(String gradebookUid, Long gradebookItemId);
	
	/**
	 * @param gradebookUid 
	 * @param gradebookItemId 
	 * @return the name of the ScoringComponent associated with the gradebookItem
	 */
	public String getScoringComponentName(String gradebookUid, Long gradebookItemId);
	
	/**
	 * 
	 * @param gradebookUid
	 * @param gradebookItemId
	 * @return the url for the ScoringComponent associated with the given gradebook item
	 */
	public String getScoringComponentUrl(String gradebookUid, Long gradebookItemId);
	
	/**
	 * 
	 * @param gradebookUid
	 * @param gradebookItemId
	 * @param studentUid
	 * @return get a url that a user may click on to launch the external app 
	 * that provides the scoring use case
	 */
	public String getScoreStudentUrl(String gradebookUid, Long gradebookItemId, String studentUid);

	/**
	 * 
	 * @param gradebookUid
	 * @param gradebookId
	 * @return a url that will be clicked on to launch the user into the external app
     * that provides the scoring use case.  Does not launch to any specific student but 
     * for the whole roster.
	 */
	public String getScoreAllUrl(String gradebookUid, Long gradebookItemId);
	
	/**
	 * 
	 * @param gradebookUid
	 * @param gradebookItemId
	 * @param studentUid
	 * @return a url that can be clicked on to launch into the "view score" use case in the external 
	 * app for a given student.
	 */
	public String getViewStudentScoreUrl(String gradebookUid, Long gradebookItemId, String studentUid);
	
	/**
	 * 
	 * @return the reference to the image representing the ScoringAgent
	 */
	public String getScoringAgentImageRef();
	
	/**
	 * 
	 * @param gradebookUid
	 * @param gradebookItemId
	 * @return url for retrieving all scores from the external ScoringAgent for
	 * the ScoringComponent associated with the given gradebook item
	 */
	public String getScoresUrl(String gradebookUid, Long gradebookItemId);
	
	/**
	 * 
	 * @param gradebookUid
	 * @param gradebookItemId
	 * @param studentUid
	 * @return url for retrieving the scores from the external ScoringAgent for
	 * the given student and gradebook item
	 */
	public String getScoreUrl(String gradebookUid, Long gradebookItemId, String studentUid);

	
	/**
	 * 
	 * @param gradebookUid
	 * @param studentUid
	 * @return url for retrieving the scores from the external ScoringAgent for
	 * the given student for all gradebook items
	 */
	public String getStudentScoresUrl(String gradebookUid, String studentUid);

}
