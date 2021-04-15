package org.sakaiproject.conversations.api;

import java.util.stream.Stream;

public enum Permissions {
    TOPIC_CREATE("conversations.topic.create"),
    TOPIC_DELETE_ANY("conversations.topic.delete.any"),
    TOPIC_TAG("conversations.topic.tag"),
    TOPIC_VIEW_HIDDEN("conversations.topic.view.hidden"),
    TOPIC_VIEW_GROUPS("conversations.topic.view.groups"),
    POST_CREATE("conversations.post.create"),
    POST_DELETE_ANY("conversations.post.delete.any"),
    POST_TAG("conversations.post.tag"),
    POST_ACCEPT("conversations.post.accept");

    public final String permission;

    private Permissions(String permission) {
        this.permission = permission;
    }

    public static Stream<Permissions> stream() {
        return Stream.of(Permissions.values());
    }
}
