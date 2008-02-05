/**********************************************************************************
 * $URL:  $
 * $Id:  $
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

package org.sakaiproject.content.util;

import java.util.Set;
import java.util.TreeSet;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ExpandableResourceType;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.entity.api.Reference;

/**
 * 
 *
 */
public abstract class BaseResourceType implements ResourceType 
{
	
	public boolean hasAvailabilityDialog() 
	{
		return true;
	}

	public boolean hasDescription() 
	{
		return true;
	}

	public boolean hasGroupsDialog() 
	{
		return true;
	}

	public boolean hasNotificationDialog() 
	{
		return true;
	}

	public boolean hasOptionalPropertiesDialog() 
	{
		return true;
	}

	public boolean hasPublicDialog() 
	{
		return true;
	}

	public boolean hasRightsDialog() 
	{
		return true;
	}
	
	public boolean isExpandable()
    {
	    return (this instanceof ExpandableResourceType);
    }	

	/**
	 * Returns null to indicate that the Resources tool should display the byte count 
	 * or member count for the entity (depending on whether the entity is a 
	 * ContentResource or ContentCollection). If a different measure of the "size" of 
	 * the entity is needed, overrid this method to return a short string (no more than 
	 * 25 characters) describing the "size" of the entity as appropriate.
	 */
	public String getSizeLabel(ContentEntity entity) 
	{
		return null;
	}

	/**
	 * Returns null to indicate that the Resources tool should display the byte count 
	 * or member count for the entity (depending on whether the entity is a 
	 * ContentResource or ContentCollection). If a different measure of the "size" of 
	 * the entity is needed, overrid this method to return a short string (no more than 
	 * 80 characters) describing the "size" of the entity as appropriate.
	 */
	public String getLongSizeLabel(ContentEntity entity) 
	{
		return null;
	}

}
