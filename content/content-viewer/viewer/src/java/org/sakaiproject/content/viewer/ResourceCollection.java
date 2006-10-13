/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.content.viewer;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.viewer.ResourceItem;
import org.sakaiproject.content.viewer.ResourceViewer;
import org.sakaiproject.entity.api.ResourceProperties;

public class ResourceCollection
{
	protected ResourceViewer resourceViewer;
	protected ContentCollection entity;
	protected ResourceCollection container = null;
	protected ResourceProperties properties;
	protected Map savedValues = new Hashtable();
	protected List members = new Vector();
	protected boolean expandFolders = false;
	
	public ResourceCollection(ContentCollection collection, ResourceViewer viewer)
	{
		entity = collection;
		resourceViewer = viewer;
		setValues();
	}
	
	public ResourceCollection(ContentCollection collection, ResourceCollection containingCollection, ResourceViewer viewer)
	{
		entity = collection;
		container = containingCollection;
		resourceViewer = viewer;
		setValues();
	}
	
	protected void setValues()
	{
		properties = entity.getProperties();
		List children = entity.getMemberResources();
		String sortBy = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		boolean sortAsc = true;
		if(resourceViewer.isSortByPriorityEnabled())
		{
			String usePrioritySort = properties.getProperty(ResourceProperties.PROP_HAS_CUSTOM_SORT);
			if(usePrioritySort != null && Boolean.TRUE.toString().equals(usePrioritySort))
			{
				sortBy = ResourceProperties.PROP_CONTENT_PRIORITY;
			}
		}
		Comparator comparator = resourceViewer.getContentHostingService().newContentHostingComparator(sortBy, sortAsc);
		Collections.sort(children, comparator);
		Iterator it = children.iterator();
		while(it.hasNext())
		{
			Object obj = it.next();
			if(obj instanceof ContentCollection)
			{
				members.add(new ResourceCollection((ContentCollection) obj, this, resourceViewer));
			}
			else if(obj instanceof ContentResource)
			{
				members.add(new ResourceItem((ContentResource) obj, this, resourceViewer));
			}
		}
	}
	
	public String getTitle()
	{
		String rv = (String) savedValues.get("getTitle");
		if(rv == null)
		{
			// if container is null, this is site-level collection
			// in that case, construct title for site collection ...
			
			// otherwise use display name
			rv = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			savedValues.put("getTitle", rv);
		}
		return rv;
	}
	
	public void setExpandFolders(boolean expand)
	{
		expandFolders = expand;
	}
	

}	// class ResourceCollection
