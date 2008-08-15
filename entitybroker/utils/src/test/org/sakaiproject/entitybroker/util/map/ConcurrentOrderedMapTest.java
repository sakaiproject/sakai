/**
 * $Id$
 * $URL$
 * ConcurrentOrderedMapTest.java - entity-broker - Aug 15, 2008 5:58:03 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.util.map;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Testing the concurrent ordered map
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ConcurrentOrderedMapTest extends OrderedMapTest {

    protected ConcurrentOrderedMap<String, String> m1 = null;

    @Override
    protected void setUp() throws Exception {
        m1 = new ConcurrentOrderedMap<String, String>();
        m1.put("AAA", "aaronz");
        m1.put("BBB", "beckyz");
        m1.put("CCC", "cat");
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.map.ConcurrentOrderedMap#putIfAbsent(java.lang.Object, java.lang.Object)}.
     */
    public void testPutIfAbsentKV() {
        m1.putIfAbsent("AAA", "az");
        assertEquals(3, m1.size());

        m1.putIfAbsent("DDD", "dog");
        assertEquals(4, m1.size());

        m1.putIfAbsent("BBB", "beckers");
        assertEquals(4, m1.size());

        List<String> keys = m1.getKeys();
        assertNotNull(keys);
        assertEquals(4, keys.size());
        assertEquals("AAA", keys.get(0));
        assertEquals("BBB", keys.get(1));
        assertEquals("CCC", keys.get(2));
        assertEquals("DDD", keys.get(3));

        m1.putIfAbsent("DDD", "dog");
        assertEquals(4, m1.size());
        
        keys = m1.getKeys();
        assertEquals(4, keys.size());
        assertEquals("AAA", keys.get(0));
        assertEquals("BBB", keys.get(1));
        assertEquals("CCC", keys.get(2));
        assertEquals("DDD", keys.get(3));
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.map.ConcurrentOrderedMap#remove(java.lang.Object, java.lang.Object)}.
     */
    public void testRemoveObjectObject() {
        m1.remove("AAA", "sadfsd");
        assertEquals(3, m1.size());
        
        m1.remove("QWAS", "aaronz");
        assertEquals(3, m1.size());
        
        m1.remove("AAA", "aaronz");
        assertEquals(2, m1.size());
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.map.OrderedMap#clear()}.
     */
    public void testClear() {
        assertNotNull(m1);
        assertEquals(3, m1.size());
        assertFalse(m1.isEmpty());
        m1.clear();
        assertNotNull(m1);
        assertEquals(0, m1.size());
        assertTrue(m1.isEmpty());
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.map.OrderedMap#getKeys()}.
     */
    public void testGetKeys() {
        List<String> keys = m1.getKeys();
        assertNotNull(keys);
        assertEquals(3, keys.size());
        assertEquals("AAA", keys.get(0));
        assertEquals("BBB", keys.get(1));
        assertEquals("CCC", keys.get(2));
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.map.OrderedMap#getEntries()}.
     */
    public void testGetEntries() {
        List<Entry<String, String>> entries = m1.getEntries();
        assertNotNull(entries);
        assertEquals(3, entries.size());
        assertEquals("AAA", entries.get(0).getKey());
        assertEquals("BBB", entries.get(1).getKey());
        assertEquals("CCC", entries.get(2).getKey());
        assertEquals("aaronz", entries.get(0).getValue());
        assertEquals("beckyz", entries.get(1).getValue());
        assertEquals("cat", entries.get(2).getValue());
    }

    public void testGetEntry() {
        Entry<String, String> entry = m1.getEntry(1);
        assertNotNull(entry);
        assertEquals("BBB", entry.getKey());

        try {
            entry = m1.getEntry(5);
            fail("should have died");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    public void testPutKV() {
        m1.put("DDD", "dog");
        assertEquals(4, m1.size());

        m1.put("BBB", "beckers");
        assertEquals(4, m1.size());

        List<String> keys = m1.getKeys();
        assertNotNull(keys);
        assertEquals(4, keys.size());
        assertEquals("AAA", keys.get(0));
        assertEquals("CCC", keys.get(1));
        assertEquals("DDD", keys.get(2));
        assertEquals("BBB", keys.get(3));

        m1.put("DDD", "dog");
        assertEquals(4, m1.size());
        
        keys = m1.getKeys();
        assertEquals(4, keys.size());
        assertEquals("AAA", keys.get(0));
        assertEquals("CCC", keys.get(1));
        assertEquals("BBB", keys.get(2));
        assertEquals("DDD", keys.get(3));
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.map.OrderedMap#remove(java.lang.Object)}.
     */
    public void testRemoveObject() {
        String val = m1.remove("AAA");
        assertNotNull(val);
        assertEquals("aaronz", val);
        assertEquals(2, m1.size());

        List<String> keys = m1.getKeys();
        assertNotNull(keys);
        assertEquals(2, keys.size());
        assertEquals("BBB", keys.get(0));
        assertEquals("CCC", keys.get(1));

        val = m1.remove("EEE");
        assertNull(val);
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.map.OrderedMap#keys()}.
     */
    public void testKeys() {
        Enumeration<String> e = m1.keys();
        assertTrue( e.hasMoreElements() );
        assertEquals(e.nextElement(), "AAA");
        assertTrue( e.hasMoreElements() );
        assertEquals(e.nextElement(), "BBB");
        assertTrue( e.hasMoreElements() );
        assertEquals(e.nextElement(), "CCC");
        assertFalse( e.hasMoreElements() );
        try {
            e.nextElement();
            fail("should have died");
        } catch (NoSuchElementException ne) {
            assertNotNull(ne.getMessage());
        }
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.map.OrderedMap#elements()}.
     */
    public void testElements() {
        Enumeration<String> e = m1.elements();
        assertTrue( e.hasMoreElements() );
        assertEquals(e.nextElement(), "aaronz");
        assertTrue( e.hasMoreElements() );
        assertEquals(e.nextElement(), "beckyz");
        assertTrue( e.hasMoreElements() );
        assertEquals(e.nextElement(), "cat");
        assertFalse( e.hasMoreElements() );
        try {
            e.nextElement();
            fail("should have died");
        } catch (NoSuchElementException ne) {
            assertNotNull(ne.getMessage());
        }
    }


    public void testKeySet() {
        Set<String> keys = m1.keySet();
        assertEquals(3, keys.size());
        Iterator<String> it = keys.iterator();
        assertTrue( it.hasNext() );
        assertEquals(it.next(), "AAA");
        assertTrue( it.hasNext() );
        assertEquals(it.next(), "BBB");
        assertTrue( it.hasNext() );
        assertEquals(it.next(), "CCC");
        assertFalse( it.hasNext() );
        try {
            it.next();
            fail("should have died");
        } catch (NoSuchElementException e) {
            assertNotNull(e.getMessage());
        }

        // test remove
        it = keys.iterator();
        it.next();
        it.next();
        it.remove();
        assertEquals(2, keys.size());
        assertEquals(2, m1.size());

        // test remove all
        keys.clear();
        assertEquals(0, keys.size());
        assertTrue(keys.isEmpty());
        assertEquals(0, m1.size());
        assertTrue(m1.isEmpty());
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.map.OrderedMap#values()}.
     */
    public void testValues() {
        Collection<String> values = m1.values();
        assertEquals(3, values.size());
        Iterator<String> it = values.iterator();
        assertTrue( it.hasNext() );
        assertEquals(it.next(), "aaronz");
        assertTrue( it.hasNext() );
        assertEquals(it.next(), "beckyz");
        assertTrue( it.hasNext() );
        assertEquals(it.next(), "cat");
        assertFalse( it.hasNext() );
        try {
            it.next();
            fail("should have died");
        } catch (NoSuchElementException e) {
            assertNotNull(e.getMessage());
        }

        // test remove
        it = values.iterator();
        it.next();
        it.next();
        it.remove();
        assertEquals(2, values.size());
        assertEquals(2, m1.size());

        // test remove all
        values.clear();
        assertEquals(0, values.size());
        assertTrue(values.isEmpty());
        assertEquals(0, m1.size());
        assertTrue(m1.isEmpty());
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.util.map.OrderedMap#entrySet()}.
     */
    public void testEntrySet() {
        Set<Entry<String, String>> entries = m1.entrySet();
        assertEquals(3, entries.size());
        Iterator<Entry<String, String>> it = entries.iterator();
        assertTrue( it.hasNext() );
        assertEquals(it.next().getKey(), "AAA");
        assertTrue( it.hasNext() );
        assertEquals(it.next().getKey(), "BBB");
        assertTrue( it.hasNext() );
        assertEquals(it.next().getKey(), "CCC");
        assertFalse( it.hasNext() );
        try {
            it.next();
            fail("should have died");
        } catch (NoSuchElementException e) {
            assertNotNull(e.getMessage());
        }

        // test remove
        it = entries.iterator();
        it.next();
        it.next();
        it.remove();
        assertEquals(2, entries.size());
        assertEquals(2, m1.size());

        // test remove all
        entries.clear();
        assertEquals(0, entries.size());
        assertTrue(entries.isEmpty());
        assertEquals(0, m1.size());
        assertTrue(m1.isEmpty());
    }

}
