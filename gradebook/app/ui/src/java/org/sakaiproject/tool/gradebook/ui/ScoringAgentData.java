/**********************************************************************************
 *
 * $Id: ScoringAgentData.java  $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;

/**
 * A class representing data related to a gradebook's association with a
 * ScoringAgent for grading via an external scoring service
 */
public class ScoringAgentData implements Serializable {

	private static final long serialVersionUID = 1L;

	private String scoringAgentName;
	private String scoringAgentImageRef;

	private Boolean scoringComponentEnabled;
	private String scoringComponentName;
	private String scoringComponentUrl;
	
	private String scoreAllUrl;
	private String retrieveScoresUrl;
	private String retrieveScoreUrl;
	
	private String retrieveStudentScoresUrl;
	
	private String selectScoringComponentText;
	private String gradeWithScoringAgentText;
	private String gradeAllWithScoringAgentText;
	private String viewWithScoringAgentText;
	private String refreshAllGradesText;
	private String refreshGradeText;
	
	public ScoringAgentData() {
	}

	/**
	 * 
	 * @return is ScoringComponent registered for this gb item?
	 */
	public boolean isScoringComponentEnabled() {
		return scoringComponentEnabled;
	}
	
	/**
	 * Is a Scoring Component (like a rubric) registered for this gb item?
	 * @param scoringComponentEnabled
	 */
	public void setScoringComponentEnabled(boolean scoringComponentEnabled) {
		this.scoringComponentEnabled = scoringComponentEnabled;
	}

	/**
	 * 
	 * @return the name of the ScoringAgent
	 */
	public String getScoringAgentName() {
		return scoringAgentName;
	}
	
	/**
	 * the name of the ScoringAgent
	 * @param scoringAgentName
	 */
	public void setScoringAgentName(String scoringAgentName) {
		this.scoringAgentName = scoringAgentName;
	}

	/**
	 * 
	 * @return the url for the ScoringComponent (like a rubric) associated with this
	 * gradebook item
	 */
	public String getScoringComponentUrl() {

		return scoringComponentUrl;
	}
	
	/**
	 * the url for the Scoring Component (like a rubric) associated with this
	 * gradebook item
	 * @param scoringComponentUrl
	 */
	public void setScoringComponentUrl(String scoringComponentUrl) {
		this.scoringComponentUrl = scoringComponentUrl;
	}

	/**
	 * 
	 * @return the name of the ScoringComponent associated with this
	 * gradebook item
	 */
	public String getScoringComponentName() {
		return scoringComponentName;
	}
	
	/**
	 * the name of the ScoringComponent associated with this
	 * gradebook item
	 * @param scoringComponentName
	 */
	public void setScoringComponentName(String scoringComponentName) {
		this.scoringComponentName = scoringComponentName;
	}
	
	/**
	 * 
	 * @return the URL for grading all gradable students for a given gradebook item
	 * that is associated with a ScoringComponent (ie a rubric)
	 */
	public String getScoreAllUrl() {
		return scoreAllUrl;
	}
	
	/**
	 * the URL for grading all gradable students for a given gradebook item
	 * that is associated with a ScoringComponent (ie a rubric)
	 * @param scoreAllUrl
	 */
	public void setScoreAllUrl(String scoreAllUrl) {
		this.scoreAllUrl = scoreAllUrl;
	}
	
	/**
	 * @return the url for retrieving the scores from the external scoring agent for this
	 * gradebook item
	 */
	public String getRetrieveScoresUrl() {
		return retrieveScoresUrl;
	}

	/**
	 * @param retrieveScoresUrl 
	 * the url for retrieving the scores from the external scoring agent for this
	 * gradebook item
	 */
	public void setRetrieveScoresUrl(String retrieveScoresUrl) {
		this.retrieveScoresUrl = retrieveScoresUrl;
	}

	/**
	 * @return the url for retrieving a single student's score from the external scoring agent
	 * for this gradebook item
	 */
	public String getRetrieveScoreUrl() {
		return retrieveScoreUrl;
	}

	/**
	 * @param retrieveScoreUrl 
	 * the url for retrieving a single student's score from the external scoring agent
	 * for this gradebook item
	 */
	public void setRetrieveScoreUrl(String retrieveScoreUrl) {
		this.retrieveScoreUrl = retrieveScoreUrl;
	}

	/**
	 * @return url for retrieving the scores for a single student
	 */
	public String getRetrieveStudentScoresUrl() {
		return retrieveStudentScoresUrl;
	}

	/**
	 * @param retrieveStudentScoresUrl 
	 * url for retrieving the scores for a single student
	 */
	public void setRetrieveStudentScoresUrl(String retrieveStudentScoresUrl) {
		this.retrieveStudentScoresUrl = retrieveStudentScoresUrl;
	}

