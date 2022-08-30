/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class MapResourceBundleTest {

    Map<String, Object> map;

    @Before
    public void setup() {
        map = new HashMap<String, Object>();
        map.put("1", "one");
        map.put("2", "two");
        map.put("3", "three");
        map.put("4", "four");
    }

    @Test
    public void testMapResourceBundle() {
        MapResourceBundle bundle = new MapResourceBundle(map, "test", Locale.getDefault());

        assertEquals(bundle.getBaseBundleName(), "test");
        assertEquals(bundle.getLocale(), Locale.getDefault());

        assertTrue(bundle.handleKeySet().size() == 4);
        assertEquals(bundle.getString("1"), "one");
        assertEquals(bundle.getString("2"), "two");
        assertEquals(bundle.getString("3"), "three");
        assertEquals(bundle.getString("4"), "four");
    }
}
