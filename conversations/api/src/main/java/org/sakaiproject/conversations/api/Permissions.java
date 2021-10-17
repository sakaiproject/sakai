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
package org.sakaiproject.conversations.api;

import java.util.stream.Stream;

public enum Permissions {
    ROLETYPE_INSTRUCTOR("conversations.roletype.instructor"),
    MODERATE("conversations.moderate"),
    TOPIC_CREATE("conversations.topic.create"),
    TOPIC_UPDATE_OWN("conversations.topic.update.own"),
    TOPIC_UPDATE_ANY("conversations.topic.update.any"),
    TOPIC_DELETE_OWN("conversations.topic.delete.own"),
    TOPIC_DELETE_ANY("conversations.topic.delete.any"),
    TOPIC_TAG("conversations.topic.tag"),
    TOPIC_PIN("conversations.topic.pin"),
    TAG_CREATE("conversations.tag.create"),
    VIEW_GROUP_TOPICS("conversations.topic.view.groups"),
    POST_CREATE("conversations.post.create"),
    POST_UPDATE_OWN("conversations.post.update.own"),
    POST_UPDATE_ANY("conversations.post.update.any"),
    POST_DELETE_OWN("conversations.post.delete.own"),
    POST_DELETE_ANY("conversations.post.delete.any"),
    POST_REACT("conversations.post.react"),
    POST_UPVOTE("conversations.post.upvote"),
    COMMENT_CREATE("conversations.comment.create"),
    COMMENT_UPDATE_OWN("conversations.comment.update.own"),
    COMMENT_UPDATE_ANY("conversations.comment.update.any"),
    COMMENT_DELETE_OWN("conversations.comment.delete.own"),
    COMMENT_DELETE_ANY("conversations.comment.delete.any"),
    VIEW_ANONYMOUS("conversations.anonymous.view"),
    VIEW_STATISTICS("conversations.statistics.view");

    public final String label;

    private Permissions(String label) {
        this.label = label;
    }

    public static Stream<Permissions> stream() {
        return Stream.of(Permissions.values());
    }
}
