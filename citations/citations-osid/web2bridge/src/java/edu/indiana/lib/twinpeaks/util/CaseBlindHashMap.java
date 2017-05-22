/*
Copyright (c) 2000-2003 Board of Trustees of Leland Stanford Jr. University,
all rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
STANFORD UNIVERSITY BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of Stanford University shall not
be used in advertising or otherwise to promote the sale, use or other dealings
in this Software without prior written authorization from Stanford University.
*/

package edu.indiana.lib.twinpeaks.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

/**
 * CaseBlindHashMap - a HashMap extension, using <code>String</code>s as
 * key values.
 * <p>
 * Internally, keys are case insensitive: <code>ABC</code> = <code>abc</code>.
 * <p>
 * Two methods have been added to facilitate working with Sets of key strings.
 * See <code>stringKeySet()</code> and <code>stringKeyIterator()</code>.
 */
@Slf4j
public class CaseBlindHashMap extends HashMap {
  /**
   * Constructors
   */
  public CaseBlindHashMap() {
    super();
  }

  public CaseBlindHashMap(CaseBlindHashMap map) {
    super(map);
  }

  public CaseBlindHashMap(int initCap) {
    super(initCap);
  }

  public CaseBlindHashMap(int initCap, float loadFactor) {
    super(initCap, loadFactor);
  }

  /*
   * Extensions
   */

  /**
   * Get the set of keys contained in this map.  Keys values are returned as
   * simple <code>String</code>s (not the <code>CaseBlindString</code>s used
   * internally).
   *<p>
   * This is accopmlished by making a copy of the original map - modifications
   * made to this copy are not reflected in the original.
   *
   * @return The set of keys
   */
  public Set stringKeySet() {
    Iterator iterator   = super.keySet().iterator();
    HashMap  stringKeys = new HashMap();

    while (iterator.hasNext()) {
      String key = ((CaseBlindString) iterator.next()).toString();

      stringKeys.put(key, get(key));
    }
    return stringKeys.keySet();
  }

  /**
   * Get an Iterator to the String based key set
   * @return An iterator to the key set
   */
  public Iterator stringKeyIterator() {
    return stringKeySet().iterator();
  }

  /*
   * Overridden HashMap methods
   */

  /**
   * Does the map contain this key?
   * @param key The key to look up
   * @return true If the key is present in the map
   */
  public boolean containsKey(String key) {
    return super.containsKey(new CaseBlindString(key));
  }

  /**
   * Fetch a value by name - null keys are not supported
   * @param key The key to look up
   * @return The associated value object
   */
  public Object get(String key) {
     return super.get(new CaseBlindString(key));
  }

  /**
   * Add the key/value pair to the map - null values are not supported
   * @param key The key name
   * @param value The object to store
   */
  public void put(String key, Object value) {
    super.put(new CaseBlindString(key), value);
  }

  /**
   * Remove a key/value pair from this map
   * @param key Non-null key to remove
   */
  public void remove(String key) {
    if (key == null) {
      throw new UnsupportedOperationException("null key");
    }
    super.remove(new CaseBlindString(key));
  }

  /**
   * A crude, case insensitive string - used internally to represent
   * key values.  Preserve the originl case, but compare for equality
   * in a case blind fashion.
   */
  public static class CaseBlindString {

    String string;

    /**
     * Constructors
     */
    private CaseBlindString() {
    }

    public CaseBlindString(String string) {
      this.string = string;
    }

    /**
     * Fetch the original string
     * @return The original string
     */
    public String toString() {
      return string;
    }

    /**
     * Case insensitive compare
     * @return True if the two strings match
     */
    public boolean equals(Object object) {
      if (string == null) {
        return string == null;
      }
      return string.equalsIgnoreCase(((CaseBlindString) object).toString());
    }

    /**
     * Get a hash code for this case insensitive string
     * @return Hash code value
     */
    public int hashCode() {
      if (string == null) {
        return "null".hashCode();
      }
      return string.toUpperCase().hashCode();
    }
  }
}
