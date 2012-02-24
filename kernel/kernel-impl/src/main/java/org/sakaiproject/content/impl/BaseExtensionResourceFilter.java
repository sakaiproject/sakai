/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl;

import java.util.Iterator;
import java.util.List;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceFilter;
import org.sakaiproject.content.api.ResourceToolAction;

/**
 * This class implements the typical mime type and extension filter. This will be a registered bean with the component manager that application components can extend to control the list of mime types and the list of acceptable extentions.
 */
public class BaseExtensionResourceFilter implements ContentResourceFilter
{
	private boolean viewAll = true;

	private List mimeTypes;

	private List acceptedExtensions;

	public boolean allowSelect(ContentResource resource)
	{
		// on a new resource, it seems that getContentType is more reliable than accessing that value through properties.
		String mimeType = resource.getContentType();  // .getProperties().getProperty(ResourceProperties.PROP_CONTENT_TYPE);
		String[] parts = mimeType.split("/");
		String primaryType = parts[0];

		if (!getMimeTypes().isEmpty() && !getMimeTypes().contains(primaryType) && !getMimeTypes().contains(mimeType))
		{
			return false;
		}

		String filePath = resource.getUrl();

		if (getAcceptedExtensions() != null)
		{
			// check extension
			for (Iterator i = getAcceptedExtensions().iterator(); i.hasNext();)
			{
				if (filePath.endsWith("." + i.next().toString().toLowerCase()))
				{
					return true;
				}
			}

			return false;
		}
		else
		{
			return true;
		}
	}

	public boolean allowView(ContentResource contentResource)
	{
		if (isViewAll())
		{
			return true;
		}

		return allowSelect(contentResource);
	}

	public List getMimeTypes()
	{
		return mimeTypes;
	}

	/**
	 * The list of mime types to allow. The passed in content resource will be tested to see if the resouce's primary mime type is included in the list (ie "text" for "text/xml") and then the whole mime type will be tested for existence in the list.
	 * 
	 * @param mimeTypes
	 */
	public void setMimeTypes(List mimeTypes)
	{
		this.mimeTypes = mimeTypes;
	}

	public boolean isViewAll()
	{
		return viewAll;
	}

	/**
	 * boolean to indicate if all resources should be viewable. If this is false, then the viewable resources will be based on the mime types and extention set in the other properties.
	 * 
	 * @param viewAll
	 */
	public void setViewAll(boolean viewAll)
	{
		this.viewAll = viewAll;
	}

	public List getAcceptedExtensions()
	{
		return acceptedExtensions;
	}

	/**
	 * List of accepted file name extensions. If this list is null, all extensions are acceptable.
	 * 
	 * @param acceptedExtensions
	 */
	public void setAcceptedExtensions(List acceptedExtensions)
	{
		this.acceptedExtensions = acceptedExtensions;
	}

	/**
	 * This method controls which "create" actions are allowed for ContentCollections.  
	 * Filtering "create" actions based on mimetype and extension is not yet
	 * supported very well, so this may require tweaking over time.
	 * @param actions A collection of actions to test
	 * @return A list of actions that should be shown in the filepicker
	 * 	for each collection.
	 */
    public List<ResourceToolAction> filterAllowedActions(List<ResourceToolAction> actions)
    {
    	// suggest checking for html, text, upload, etc and treating each separately
	    return actions;
    }
}
