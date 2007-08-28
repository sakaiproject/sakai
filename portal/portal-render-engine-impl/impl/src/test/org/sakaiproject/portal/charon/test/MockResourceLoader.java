/*
 * Created on 28 Aug 2007
 */
package org.sakaiproject.portal.charon.test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MockResourceLoader implements Map {
  public void clear()
  {
      throw new UnsupportedOperationException();
  }

  public boolean containsKey(Object key)
  {
      return true;
  }

  public boolean containsValue(Object value)
  {
      throw new UnsupportedOperationException();
  }

  public Set entrySet()
  {
      throw new UnsupportedOperationException();
  }

  public Object get(Object key) {
    return "Message for key " + key;
  }

  public boolean isEmpty()
  {
      throw new UnsupportedOperationException();
  }

  public Set keySet()
  {
      throw new UnsupportedOperationException();
  }

  public Object put(Object arg0, Object arg1)
  {
      throw new UnsupportedOperationException();
  }

  public void putAll(Map arg0)
  {
      throw new UnsupportedOperationException();
  }

  public Object remove(Object key)
  {
      throw new UnsupportedOperationException();
  }

  public int size()
  {
      throw new UnsupportedOperationException();
  }

  public Collection values()
  {
      throw new UnsupportedOperationException();
  }
}
