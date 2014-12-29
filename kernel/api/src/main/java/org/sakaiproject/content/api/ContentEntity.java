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

import org.sakaiproject.content.api.GroupAwareEntity;

public interface ContentEntity extends GroupAwareEntity
{
	/**
	 * Access this ContentEntity's containing collection.
	 * This was documented as returning <code>null</code> for a site collection call, but has
	 * never been the case.
	 * @return The containing collection, or <code>null</code> if this is the root collection.
	 */
	public ContentCollection getContainingCollection(); 
	
	/**
	 * Check whether an entity is a ContentResource.
	 * @return true if the entity implements the ContentResource interface, false otherwise.
	 */
	public boolean isResource();
	
	/**
	 * Check whether an entity is a ContentCollection.
	 * @return true if the entity implements the ContentCollection interface, false otherwise.
	 */
	public boolean isCollection();
	
	/**
	 * Access the "type" of this ContentEntity, which defines which ResourceType registration defines
	 * its properties.
	 * @return
	 */
	public String getResourceType();
	
	/**
	 * 
	 * @return
	 */
	public ContentHostingHandler getContentHandler();
	
	/**
	 * 
	 * @param chh
	 */
	public void setContentHandler(ContentHostingHandler chh);
	
	/**
	 * 
	 * @return
	 */
	public ContentEntity getVirtualContentEntity();
	
	/**
	 * 
	 * @param ce
	 */
	public void setVirtualContentEntity(ContentEntity ce);
	
	/**
	 * 
	 * @param nextId
	 * @return
	 */
	public ContentEntity getMember(String nextId);
	
	/**
	 * Access the URL which can be used to access the entity. Will return a relative or absolute url, 
	 * depending on value of the parameter. If parameter is true, URL will be relative to the server's
	 * root.  Otherwise, it will be a complete URL starting with the base URL of the server. 
	 * @param relative 
	 * @return The URL which can be used to access the resource.
	 */
	public String getUrl(boolean relative);
	
}
