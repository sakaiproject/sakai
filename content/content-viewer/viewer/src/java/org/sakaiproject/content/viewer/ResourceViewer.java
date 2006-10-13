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

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.viewer.ResourceCollection;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.util.ResourceLoader;

public class ResourceViewer
{
	private static Log logger = LogFactory.getLog(ResourceViewer.class);
	
	private static ResourceLoader rb = new ResourceLoader("content_viewer");
	
	protected ContentHostingService contentHostingService;
	protected ResourceCollection collection = null;
	protected boolean expandHierarchy = false;
	protected List errorMessages = new Vector();
	
	/**
	 * Dependency: ContentHostingService
	 * @param service
	 */
	public void setContentHostingService(ContentHostingService service)
	{
		contentHostingService = service;
	}
	
	public ContentHostingService getContentHostingService()
	{
		return contentHostingService;
	}
	
	/**
	 * Config: set the collection to which this instance of the viewer gives access.
	 * @param collectionId
	 */
	public void setCollectionId(String collectionId)
	{
		try
		{
			ContentCollection entity = contentHostingService.getCollection(collectionId);
			if(entity == null)
			{
				
			}
			else
			{
				collection = new ResourceCollection(entity, this);
				collection.setExpandFolders(expandHierarchy);
			}
		}
		catch(IdUnusedException e)
		{
			logger.debug("IdUnusedException collectionId == " + collectionId);
			errorMessages.add(rb.getFormattedMessage("viewer.idunused", new Object[]{collectionId}));
		}
		catch(PermissionException e)
		{
			logger.debug("PermissionException collectionId == " + collectionId);
			errorMessages.add(rb.getString("viewer.nopermit"));
		}
		catch(TypeException e)
		{
			logger.debug("TypeException collectionId == " + collectionId);
			errorMessages.add(rb.getString("viewer.type"));
		}
	}
	
	/**
	 * Access the current collection
	 * @return
	 */
	public ResourceCollection getCollection()
	{
		return collection;
	}
	
	/**
	 * 
	 * @param expand
	 */
	public void setExpandHierarchy(boolean expand)
	{
		expandHierarchy = expand;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isExpandHierarchy()
	{
		return expandHierarchy;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSortByPriorityEnabled()
	{
		return contentHostingService.isSortByPriorityEnabled();
	}

}	// class ResourceViewer
