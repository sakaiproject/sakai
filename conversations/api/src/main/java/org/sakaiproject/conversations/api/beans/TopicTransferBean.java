/*
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.conversations.api.beans;

import org.sakaiproject.conversations.api.model.Metadata;
import org.sakaiproject.conversations.api.model.Tag;
import org.sakaiproject.conversations.api.model.Topic;
import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.TopicType;
import org.sakaiproject.conversations.api.TopicVisibility;
import org.sakaiproject.entity.api.Entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicTransferBean implements Entity {

    public String id;
    public String title;
    public String siteId;
    public String aboutReference;
    public String message;
    public long numberOfPosts;
    public long numberOfUnreadPosts;
    public String creator;
    public Instant created;
    public String formattedCreatedDate;
    public String modifier;
    public Instant modified;
    public String formattedModifiedDate;
    public boolean pinned;
    public boolean locked;
    public boolean hidden;
    public boolean resolved;
    public List<Tag> tags = new ArrayList<>();
    public Map<Reaction, Boolean> myReactions = new HashMap<>();
    public Map<Reaction, Integer> reactionTotals = new HashMap<>();
    public Set<String> groups = new HashSet<>();
    public String type;
    public String visibility;
    public int unread;
    public boolean bookmarked;
    public boolean draft;
    public boolean anonymous;
    public boolean allowAnonymousPosts;

    public String creatorDisplayName;
    public boolean canEdit;
    public boolean canDelete;
    public boolean canPost;
    public boolean canPin;
    public boolean canBookmark;
    public boolean canTag;
    public boolean canReact;
    public boolean canModerate;
    public boolean isMine;

    public String url;
    public String reference;

    public static TopicTransferBean of(Topic topic) {

        TopicTransferBean topicBean = new TopicTransferBean();

        topicBean.id = topic.getId();
        topicBean.title = topic.getTitle();
        topicBean.siteId = topic.getSiteId();
        topicBean.aboutReference = topic.getAboutReference();
        topicBean.message = topic.getMessage();
        Metadata metadata = topic.getMetadata();
        topicBean.creator = metadata.getCreator();
        topicBean.created = metadata.getCreated();
        topicBean.modifier = metadata.getModifier();
        topicBean.modified = metadata.getModified();
        topicBean.pinned = topic.getPinned();
        topicBean.locked = topic.getLocked();
        topicBean.hidden = topic.getHidden();
        topicBean.resolved = topic.getResolved();
        topicBean.type = topic.getType().name();
        topicBean.visibility = topic.getVisibility().name();
        topicBean.draft = topic.getDraft();
        topicBean.anonymous = topic.getAnonymous();
        topicBean.allowAnonymousPosts = topic.getAllowAnonymousPosts();
        topicBean.groups = topic.getGroups();

        return topicBean;
    }

    public Topic asTopic() {

        Topic topic = new Topic();
        topic.setId(this.id);
        topic.setSiteId(this.siteId);
        topic.setTitle(this.title);
        topic.setAboutReference(this.aboutReference);
        topic.setMessage(this.message);

        Metadata metadata = new Metadata();
        metadata.setCreator(this.creator);
        metadata.setCreated(this.created);
        metadata.setModifier(this.modifier);
        metadata.setModified(this.modified);
        topic.setMetadata(metadata);

        topic.setTagIds(this.tags.stream().map(t -> t.getId()).collect(Collectors.toSet()));
        topic.setGroups(this.groups);
        topic.setPinned(this.pinned);
        topic.setLocked(this.locked);
        topic.setHidden(this.hidden);
        topic.setType(Enum.valueOf(TopicType.class, this.type));
        topic.setVisibility(Enum.valueOf(TopicVisibility.class, this.visibility));
        topic.setDraft(this.draft);
        topic.setAnonymous(this.anonymous);
        topic.setAllowAnonymousPosts(this.allowAnonymousPosts);

        return topic;
    }
}
