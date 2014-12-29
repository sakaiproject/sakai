/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-api/api/src/java/org/sakaiproject/taggable/api/TaggableActivityProducer.java $
 * $Id: TaggableActivityProducer.java 10548 2007-07-06 19:51:40Z jmpease@syr.edu $
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

import org.sakaiproject.entity.api.EntityTransferrer;

/**
 * A service that produces activities that can be tagged.
 * 
 * @author The Sakai Foundation.
 */
public interface TaggableActivityProducer {

	/**
	 * Method to check if the current user can get all of the items for an
	 * activity.
	 * 
	 * @param activity
	 *            The activity that contains the items.
	 * @param provider
	 *            The provider that is checking for permission.
	 * @param getMyItemsOnly
	 * 			  This flag will return only items where the owner matches userId
	 * @param taggedItem Reference to an item that can be used for additional permission lookups
	 * @return True if the current user can get all of the items for the given
	 *         activity, false otherwise.
	 */
	boolean allowGetItems(TaggableActivity activity, TaggingProvider provider, boolean getMyItemsOnly, String taggedItem);

	/**
	 * Method to determine if the current user has permission to remove tags
	 * from the given activity. This should be checked by
	 * {@link TaggingProvider#removeTags(TaggableActivity)} before removing
	 * tags. While {@link TaggingProvider} objects determine tagging access
	 * given an activity, the producer has control over who can remove the
	 * activity (ex. deletion) and, therefore, who can remove tags from the
	 * activity.
	 * 
	 * @param activity
	 *            The activity to check permission against.
	 * @return True if the current user is allowed to delete the specified
	 *         activity, false otherwise.
	 */
	boolean allowRemoveTags(TaggableActivity activity);

	/**
	 * Method to determine if the current user has permission to remove tags
	 * from the given item. This should be checked by
	 * {@link TaggingProvider#removeTags(TaggableItem)} before removing tags.
	 * While {@link TaggingProvider} objects determine tagging access given an
	 * item, the producer has control over who can remove the item (ex.
	 * deletion) and, therefore, who can remove tags from the item.
	 * 
	 * @param item
	 *            The item to check permission against.
	 * @return True if the current user is allowed to delete the specified item,
	 *         false otherwise.
	 */
	boolean allowRemoveTags(TaggableItem item);

	/**
	 * Method to determine if the current user has permission to copy tags from
	 * one activity to another, as would happen during
	 * {@link EntityTransferrer#transferCopyEntities(String, String, List)}.
	 * This should be checked by
	 * {@link TaggingProvider#transferCopyTags(TaggableActivity, TaggableActivity)}
	 * before copying tags. We want tags to be copied when activities are
	 * duplicated, but only want to do so if the current user is allowed to copy
	 * the activities.
	 * 
	 * @param activity
	 *            The activity to check permission against.
	 * @return True if the current user is allowed to copy the specified
	 *         activity, false otherwise.
	 */
	boolean allowTransferCopyTags(TaggableActivity activity);

	/**
	 * Method to check if this producer handles the given reference.
	 * 
	 * @param ref
	 *            A reference for an object produced by a taggable activity
	 *            producer.
	 * @return True if this producer handles the reference, false otherwise.
	 */
	boolean checkReference(String ref);

	/**
	 * Method to get the context of the object represented by this reference.
	 * 
	 * @param ref
	 *            A reference for an object produced by a taggable activity
	 *            producer.
	 * @return The context of the referenced object.
	 */
	String getContext(String ref);

	/**
	 * Method to get a displayable name for the producing service.
	 * 
	 * @return A common displayable name for this service.
	 */
	String getName();

	/**
	 * Method to get the unique identifier for this producing service.
	 * 
	 * @return A unique identifier for this service.
	 */
	String getId();

	/**
	 * Method to get a list of all taggable activities within the given context.
	 * 
	 * @param context
	 *            The context to search.
	 * @param provider
	 *            The provider that is getting the activities. This allows the
	 *            producer to selectively return different lists of activities
	 *            depending on the given provider.
	 * @return A list, possibly empty, of all taggable activities within the
	 *         given context.
	 */
	List<TaggableActivity> getActivities(String context,
			TaggingProvider provider);

	/**
	 * Method to get a taggable activity by reference string.
	 * 
	 * @param activityRef
	 *            A reference for the taggable activity.
	 * @param provider
	 *            The provider that is getting the activity. This allows the
	 *            producer to selectively return an activity, or none at all,
	 *            depending on the given provider.
	 * @return The taggable activity, or null if no such activity exists or the
	 *         provider cannot access it.
	 */
	TaggableActivity getActivity(String activityRef, TaggingProvider provider);

	/**
	 * Method to get a list of items for an activity.
	 * 
	 * @param activity
	 *            The activity that contains the items to retrieve.
	 * @param provider
	 *            The provider that is getting the items. This allows the
	 *            producer to selectively return different lists of items
	 *            depending on the given provider.
	 * @param getMyItemsOnly
	 * 			  This flag will return only items where the owner matches userId
	 * @param taggedItem Reference to an item that can be used for additional permission lookups
	 * @return A list of items for the given activity.
	 */
	List<TaggableItem> getItems(TaggableActivity activity,
			TaggingProvider provider, boolean getMyItemsOnly, String taggedItem);

	/**
	 * Method to get a list of items belonging to a specific user for an
	 * activity.
	 * 
	 * @param activity
	 *            The activity that contains the items to retrieve.
	 * @param userId
	 *            The identifier of the user who submitted the items for the
	 *            given activity.
	 * @param provider
	 *            The provider that is getting the items. This allows the
	 *            producer to selectively return different lists of items
	 *            depending on the given provider.
	 * @param getMyItemsOnly
	 * 			  This flag will return only items where the owner matches userId
	 * @param taggedItem Reference to an item that can be used for additional permission lookups
	 * @return A list of items submitted by the specified user for the given
	 *         activity.
	 */
	List<TaggableItem> getItems(TaggableActivity activity, String userId,
			TaggingProvider provider, boolean getMyItemsOnly, String taggedItem);

	/**
	 * Method to get a taggable item by reference string.
	 * 
	 * @param itemRef
	 *            The reference for the taggable item.
	 * @param provider
	 *            The provider that is getting the item. This allows the
	 *            producer to selectively return an item, or none at all,
	 *            depending on the given provider.
	 *@param getMyItemOnly
	 * 			  This flag will return only items where the owner matches userId
	 * @param taggedItem Reference to an item that can be used for additional permission lookups
	 * @return The taggable item, or null if no such item exists or the provider
	 *         cannot access it.
	 */
	TaggableItem getItem(String itemRef, TaggingProvider provider, boolean getMyItemOnly, String taggedItem);
	
	/**
	 * Method to get the permission to add to a secirity advisor so we can 
	 * view the item (in case we don't have normal permissions)
	 * @return
	 */
	String getItemPermissionOverride();
	
	/**
	 * Method to figure out if there are any submissions
	 * @param taggedItem Reference to an item that can be used for additional permission lookups
	 * @return
	 */
	boolean hasSubmissions(TaggableActivity activity,
			TaggingProvider provider, boolean getMyItemsOnly, String taggedItem);
	
	/**
	 * Method to figure out if there are any submissions
	 * @param taggedItem Reference to an item that can be used for additional permission lookups
	 * @return
	 */
	boolean hasSubmissions(TaggableActivity activity, String userId,
			TaggingProvider provider, boolean getMyItemsOnly, String taggedItem);
}
