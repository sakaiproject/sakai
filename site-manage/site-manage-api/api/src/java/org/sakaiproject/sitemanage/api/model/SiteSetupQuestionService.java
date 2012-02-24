/**********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.sitemanage.api.model;

import java.util.List;

/**
 * This is the interface for the Service of SiteSetupQuestion. It contains the backend logic for the tool
 * @author zqian
 *
 */
public interface SiteSetupQuestionService {
	
	/**
	 * Is there any SiteTypeQuestions object in db
	 * @return
	 */
	public boolean hasAnySiteTypeQuestions();
	
	/**
	 * remove all site type questions
	 */
	public void removeAllSiteTypeQuestions();
	
	/**
	 * Get all questions
	 * @return
	 */
	public List<SiteSetupQuestion> getAllSiteQuestions();
	
	/**
	 * Get the SiteTypeQuestions object id the site type
	 * @param siteType
	 * @return
	 */
	public SiteTypeQuestions getSiteTypeQuestions(String siteType);
	
	/**
	 * find the SiteSetupQuestion object by id
	 * @param answerId
	 * @return
	 */
	public SiteSetupQuestionAnswer getSiteSetupQuestionAnswer(String answerId);

	/*********** SiteSetupQuestion **************/
	/**
	 * new question
	 * @param q
	 * @return
	 */
	public SiteSetupQuestion newSiteSetupQuestion();
	
	/**
	 * Save question
	 * @param q
	 * @return
	 */
	public boolean saveSiteSetupQuestion(SiteSetupQuestion q);
	
	/**
	 * remove SiteSetupQuestion
	 * @return
	 */
	public boolean removeSiteSetupQuestion(SiteSetupQuestion question);
	
	/********* SiteSetupQuestionAnswer ***************/
	
	/**
	 * new answer
	 * @param q
	 * @return
	 */
	public SiteSetupQuestionAnswer newSiteSetupQuestionAnswer();
	
	/**
	 * save the SiteSetupQuestionAnswer object
	 * @param answer
	 * @return
	 */
	public boolean saveSiteSetupQuestionAnswer(SiteSetupQuestionAnswer answer);
	
	/**
	 * remove the SiteSetupQuestionAnswer object
	 * @param answer
	 * @return
	 */
	public boolean removeSiteSetupQuestionAnswer(SiteSetupQuestionAnswer answer);

	/************ SiteTypeQuestions *******************/
	
	/**
	 * new SiteTypeQuestions
	 */
	public SiteTypeQuestions newSiteTypeQuestions();
	
	/**
	 * save the SiteTypeQuestion object
	 * @param siteTypeQuestions
	 * @return
	 */
	public boolean saveSiteTypeQuestions(SiteTypeQuestions siteTypeQuestions);
	
	/**
	 * remove the SiteTypeQuestion object
	 * @param siteTypeQuestions
	 * @return
	 */
	public boolean removeSiteTypeQuestions(SiteTypeQuestions siteTypeQuestions);
	
	/************ SiteSetupUserAnswer *******************/
	/**
	 * new SiteSetupUserAnswer
	 * @param uAnswer
	 * @return
	 */
	public SiteSetupUserAnswer newSiteSetupUserAnswer();
	
	/**
	 * Save the SiteTypeQuestion object
	 * @param SiteSetupUserAnswer
	 * @return
	 */
	public boolean saveSiteSetupUserAnswer(SiteSetupUserAnswer siteSetupUserAnswer);
	
	/**
	 * remove the SiteTypeQuestion object
	 * @param siteSetupUserAnswer
	 * @return
	 */
	public boolean removeSiteSetupUserAnswer(SiteSetupUserAnswer siteSetupUserAnswer);
	
}
