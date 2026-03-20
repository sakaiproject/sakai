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

import java.util.Optional;
import java.util.stream.Stream;

public enum ConversationsEvent {
    TOPIC_CREATED("conversations.topic.created"),
    TOPIC_DELETED("conversations.topic.deleted"),
    REACTED_TO_TOPIC("conversations.topic.reacted"),
    UNREACTED_TO_TOPIC("conversations.topic.unreacted"),
    POSTED_TO_TOPIC("conversations.topic.posted"),
    TOPIC_UPDATED("conversations.topic.updated"),
    TOPIC_UPVOTED("conversations.topic.upvoted"),
    POST_CREATED("conversations.post.created"),
    POST_REPLIED("conversations.post.replied"),
    POST_VIEWED("conversations.post.viewed"),
    POST_DELETED("conversations.post.deleted"),
    REACTED_TO_POST("conversations.post.reacted"),
    POST_RESTORED("conversations.post.restored"),
    POST_UPDATED("conversations.post.updated"),
    POST_UPVOTED("conversations.post.upvoted"),
    COMMENT_CREATED("conversations.comment.created"),
    COMMENT_DELETED("conversations.comment.deleted"),
    COMMENT_UPDATED("conversations.comment.updated");

    public final String label;

    private ConversationsEvent(String label) {
        this.label = label;
    }

    public static Stream<ConversationsEvent> stream() {
        return Stream.of(ConversationsEvent.values());
    }

    public static Optional<ConversationsEvent> from(String label) {

        for (ConversationsEvent ce : ConversationsEvent.values()) {
            if (ce.label.equalsIgnoreCase(label)) {
                return Optional.of(ce);
            }
        }
        return Optional.empty();
    }
}
