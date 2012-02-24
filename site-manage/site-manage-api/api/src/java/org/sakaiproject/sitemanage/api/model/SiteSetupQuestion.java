/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
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
 * The SiteSetupQuestion object is to store user-defined question.
 * A question would have two parts: the question part and the list of answers
 * @author zqian
 *
 */
public interface SiteSetupQuestion extends java.io.Serializable {
	
	/**
	 * get id of question
	 * @return
	 */
	public String getId();
	
	/**
	 * set the id of question
	 * @param id
	 */
	public void setId(String id);
	
	/**
	 * get the question prompt
	 * @return
	 */
	public String getQuestion();

	/**
	 * set the question prompt
	 * @param question
	 */
	public void setQuestion(String question);

	/**
	 * get the set of answers
	 * @return
	 */
	public List<SiteSetupQuestionAnswer> getAnswers();

	/**
	 * set the set of answers
	 * @param answers
	 */
	public void setAnswers(List<SiteSetupQuestionAnswer> answers);
	
	/**
	 * add into the list of answers
	 * @param answers
	 */
	public void addAnswer(SiteSetupQuestionAnswer answer);
	
	/**
	 * is question required or not
	 * @return
	 */
	public boolean isRequired();

	/**
	 * set required
	 * @param required
	 */
	public void setRequired(boolean required);

	/**
	 * does question have multiple answers
	 * @return
	 */
	public boolean getIsMultipleAnswers();

	/**
	 * set flag for multiple answers
	 * @param isMultipleAnswsers
	 */
	public void setIsMultipleAnswers(boolean isMultipleAnswers);
	
	/**
	 * get the order number
	 * @return
	 */
	public Integer getOrderNum();
	
	/**
	 * set the order number
	 * @param orderNum
	 */
	public void setOrderNum(Integer orderNum);
	
	/**
	 * get the SiteTypeQuestions object
	 * @return
	 */
	public SiteTypeQuestions getSiteTypeQuestions();
	
	/**
	 * set the SiteTypeQuestions
	 * @param siteTypeQuestions
	 */
	public void setSiteTypeQuestions(SiteTypeQuestions siteTypeQuestions);
	
	/**
	 * is the question been used currently?
	 * @return
	 */
	public String getCurrent();
	
	/**
	 * set current status of question
	 * @param current
	 */
	public void setCurrent(String current);
	
}