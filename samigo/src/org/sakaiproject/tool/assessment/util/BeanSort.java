/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
  private BeanSortComparator bsc;
  private boolean string = true;
  private boolean numeric = false;
  private boolean date = false;

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
    if(string)
    {
      bsc = new BeanSortComparator(property);
    }
    else if(numeric)
    {
      bsc = new BeanFloatComparator(property);
    }
    else if(date)
    {
      bsc = new BeanDateComparator(property);
    }
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
      bsc = new BeanFloatComparator(property);
    }
    else if(date)
    {
      bsc = new BeanDateComparator(property);
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
  }

  /**
   * DOCUMENTATION PENDING
   */
  public void toNumericSort()
  {
    string = false;
    numeric = true;
       date = false;
  }

  /**
   * @todo add date support
   */

    public void toDateSort()
    {
      string = false;
      numeric = false;
      date = true;
    }
}
