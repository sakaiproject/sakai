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
import java.util.Optional;

/**
 * The interface for the tags service.
 * Reads follow the normal JPA lifecycle: entities remain managed while their
 * persistence context is open.
 */
public interface TagService {

    public static final String TAGSERVICE_MANAGE_PERMISSION = "tagservice.manage";
    public static final String TOOL_ASSIGNMENTS = "assignments";
    public static final String TOOL_PRIVATE_MESSAGES = "privatemessages";
    
    public static final String TAGSERVICE_ENABLED_INTEGRATION_PROP = "tagservice.enable.integrations";
    public static final boolean TAGSERVICE_ENABLED_INTEGRATION_DEFAULT = true;

    public void init();

    public String createTag(Tag tag);

    /** Apply submitted values to the tag within a transaction, preserving creation metadata.
     * Build edits separately (for example with {@code tag.toBuilder()}); do not mutate
     * a queried entity before calling this method in an enclosing transaction.
     */
    public void updateTag(Tag tag);

    public void deleteTag(String tagId);

    public List<Tag> getTags();

    public Optional<Tag> getTag(String tagId);

    public List<Tag> getTagsPaginatedInCollection(int pageNum, int pageSize, String tagCollectionId);

    public int getTotalTagsInCollection(final String tagCollectionId);

    public int getTotalTagsByPrefixInLabel(final String label);

    public Optional<Tag> getTagForExternalIdAndCollection(String tagExternalId, String tagCollectionId);

    public List<Tag> getTagsInCollection(String tagCollectionId);

    public List<Tag> getTagsByPartialLabel(String label);

    public List<Tag> getTagsByPrefixInLabel(String label);

    public List<Tag> getTagsPaginatedByPrefixInLabel(int pageNum, int pageSize, String label);

    public List<String> deleteTagsOlderThanDateFromCollection(String tagCollectionId, long lastModificationDate);

    public List<String> deleteTagFromExternalCollection(String externalId, String tagCollectionId);

    public String createTagCollection(TagCollection tagCollection);

    /** Apply separately constructed edits within a transaction, preserving creation metadata. */
    public void updateTagCollection(TagCollection tagCollection);

    public void deleteTagCollection(String tagCollectionId);

    public List<TagCollection> getTagCollections();

    public Optional<TagCollection> getTagCollection(String tagCollectionId);

    public Optional<TagCollection> getTagCollectionForName(String name);

    public Optional<TagCollection> getTagCollectionForExternalSourceName(String externalSourceName);

    public List<TagCollection> getTagCollectionsPaginated(int pageNum, int pageSize);

    public int getTotalTagCollections();

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
        * Associate an existing tag ID; a missing ID is an error, never a label.
        * @param itemId
        * The ID of the item to be associated.
        * @param tagId
        * The ID of the tag to associate with the item.
    */
    public void associateExistingTag(String itemId, String tagId);

    /** Create a new tag from a literal label and associate it with the item. */
    public String createAndAssociateTag(String collectionId, String itemId, String label, boolean isSite);
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
    /** Replace the UI selection. Existing IDs are associated; other values are literal labels.
     * Prefer the explicit association methods when the input kind is already known.
     */
    public void updateTagAssociations(String collectionId, String itemId, Collection<String> selections, boolean isSite);
}
