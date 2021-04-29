package org.sakaiproject.messaging.api.testhandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.messaging.api.BullhornData;
import org.sakaiproject.messaging.api.BullhornHandler;

import org.springframework.stereotype.Component;

@Component
public class TestBullhornHandler implements BullhornHandler {

    public List<String> getHandledEvents() {
        return Collections.EMPTY_LIST;
    }

    public Optional<List<BullhornData>> handleEvent(Event e) {
        return null;
    }
}
