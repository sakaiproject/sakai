/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.component.app.help;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A List that won't grow any larger that the specified size.
 * 
 * @version $Id$
 */
public class SizedList<T> extends ArrayList<T>
{
  private static final long serialVersionUID = 1L;
  private int size = -1;

  /**
   * constructor
   */
  public SizedList()
  {
    super();
  }

  /**
   * overloaded constructor
   * @param size
   */
  public SizedList(int size)
  {
    super();
    this.size = size;
  }

  /**
   * Add an item to the list, if the list is already full then and item is also removed from the
   * end of the list. 
   * @see java.util.Collection#add(java.lang.Object)
   */
  public boolean add(T item)
  {
    if (this.contains(item))
    {
      this.remove(item);
    }
    super.add(0, item);
    if (this.size() > size)
    {
      this.remove(this.size() - 1);
    }
    return true;
  }

  /** 
   * @see java.util.Collection#addAll(java.util.Collection)
   */
  public boolean addAll(Collection<? extends T> c)
  {
    for (T name : c) {
      add(name);
    }
    return true;
  }

}


