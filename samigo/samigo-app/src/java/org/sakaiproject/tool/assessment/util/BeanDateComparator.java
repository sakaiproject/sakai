/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
    if(s1 == null) s1="";
    if(s2 == null) s2="";

    DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    Date i1 = null;
    Date i2 = null;

    boolean firstDateValid = true;
    boolean secondDateValid = true;

    try
    {
      i1 = dateFormat.parse(s1);
    }
    catch (ParseException e)
    {
      firstDateValid = false;
    }

    try
    {
      i2 = dateFormat.parse(s2);
    }
    catch (ParseException e)
    {
      secondDateValid = false;
    }

    int returnValue=0;
   	if (firstDateValid && secondDateValid) {
   		if (i1 != null) {
   			returnValue = i1.compareTo(i2);
   		}
    }
    if (firstDateValid && !secondDateValid) returnValue = 1;
    if (!firstDateValid && secondDateValid) returnValue = -1;
    if (!firstDateValid && !secondDateValid) returnValue = 0;

    return returnValue;
  }
}
