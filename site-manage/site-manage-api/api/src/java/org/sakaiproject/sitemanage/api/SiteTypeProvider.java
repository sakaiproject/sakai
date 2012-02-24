/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.sitemanage.api;

import java.util.List;
import java.util.HashMap;


/**
 * This is to allow individual Sakai deployment to add customized site types and logic to apply those to users
 * @author zqian
 *
 */
public interface SiteTypeProvider {

	/**
	 * The whole list of customized types
	 * @return
	 */
	public List<String> getTypes();
	
	/**
	 * The site types for site list view
	 * @return
	 */
	public List<String> getTypesForSiteList();
	
	/**
	 * The site types would show up in the site creation list
	 * @return
	 */
	public List<String> getTypesForSiteCreation();
	
	/**
	 * List of template site for creating certain types of sites
	 * @return
	 */
	public HashMap<String, String> getTemplateForSiteTypes();
	
	/**
	 * Construct site title for certain type
	 * @param type
	 * @param params
	 * @return
	 */
	public String getSiteTitle(String type, List<String> params);
	
	/**
	 * Construct site title for certain type
	 * @param type
	 * @param params
	 * @return
	 */
	public String getSiteDescription(String type, List<String> params);
	
	/**
	 * Construct site title for certain type
	 * @param type
	 * @param params
	 * @return
	 */
	public String getSiteShortDescription(String type, List<String> params);
	
	/**
	 * Construct site alias for certain type
	 * @param type
	 * @param params
	 * @return
	 */
	public String getSiteAlias(String type, List<String> params);

}
