/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tags.tool.service;

import java.util.Objects;
import java.util.Optional;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.api.TagService;

/** Authorized browser management operations; synchronization uses TagService directly. */
public class TagManagementService {

    @Setter private TagService tagService;

    public Optional<TagCollection> getCollection(String siteId, String collectionId) {
        tagService.checkCollectionAccess(siteId, collectionId);
        return tagService.getTagCollection(collectionId);
    }

    public Optional<Tag> getTag(String siteId, String tagId) {
        Optional<Tag> tag = tagService.getTag(tagId);
        tag.ifPresent(value -> tagService.checkCollectionAccess(siteId, value.getTagCollectionId()));
        return tag;
    }

    public String createTag(String siteId, Tag submitted) {
        String collectionId = submitted.getTagCollectionId();
        requireEditableCollection(siteId, collectionId);
        Tag tag = Tag.builder().tagCollectionId(collectionId).tagLabel(submitted.getTagLabel())
            .description(submitted.getDescription()).externalId(submitted.getExternalId()).build();
        tagService.saveTag(siteId, tag);
        return collectionId;
    }

    public String updateTag(String siteId, Tag submitted) {
        Tag original = requireTag(siteId, submitted.getTagId());
        if (StringUtils.isNotBlank(submitted.getTagCollectionId())
                && !Objects.equals(original.getTagCollectionId(), submitted.getTagCollectionId())) {
            throw new SecurityException("A tag cannot be moved through the management form");
        }
        requireEditableTag(siteId, original);
        Tag edits = original.toBuilder().tagLabel(submitted.getTagLabel())
            .description(submitted.getDescription()).externalId(submitted.getExternalId()).build();
        tagService.saveTag(siteId, edits);
        return original.getTagCollectionId();
    }

    public String deleteTag(String siteId, String tagId) {
        Tag tag = requireTag(siteId, tagId);
        requireEditableTag(siteId, tag);
        tagService.deleteTag(siteId, tagId);
        return tag.getTagCollectionId();
    }

    public void createCollection(String siteId, TagCollection submitted) {
        TagCollection collection = TagCollection.builder().name(submitted.getName())
            .description(submitted.getDescription()).externalSourceName(submitted.getExternalSourceName())
            .externalSourceDescription(submitted.getExternalSourceDescription()).build();
        tagService.saveTagCollection(siteId, collection);
    }

    public void updateCollection(String siteId, TagCollection submitted) {
        TagCollection original = requireEditableCollection(siteId, submitted.getTagCollectionId());
        TagCollection edits = original.toBuilder().name(submitted.getName()).description(submitted.getDescription())
            .externalSourceName(submitted.getExternalSourceName())
            .externalSourceDescription(submitted.getExternalSourceDescription()).build();
        tagService.saveTagCollection(siteId, edits);
    }

    public void deleteCollection(String siteId, String collectionId) {
        requireEditableCollection(siteId, collectionId);
        if (tagService.getTagsInCollection(collectionId).stream()
                .anyMatch(tag -> Boolean.TRUE.equals(tag.getExternalCreation()))) {
            throw new SecurityException("A collection containing externally created tags cannot be deleted");
        }
        tagService.deleteTagCollection(siteId, collectionId);
    }

    private Tag requireTag(String siteId, String tagId) {
        return getTag(siteId, tagId).orElseThrow(() -> new IllegalArgumentException("No matching tag"));
    }

    private TagCollection requireEditableCollection(String siteId, String collectionId) {
        if (!tagService.canManageCollection(siteId, collectionId)) {
            throw new SecurityException("Current user cannot manage this tag collection");
        }
        TagCollection collection = getCollection(siteId, collectionId)
            .orElseThrow(() -> new IllegalArgumentException("No matching tag collection"));
        checkEditable(collection);
        return collection;
    }

    private void requireEditableTag(String siteId, Tag tag) {
        requireEditableCollection(siteId, tag.getTagCollectionId());
        if (Boolean.TRUE.equals(tag.getExternalCreation())) {
            throw new SecurityException("Externally created tags are read-only");
        }
    }

    private void checkEditable(TagCollection collection) {
        if (Boolean.TRUE.equals(collection.getExternalCreation())) {
            throw new SecurityException("Externally created collections are read-only");
        }
    }
}
