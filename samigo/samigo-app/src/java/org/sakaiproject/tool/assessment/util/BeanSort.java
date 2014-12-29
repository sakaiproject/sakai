/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.util;

import java.util.Arrays;
import java.util.Collection;

/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id$
 */
public class BeanSort
{
  private Collection collection;
  private String property;
  //private BeanSortComparator bsc;
  private boolean string = true;
  private boolean numeric = false;
  private boolean date = false;
  private boolean ipAddress = false;

  /**
   * The only public constructor.  Requires a valid property name for a a Java
   * Bean as a sole parameter.
   *
   * @param c the property name for Java Bean to sort by
   * @param onProperty DOCUMENTATION PENDING
   */
  public BeanSort(Collection c, String onProperty)
  {
    collection = c;
    property = onProperty;
  }

  /**
   * Creates a new BeanSort object.
   */
  private BeanSort()
  {
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Object[] arraySort()
  {
    Object[] array = collection.toArray();
    Arrays.sort(array, getBeanSortComparator(property));
    return array;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection sort()
  {
    Object[] array = arraySort();
    collection.clear();
    for(int i = 0; i < array.length; i++)
    {
      collection.add(array[i]);
    }

    return collection;
  }
  
  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection sortDesc()
  {
    Object[] array = arraySort();
    collection.clear();
    for(int i = array.length-1; i >= 0; i--)
    {
      collection.add(array[i]);
    }

    return collection;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param property DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  private BeanSortComparator getBeanSortComparator(String property)
  {
    BeanSortComparator bsc = null;

    if(string)
    {
      bsc = new BeanSortComparator(property);
    }
    else if(numeric)
    {
      if ("timeElapsed".equals(property)) {
    	  bsc = new BeanIntegerComparator(property);
      }
      else {
    	  bsc = new BeanDoubleComparator(property);
      }
    }
    else if(date)
    {
      bsc = new BeanDateComparator(property);
    }
    else if (ipAddress)
    {
       bsc = new BeanIPComparator(property);
    }

    return bsc;
  }

  /**
   * DOCUMENTATION PENDING
   */
  public void toStringSort()
  {
    string = true;
    numeric = false;
    date = false;
    ipAddress = false;
  }

  /**
   * DOCUMENTATION PENDING
   */
  public void toNumericSort()
  {
    string = false;
    numeric = true;
    date = false;
       ipAddress = false;
  }

  /**
   * @todo add date support
   */

    public void toDateSort()
    {
      string = false;
      numeric = false;
      date = true;
      ipAddress = false;
    }
    
    public void toIPAddressSort()
    {
       string = false;
       numeric = false;
       date = false;
       ipAddress = true;
    }
}
