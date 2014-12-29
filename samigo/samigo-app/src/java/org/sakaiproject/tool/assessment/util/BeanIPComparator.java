/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/util/BeanDoubleComparator.java $
 * $Id: BeanDoubleComparator.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.util;

import java.util.Map;

/**
 * This is a Comparator class so that IP addresses can be sorted properly.
 * @author chrismaurer
 *
 */
public class BeanIPComparator
  extends BeanSortComparator
{
  private String propertyName;

  /**
   * The only public constructor.  Requires a valid property name for a a Java
   * Bean as a sole parameter.
   *
   * @param propertyName the property name for Java Bean to sort by
   */
  public BeanIPComparator(String propertyName)
  {
    this.propertyName = propertyName;
  }

  /**
   * Creates a new BeanDoubleComparator object.
   */
  protected BeanIPComparator()
  {
  }

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
    Long i1 = ipToInt(s1);
    Long i2 = ipToInt(s2);
    return i1.compareTo(i2);
  }
  
  private Long ipToInt(String addr) {
     long num = 0;
     if (addr != null) {
        String[] addrArray = addr.split("\\.");


        for (int i=0;i<addrArray.length;i++) {
           int power = 3-i;

           num += ((Integer.parseInt(addrArray[i])%256 * Math.pow(256,power)));
        }
     }
     return num;
 }

}
