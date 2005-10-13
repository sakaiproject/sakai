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

import java.util.*;
import java.text.*;


/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id$
 */
public class BeanDateComparator
  extends BeanSortComparator
{
  private String propertyName;

  /**
   * The only public constructor.  Requires a valid property name for a a Java
   * Bean as a sole parameter.
   *
   * @param propertyName the property name for Java Bean to sort by
   */
  public BeanDateComparator(String propertyName)
  {
    this.propertyName = propertyName;
  }

  /**
   * Creates a new BeanDateComparator object.
   */
  protected BeanDateComparator()
  {
  }
  ;

  /**
   * standard compare method
   *
   * @param o1 object
   * @param o2 object
   *
   * @return lt, eq, gt zero depending on whether o1 numerically lt,eq,gt o2
   *
   * @throws java.lang.UnsupportedOperationException DOCUMENTATION PENDING
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
    DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd hh:ss:mm.SSS");

    Date i1 = null;
    Date i2 = null;

    boolean firstDate = true;

    try
    {
      i1 = dateFormat.parse(s1);
    }
    catch (Exception e)
    {
      firstDate = false;
    }

    try
    {
      i2 = dateFormat.parse(s2);
      if (!firstDate)
        return 1;
    }
    catch(Exception e)
    {
      if (!firstDate)
       return s1.toLowerCase().compareTo(s2.toLowerCase());
    }

    return i1.compareTo(i2);
  }
}
