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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id$
 */
@Slf4j
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

    // Possible patterns only accepts Dates in English format (example: Tue Jun 22 00:43:27 CEST 2021)
    // Dates in Spanish (or any other language) format (example: Mar 22 de Jun 00:43:27 CEST 2021) will be not accepted by SimpleDateFormat
    // Setting Locale in English will do the trick to avoid conversion problems
    Locale localeDefault = Locale.getDefault();
    Locale.setDefault(Locale.ENGLISH);

    // we do not want to use null values for sorting
    if(s1 == null) s1="";
    if(s2 == null) s2="";

    // This is one Date string observed: Mon Oct 05 18:48:15 CDT 2020
    // EventLog uses 2021-02-10 13:19:28.0
    List<SimpleDateFormat> possiblePatterns = new ArrayList<>();
    possiblePatterns.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy"));
    possiblePatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
    possiblePatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss'Z'"));
    possiblePatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
    possiblePatterns.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"));
    possiblePatterns.add(new SimpleDateFormat("yyyy-MM-dd HH:mm"));

    Date i1 = null;
    Date i2 = null;

    for (SimpleDateFormat sdf : possiblePatterns) {
      try {
        i1 = sdf.parse(s1);
        i2 = sdf.parse(s2);
        break;
      } catch (ParseException e) {
        // Ignore and log only if all parsers fail
      }
    }

    // Restoring the default environment Locale
    Locale.setDefault(localeDefault);

    if (StringUtils.isNoneBlank(s1, s2) && i1 == null && i2 == null) {
      log.warn("Could not parse date patterns for s1={}, s2={}", s1, s2);
    }

    if (i1 != null && i2 != null) return i1.compareTo(i2);
    if (i1 != null && i2 == null) return 1;
    if (i1 == null && i2 != null) return -1;
    return 0;
  }
}
