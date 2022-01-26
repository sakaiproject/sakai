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

import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.model.Metadata;
import org.sakaiproject.conversations.api.model.Post;
import org.sakaiproject.entity.api.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostTransferBean implements Entity {

    public String id;
    public String message;
    public int numberOfComments;
    public List<CommentTransferBean> comments;
    public String creator;
    public Instant created;
    public String formattedCreatedDate;
    public String siteId;
    public String modifier;
    public Instant modified;
    public String formattedModifiedDate;
    public Map<Reaction, Boolean> myReactions = new HashMap<>();
    public Map<Reaction, Integer> reactionTotals = new HashMap<>();
    public boolean viewed;
    public boolean locked;
    public boolean hidden;
    public boolean draft;
    public boolean anonymous;
    public boolean isMine;
    public boolean privatePost;
    public int upvotes;
    public String topic;

    public String creatorDisplayName;
    public String verifierDisplayName;
    public boolean upvoted;
    public boolean canView;
    public boolean canEdit;
    public boolean canDelete;
    public boolean canComment;
    public boolean canUpvote;
    public boolean canReact;
    public boolean canModerate;
    public boolean isInstructor;

    public String url;
    public String reference;

    public static PostTransferBean of(Post post) {

        PostTransferBean postBean = new PostTransferBean();

        postBean.id = post.getId();
        postBean.message = post.getMessage();
        postBean.numberOfComments = post.getNumberOfComments();
        Metadata metadata = post.getMetadata();
        postBean.creator = metadata.getCreator();
        postBean.created = metadata.getCreated();
        postBean.modifier = metadata.getModifier();
        postBean.modified = metadata.getModified();
        postBean.hidden = post.getHidden();
        postBean.locked = post.getLocked();
        postBean.draft = post.getDraft();
        postBean.siteId = post.getSiteId();
        postBean.privatePost = post.getPrivatePost();
        postBean.upvotes = post.getUpvotes();
        postBean.topic = post.getTopic().getId();
        postBean.anonymous = post.getAnonymous();

        return postBean;
    }

    public Post asPost() {

        Post post = new Post();
        post.setId(this.id);
        post.setMessage(this.message);
        post.setNumberOfComments(this.numberOfComments);

        Metadata metadata = new Metadata();
        metadata.setCreator(this.creator);
        metadata.setCreated(this.created);
        metadata.setModifier(this.modifier);
        metadata.setModified(this.modified);
        post.setMetadata(metadata);

        post.setSiteId(this.siteId);

        post.setHidden(this.hidden);
        post.setLocked(this.locked);
        post.setDraft(this.draft);
        post.setUpvotes(this.upvotes);
        post.setPrivatePost(this.privatePost);
        post.setAnonymous(this.anonymous);

        return post;
    }
}
