/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.site.api;

import java.util.List;

/**
 * SAK-29138 - Portal, user and site-manage tools will optionally (if an implementation is
 * present) use this to conditionally display the site or section title in the UI.
 * 
 * @author bjones86
 */
public interface SiteTitleAdvisor
{
	/**
	 * Given a site and a user ID, return the appropriate site or section title for the user.
	 * 
	 * SAK-29138 - Takes into account 'portal.use.sectionTitle' sakai.property; 
	 * if set to true, this method will return the title of the section the current 
	 * user is enrolled in for the site (if it can be found). Otherwise, it will 
	 * return the site title (default behaviour).
	 * 
	 * @param site the site in question
	 * @param userID the ID of the current user
	 * @param siteProviders the providerIDs associated with this site; they will be queried if this parameter is null
	 * @return the site or section title
	 */
	public String getUserSpecificSiteTitle( Site site, String userID, List<String> siteProviders );
}
