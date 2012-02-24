/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.api;

/**
 * SiteSpecificResourceType is a ResourceType that can be enabled or disabled on a site-by-site basis.
 *
 */
public interface SiteSpecificResourceType extends ResourceType 
{
	/**
	 * Determine whether the type is enabled by default. 
	 * @return true if the type is enabled by default and false otherwise. 
	 */
	public boolean isEnabledByDefault();
	
	

}
