/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.commons.api;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.commons.api.datamodel.Comment;
import org.sakaiproject.commons.api.datamodel.Post;
import org.sakaiproject.entity.api.Entity;

@Slf4j
public class CommonsReferenceReckoner {

    @Value
    public static class CommonsReference {

        private CommonsConstants.PostType type;
        private String context;
        private String postId;
        private String commentId;
        @Getter(AccessLevel.NONE) private String reference;

        @Override
        public String toString() {

            if (type == CommonsConstants.PostType.POST) {
                return CommonsManager.REFERENCE_ROOT + Entity.SEPARATOR + context + Entity. SEPARATOR + "posts" + Entity.SEPARATOR + postId;
            } else {
                return CommonsManager.REFERENCE_ROOT + Entity.SEPARATOR + context + Entity. SEPARATOR + "posts" + Entity.SEPARATOR + postId + Entity.SEPARATOR + "comments" + Entity.SEPARATOR + commentId;
            }
        }

        public String getReference() {
            return toString();
        }
    }

    /**
     * This is a builder for an AssignmentReference
     *
     * @param context
     * @param id
     * @param reference
     * @return
     */
    @Builder(builderMethodName = "reckoner", buildMethodName = "reckon")
    public static CommonsReference newCommonsReferenceReckoner(Post post, Comment comment, String context, String postId, String commentId, String reference) {

        CommonsConstants.PostType type = null;

        if (StringUtils.startsWith(reference, CommonsManager.REFERENCE_ROOT)) {
            String[] parts = StringUtils.splitPreserveAllTokens(reference, Entity.SEPARATOR);
            if (parts.length >= 5) {
                if (context == null) context = parts[2];
                if (postId == null) postId = parts[4];
                if (parts.length == 5) {
                    type = CommonsConstants.PostType.POST;
                } else if (parts.length == 7) {
                    type = CommonsConstants.PostType.COMMENT;
                    if (commentId == null) commentId = parts[6];
                }
            }
        } else if (post != null) {
            context = post.getSiteId();
            type = CommonsConstants.PostType.POST;
            postId = post.getId();
        } else if (comment != null) {
            type = CommonsConstants.PostType.COMMENT;
            if (context == null) context = comment.getPost().getSiteId();
            postId = comment.getPost().getId();
            commentId = comment.getId();
        }

        return new CommonsReference(
                type,
                (context == null) ? "" : context,
                (postId == null) ? "" : postId,
                (commentId == null) ? "" : commentId,
                (reference == null) ? "" : reference);
    }
}
