/**********************************************************************************
* $URL$
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

import java.io.Serializable;

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id$
 */
public class BeanSortComparator
  implements Comparator
{
  private String propertyName;

  /**
   * The only public constructor.  Requires a valid property name for a a Java
   * Bean as a sole parameter.
   *
   * @param propertyName the property name for Java Bean to sort by
   */
  public BeanSortComparator(String propertyName)
  {
    this.propertyName = propertyName;
  }

  /**
   * Creates a new BeanSortComparator object.
   */
  protected BeanSortComparator()
  {
  }
  ;

  /**
   * standard compare method
   *
   * @param o1 object
   * @param o2 object
   *
   * @return lt, eq, gt zero depending on whether o1 lt, eq, gt o2
   */
  public int compare(Object o1, Object o2)
  {
    Map m1 = describeBean(o1);
    Map m2 = describeBean(o2);
    String s1 = (String) m1.get(propertyName);
    String s2 = (String) m2.get(propertyName);

    // we do not want to use null values for sorting
    if(s1 == null)
    {
      s1 = "";
    }

    if(s2 == null)
    {
      s2 = "";
    }

    // Deal with n/a case
    if (s1.toLowerCase().startsWith("n/a")
        && !s2.toLowerCase().startsWith("n/a"))
      return 1;

    if (s2.toLowerCase().startsWith("n/a") &&
        !s1.toLowerCase().startsWith("n/a"))
      return -1;

    // Take out tags
    return s1.replaceAll("<.*?>", "").toLowerCase().compareTo
      (s2.replaceAll("<.*?>", "").toLowerCase());
  }

  /**
   * protected utility method to wrap BeanUtils
   *
   * @param o DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   *
   * @throws java.lang.UnsupportedOperationException DOCUMENTATION PENDING
   */
  protected Map describeBean(Object o)
  {
    Map m;

    try
    {
      m = BeanUtils.describe((Serializable) o);
    }
    catch(Throwable t)
    {
      throw new java.lang.UnsupportedOperationException(
        "Invalid describeBean. Objects may not be Java Beans.  " + t);
    }

    return m;
  }
}
