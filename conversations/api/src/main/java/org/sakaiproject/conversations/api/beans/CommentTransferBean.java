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
import org.sakaiproject.conversations.api.model.Comment;

import java.time.Instant;
import lombok.Setter;

@Setter
public class CommentTransferBean {

    public String id;
    public String message;
    public String creator;
    public Instant created;
    public String formattedCreatedDate;
    public String modifier;
    public Instant modified;
    public String formattedModifiedDate;
    public String post;
    public String siteId;
    public boolean locked;

    public String creatorDisplayName;
    public boolean canView;
    public boolean canEdit;
    public boolean canDelete;

    public static CommentTransferBean of(Comment comment) {

        CommentTransferBean commentBean = new CommentTransferBean();

        commentBean.id = comment.getId();
        commentBean.message = comment.getMessage();
        Metadata metadata = comment.getMetadata();
        commentBean.creator = metadata.getCreator();
        commentBean.created = metadata.getCreated();
        commentBean.modifier = metadata.getModifier();
        commentBean.modified = metadata.getModified();
        commentBean.post = comment.getPost().getId();
        commentBean.siteId = comment.getSiteId();
        commentBean.locked = comment.getLocked();

        return commentBean;
    }

    public Comment asComment() {

        Comment comment = new Comment();
        comment.setId(this.id);
        comment.setMessage(this.message);

        Metadata metadata = new Metadata();
        metadata.setCreator(this.creator);
        metadata.setCreated(this.created);
        metadata.setModifier(this.modifier);
        metadata.setModified(this.modified);
        comment.setMetadata(metadata);
        comment.setSiteId(this.siteId);

        return comment;
    }
}
