/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/content/util/BasicSiteSelectableResourceType.java $
 * $Id: BasicSiteSelectableResourceType.java 101634 2011-12-12 16:44:33Z aaronz@vt.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.util;

import org.sakaiproject.content.api.SiteSpecificResourceType;

/**
 * @author jreng
 *
 */
public class BasicSiteSelectableResourceType extends BasicResourceType
		implements SiteSpecificResourceType 
{

	protected boolean isEnabledByDefault;

	public BasicSiteSelectableResourceType(String id) 
	{
		super(id);
	}

	public boolean isEnabledByDefault() 
	{
		return this.isEnabledByDefault;
	}

	public void setEnabledByDefault(boolean isEnabledByDefault) 
	{
		this.isEnabledByDefault = isEnabledByDefault;
	}

}
