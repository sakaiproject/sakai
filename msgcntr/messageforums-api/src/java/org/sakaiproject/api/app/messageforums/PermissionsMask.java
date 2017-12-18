/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/PermissionsMask.java $
 * $Id: PermissionsMask.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.api.app.messageforums;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is critical for the interaction with AuthorizationManager.
 * This class will be used for creating Authorizations and querying 
 * Authorizations. The implementation of this class is not thread safe.
 * 
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id: PermissionsMask.java 632 2005-07-14 21:22:50 +0000 (Thu, 14 Jul 2005) janderse@umich.edu $
 */
@Slf4j
public class PermissionsMask implements Map
{

  private Map map;

  /**
   * @see HashMap#HashMap()
   */
  public PermissionsMask()
  {
    map = new HashMap();
  }

  /**
   * @see HashMap#HashMap(int)
   */
  public PermissionsMask(int initialCapacity)
  {
    map = new HashMap(initialCapacity);
  }

  /**
   * @see HashMap#HashMap(int, float)
   */
  public PermissionsMask(int initialCapacity, float loadFactor)
  {
    map = new HashMap(initialCapacity, loadFactor);
  }

  /**
   * @see java.util.Map#clear()
   */
  public void clear()
  {
    map.clear();
  }

  /**
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object key)
  {
    return map.containsKey(key);
  }

  /**
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue(Object value)
  {
    return map.containsValue(value);
  }

  /**
   * @see java.util.Map#entrySet()
   */
  public Set entrySet()
  {
    return map.entrySet();
  }

  /**
   * @see java.util.Map#equals(java.lang.Object)
   */
  public boolean equals(Object o)
  {
    return map.equals(o);
  }

  /**
   * @see java.util.Map#get(java.lang.Object)
   */
  public Object get(Object key)
  {
    return map.get(key);
  }

  /**
   * @see java.util.Map#hashCode()
   */
  public int hashCode()
  {
    return map.hashCode();
  }

  /**
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty()
  {
    return map.isEmpty();
  }

  /**
   * @see java.util.Map#keySet()
   */
  public Set keySet()
  {
    return map.keySet();
  }

  /**
   * @param key Must be of type String.
   * @param value Must be of type Boolean or null.
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  public Object put(Object key, Object value)
  {
    if (log.isDebugEnabled())
    {
      log.debug("put(Object " + key + ", Object " + value + ")");
    }
    if (key == null || !(key instanceof String))
      throw new IllegalArgumentException("Illegal key argument passed!");
    if (value != null && !(value instanceof Boolean))
      throw new IllegalArgumentException("Illegal value argument passed!");

    return map.put(key, value);
  }

  /**
   * @throws IllegalArgumentException if the specified map is null or any of the
   * keys are not Strings.
   * @see java.util.Map#putAll(java.util.Map)
   */
  public void putAll(Map t)
  {
    if (log.isDebugEnabled())
    {
      log.debug("putAll(Map " + t + ")");
    }
    if (t == null)
      throw new IllegalArgumentException("Illegal map argument passed!");
    for (Iterator iter = t.entrySet().iterator(); iter.hasNext();)
    {
      Map.Entry entry = (Entry) iter.next();
      if (!(entry.getKey() instanceof String))
        throw new IllegalArgumentException(
            "Illegal key found in Map, must be a String!");
      if (entry.getValue() != null && !(entry.getValue() instanceof Boolean))
        throw new IllegalArgumentException(
            "Illegal value found in Map; must be a Boolean or null!");
    }

    map.putAll(t);
  }

  /**
   * @see java.util.Map#remove(java.lang.Object)
   */
  public Object remove(Object key)
  {
    return map.remove(key);
  }

  /**
   * @see java.util.Map#size()
   */
  public int size()
  {
    return map.size();
  }

  /**
   * @see java.util.Map#values()
   */
  public Collection values()
  {
    return map.values();
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return map.toString();
  }

}
