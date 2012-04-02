/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
import java.util.List;

/**
 * <p>
 * Title: sakaiproject.org
 * </p>
 *
 * <p>
 * Description: AAM - Date Class handling the Date funcionality
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 *
 * <p>
 * Company: Stanford University
 * </p>
 *
 * @author Durairaju Madhu
 * @author Rachel Gollub
 * @version 1.0
 */
public class DateHandlerWithNull
{
  private String[] dayArray =
  {
    "--", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
    "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26",
    "27", "28", "29", "30", "31"
  };
  private String[] yearArray = { "--", "2003", "2004" };
  private String[] monthArray =
  { "--", "1", "2", "3", "4", "5", "6", "7", "8", "9", " 10", "11", "12" };
  private String[] hourArray =
  {
    "01", "02", "03", "04",
    "05", "06", "07", "08",
    "09", "10", "11", "12"
  };
  private String[] minArray =
  {
    "00", "05", "10", "15",
    "20", "25", "30", "35",
    "40", "45", "50", "55"
  };
  private String[] ampmArray = { "AM", "PM" };
  private List day = Arrays.asList(dayArray);
  private List month = Arrays.asList(monthArray);
  private List year = Arrays.asList(yearArray);
  private List hour = Arrays.asList(hourArray);
  private List min = Arrays.asList(minArray);
  private List ampm = Arrays.asList(ampmArray);
  private LabelValue[] wmonthArray =
  {
    new LabelValue("January", "1"), new LabelValue("February", "2"),
    new LabelValue("March", "3"), new LabelValue("April", "4"),
    new LabelValue("May", "5"), new LabelValue("June", "6"),
    new LabelValue("July", "7"), new LabelValue("August", "8"),
    new LabelValue("September", "9"), new LabelValue("October", "10"),
    new LabelValue("November", "11"), new LabelValue("December", "12")
  };
  private List wmonth = Arrays.asList(wmonthArray);

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getDay()
  {
    return (Collection) day;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getMonth()
  {
    return (Collection) month;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getYear()
  {
    return (Collection) year;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getHour()
  {
    return (Collection) hour;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getMin()
  {
    return (Collection) min;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getAmPm()
  {
    return (Collection) ampm;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public Collection getWmonth()
  {
    return (Collection) wmonth;
  }
}
