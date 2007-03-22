/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.SiteSpecificResourceType;

public class DbResourceTypeRegistry extends ResourceTypeRegistryImpl 
{
	/** Our logger. */
	protected static Log M_log = LogFactory.getLog(DbResourceTypeRegistry.class);

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#getTypes(java.lang.String)
	 */
	public Collection<ResourceType> getTypes(String context) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#setResourceTypesForContext(java.lang.String, java.util.Map)
	 */
	public void setMapOfResourceTypesForContext(String context, Map<String, Boolean> enabled) 
	{
		
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceTypeRegistry#getResourceTypesForContext(java.lang.String)
	 */
	public Map<String, Boolean> getMapOfResourceTypesForContext(String context) 
	{
		return null;
	} 


	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			M_log.info("init()");
		}
		catch (Throwable t)
		{
		}
	}

}
