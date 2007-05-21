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

package org.sakaiproject.assignment.taggable.api;

/**
 * A provider of tagging capabilities for services that produce taggable
 * activities.
 * 
 * @author The Sakai Foundation.
 */
public interface TaggingProvider {

	/**
	 * Method to check if current user is allowed to view tags. Generally, if
	 * this returns false the list returned by the
	 * {@link #getTags(TaggableActivity) getTags} method will be empty.
	 * 
	 * @return True if current user is allowed to view tags, false otherwise.
	 */
	public boolean allowViewTags();

	/**
	 * Method to get the necessary data to invoke a helper tool for tagging the
	 * activity identified by the given ref.
	 * 
	 * @param activityRef
	 *            The reference to the activity that is to be tagged.
	 * @return An object containing the data to invoke the appropriate helper
	 *         tool. Returns null if this is not supported or if the current
	 *         user doesn't have permission to access the helper.
	 * @see TaggingHelperInfo
	 */
	public TaggingHelperInfo getActivityHelperInfo(String activityRef);

	/**
	 * Method to get the necessary data to invoke a helper tool for tagging
	 * items that belong to the given activity.
	 * 
	 * @param activityRef
	 *            The reference to the activity that contains the items to be
	 *            tagged.
	 * @return An object containing the data to invoke the appropriate helper
	 *         tool. Returns null if this is not supported or if the current
	 *         user doesn't have permission to access the helper.
	 * @see TaggingHelperInfo
	 */
	public TaggingHelperInfo getItemsHelperInfo(String activityRef);

	/**
	 * Method to get the necessary data to invoke a helper tool for tagging the
	 * given item.
	 * 
	 * @param itemRef
	 *            The reference to the item that is to be tagged.
	 * @return An object containing the data to invoke the appropriate helper
	 *         tool. Returns null if this is not supported or if the current
	 *         user doesn't have permission to access the helper.
	 * @see TaggingHelperInfo
	 */
	public TaggingHelperInfo getItemHelperInfo(String itemRef);

	/**
	 * Returns a list of tags for the given activity.
	 * 
	 * @param activity
	 *            An activity that has been tagged.
	 * @return A list of tags for the given activity.
	 * @see TagList
	 */
	public TagList getTags(TaggableActivity activity);

	/**
	 * Method to get a unique identifier for this provider.
	 * 
	 * @return A unique identifier for this provider.
	 */
	public String getId();

	/**
	 * Method to get a displayable name for the provider. For example "Goal
	 * Management".
	 * 
	 * @return A displayable name for this provider.
	 */
	public String getName();
}
