/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.jsf.model;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.faces.model.*;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * <p>Models Locale specific date chanracteristics.</p>
 * <p>Includes localized slect lists.</p>
 * <p>Copyright: Copyright  Sakai (c) 2005</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class DateModel
  implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 3055823270695170660L;
	private static final Integer[] months;
	private static final Integer[] days;
	private static final Integer[] hours12;
	private static final Integer[] hours24;
	private static final Integer[] minutes;
	private static final Integer[] seconds;
	// workaround for dfs.getAmPmStrings() returning null!
	private static final String[] ampm =
	{
    "AM", "PM"};

  // calls conventiece method to set up
  static
  {
    months = makeArray(12, false);
    days = makeArray(31, false);
    hours12 = makeArray(12, false);
    hours24 = makeArray(24, false);
    minutes = makeArray(60, true);
    seconds = makeArray(60, true);
  }


  private Locale locale;
  private DateFormatSymbols dfs;

  public DateModel()
  {
    locale = Locale.getDefault();
    dfs = new DateFormatSymbols(locale);

  }


  public DateModel(Locale locale)
  {
    this.locale = locale;
    dfs = new DateFormatSymbols(locale);
  }


  /**
   * Array of year Integers starting from now - rangeBefore to now + rangeAfter
   * @param rangeBefore int
   * @param rangeAfter int
   * @return Integer[]
   */

  public Integer[] getYears(int rangeBefore, int rangeAfter)
  {
    
    Calendar cal = Calendar.getInstance(locale);
    int currentYear = cal.get(Calendar.YEAR);
    int startYear = currentYear - rangeBefore;
    int noOfYears = rangeBefore + rangeAfter + 1;

    Integer[] years = new Integer[noOfYears];
    for (int i = startYear, y = 0; i < startYear + noOfYears; i++, y++)
    {
      years[y] = Integer.valueOf(startYear + y);
    }

    return years;
  }


  /**
   * Localized array of month names
   * @return String[]
   */
  public String[] getMonthNames()
  {
    return dfs.getMonths();
  }


  /**
   * Localized array of short month strings
   * @return String[]
   */
  public String[] getMonthShortNames()
  {
    DateFormatSymbols dfs = new DateFormatSymbols(locale);
    return dfs.getShortMonths();
  }


  /**
   * Localized array of day strings
   * @return String[]
   */
  public String[] getDayNames()
  {
    return dfs.getWeekdays();
  }


  /**
   * Localized array of short day strings
   * @return String[]
   */
  public String[] getDayShortNames()
  {
    return dfs.getShortWeekdays();
  }


  /**
   * (Should be) Localized array of {AM, PM}
   * @todo localize
	 *     	Hardcoded workaround for dfs.getAmPmStrings() returning null!
   * @return String[]
   */
  public String[] getAmPm()
  {
//			return dfs.getAmPmStrings();
    return ampm;
  }


  /**
   * Array of month Itegers, starting from 1
   * @return Integer[]
   */
  public Integer[] getMonths()
  {
    return months;
  }


  /**
   * Array of day of month Integers, starting from 1
   * @return Integer[]
   */
  public Integer[] getDays()
  {
    return days;
  }


  /**
   * Array of hour Integers, starting from 1
   * @return Integer[]
   */
  public Integer[] getHours()
  {
    return getHours(true);
  }


  /**
   * Array of hour Integers, starting from 1
   * @param twentyFourHour, if true use 24 hour clock,
   * if false use 12 hour clock
   * @return Integer[]
   */
  public Integer[] getHours(boolean twentyFourHour)
  {
    return twentyFourHour ? hours24 : hours12;
  }


  /**
   * Array of minute Integers, starting from 0
   * @return Integer[]
   */
  public Integer[] getMinutes()
  {
    return minutes;
  }


  /**
   * Array of seconds Integers, starting from 0
   * @return Integer[]
   */
  public Integer[] getSeconds()
  {
    return seconds;
  }


  /**
   * unit test
   * @param args String[]
   */
  public static void main(String[] args)
  {
    DateModel dateModel1 = new DateModel();
    Integer[] ye = dateModel1.getYears(2, 2);
    Integer[] mo = dateModel1.getMonths();
    Integer[] da = dateModel1.getDays();
    Integer[] h12 = dateModel1.getHours(false);
    Integer[] h24 = dateModel1.getHours();
    Integer[] mi = dateModel1.getMinutes();
    Integer[] se = dateModel1.getSeconds();
    String[] ap = dateModel1.getAmPm();
    
    for (int i = 0; i < ye.length; i++)
    {
      log.debug("year: {}", ye[i]);
    }
    for (int i = 0; i < mo.length; i++)
    {
      log.debug("month: {}", mo[i]);
    }
    for (int i = 0; i < da.length; i++)
    {
      log.debug("day: {}", da[i]);
    }
    for (int i = 0; i < h12.length; i++)
    {
      log.debug("hour 12: {}", h12[i]);
    }
    for (int i = 0; i < h24.length; i++)
    {
      log.debug("hour 24: {}", h24[i]);
    }
    for (int i = 0; i < mi.length; i++)
    {
      log.debug("minutes: {}", mi[i]);
    }
    for (int i = 0; i < se.length; i++)
    {
      log.debug("seconds: {}", se[i]);
    }
    for (int i = 0; i < se.length; i++)
    {
      String zs =
        dateModel1.zeroPad(se[i].toString());
      log.debug("zero pad seconds={}", zs);
    }
    for (int i = 0; i < ap.length; i++)
    {
      log.debug("am pm: {}", ap[i]);
    }

    // test select items
    log.debug("testing select items 12hr");
    unitTestSelectItemList(dateModel1.get12HourSelectItems());
    log.debug("testing select items 24hr");
    unitTestSelectItemList(dateModel1.get24HourSelectItems());
    log.debug("testing select items am pm");
    unitTestSelectItemList(dateModel1.getAmPmSelectItems());
    log.debug("testing select items day");
    unitTestSelectItemList(dateModel1.getDaySelectItems());
    log.debug("testing select items minute");
    unitTestSelectItemList(dateModel1.getMinuteSelectItems());
    log.debug("testing select items month");
    unitTestSelectItemList(dateModel1.getMonthSelectItems());
    log.debug("testing select items second");
    unitTestSelectItemList(dateModel1.getSecondsSelectItems());
    log.debug("testing select items year");
    unitTestSelectItemList(dateModel1.getYearSelectItems(2, 2));
  }

  private static void unitTestSelectItemList(List list)
  {
    for (Iterator iter = list.iterator(); iter.hasNext(); ) {
      SelectItem item = (SelectItem)iter.next();
      log.debug("item.getLabel()={}", item.getLabel());
      log.debug("item.getValue()={}", item.getValue());
    }
  }


  /**
   * Select list for range of years
   * @param rangeBefore int
   * @param rangeAfter int
   * @return List of SelectItems
   */
  public List getYearSelectItems(int rangeBefore, int rangeAfter)
  {
    List selectYears = new ArrayList();
    Integer[] years = this.getYears(rangeBefore, rangeAfter);
    for (int i = 0; i < years.length; i++)
    {
      SelectItem selectYear = new SelectItem(years[i], years[i].toString());
      selectYears.add(selectYear);
    }
    return selectYears;
  }


  /**
   * Select list for range of months
   * @return List of SelectItems
   */
  public List getMonthSelectItems()
  {
    List selectMonths = new ArrayList();
    Integer[] m = this.getMonths();
    for (int i = 0; i < m.length; i++)
    {
      SelectItem selectMonth = new SelectItem(m[i], m[i].toString());
      selectMonths.add(selectMonth);
    }
    return selectMonths;
  }


  /**
   * Select list for range of days
   * @return List of SelectItems
   */
  public List getDaySelectItems()
  {
    List selectDays = new ArrayList();
    Integer[] d = this.getDays();
    for (int i = 0; i < d.length; i++)
    {
      SelectItem selectDay = new SelectItem(d[i], d[i].toString());
    }
    return selectDays;
  }


  /**
   * Select list for range of hours on 24 hour clock.
   * @return List of SelectItems
   */
  public List get24HourSelectItems()
  {
    List selectHours = new ArrayList();
    Integer[] h = this.getHours();
    for (int i = 0; i < h.length; i++)
    {
      String hourStr = zeroPad(h[i].toString());
      SelectItem selectHour =
        new SelectItem(h[i], hourStr);
      selectHours.add(selectHour);
    }
    return selectHours;
  }


  /**
   * Select list for range of hours on 12 hour clock.
   * Used in conjunction with AM/PM.
   * @return List of SelectItem
   */
  public List get12HourSelectItems()
  {
    List selectHours = new ArrayList();
    Integer[] h = this.getHours(false);
    for (int i = 0; i < h.length; i++)
    {
      String hourStr = zeroPad(h[i].toString());
      SelectItem selectHour =
        new SelectItem(h[i], hourStr);
      selectHours.add(selectHour);
    }
    return selectHours;
  }


  /**
   * Localized AM/PM select list
   * @return the List of SelectItems
   */
  public List getAmPmSelectItems()
  {
    List ampmList = new ArrayList();
    String[] ampm = getAmPm();
    for (int i = 0; i < ampm.length; i++)
    {
      ampmList.add(new SelectItem(Integer.valueOf(i), ampm[i]));
    }
    return ampmList;
  }


  /**
   * Select list for range of hours on 12 hour clock.
   * Used in conjunction with AM/PM.
   * @return List
   */
  public List getMinuteSelectItems()
  {
    List selectMinutes = new ArrayList();
    Integer[] m = this.getMinutes();
    for (int i = 0; i < m.length; i++)
    {
      String minStr = zeroPad(m[i].toString());
      SelectItem selectHour =
        new SelectItem(m[i], minStr);
      selectMinutes.add(selectHour);
    }
    return selectMinutes;
  }


  /**
   * Select list for range of hours on 12 hour clock.
   * Used in conjunction with AM/PM.
   * @return List
   */
  public List getSecondsSelectItems()
  {
    List selectSeconds = new ArrayList();
    Integer[] s = this.getSeconds();
    for (int i = 0; i < s.length; i++)
    {
      String secStr = zeroPad(s[i].toString());
      SelectItem selectHour =
        new SelectItem(s[i], secStr);
      selectSeconds.add(selectHour);
    }
    return selectSeconds;
  }


  /**
   * utility method
   * @param entries
   * @return
   */
  private static Integer[] makeArray(
    int entries, boolean zeroBase)
  {
    Integer[] mk = new Integer[entries];
    int incr = 1;
    if (zeroBase)
    {
      incr = 0;
    }
    for (int i = 0; i < entries; i++)
    {
      mk[i] = Integer.valueOf(i + incr);
    }
    return mk;
  }


  /**
   * helper method
   * @param str a string
   * @return
   */
  private String zeroPad(String str)
  {
    if (str == null)
    {
      return "00";
    }
    if (str.length() < 2)
    {
      str = "0" + str;
    }
    return str;
  }

}
