/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-api/api/src/java/org/sakaiproject/taggable/api/TaggingProvider.java $
 * $Id: TaggingProvider.java 10548 2007-07-06 19:51:40Z jmpease@syr.edu $
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

import java.util.List;
import java.util.Map;

import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.exception.PermissionException;

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
	 * @param context
	 *            The context in which to check permission.
	 * @return True if current user is allowed to view tags, false otherwise.
	 */
	boolean allowViewTags(String context);
	
	/**
	 * Method to check if current user is allowed to look at particular activity
	 * @param activityRef Reference of the activity
	 * @param userId Current user
	 * @param taggedItem Reference of the related item that can be used for additional permission checking
	 * @return
	 */
	boolean allowGetActivity(String activityRef, String userId, String taggedItem);
	
	/**
	 * Method to check if current user is allowed to look at a particular item
	 * @param itemRef Reference of the activity to which the item belongs
	 * @param itemRef Reference of the item
	 * @param userId Current user
	 * @param taggedItem Reference of the related item that can be used for additional permission checking
	 * @return
	 */
	boolean allowGetItem(String activityRef, String itemRef, String userId, String taggedItem);
	
	/**
	 * Method to check if current user is allowed to look at a particular list of items
	 * @param itemRef Reference of the activity to which the items belong
	 * @param itemRefs List of references for a bunch of items (all items should belong to the same activity)
	 * @param userId Current user
	 * @param taggedItem Reference of the related item that can be used for additional permission checking
	 * @return
	 */
	boolean allowGetItems(String activityRef, String[] itemRefs, String userId, String taggedItem);

	/**
	 * Method to get the necessary data to invoke a helper tool for tagging the
	 * activity identified by the given ref.
	 * 
	 * @param activityRef
	 *            The reference to the activity that is to be tagged.
	 * @return An object containing the data to invoke the appropriate helper
	 *         tool. Returns null if this is not supported or if the current
	 *         user doesn't have permission to access the helper.
	 */
	TaggingHelperInfo getActivityHelperInfo(String activityRef);
	
	/**
	 * Method to get the necessary data to invoke a helper tool for tagging the
	 * activity identified by the given ref.
	 * 
	 * @param activityRef
	 *            The reference to the activity that is to be tagged.
	 * @return An object containing the data to invoke the appropriate helper
	 *         tool. Returns null if this is not supported or if the current
	 *         user doesn't have permission to access the helper.
	 */
	Map<String, TaggingHelperInfo> getActivityHelperInfo(String context, List<String> activityRefs);

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
	 */
	TaggingHelperInfo getItemsHelperInfo(String activityRef);

	/**
	 * Method to get the necessary data to invoke a helper tool for tagging the
	 * given item.
	 * 
	 * @param itemRef
	 *            The reference to the item that is to be tagged.
	 * @return An object containing the data to invoke the appropriate helper
	 *         tool. Returns null if this is not supported or if the current
	 *         user doesn't have permission to access the helper.
	 */
	TaggingHelperInfo getItemHelperInfo(String itemRef);

	/**
	 * Returns a list of tags for the given activity.
	 * 
	 * @param activity
	 *            An activity that has been tagged.
	 * @return A list of tags for the given activity.
	 * @see TagList
	 */
	TagList getTags(TaggableActivity activity);

	/**
	 * Method to get a unique identifier for this provider.
	 * 
	 * @return A unique identifier for this provider.
	 */
	String getId();

	/**
	 * Method to get a displayable name for the provider. For example "Goal
	 * Management".
	 * 
	 * @return A displayable name for this provider.
	 */
	String getName();
	
	/**
	 * Method to get a displayable text label for the provider.  For example "This item is linked to:" 
	 * @return
	 */
	String getSimpleTextLabel();
	
	/**
	 * Method to get a label for some help text
	 * @return
	 */
	String getHelpLabel();

	/**
	 * Method to get some descriptive help text
	 * @return
	 */
	String getHelpDescription();

	/**
	 * Method to remove all tags from this activity. This method should check
	 * with the activity's producer using
	 * {@link TaggableActivityProducer#allowRemoveTags(TaggableActivity)} to
	 * make sure the user has permission to perform this action.
	 * 
	 * @param activity
	 *            The activity from which to remove tags.
	 * @throws PermissionException
	 *             Exception thrown if current user is not allowed to remove
	 *             tags from the given activity.
	 */
	void removeTags(TaggableActivity activity) throws PermissionException;

	/**
	 * Method to remove all tags from this item. This method should check with
	 * the item's producer using
	 * {@link TaggableActivityProducer#allowRemoveTags(TaggableItem)} to make
	 * sure the user has permission to perform this action.
	 * 
	 * @param item
	 *            The item from which to remove tags.
	 * @throws PermissionException
	 *             Exception thrown if current user is not allowed to remove
	 *             tags from the given item.
	 */
	void removeTags(TaggableItem item) throws PermissionException;

	/**
	 * Method to copy tags from an activity being duplicated to it's duplicate,
	 * as happens when a context is duplicated (see
	 * {@link EntityTransferrer#transferCopyEntities(String, String, java.util.List)}).
	 * This method should check with the activity's producer using
	 * {@link TaggableActivityProducer#allowTransferCopyTags(TaggableActivity)}
	 * to make sure the user has permission to perform this action.
	 * 
	 * @param fromActivity
	 *            The activity being duplicated from which to copy tags.
	 * @param toActivity
	 *            The duplicate activity to which to copy tags to.
	 * @throws PermissionException
	 *             Exception thrown if current user is not allowed to copy tags
	 *             for the given activities.
	 */
	void transferCopyTags(TaggableActivity fromActivity,
			TaggableActivity toActivity) throws PermissionException;
}