	/**
	 * 
	 * @return the reference to the image representing the ScoringAgent
	 */
	public String getScoringAgentImageRef() {
		return scoringAgentImageRef;
	}
	
	/**
	 * the reference to the image representing the ScoringAgent
	 */
	public void setScoringAgentImageRef(String scoringAgentImageRef) {
		this.scoringAgentImageRef = scoringAgentImageRef;
	}
	
	// There are several places in the UI where the text requires parameterized bundle
	// references, but the JSF component does not allow parameters. We will build
	// them here instead. For example, alt tags on the image

	/**
	 * @param selectScoringComponentText 
	 * Because this text includes the variable Scoring Component name and
	 * is used in contexts that do not allow parameters, retrieve the parameterized bundle
	 * reference for "selectScoringComponent" here
	 */
	public void setSelectScoringComponentText(String selectScoringComponentText) {
		this.selectScoringComponentText = selectScoringComponentText;
	}
	
	/**
	 * 
	 * @return Because this text includes the variable Scoring Component name and
	 * is used in contexts that do not allow parameters, retrieve the parameterized bundle
	 * reference for "selectScoringComponent" here
	 */
	public String getSelectScoringComponentText() {
		return this.selectScoringComponentText;
	}


	/**
	 * @param gradeWithScoringAgentText
	 * Because this text includes the variable Scoring Agent name and
	 * is used in contexts that do not allow parameters, retrieve the parameterized bundle
	 * reference for "gradeWithScoringAgent" here
	 */
	public void setGradeWithScoringAgentText(String gradeWithScoringAgentText) {
		this.gradeWithScoringAgentText = gradeWithScoringAgentText;
	}
	
	/**
	 * 
	 * @return Because this text includes the variable Scoring Agent name and
	 * is used in contexts that do not allow parameters, retrieve the parameterized bundle
	 * reference for "gradeWithScoringAgent" here
	 */
	public String getGradeWithScoringAgentText() {
		return this.gradeWithScoringAgentText;
	}


	/**
	 * @param gradeAllWithScoringAgentText 
	 * Because this text includes the variable Scoring Agent name and
	 * is used in contexts that do not allow parameters, retrieve the parameterized bundle
	 * reference for "gradeAllWithScoringAgent" here
	 */
	public void setGradeAllWithScoringAgentText(String gradeAllWithScoringAgentText) {
		this.gradeAllWithScoringAgentText = gradeAllWithScoringAgentText;
	}
	
	/**
	 * 
	 * @return Because this text includes the variable Scoring Agent name and
	 * is used in contexts that do not allow parameters, retrieve the parameterized bundle
	 * reference for "gradeAllWithScoringAgent" here
	 */
	public String getGradeAllWithScoringAgentText() {
		return this.gradeAllWithScoringAgentText;
	}


	/**
	 * @param viewWithScoringAgentText 
	 * Because this text includes the variable Scoring Agent name and
	 * is used in contexts that do not allow parameters, retrieve the parameterized bundle
	 * reference for "viewWithScoringAgent" here
	 */
	public void setViewWithScoringAgentText(String viewWithScoringAgentText) {
		this.viewWithScoringAgentText = viewWithScoringAgentText;
	}
	
	/**
	 * 
	 * @return Because this text includes the variable Scoring Agent name and
	 * is used in contexts that do not allow parameters, retrieve the parameterized bundle
	 * reference for "viewWithScoringAgent" here
	 */
	public String getViewWithScoringAgentText() {
		return this.viewWithScoringAgentText;
	}


	/**
	 * @param refreshAllGradesText 
	 * Because this text includes the variable Scoring Agent name and
	 * is used in contexts that do not allow parameters, retrieve the parameterized bundle
	 * reference for "refreshAllGrades" here
	 */
	public void setRefreshAllGradesText(String refreshAllGradesText) {
		this.refreshAllGradesText = refreshAllGradesText;
	}
	
	/**
	 * 
	 * @return Because this text includes the variable Scoring Agent name and
	 * is used in contexts that do not allow parameters, retrieve the parameterized bundle
	 * reference for "refreshAllGrades" here
	 */
	public String getRefreshAllGradesText() {
		return this.refreshAllGradesText;
	}

	/**
	 * @param refreshGradeText Because this text includes the variable Scoring Agent name and
	 * is used in contexts that do not allow parameters, retrieve the parameterized bundle
	 * reference for "refreshGrade" here
	 */
	public void setRefreshGradeText(String refreshGradeText) {
		this.refreshGradeText = refreshGradeText;
	}
	
	/**
	 * 
	 * @return Because this text includes the variable Scoring Agent name and
	 * is used in contexts that do not allow parameters, retrieve the parameterized bundle
	 * reference for "refreshGrade" here
	 */
	public String getRefreshGradeText() {
		return this.refreshGradeText;
	}

}

