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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.viewer.ResourceCollection;
import org.sakaiproject.content.viewer.ResourceViewer;
import org.sakaiproject.entity.api.ResourceProperties;

public class ResourceItem
{
	protected ResourceViewer resourceViewer;
	protected ContentResource entity;
	protected ResourceCollection container = null;
	protected ResourceProperties properties;
	protected Map savedValues = new Hashtable();
	
	public ResourceItem(ContentResource resource, ResourceCollection container, ResourceViewer viewer)
	{
		entity = resource;
		resourceViewer = viewer;
		properties = resource.getProperties();
		container = container;
	}
	
	public String getTitle()
	{
		String rv = (String) savedValues.get("getTitle");
		if(rv == null)
		{
			rv = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			savedValues.put("getTitle", rv);
		}
		return rv;
	}
	

}	// class ResourceItem
