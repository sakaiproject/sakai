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
    new String("01"), new String("02"), new String("03"), new String("04"),
    new String("05"), new String("06"), new String("07"), new String("08"),
    new String("09"), new String("10"), new String("11"), new String("12")
  };
  private String[] minArray =
  {
    new String("00"), new String("05"), new String("10"), new String("15"),
    new String("20"), new String("25"), new String("30"), new String("35"),
    new String("40"), new String("45"), new String("50"), new String("55")
  };
  private String[] ampmArray = { new String("AM"), new String("PM") };
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
