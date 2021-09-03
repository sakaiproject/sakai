package org.sakaiproject.conversations.api;

import java.util.stream.Stream;

public enum Reaction {
    GOOD_QUESTION,
    GOOD_ANSWER,
    LOVE_IT,
    GOOD_IDEA,
    KEY;

    public static Stream<Reaction> stream() {
        return Stream.of(Reaction.values());
    }
}
