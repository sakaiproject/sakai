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
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tool.api.SessionManager;

/** Authorized browser management operations; synchronization uses TagService directly. */
public class TagManagementService {

    @Setter private TagService tagService;
    @Setter private SecurityService securityService;
    @Setter private SessionManager sessionManager;
    @Setter private SiteService siteService;

    public Optional<TagCollection> getCollection(String siteId, String collectionId) {
        checkCollectionAccess(siteId, collectionId);
        return tagService.getTagCollection(collectionId);
    }

    public Optional<Tag> getTag(String siteId, String tagId) {
        Optional<Tag> tag = tagService.getTag(tagId);
        tag.ifPresent(value -> checkCollectionAccess(siteId, value.getTagCollectionId()));
        return tag;
    }

    public void checkCollectionAdministration() {
        if (StringUtils.isBlank(sessionManager.getCurrentSessionUserId()) || !securityService.isSuperUser()) {
            throw new SecurityException("Collection administration requires an administrator");
        }
    }

    public String createTag(String siteId, Tag submitted) {
        String collectionId = submitted.getTagCollectionId();
        if (StringUtils.isBlank(collectionId)) {
            collectionId = tagService.getTagCollectionForName(submitted.getCollectionName())
                .orElseThrow(() -> new IllegalArgumentException("No matching tag collection"))
                .getTagCollectionId();
        }
        requireEditableCollection(siteId, collectionId);
        Tag tag = Tag.builder().tagCollectionId(collectionId).tagLabel(submitted.getTagLabel())
            .description(submitted.getDescription()).externalId(submitted.getExternalId()).build();
        tagService.createTag(tag);
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
        tagService.updateTag(edits);
        return original.getTagCollectionId();
    }

    public String deleteTag(String siteId, String tagId) {
        Tag tag = requireTag(siteId, tagId);
        requireEditableTag(siteId, tag);
        tagService.deleteTag(tagId);
        return tag.getTagCollectionId();
    }

    public void createCollection(TagCollection submitted) {
        checkCollectionAdministration();
        TagCollection collection = TagCollection.builder().name(submitted.getName())
            .description(submitted.getDescription()).externalSourceName(submitted.getExternalSourceName())
            .externalSourceDescription(submitted.getExternalSourceDescription()).build();
        tagService.createTagCollection(collection);
    }

    public void updateCollection(TagCollection submitted) {
        TagCollection original = requireEditableAdminCollection(submitted.getTagCollectionId());
        TagCollection edits = original.toBuilder().name(submitted.getName()).description(submitted.getDescription())
            .externalSourceName(submitted.getExternalSourceName())
            .externalSourceDescription(submitted.getExternalSourceDescription()).build();
        tagService.updateTagCollection(edits);
    }

    public void deleteCollection(String collectionId) {
        requireEditableAdminCollection(collectionId);
        if (tagService.getTagsInCollection(collectionId).stream()
                .anyMatch(tag -> Boolean.TRUE.equals(tag.getExternalCreation()))) {
            throw new SecurityException("A collection containing externally created tags cannot be deleted");
        }
        tagService.deleteTagCollection(collectionId);
    }

    private void checkCollectionAccess(String siteId, String collectionId) {
        String userId = sessionManager.getCurrentSessionUserId();
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(siteId) || StringUtils.isBlank(collectionId)
                || !securityService.unlock(TagService.TAGSERVICE_MANAGE_PERMISSION, siteService.siteReference(siteId))) {
            throw new SecurityException("Current user cannot manage this tag collection");
        }
        if (!securityService.isSuperUser() && !collectionId.equals(siteId) && !collectionId.equals(userId)) {
            throw new SecurityException("Tag collection is outside the current site and user");
        }
    }

    private Tag requireTag(String siteId, String tagId) {
        return getTag(siteId, tagId).orElseThrow(() -> new IllegalArgumentException("No matching tag"));
    }

    private TagCollection requireEditableCollection(String siteId, String collectionId) {
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

    private TagCollection requireEditableAdminCollection(String collectionId) {
        checkCollectionAdministration();
        TagCollection collection = tagService.getTagCollection(collectionId)
            .orElseThrow(() -> new IllegalArgumentException("No matching tag collection"));
        checkEditable(collection);
        return collection;
    }

    private void checkEditable(TagCollection collection) {
        if (Boolean.TRUE.equals(collection.getExternalCreation())) {
            throw new SecurityException("Externally created collections are read-only");
        }
    }
}
