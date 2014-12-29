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

/**
 * A SiteAdvisor may be registered with the SiteService, and will be called immediately
 * priory to a site being saved.  A SiteAdvisor allows custom code to make changes to
 * a site, depending on its state immediately before the changes to the site are committed.
 * 
 * @author jholtzman@berkeley.edu
 *
 */
public interface SiteAdvisor {

	/**
	 * Called when a site is about to be saved or updated.
	 * 
	 * @param site The site being saved or updated.
	 */
	public void update(Site site);
}
