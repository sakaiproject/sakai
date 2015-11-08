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
