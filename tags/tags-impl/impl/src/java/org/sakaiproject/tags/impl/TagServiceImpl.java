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

package org.sakaiproject.tags.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tags.api.I18n;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagAssociation;
import org.sakaiproject.tags.api.TagAssociationRepository;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.api.TagCollectionRepository;
import org.sakaiproject.tags.api.TagRepository;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tags.api.TagServiceException;
import org.sakaiproject.tags.impl.common.SakaiI18n;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Coordinates tag persistence, associations, import metadata, and events in one transaction.
 */
@Slf4j
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private static final String TAGSERVICE_MAXPAGESIZE =  "tagservice.maxpagesize";
    private static final String TAGSERVICE_ENABLED =  "tagservice.enabled";
    private static final Boolean TAGSERVICE_ENABLED_DEFAULT_VALUE =  true;
    private static final int TAGSERVICE_MAXPAGESIZE_DEFAULT_VALUE = 200;
    private static final int TAG_MAX_LABEL = 255;

    @Setter private FunctionManager functionManager;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private TagAssociationRepository tagAssociationRepository;

    @Setter private TagRepository tagRepository;
    @Setter private TagCollectionRepository tagCollectionRepository;
    @Setter private SessionManager sessionManager;
    @Setter private EventTrackingService eventTrackingService;

    @Override
    public void init() {
        functionManager.registerFunction(TAGSERVICE_MANAGE_PERMISSION);
    }

    @Override
    @Transactional
    public void saveTagAssociation(String itemId, String tagId) {
        TagAssociation tagAssociation = new TagAssociation();
        tagAssociation.setItemId(itemId);
        tagAssociation.setTagId(tagId);
        tagAssociationRepository.save(tagAssociation);
    }

    @Override
    public List<Tag> getTagsByExactLabel(String label, String collectionId) {
        return tagCopies(tagRepository.findByLabel(label, collectionId));
    }

    @Override
    public List<String> getTagAssociationIds(String collectionId, String itemId) {
        return tagAssociationRepository.findTagAssociationByCollectionAndItem(collectionId, itemId).stream().map(TagAssociation::getTagId).collect(Collectors.toList());
    }

    @Override
    public List<Tag> getAssociatedTagsForItem(String collectionId, String itemId) {
        return tagCopies(tagRepository.findAssociatedTags(collectionId, itemId));
    }

    @Override
    @Transactional
    public List<Tag> duplicateTags(String targetCollectionId, boolean isSite, Collection<String> tagIds, String targetItemId) {
        List<Tag> duplicatedTags = new ArrayList<>();

        ensureCollectionExists(targetCollectionId, isSite);

        for (String tagId : tagIds) {
            Tag tag = getTag(tagId).orElse(null);
            if (tag == null) {
                log.warn("Tag with id {} does not exist anymore", tagId);
                continue;
            }

            Tag duplicatedTag = Tag.builder()
                .tagCollectionId(targetCollectionId)
                .tagLabel(tag.getTagLabel())
                .description(tag.getDescription())
                .build();
            String id = createTag(duplicatedTag);
            duplicatedTag.setTagId(id);

            duplicatedTags.add(duplicatedTag);

            if (targetItemId != null) {
                saveTagAssociation(targetItemId, duplicatedTag.getTagId());
            }
        }

        return duplicatedTags;
    }

    @Override
    @Transactional
    public void updateTagAssociations(String collectionId, String itemId, Collection<String> tagIds, boolean isSite) {
        ensureCollectionExists(collectionId, isSite);

        // obtain previous asociations
        List<String> oldAssociationIds = getTagAssociationIds(collectionId, itemId);
        for (String tagId : tagIds) {
            // skip if already associated or empty
            if (StringUtils.isEmpty(tagId) || oldAssociationIds.contains(tagId)) {
                continue;
            }
            // we cut the tag
            if (tagId.length() > TAG_MAX_LABEL) {
                tagId = tagId.substring(0, TAG_MAX_LABEL);
            }
            // new association, check tag exists
            Tag t = getTag(tagId).orElse(null);
            if (t == null) {
                
                t = Tag.builder()
                    .tagCollectionId(collectionId)
                    .tagLabel(tagId)
                    .build();
                String id = createTag(t);
                t.setTagId(id);
            }

            // save tag association
            saveTagAssociation(itemId, t.getTagId());
        }

        // remove deselected
        oldAssociationIds.removeAll(tagIds);
        for (String oldId : oldAssociationIds) {
            TagAssociation ta = tagAssociationRepository.findTagAssociationByItemIdAndTagId(itemId, oldId);
            tagAssociationRepository.delete(ta);
        }
    
    }

    private void ensureCollectionExists(String collectionId, boolean isSite) {
        if (tagCollectionRepository.existsById(collectionId)) {
            return;
        }
        I18n i18n = getI18n(getClass().getClassLoader(), "org.sakaiproject.tags.api.i18n.tagservice");
        String description = isSite ? i18n.tFormatted("site_collection", collectionId) : i18n.t("user_collection");
        createTagCollection(TagCollection.builder()
            .tagCollectionId(collectionId)
            .name(collectionId)
            .description(description)
            .build());
    }

    @Override
    public I18n getI18n(ClassLoader loader, String resourceBase) {
        return new SakaiI18n(loader, resourceBase);
    }

    @Override
    public int getMaxPageSize() { return serverConfigurationService.getInt(TAGSERVICE_MAXPAGESIZE, TAGSERVICE_MAXPAGESIZE_DEFAULT_VALUE); }


    @Override
    public Boolean getServiceActive (){
        return serverConfigurationService.getBoolean(TAGSERVICE_ENABLED, TAGSERVICE_ENABLED_DEFAULT_VALUE);
    }

    @Override
    public List<Tag> getTags() {
        return tagCopies(tagRepository.findAllOrdered());
    }

    @Override
    public Optional<Tag> getTag(String id) {
        return tagRepository.findById(id).map(tag -> tagCopies(Collections.singletonList(tag)).get(0));
    }

    @Override
    public List<Tag> getTagsInCollection(String collectionId) {
        return getTagsPaginatedInCollection(1, Integer.MAX_VALUE, collectionId);
    }

    @Override
    public List<Tag> getTagsPaginatedInCollection(int pageNum, int pageSize, String collectionId) {
        return tagCopies(tagRepository.findByCollection(collectionId, offset(pageNum, pageSize), pageSize));
    }

    @Override
    public List<Tag> getTagsByPartialLabel(String label) {
        return tagCopies(tagRepository.findByPartialLabel(label));
    }

    @Override
    public List<Tag> getTagsByPrefixInLabel(String label) {
        return getTagsPaginatedByPrefixInLabel(1, Integer.MAX_VALUE, label);
    }

    @Override
    public List<Tag> getTagsPaginatedByPrefixInLabel(int pageNum, int pageSize, String label) {
        return tagCopies(tagRepository.findByPrefix(label, offset(pageNum, pageSize), pageSize));
    }

    @Override
    public int getTotalTagsInCollection(String collectionId) {
        return Math.toIntExact(tagRepository.countByCollection(collectionId));
    }

    @Override
    public int getTotalTagsByPrefixInLabel(String label) {
        return Math.toIntExact(tagRepository.countByPrefix(label));
    }

    @Override
    public Optional<Tag> getTagForExternalIdAndCollection(String externalId, String collectionId) {
        return tagRepository.findByExternalId(externalId, collectionId).map(tag -> tagCopies(Collections.singletonList(tag)).get(0));
    }

    @Override
    public List<TagCollection> getTagCollections() {
        return getTagCollectionsPaginated(1, Integer.MAX_VALUE);
    }

    @Override
    public List<TagCollection> getTagCollectionsPaginated(int pageNum, int pageSize) {
        return tagCollectionRepository.findAllOrdered(offset(pageNum, pageSize), pageSize).stream()
            .map(this::copy).collect(Collectors.toList());
    }

    @Override
    public int getTotalTagCollections() {
        return Math.toIntExact(tagCollectionRepository.count());
    }

    @Override
    public Optional<TagCollection> getTagCollection(String id) {
        return tagCollectionRepository.findById(id).map(this::copy);
    }

    @Override
    public Optional<TagCollection> getTagCollectionForName(String name) {
        return tagCollectionRepository.findByName(name).map(this::copy);
    }

    @Override
    public Optional<TagCollection> getTagCollectionForExternalSourceName(String name) {
        return tagCollectionRepository.findByExternalSourceName(name).map(this::copy);
    }

    @Override
    @Transactional
    public String createTag(Tag tag) {
        Tag created = copy(tag);
        created.setTagId(UUID.randomUUID().toString());
        created.setCreatedBy(sessionManager.getCurrentSessionUserId());
        created.setCreationDate(Instant.now().toEpochMilli());
        created.setLastModifiedBy(created.getCreatedBy());
        created.setLastModificationDate(created.getCreationDate());
        tagRepository.create(created);
        postAfterCommit("tags.new.tag", "/tags/" + created.getTagId());
        return created.getTagId();
    }

    @Override
    @Transactional
    public String createTagCollection(TagCollection collection) {
        TagCollection created = copy(collection);
        if (created.getTagCollectionId() == null) {
            created.setTagCollectionId(UUID.randomUUID().toString());
        }
        created.setCreatedBy(sessionManager.getCurrentSessionUserId());
        created.setCreationDate(Instant.now().toEpochMilli());
        created.setLastModifiedBy(created.getCreatedBy());
        created.setLastModificationDate(created.getCreationDate());
        tagCollectionRepository.create(created);
        postAfterCommit("tags.new.collection", "/tagcollections/" + created.getTagCollectionId());
        return created.getTagCollectionId();
    }

    @Override
    @Transactional
    public void updateTag(Tag tag) {
        Tag original = tagRepository.findById(tag.getTagId())
            .orElseThrow(() -> new TagServiceException("No tag with id " + tag.getTagId()));
        boolean generateEvent = isEventGeneratingUpdate(tag, original);
        Tag updated = isDirtyingUpdate(tag, original) ? copy(tag) : copy(original);
        updated.setCreatedBy(original.getCreatedBy());
        updated.setCreationDate(original.getCreationDate());
        // Importers use this timestamp even for otherwise unchanged tags.
        updated.setLastModifiedBy(sessionManager.getCurrentSessionUserId());
        updated.setLastModificationDate(Instant.now().toEpochMilli());
        tagRepository.save(updated);
        if (generateEvent) {
            postAfterCommit("tags.update.tag", "/tags/" + tag.getTagId());
        }
    }

    @Override
    @Transactional
    public void updateTagCollection(TagCollection collection) {
        TagCollection original = tagCollectionRepository.findById(collection.getTagCollectionId())
            .orElseThrow(() -> new TagServiceException("No collection with id " + collection.getTagCollectionId()));
        if (!isDirtyingUpdate(collection, original)) {
            return;
        }
        boolean generateEvent = isEventGeneratingUpdate(collection, original);
        TagCollection updated = copy(collection);
        updated.setCreatedBy(original.getCreatedBy());
        updated.setCreationDate(original.getCreationDate());
        updated.setLastModifiedBy(sessionManager.getCurrentSessionUserId());
        updated.setLastModificationDate(Instant.now().toEpochMilli());
        tagCollectionRepository.save(updated);
        if (generateEvent) {
            postAfterCommit("tags.update.collection", "/tagcollections/" + collection.getTagCollectionId());
        }
    }

    @Override
    @Transactional
    public void deleteTag(String id) {
        tagRepository.findById(id).ifPresent(tag -> {
            tagAssociationRepository.deleteByTagId(id);
            tagRepository.delete(tag);
            postAfterCommit("tags.delete.tag", "/tags/" + id);
        });
    }

    @Override
    @Transactional
    public void deleteTagCollection(String id) {
        tagCollectionRepository.findById(id).ifPresent(collection -> {
            for (Tag tag : tagRepository.findByCollection(id, 0, Integer.MAX_VALUE)) {
                tagAssociationRepository.deleteByTagId(tag.getTagId());
                tagRepository.delete(tag);
            }
            tagCollectionRepository.delete(collection);
            postAfterCommit("tags.delete.collection", "/tagcollections/" + id);
        });
    }

    @Override
    @Transactional
    public List<String> deleteTagsOlderThanDateFromCollection(String collectionId, long timestamp) {
        return deleteTags(tagRepository.findOlderThan(collectionId, timestamp));
    }

    @Override
    @Transactional
    public List<String> deleteTagFromExternalCollection(String externalId, String collectionId) {
        return deleteTags(tagRepository.findAllByExternalId(externalId, collectionId));
    }

    private List<String> deleteTags(List<Tag> tags) {
        List<String> ids = new ArrayList<>();
        for (Tag tag : tags) {
            ids.add(tag.getTagId());
            deleteTag(tag.getTagId());
        }
        return ids;
    }

    private int offset(int pageNum, int pageSize) {
        if (pageNum < 1 || pageSize < 1) {
            throw new IllegalArgumentException("Page number and size must be positive");
        }
        return Math.multiplyExact(pageNum - 1, pageSize);
    }

    private List<Tag> tagCopies(List<Tag> tags) {
        Map<String, String> collectionNames = new HashMap<>();
        for (TagCollection collection : tagCollectionRepository.findAllByIds(tags.stream()
                .map(Tag::getTagCollectionId).distinct().collect(Collectors.toList()))) {
            collectionNames.put(collection.getTagCollectionId(), collection.getName());
        }
        List<Tag> copies = new ArrayList<>();
        for (Tag tag : tags) {
            Tag detached = copy(tag);
            detached.setCollectionName(collectionNames.get(tag.getTagCollectionId()));
            copies.add(detached);
        }
        return copies;
    }

    private void postAfterCommit(String name, String reference) {
        Event event = eventTrackingService.newEvent(name, reference, true);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventTrackingService.post(event);
            }
        });
    }

    private Tag copy(Tag tag) {
        return new Tag(tag.getTagId(),
            tag.getTagCollectionId(),
            tag.getTagLabel(),
            tag.getDescription(),
            tag.getCreatedBy(),
            tag.getCreationDate(),
            tag.getLastModifiedBy(),
            tag.getLastModificationDate(),
            tag.getExternalId(),
            tag.getAlternativeLabels(),
            tag.getExternalCreation(),
            tag.getExternalCreationDate(),
            tag.getExternalUpdate(),
            tag.getLastUpdateDateInExternalSystem(),
            tag.getParentId(),
            tag.getExternalHierarchyCode(),
            tag.getExternalType(),
            tag.getData(),
            tag.getCollectionName(), null);
    }

    private TagCollection copy(TagCollection collection) {
        return new TagCollection(collection.getTagCollectionId(),
            collection.getName(),
            collection.getDescription(),
            collection.getCreatedBy(),
            collection.getCreationDate(),
            collection.getExternalSourceName(),
            collection.getExternalSourceDescription(),
            collection.getLastModifiedBy(),
            collection.getLastModificationDate(),
            collection.getExternalUpdate(),
            collection.getExternalCreation(),
            collection.getLastSynchronizationDate(),
            collection.getLastUpdateDateInExternalSystem());
    }

    private boolean isDirtyingUpdate(Tag proposed, Tag original) {
        return isEventGeneratingUpdate(proposed, original)
                || !(Objects.equals(proposed.getExternalCreation(), original.getExternalCreation()))
                || !(Objects.equals(proposed.getExternalCreationDate(), original.getExternalCreationDate()))
                || !(Objects.equals(proposed.getExternalUpdate(), original.getExternalUpdate()))
                || !(Objects.equals(proposed.getLastUpdateDateInExternalSystem(), original.getLastUpdateDateInExternalSystem()));
    }

    private boolean isEventGeneratingUpdate(Tag proposed, Tag original) {
        return !(Objects.equals(proposed.getTagLabel(), original.getTagLabel()))
                || !(Objects.equals(proposed.getDescription(), original.getDescription()))
                || !(Objects.equals(proposed.getExternalId(), original.getExternalId()))
                || !(Objects.equals(proposed.getAlternativeLabels(), original.getAlternativeLabels()))
                || !(Objects.equals(proposed.getParentId(), original.getParentId()))
                || !(Objects.equals(proposed.getExternalHierarchyCode(), original.getExternalHierarchyCode()))
                || !(Objects.equals(proposed.getExternalType(), original.getExternalType()))
                || !(Objects.equals(proposed.getData(), original.getData()));
    }

    private boolean isDirtyingUpdate(TagCollection proposed, TagCollection original) {
        return isEventGeneratingUpdate(proposed, original)
                || !(Objects.equals(proposed.getExternalUpdate(), original.getExternalUpdate()))
                || !(Objects.equals(proposed.getLastSynchronizationDate(), original.getLastSynchronizationDate()))
                || !(Objects.equals(proposed.getLastUpdateDateInExternalSystem(), original.getLastUpdateDateInExternalSystem()));
    }

    private boolean isEventGeneratingUpdate(TagCollection proposed, TagCollection original) {
        return !(Objects.equals(proposed.getName(), original.getName()))
                || !(Objects.equals(proposed.getDescription(), original.getDescription()))
                || !(Objects.equals(proposed.getExternalSourceName(), original.getExternalSourceName()))
                || !(Objects.equals(proposed.getExternalSourceDescription(), original.getExternalSourceDescription()));
    }

}
