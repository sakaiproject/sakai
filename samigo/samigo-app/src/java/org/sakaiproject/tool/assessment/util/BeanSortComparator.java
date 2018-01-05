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

import java.io.Serializable;

import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id$
 */
@Slf4j
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
	int result = 0;  
    Map m1 = describeBean(o1);
    Map m2 = describeBean(o2);
    String s1 = (String) m1.get(propertyName);
    String s2 = (String) m2.get(propertyName);

    result = subCompare(s1, s2);
    
    // If students have the same last name, then we need to compare their first name
    if (result == 0 && "lastName".equals(propertyName)) {
    	String firstName1 = (String) m1.get("firstName");
        String firstName2 = (String) m2.get("firstName");
        result = subCompare(firstName1, firstName2);
    }
    // Take out tags
    return result;
  }
  
  private int subCompare(String s1, String s2)
  {
	  //we do not want to use null values for sorting
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


	  String finalS1 = s1.replaceAll("<.*?>", "");
	  String finalS2 = s2.replaceAll("<.*?>", "");
	  RuleBasedCollator collator_ini = (RuleBasedCollator)Collator.getInstance();
	  try {
		RuleBasedCollator collator= new RuleBasedCollator(collator_ini.getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
		return collator.compare(finalS1.toLowerCase(), finalS2.toLowerCase());
	  } catch (ParseException e) {}
	  return Collator.getInstance().compare(finalS1.toLowerCase(), finalS2.toLowerCase());	  
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
    Map m = null;

    try
    {
      m = BeanUtils.describe((Serializable) o);
    }
    catch(Throwable t)
    {
      log.debug("Caught error in BeanUtils.describe(): " + t.getMessage());
      throw new java.lang.UnsupportedOperationException(
        "Invalid describeBean. Objects may not be Java Beans.  " + t);
      
    }

    return m;
  }
}
