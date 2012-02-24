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

public interface SiteSetupUserAnswer {
	
	/**
	 * get the id
	 * @return
	 */
	public String getId();
	
	/**
	 * set the id
	 * @param id
	 */
	public void setId(String id);
	
	/**
	 * get the user id
	 * @return
	 */
	public String getUserId();
	
	/**
	 * set the user id
	 * @param userId
	 */
	public void setUserId(String userId);
	
	/**
	 * get site id
	 * @return
	 */
	public String getSiteId();
	
	/**
	 * 
	 * @param siteId
	 */
	public void setSiteId(String siteId);
	
	/**
	 * get question id
	 * @return
	 */
	public String getQuestionId();
	
	/**
	 * set question id
	 * @param questionId
	 * @return
	 */
	public void setQuestionId(String questionId);
	
	/**
	 * get the SiteSetupQuestion id
	 * @return
	 */
	public String getAnswerId();

	/**
	 * set the SiteSetupQuestion id
	 * @param answerId
	 */
	public void setAnswerId(String answerId);
	
	/**
	 * get the answer string if the answer type is fill in blank one
	 * @return
	 */
	public String getAnswerString();
	
	/**
	 * set the answer string if the answer type is fill in blank one
	 * @param answerString
	 */
	public void setAnswerString(String answerString);
}
