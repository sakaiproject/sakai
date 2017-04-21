package org.sakaiproject.assignment.tool;

import org.sakaiproject.event.api.SessionState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fake SessionState that just uses a map.
 */
public class SessionStateFake implements SessionState {
    private Map<String, Object> map = new HashMap<>();

    @Override
    public Object getAttribute(String name) {
        return map.get(name);
    }

    @Override
    public Object setAttribute(String name, Object value) {
        return map.put(name, value);
    }

    @Override
    public Object removeAttribute(String name) {
        return map.remove(name);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public List<String> getAttributeNames() {
        return new ArrayList<>(map.keySet());
    }
}
