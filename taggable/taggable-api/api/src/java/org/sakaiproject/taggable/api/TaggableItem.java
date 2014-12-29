/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-api/api/src/java/org/sakaiproject/taggable/api/TaggableItem.java $
 * $Id: TaggableItem.java 10548 2007-07-06 19:51:40Z jmpease@syr.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.taggable.api;

import java.util.Date;

/**
 * An object related to a tagged activity. For example, when tagging an
 * assignment, the items would be submissions for the assignment.
 * 
 * @author The Sakai Foundation.
 */
public interface TaggableItem {

	/**
	 * Method to get the base object that is wrapped as an item.
	 * 
	 * @return The base object.
	 */
	public Object getObject();

	/**
	 * @return A reference for this item.
	 */
	public String getReference();

	/**
	 * @return A title for this item.
	 */
	public String getTitle();

	/**
	 * @return The content of this item.
	 */
	public String getContent();

	/**
	 * @return The identifier of the user that created/owns this item.
	 */
	public String getUserId();

	/**
	 * @return The activity to which this item belongs.
	 */
	public TaggableActivity getActivity();
	
	/**
	 * Get the url that will render detail information about the item
	 * @return
	 */
	public String getItemDetailUrl();
	
	/**
	 * Get the url that will render private (name is hidden, etc) detail information about the item
	 * @return
	 */
	public String getItemDetailPrivateUrl();
	
	/**
	 * Get the params that will be added to the item's url
	 * @return
	 */
	public String getItemDetailUrlParams();
	
	/**
	 * Get the url for the icon that will represent this item
	 * @return
	 */
	public String getIconUrl();
	
	/**
	 * Determine if decorating the itemDetailUrl is necessary
	 * @return
	 */
	public boolean getUseDecoration();
	
	/**
	 * Get the display name of the owner of this object
	 * @return
	 */
	public String getOwner();
	
	/**
	 * Get the site title of the site where this object lives
	 * @return
	 */
	public String getSiteTitle();
	
	/**
	 * Get the item's last modification date
	 * @return
	 */
	public Date getLastModifiedDate();
	
	/**
	 * Get the type name of the item
	 * @return
	 */
	public String getTypeName();
	
	
}
