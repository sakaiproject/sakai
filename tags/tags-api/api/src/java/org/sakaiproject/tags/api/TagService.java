/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tags.api;

import java.util.Collection;
import java.util.List;

/**
 * The interface for the tags service.
 */
public interface TagService {

    public static final String TAGSERVICE_MANAGE_PERMISSION = "tagservice.manage";
    public static final String TOOL_ASSIGNMENTS = "assignments";
    public static final String TOOL_PRIVATE_MESSAGES = "privatemessages";
    
    public static final String TAGSERVICE_ENABLED_INTEGRATION_PROP = "tagservice.enable.integrations";
    public static final boolean TAGSERVICE_ENABLED_INTEGRATION_DEFAULT = true;

    public void init();

    public void destroy();

    /**
        * Return the tags sub-service.
    */
    public Tags getTags();

    /**
        * Return the collections sub-service
    */
    public TagCollections getTagCollections();

    /**
        * Return an I18N translator for a given file and locale.
    */
    public I18n getI18n(ClassLoader loader, String resourceBase);

    /**
        * Return if the service is enabled or not.
    */
    public Boolean getServiceActive ();

    /**
        * Return the max size of the pages
    */
    public int getMaxPageSize();

    /**
        * Save a new association between an item and a specific tag.
        * @param itemId
        * The ID of the item to be associated.
        * @param tagId
        * The ID of the tag to associate with the item.
    */
    public void saveTagAssociation(String itemId, String tagId);
    /**
        * Retrieve a list of tags that match an exact label within a specific collection.
        * @param label
        * The exact text label of the tag to look for.
        * @param collectionId
        * The ID of the collection to search within.
        * @return A list of tags matching the given label.
	*/
    public List<Tag> getTagsByExactLabel(String label, String collectionId);
    /**
        * Retrieve the IDs of all tag associations for a given collection and item.
        * @param collectionId
        * The ID of the collection context.
        * @param itemId
        * The ID of the item whose tag association IDs are being requested.
        * @return A list of tag IDs associated with the specified item.
	*/
    public List<String> getTagAssociationIds(String collectionId, String itemId);
    /**
        * Retrieve the full tag objects associated with a specific item.
        * @param collectionId
        * The ID of the collection context.
        * @param itemId
        * The ID of the item whose associated tags are being requested.
        * @return A list of Tag objects associated with the item, skipping any that no longer exist.
	*/
    public List<Tag> getAssociatedTagsForItem(String collectionId, String itemId);
    /**
        * Duplicate a list of tags into a target collection, creating the collection if it does not exist,
	    * and optionally associate them with a target item.
	    * @param targetCollectionId
	    * The ID of the collection where the tags will be duplicated.
	    * @param isSite
	    * Whether the target collection belongs to a site context or a user context.
	    * @param tagIds
	    * The collection of IDs of the tags to be duplicated.
	    * @param targetItemId
	    * The ID of the item to associate the duplicated tags with, or null if no association is needed.
	    * @return A list containing the newly duplicated tags.
    */
    public List<Tag> duplicateTags(String targetCollectionId, boolean isSite, Collection<String> tagIds, String targetItemId);
    /**
        * Update the tag associations for an item by adding new ones and removing those deselected.
        * @param collectionId
        * The ID of the collection context.
        * @param itemId
        * The ID of the item to update associations for.
        * @param tagIds
        * The current collection of tag IDs that should be associated with the item.
        * @param isSite
        * Whether the collection belongs to a site context or a user context.
	*/
    public void updateTagAssociations(String collectionId, String itemId, Collection<String> tagIds, boolean isSite);
}
