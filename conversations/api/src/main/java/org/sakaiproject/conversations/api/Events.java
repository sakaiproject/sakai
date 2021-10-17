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

public enum Events {
    TOPIC_CREATED("conversations.topic.created"),
    TOPIC_DELETED("conversations.topic.updated"),
    TOPIC_UPDATED("conversations.topic.deleted"),
    POST_CREATED("conversations.post.created"),
    POST_DELETED("conversations.post.updated"),
    POST_UPDATED("conversations.post.deleted"),
    REACTED_TO_TOPIC("conversations.topic.reacted"),
    UNREACTED_TO_TOPIC("conversations.topic.unreacted");

    public final String label;

    private Events(String label) {
        this.label = label;
    }

    public static Stream<Events> stream() {
        return Stream.of(Events.values());
    }
}
