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

/**
 * The SiteSetupQuestion object is to store answers to SiteSetupQuestion.
 * @author zqian
 *
 */
public interface SiteSetupQuestionAnswer extends java.io.Serializable {
	
	/**
	 * get the associated question id
	 * @return
	 */
	public String getId();
	
	/**
	 * set the associated question id
	 * @param id
	 */
	public void setId(String id);
	
	/**
	 * whether the answer is fill-in-blank
	 * @return
	 */
	public boolean getIsFillInBlank();

	/**
	 * set the fill-in-blank type
	 * @param isFillInBlank
	 */
	public void setIsFillInBlank(boolean isFillInBlank);

	/**
	 * get the answer content
	 * @return
	 */
	public String getAnswer();

	/**
	 * set the answer content
	 * @param answer
	 */
	public void setAnswer(String answer);

	/**
	 * if the answer is fill-in-blank, get the input value
	 * @return
	 */
	public String getAnswerString();

	/**
	 * if the answer is fill-in-blank, set the input value
	 * @param fillInBlankString
	 */
	public void setAnswerString(String AnswerString);
	
	/**
	 * get the associated question
	 * @return
	 */
	public SiteSetupQuestion getQuestion();
	
	/**
	 * set the question 
	 * @return
	 */
	public void setQuestion(SiteSetupQuestion question);
	
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
}