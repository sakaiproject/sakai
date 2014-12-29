/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/AreaManager.java $
 * $Id: AreaManager.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.api.app.messageforums;

/**
 * The Area id the high level object of the object model. Typicaly a site
 * can contain up to 2 Areas - a Discussion Area and a Private Message Area
 * 
 * @author rshastri
 *
 */


public interface AreaManager
{
	/**
	 * Is the private area enabeled in this site?
	 * @return
	 */
	public boolean isPrivateAreaEnabled();
	
	/**
	 * Save an area
	 * @param area
	 */
	public void saveArea(Area area);
	public void saveArea(Area area, String currentUser);
	
	/**
	 * Create an area of the given type in the given context
	 * @param typeId the type id (private or discussion)
	 * @param contextId the context
	 * @return the created Area object
	 */
	public Area createArea(String typeId, String contextId);
	
	/**
	 * 
	 * @param area
	 */
	public void deleteArea(Area area);
	
	/**
	 * Get an area of the given type
	 * @param typeId
	 * @return
	 */
	public Area getAreaByContextIdAndTypeId(String typeId);
    
	
	/** Get an Area by context and type
	 * @param contextId
	 * @param typeId
	 * @return
	 */
	public Area getAreaByContextIdAndTypeId(String contextId, String typeId);
	
	/** 
	 * Get all Areas of the given type
	 * @param typeId
	 * @return
	 * 
	 * @deprecated since Jan 2008, seems never to have been used
	 */
	public Area getAreaByType(final String typeId);  
	
	/**
	 * Get the private area for this site
	 * @return
	 */
	public Area getPrivateArea();
	public Area getPrivateArea(String siteId);
	
	/**
	 * Get the discussion are for this site
	 * @return
	 * 
	 * @deprecated rather use getDiscussionArea(String)
	 */
	public Area getDiscusionArea();
	
	/**
	 * Get the discussion are for the given context
	 * @param contextId
	 * @return
	 */
	public Area getDiscussionArea(final String contextId);
	
	/**
	 * Get the discussion are for the given context
	 * @param contextId
	 * @param createDefaultForum
	 * @return
	 */
	public Area getDiscussionArea(final String contextId, boolean createDefaultForum);

	/**
	 * @param key
	 * @return
	 */
	public String getResourceBundleString(String key);
}
