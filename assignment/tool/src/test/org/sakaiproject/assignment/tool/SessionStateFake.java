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
