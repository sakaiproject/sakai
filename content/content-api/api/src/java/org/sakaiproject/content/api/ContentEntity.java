/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.content.api;

import org.sakaiproject.content.api.GroupAwareEntity;

public interface ContentEntity extends GroupAwareEntity
{
	/**
	 * Access this ContentEntity's containing collection, or null if this entity is the site collection.
	 * @return
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
}
