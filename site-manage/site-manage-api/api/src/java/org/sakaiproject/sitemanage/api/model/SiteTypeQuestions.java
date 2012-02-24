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
 * There is a list of SiteSetupQuestion object for site type
 * @author zqian
 *
 */
import java.util.List;

public interface SiteTypeQuestions {
	
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
	 * get the site type id
	 * @return
	 */
	public String getSiteTypeId();
	
	/**
	 * set the site type id
	 * @param siteTypeId
	 */
	public void setSiteTypeId(String siteTypeId);
	
	/**
	 * get the site type 
	 * @return
	 */
	public String getSiteType();
	
	/**
	 * set the site type 
	 * @param siteType
	 */
	public void setSiteType(String siteType);

	/**
	 * get the list of SiteSetupQuestion objects
	 * @return
	 */
	public List<SiteSetupQuestion> getQuestions();
	
	/**
	 * add the SiteSetupQuestion object
	 * @return
	 */
	public void addQuestion(SiteSetupQuestion question);
	
	/**
	 * set the list of SiteSetupQuestion objects
	 * @param qList
	 */
	public void setQuestions(List<SiteSetupQuestion> questions);
	
	/**
	 * get the instruction for taking the questions
	 * @return
	 */
	public String getInstruction();
	
	/**
	 * set the instruction for taking the questions
	 * @param instruction
	 */
	public void setInstruction(String instruction);
	
	/**
	 * get the URL
	 * @return
	 */
	public String getUrl();
	
	/**
	 * set the URL
	 * @param url
	 */
	public void setUrl(String url);
	
	/**
	 * get the URL target
	 * @return
	 */
	public String getUrlTarget();
	
	/**
	 * set the URL target
	 * @param url
	 */
	public void setUrlTarget(String urlTarget);
	
	/**
	 * get the URL label
	 * @return
	 */
	public String getUrlLabel();
	
	/**
	 * set the URL
	 * @param url
	 */
	public void setUrlLabel(String urlLabel);
}
