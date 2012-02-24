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

package org.sakaiproject.site.util;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import org.sakaiproject.sitemanage.api.model.*;

/**
 * The SiteSetupQuestionMap object is to store user-defined questions based on site types. 
 * Those questions could be presented as survey questions during worksite setup process.
 * @author zqian
 *
 */
public class SiteSetupQuestionMap
{
	private Map<String, SiteSetupQuestionTypeSet> questionsMap = new HashMap<String, SiteSetupQuestionTypeSet>();  
	
	/**
	 * get the set of site types which has questions defined 
	 * @return
	 */
	public Set<String> getSiteTypes()
	{
		return questionsMap.keySet();
	}
	
	/**
	 * get the question set object for specified site type
	 * @param siteType
	 * @return
	 */
	public SiteSetupQuestionTypeSet getQuestionSetBySiteType(String siteType)
	{
		if (questionsMap.containsKey(siteType))
		{
			return (SiteSetupQuestionTypeSet) questionsMap.get(siteType);
		}
		else
		{
			return null;
		}	
	}
	
	/**
	 * Set the question list for specified site type
	 * @param siteType
	 * @param l
	 */
	public void setQuestionListBySiteType(String siteType, SiteSetupQuestionTypeSet s)
	{
		if (questionsMap.containsKey(siteType))
		{
			questionsMap.put(siteType, s);
		}	
	}

	/**
	 * get the question map
	 * @return
	 */
	public Map<String, SiteSetupQuestionTypeSet> getQuestionsMap() {
		return questionsMap;
	}

	/**
	 * set the question map
	 * @param questionsMap
	 */
	public void setQuestionsMap(Map<String, SiteSetupQuestionTypeSet> questionsMap) {
		this.questionsMap = questionsMap;
	}
	
	/**
	 * Add a new site type
	 * @param siteType
	 */
	public void addSiteType(String siteType)
	{
		this.questionsMap.put(siteType, null);
	}
}