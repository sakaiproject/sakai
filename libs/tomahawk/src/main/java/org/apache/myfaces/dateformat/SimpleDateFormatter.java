/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.dateformat;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * A reimplementation of the java.text.SimpleDateFormat class.
 * <p>
 * This class has been created for use with the tomahawk InputCalendar
 * component. It exists for the following reasons:
 * <ul>
 * <li>The java.text.SimpleDateFormat class is simply broken with respect
 * to "week of year" functionality.
 * <li>The inputCalendar needs a javascript equivalent of SimpleDateFormat
 * in order to process data in the popup calendar. But it is hard to
 * unit-test javascript code. By maintaining a version in Java that is
 * unit-tested, then making the javascript version a direct "port" of that
 * code the javascript gets improved reliability.
 * <li>Documentation is necessary for this code, but it is not desirable to
 * add lots of docs to a javascript file that is downloaded. The javascript
 * version can simply reference the documentation here.
 * </ul>
 * Note that the JODA project also provides a SimpleDateFormat implementation,
 * but that does not support firstDayOfWeek functionality. In any case,
 * it is not desirable to add a dependency from tomahawk on JODA just for
 * the InputCalendar.
 * <p>
 * This implementation does extend the SimpleDateFormat class by adding the
 * JODA "xxxx" yearOfWeekYear format option, as this is missing in the
 * standard SimpleDateFormat class.
 * <p>
 * The parse methods also return null on error rather than throw an exception.
 * <p>
 * The code here was originally written in javascript (date.js), and has been
 * ported to java.
 * <p>
 * At the current time, the following format options are NOT supported:
 * <code>DFkKSzZ</code>.
 * <p>
 * <h2>Week Based Calendars</h2>
 * <p>
 * ISO standard ISO-8601 defines a calendaring system based not upon
 * year/month/day_in_month but instead year/week/day_in_week. This is
 * particularly popular in embedded systems as date arithmetic is
 * much simpler; there are no irregular month lengths to handle.
 * <p>
 * The only tricky part is mapping to and from year/month/day formats.
 * Unfortunately, while java.text.SimpleDateFormat does support a "ww"
 * week format character, it has a number of flaws.
 * <p>
 * Weeks are always complete and discrete, ie week yyyy-ww always has
 * 7 days in it, and never "shares" days with yyyy-(ww+1). However to
 * achieve this, the last week of a year might include a few days of
 * the next year, or the last few days of a year might be counted as
 * part of the first week of the following year. The decision is made
 * depending on which year the "majority" of days in that week belong to.
 * <p>
 * With ISO-8601, a week always starts on a monday. However many countries
 * use a different convention, starting weeks on saturday, sunday or monday.
 * This class supports setting the firstDayOfWeek.
 * 
 * @since 1.1.7
 * @author Simon Kitching (latest modification by $Author: grantsmith $)
 * @version $Revision: 472638 $ $Date: 2006-11-08 15:54:13 -0500 (Wed, 08 Nov 2006) $
 */
public class SimpleDateFormatter
{
    private static final long MSECS_PER_SEC = 1000;
    private static final long MSECS_PER_MIN = 60 * MSECS_PER_SEC;
    private static final long MSECS_PER_HOUR = 60 * MSECS_PER_MIN;
    private static final long MSECS_PER_DAY = 24 * MSECS_PER_HOUR;
    private static final long MSECS_PER_WEEK = 7 * MSECS_PER_DAY;

    // ======================================================================
    // Static Week-handling Methods
    // ======================================================================

    /**
     * Cumulative sum of the number of days in the year up to the first
     * day of each month.
     */
    private static final int[] MONTH_LEN =
    {
        0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334
    };

    /**
     * Return the ISO week# represented by the specified date (1..53).
     *
     * This implements the ISO-8601 standard for week numbering, as documented in
     * Klaus Tondering's Calendar document, version 2.8:
     *   http://www.tondering.dk/claus/calendar.html
     *
     * For dates in January and February, calculate:
     *
     *   a = year-1
     *   b = a/4 - a/100 + a/400
     *   c = (a-1)/4 - (a-1)/100 + (a-1)/400
     *   s = b-c
     *   e = 0
     *   f = day - 1 + 31*(month-1)
     *
     * For dates in March through December, calculate:
     *
     *    a = year
     *    b = a/4 - a/100 + a/400
     *    c = (a-1)/4 - (a-1)/100 + (a-1)/400
     *    s = b-c
     *    e = s+1
     *    f = day + (153*(month-3)+2)/5 + 58 + s
     *
     * Then, for any month continue thus:
     *
     *    g = (a + b) mod 7
     *    d = (f + g - e) mod 7
     *    n = f + 3 - d
     *
     * We now have three situations:
     *
     *    If n<0, the day lies in week 53-(g-s)/5 of the previous year.
     *    If n>364+s, the day lies in week 1 of the coming year.
     *    Otherwise, the day lies in week n/7 + 1 of the current year.
     *
     * This algorithm gives you a couple of additional useful values:
     *
     *    d indicates the day of the week (0=Monday, 1=Tuesday, etc.)
     *    f+1 is the ordinal number of the date within the current year.
     *
     * Note that ISO-8601 specifies that week1 of a year is the first week in
     * which the majority of days lie in that year. An equivalent description
     * is that it is the first week including the 4th of january. This means
     * that the 1st, 2nd and 3rd of January might lie in the last week of the
     * previous year, and that the last week of a year may include the first
     * few days of the following year.
     *
     * ISO-8601 also specifies that the first day of the week is always Monday.
     *
     * This function returns the week number regardless of which year it lies in.
     * That means that asking for the week# of 01/01/yyyy might return 52 or 53,
     * and asking for the week# of 31/12/yyyy might return 1.
     */
    public static WeekDate getIsoWeekDate(Date date)
    {
        int year = fullYearFromDate(date.getYear());
        int month = date.getMonth() + 1;
        int day = date.getDate();

        int a,b,c,d,e,f,g,s,n;

        if (month <= 2)
        {
            a = year - 1;
            b = (int) Math.floor(a/4) - (int) Math.floor(a/100) + (int) Math.floor(a/400);
            c = (int) Math.floor((a-1)/4) - (int) Math.floor((a-1)/100) + (int) Math.floor((a-1)/400);
            s = b - c;
            e = 0;
            f = day - 1 + 31*(month-1);
        }
        else
        {
            a = year;
            b = (int) Math.floor(a/4) - (int) Math.floor(a/100) + (int) Math.floor(a/400);
            c = (int) Math.floor((a-1)/4) - (int) Math.floor((a-1)/100) + (int) Math.floor((a-1)/400);
            s = b - c;
            e = s + 1;
            f = day + (int) Math.floor((153*(month-3) + 2)/5) + 58 + s;
        }

        g = (a + b) % 7;
        d = (f + g - e) % 7;
        n = f + 3 - d;

        if (n<0)
        {
            // previous year
            int resultWeek = 53 - (int) Math.floor((g-s)/5);
            return new WeekDate(year-1, resultWeek);
        }
        else if (n > (364+s))
        {
            // next year
            int resultWeek = 1;
            return new WeekDate(year+1, resultWeek);
        }
        else
        {
            // current year
            int resultWeek = (int) Math.floor(n/7) + 1;
            return new WeekDate(year, resultWeek);
        }
    }

    /** Return true if the specified year is a leapyear (has 29 days in feb). */
    private static boolean isLeapYear(int year)
    {
        return ((year%4 == 0) && (year%100 != 0)) || (year%400 == 0);
    }

    /**
     * Compute which day of the week (sun,mon, etc) a particular date
     * falls on.
     * <p>
     * Returns 0 for sunday, 1 for monday, 6 for saturday (the java.util.Date
     * and the javascript Date convention):
     * <p>
     * Note that java.util.Calendar uses 1=sun, 7=sat.
     * <p>
     * This algorithm is documented as part of the RFC3339 specification.
     * 
     * @param year is full year value (eg 2007).
     * @param month is 1..12
     * @param day is 1..31
     */
    private static int dayOfWeek(int year, int month, int day)
    {
       /* adjust months so February is the last one */
       month -= 2;
       if (month < 1)
       {
          month += 12;
          --year;
       }

       /* split by century */
       int cent = year / 100;
       year %= 100;

       // dow (0=sunday)
       int base =
            (26 * month - 2) / 10
            + day
            + year
               + (year / 4)
               + (cent / 4)
               + (5 * cent);

       int dow = base % 7;

       return dow;
    }

    /**
     * Return the (year, week) representation of the given date.
     * <p>
     * This is exactly like getIsoWeekNumber, except that a firstDayOfWeek
     * can be specified; ISO-8601 hard-wires "monday" as first day of week.
     * <p>
     * TODO: support minimumDaysInWeek property. Currently, assumes
     * this is set to 4 (the ISO standard).
     * <p>
     * @param firstDayOfWeek is: 0=sunday, 1=monday, 6=sat. This is the
     * convention used by java.util.Date. NOTE: java.util.Calendar uses
     * 1=sunday, 2=monday, 7=saturday.
     */
    public static WeekDate getWeekDate(Date date, int firstDayOfWeek)
    {
        int year = fullYearFromDate(date.getYear());
        int month = date.getMonth() + 1;
        int day = date.getDate();

        boolean thisIsLeapYear = isLeapYear(year);

        int dayOfYear = day + MONTH_LEN[month-1];
        if (thisIsLeapYear && (month>2))
        {
            ++dayOfYear;
        }

        int jan1Weekday = dayOfWeek(year, 1, 1);

        // The first week of a year always starts on firstDayOfWeek. However that
        // week starts up to 3 days before the 1st of the year, or 3 days after.
        //
        // Here, we find where the first week actually starts, measured as an
        // offset from the first day of the year (-3..+3).
        //
        // Examples:
        // * if firstDayOfWeek=mon, and 1st jan is wed, then pivotOffset=-2,
        //   ie 30 dec of previous year is where the first week starts.
        // * if firstDayOfWeek=sun and 1st jan is fri, then pivotOffset=2,
        //   ie 3 jan is where the first week starts.
        int pivotOffset = firstDayOfWeek - jan1Weekday;
        if (pivotOffset > 3)
        {
            pivotOffset -= 7;
        }
        else if (pivotOffset < -3)
        {
            pivotOffset += 7;
        }

        // Compute the offset of date relative to the start of this year.
        // This will be in range 0..364 (or 365 for leap year)
        int dayOffset = dayOfYear-1;
        if (dayOffset < pivotOffset)
        {
            // This date falls in either week52 or week53 of the previous year
            //
            // Because (365%7)=1, the pivotOffset moves forweards by one if the previous
            // year is a normal one, or two if the previous year is a leapyear (wrapping
            // around from +3 to -3). And a year has 53 weeks only when its pivotOffset
            // is -3 (or -2 for leapyear).
            //
            // so:
            //  when prev is not leapyear, has53 when pivotOffset is 3 for this year.
            //  when prev is leapyear, has53 when pivotOffset is 2 or 3 for this year.
            boolean prevIsLeapYear = isLeapYear(year-1);
            if ((pivotOffset==3) || ((pivotOffset==2) && prevIsLeapYear))
            {
                return new WeekDate(year-1, 53);
            }
            return new WeekDate(year-1, 52);
        }

        // Compute the number of days relative to the start of the first
        // week in this year, then divide by seven to get the week count.
        int daysFromFirstWeekStart = (dayOfYear - 1 - pivotOffset);
        int weekNumber = daysFromFirstWeekStart/7 + 1;

        // In a normal year, there are 52 weeks with 1 day (365%7) left over.
        //
        // So, when weeks start on the first day of a year, there is one day left
        // at the end, which will fall into the first week of the next year. When
        // weeks start on the 2nd, then week 52 ends on 31 dec. When weeks start on
        // the max pivotOffset of +3, then week52 includes 3jan of next year. It is
        // still week52 because only 3 days are from the next year adn 4 are in the
        // current year.
        //
        // But when pivotOffset is -3, then there are 4 days left over at the end of
        // the year - making week 53. And in a leap year, pivotOffset=-2 is sufficient
        // to create a week53.
        if ((weekNumber < 53)
            || (pivotOffset==-3)
            || (pivotOffset==-2 && thisIsLeapYear))
        {
            return new WeekDate(year, weekNumber);
        }
        else
        {
            // weekNumber=53, but this year only has 52 weeks so this must be week
            // one of the next year.
            return new WeekDate(year+1, 1);
        }
    }

    /**
     * Return the point in time at which the first week of the specified year starts.
     */
    private static long getStartOfWeekYear(int year, int firstDayOfWeek)
    {
        // Create a new date on the 1st.
        Date d1 = new Date(shortYearFromDate(year), 0, 1, 0, 0, 0);

        // adjust forward or backwards to the nearest firstDayOfWeek
        int firstDayOfYear = d1.getDay(); // 0 = sunday
        int dayDiff = firstDayOfWeek - firstDayOfYear;
        int dayShift;
        if (dayDiff >= 4)
        {
            dayShift = 7-dayDiff;
        }
        else if (dayDiff >= 0)
        {
            dayShift = dayDiff;
        }
        else if (dayDiff >= -3)
        {
            dayShift = dayDiff;
        } else
        {
            dayShift = 7 + dayDiff;
        }

        // now compute the number of weeks between start of weekYear and input date.
        long weekYearStartMsecs = d1.getTime() + (dayShift* MSECS_PER_DAY);
        return weekYearStartMsecs;
    }

    /**
     * This is the inverse of method getJavaWeekNumber.
     */
    private static Date getDateForWeekDate
    (
            int year, int week, int day,
            int hour, int min, int sec,
            int firstDayOfWeek)
    {
        long msecsBase = getStartOfWeekYear(year, firstDayOfWeek);

        long msecsOffset = (week - 1) * MSECS_PER_WEEK;
        msecsOffset += (day-1) * MSECS_PER_DAY;
        msecsOffset += hour * MSECS_PER_HOUR;
        msecsOffset += min * MSECS_PER_MIN;
        msecsOffset += sec * MSECS_PER_SEC;

        Date finalDate = new Date();
        finalDate.setTime(msecsBase + msecsOffset);
        return finalDate;
    }

    // ======================================================================
    // Static Generic Date Manipulation Methods
    // ======================================================================

    private static int fullYearFromDate(int year)
    {
        if (year < 1900)
        {
            return year + 1900;
        }
        else
        {
            return year;
        }
    }

    private static int shortYearFromDate(int year)
    {
        if (year > 1900)
        {
            return year - 1900;
        }
        else
        {
            return year;
        }
    }

    private static Date createDateFromContext(ParserContext context)
    {
        Date date;
        if (context.weekOfWeekYear != 0)
        {
            date = getDateForWeekDate(
                    context.weekYear, context.weekOfWeekYear, context.day,
                    context.hour, context.min, context.sec,
                    context.firstDayOfWeek);
        }
        else
        {
            // Class java.util.Date expects year to be relative to 1900. Note that
            // this is different for javascript Date class - that takes a year
            // relative to 0AD.
            date = new Date(
                    context.year - 1900, context.month, context.day,
                    context.hour, context.min, context.sec);
        }
        return date;
    }

    /**
     * Return a substring starting from a specific location, and extending
     * len characters.
     * <p>
     * It is an error if s is null.
     * It is an error if s.length <= start.
     * <p>
     * It is NOT an error if s.length < start+len; in this case a string
     * starting at "start" but less than len characters will be returned.
     */
    private static String substr(String s, int start, int len)
    {
        String s2 = s.substring(start);
        if (s2.length() <= len)
            return s2;
        else
            return s2.substring(0, len);
    }

    // ======================================================================
    // Static Parsing Methods
    // ======================================================================

    /**
     * Parse a string according to the provided sequence of parsing ops.
     * <p>
     * Returns a ParserContext object that has its year/month/day etc fields
     * set according to data extracted from the string.
     * <p>
     * If an error has occured during parsing, context.invalid will be true.
     */
    private static ParserContext parseOps(
            DateFormatSymbols symbols, boolean yearIsWeekYear,
            int firstDayOfWeek,
            String[] ops, String dateStr)
    {

        ParserContext context = new ParserContext(firstDayOfWeek);

        int dateIndex = 0;
        int dateStrLen = dateStr.length();
        for(int i=0; (i<ops.length) && (dateIndex < dateStrLen); ++i)
        {
            String op = ops[i];
            String optype = op.substring(0, 2);
            String opval = op.substring(2);

            if (optype.equals("f:"))
            {
                parsePattern(symbols, yearIsWeekYear, context, opval, dateStr, dateIndex);

                if ((context.newIndex < 0) || context.invalid)
                    break;

                dateIndex = context.newIndex;
            }
            else if (optype.equals("q:") || optype.equals("l:"))
            {
                // verify that opval matches the next chars in dateStr
                int oplen = opval.length();
                String s = substr(dateStr, dateIndex, oplen);
                if (!opval.equals(s))
                {
                    context.invalid = true;
                    break;
                }
                dateIndex += oplen;
            }
        }
        
        if (dateIndex < dateStrLen)
        {
            // TOMAHAWK-1390
            //Remaining chars are on the string. All chars should be processed, otherwise
            //the dateStr is invalid
            context.invalid = true;
        }

        return context;
    }

    /**
     * Handle parsing of a single property, eg "yyyy" or "EEE".
     */
    private static void parsePattern(DateFormatSymbols symbols, boolean yearIsWeekYear, 
            ParserContext context, String patternSub,
            String dateStr, int dateIndex)
    {

        char c = patternSub.charAt(0);
        int patlen = patternSub.length();
        context.newIndex = dateIndex;

        if (c == 'y')
        {
            int year = parseNum(context, dateStr, 4, dateIndex);
            if ((context.newIndex-dateIndex) < 4)
            {
                // see method adjustTwoDigitYear
                context.year = year;
                context.ambiguousYear = true;
            }
            else
            {
                context.year = year;
            }

            if (yearIsWeekYear)
            {
                // There is a "ww" pattern present, so set weekYear as well as year.
                context.weekYear = context.year;
                context.ambiguousWeekYear = context.ambiguousYear;
            }
        }
        else if (c == 'x')
        {
            // extension to standard java.text.SimpleDateFormat class, to support the
            // JODA "weekYear" formatter.
            int year = parseNum(context, dateStr, 4, dateIndex);

            if ((context.newIndex-dateIndex) < 4)
            {
                context.weekYear = year;
                context.ambiguousWeekYear = true;
            }
            else
            {
                context.weekYear = year;
            }
        }
        else if (c == 'M')
        {
            if (patlen == 3)
            {
                String fragment = substr(dateStr, dateIndex, 3);
                int index = parseIndexOf(context, symbols.shortMonths, fragment);
                if (index != -1)
                {
                    context.month = index;
                }
            }
            else if (patlen >= 4)
            {
                String fragment = dateStr.substring(dateIndex);
                int index = parsePrefixOf(context, symbols.months, fragment);
                if (index != -1)
                {
                    context.month = index;
                }
            }
            else
            {
                context.month = parseNum(context, dateStr, 2, dateIndex) - 1;
            }
        }
        else if (c == 'd')
        {
            context.day = parseNum(context, dateStr, 2, dateIndex);
        }
        else if (c == 'E')
        {
            if (patlen <= 3)
            {
                String fragment = dateStr.substring(dateIndex, dateIndex+3);
                int index = parseIndexOf(context, symbols.shortWeekdays, fragment);
                if (index != -1)
                {
                    context.dayOfWeek = index;
                }
            }
            else
            {
                String fragment = dateStr.substring(dateIndex);
                int index = parsePrefixOf(context, symbols.weekdays, fragment);
                if (index != -1)
                {
                    context.dayOfWeek = index;
                }
            }
        }
        else if (c == 'H')
        {
            // H is in range 0..23
            context.hour = parseNum(context, dateStr, 2, dateIndex);
        }
        else if (c == 'h')
        {
            // h is in range 1am..12pm or 1pm-12am.
            // Note that this field is later post-adjusted
            context.hourAmpm = parseNum(context, dateStr, 2, dateIndex);
        }
        else if (c == 'm')
        {
            context.min = parseNum(context, dateStr, 2, dateIndex);
        }
        else if (c == 's')
        {
            context.sec = parseNum(context, dateStr, 2, dateIndex);
        }
        else if (c == 'a')
        {
            context.ampm = parseString(context, dateStr, dateIndex, symbols.ampms);
        }
        else if (c == 'w')
        {
            context.weekOfWeekYear = parseNum(context, dateStr, 2, dateIndex);
        }
        else
        {
            context.invalid = true;
        }
    }

    /**
     * Convert a string of digits (in base 10) to an integer.
     * <p>
     * Only positive values are accepted. Returns -1 on failure.
     */
    private static int parseInt(String value)
    {
        int sum = 0;
        for(int i=0; i< value.length(); ++i)
        {
            char c = value.charAt(i);

            if ((c<'0') || (c>'9'))
            {
                return -1;
            }
            sum = sum*10 + (c-'0');
        }
        return sum;
    }

    /**
     * Convert at most the next nChars characters to numeric, starting from offset dateIndex
     * within dateStr.
     * <p>
     * Updates context.newIndex to contain the offset of the next unparsed char.
     */
    private static int parseNum(ParserContext context, String dateStr, int nChars, int dateIndex)
    {
        // Try to convert the most possible characters (nChars). If that fails,
        // then try again without the last character. Repeat until successful
        // numeric conversion occurs.
        int nToParse = Math.min(nChars, dateStr.length() - dateIndex);
        for(int i=nToParse;i>0;i--)
        {
            String numStr = dateStr.substring(dateIndex,dateIndex+i);
            int value = parseInt(numStr);

            if(value == -1)
                continue;

            context.newIndex = dateIndex+i;
            return value;
        }

        context.newIndex = -1;
        context.invalid = true;
        return -1;
    }

    /**
     * Return the index of the array element which matches the provided string.
     * <p>
     * This is used when the next thing in value (string being parsed) is expected
     * to be one of the values in the provided array, AND all the array entries
     * are of the same length. The appropriate sequence of chars can then be
     * extracted from the string to parse, and passed here as the exact value
     * to be matched.
     */
    private static int parseIndexOf(ParserContext context, String[] array, String value)
    {
        for(int i=0; i<array.length; ++i)
        {
            String s = array[i];
            if (value.equals(s))
            {
                context.newIndex += s.length();
                return i;
            }
        }
        context.invalid = true;
        context.newIndex = -1;
        return -1;
    }

    /**
     * Return the index of the array element which is a prefix of the value string.
     * <p>
     * This is used when the next thing in value (string being parsed) is expected
     * to be one of the values in the provided array.
     * <p>
     * This is like indexOf, except that an exact match is not expected.
     */
    private static int parsePrefixOf(ParserContext context, String[] array, String value)
    {
        for(int i=0; i<array.length; ++i)
        {
            String s = array[i];
            if (value.startsWith(s))
            {
                context.newIndex += s.length();
                return i;
            }
        }
        context.invalid = true;
        context.newIndex = -1;
        return -1;
    }

    /**
     * This is used when parsing is currently at location dateIndex within the date string,
     * and one of the values in the strings array is now expected.
     * <p>
     * Returns an index into the strings array, or -1 if none match.
     * <p>
     * Also updates context.newIndex to be the location after the matched string (if any).
     * On failure, the context.invalid flag is set before returning -1.
     */
    private static int parseString(ParserContext context, String dateStr, int dateIndex, String[] strings)
    {
        String fragment = dateStr.substring(dateIndex);
        return parsePrefixOf(context, strings, fragment);
    }

    /**
     * Handle fields that need to be processed after all information is available.
     */
    private static void parsePostProcess(DateFormatSymbols symbols, ParserContext context)
    {
        if (context.ambiguousYear)
        {
            // TODO: maybe this adjustment could be made while parsing?

            context.year += 1900;
            Date date = createDateFromContext(context);
            Date threshold = symbols.twoDigitYearStart;
            if (date.getTime() < threshold.getTime())
            {
                context.year += 100;
            }
        }

        if (context.hourAmpm > 0)
        {
            // yes, the user has set the hour using 12-hour clock
            // 01am->01, 11am->11, 12pm->12, 1pm->13, 11pm->23, 12pm->00
            if (context.ampm == 1)
            {
                context.hour = context.hourAmpm + 12;
                if (context.hour == 24)
                    context.hour = 0;
            }
            else
            {
                context.hour = context.hourAmpm;
            }
        }
    }

    // ======================================================================
    // Static Formatting Methods
    // ======================================================================

    private static String formatOps(
            DateFormatSymbols symbols, boolean yearIsWeekYear,
            int firstDayOfWeek,
            String[] ops, Date date)
    {
        ParserContext context = new ParserContext(firstDayOfWeek);

        context.year = fullYearFromDate(date.getYear());
        context.month = date.getMonth();
        context.day = date.getDate();
        context.dayOfWeek = date.getDay();
        context.hour = date.getHours();
        context.min = date.getMinutes();
        context.sec = date.getSeconds();

        // 00 --> 12am, 01->1am, 12 --> 12pm, 13 -> 1pm, 23->11pm
        context.ampm = (context.hour < 12) ? 0 : 1;

        WeekDate weekDate = getWeekDate(date, firstDayOfWeek);
        context.weekYear = weekDate.getYear();
        context.weekOfWeekYear = weekDate.getWeek();

        StringBuffer str = new StringBuffer();
        for(int i=0; i<ops.length; ++i)
        {
            String op = ops[i];
            String optype = op.substring(0, 2);
            String opval = op.substring(2);

            if (optype.equals("f:"))
            {
                formatPattern(symbols, context, opval, yearIsWeekYear, str);
                if (context.invalid)
                    break;
            }
            else if (optype.equals("l:"))
            {
                // Just copy the literal sequence
                str.append(opval);
            }
            else if (optype.equals("q:"))
            {
                // Just copy the literal sequence
                str.append(opval);
            }
        }

        if (context.invalid)
            return null;
        else
            return str.toString();
    }

    private static void formatPattern(DateFormatSymbols symbols, ParserContext context, 
            String patternSub, boolean yearIsWeekYear, StringBuffer out)
    {
        char c = patternSub.charAt(0);
        int patlen = patternSub.length();

        if (c == 'y')
        {
            if (!yearIsWeekYear)
                formatNum(context.year, patlen <= 3 ? 2 : 4, true, out);
            else
                formatNum(context.weekYear, patlen <= 3 ? 2 : 4, true, out);
        }
        else if (c == 'x')
        {
            formatNum(context.weekYear, patlen <= 3 ? 2 : 4, true, out);
        }
        else if (c == 'M')
        {
            if (patlen == 3)
            {
                out.append(symbols.shortMonths[context.month]);
            }
            else if (patlen >= 4)
            {
                out.append(symbols.months[context.month]);
            }
            else
            {
                formatNum(context.month+1, patlen, false, out);
            }
        }
        else if (c == 'd')
        {
            formatNum(context.day, patlen, false, out);
        }
        else if (c == 'E')
        {
            if (patlen <= 3)
            {
                out.append(symbols.shortWeekdays[context.dayOfWeek]);
            }
            else
            {
                out.append(symbols.weekdays[context.dayOfWeek]);
            }
        }
        else if (c == 'H')
        {
            // output hour in range 0..23
            formatNum(context.hour, patlen, false, out);
        }
        else if (c == 'h')
        {
            // output hour in range 1..12:
            // 00 --> 12am, 01->1am, 12 --> 12pm, 13 -> 1pm, 23->11pm
            int hour = context.hour;
            if (hour == 0)
            {
                hour = 12; // 12am
            }
            else if (hour > 12)
            {
                hour = hour - 12;
            }
            formatNum(hour, patlen, false, out);
        }
        else if (c == 'm')
        {
            formatNum(context.min, patlen, false, out);
        }
        else if (c == 's')
        {
            formatNum(context.sec, patlen, false, out);
        }
        else if (c == 'a')
        {
            out.append(symbols.ampms[context.ampm]);
        }
        else if (c == 'w')
        {
            formatNum(context.weekOfWeekYear, patlen, false, out);
        }
        else
        {
            context.invalid = true;
        }
    }

    /**
     * Write out an integer padded with leading zeros to a specified width.
     * <p>
     * If ensureLength is set, and the number is longer than length, then display only the
     * rightmost length digits.
     */
    private static void formatNum(int num, int length, boolean ensureLength, StringBuffer out)
    {
        String str = String.valueOf(num);
        while (str.length() < length)
        {
            str = "0" + str;
        }

        // XXX do we have to distinguish left and right 'cutting'
        //ensureLength - enable cutting only for parameters like the year, the other
        if (ensureLength && str.length() > length)
        {
          str = str.substring(str.length() - length);
        }

        out.append(str);
    }

    // ======================================================================
    // Pattern Processing Methods
    // ======================================================================

    /**
     * Given a date parsing or formatting pattern, split it up into an
     * array of separate pieces to be processed.
     * <p>
     * Each piece is either:
     * <ul>
     * <li> a "format" section
     * <li> a "quote" section,
     * <li> a "literal" section, or
     * </ul>
     * <p>
     * A format section is a sequence of 1 or more identical alphabetical
     * characters, eg "yyyy", "MMM" or "dd". When parsing, this indicates what
     * data is expected next; if it is not a recognised sequence then it is
     * just ignored. When formatting, this indicates which part of the provided
     * date object should be output, and how to format it; if it is not a
     * recognised sequence then it is simply written literally to the output.
     * <p>
     * A quote section is something in the pattern that was enclosed in quote
     * marks. When parsing, quote sections are expected to be present in exactly
     * the same form in the input string; an error is reported if the data is
     * not present. When formatting, quote sections are output literally as
     * they occurred in the pattern.
     * <p>
     * A literal section is a sequence of 1 or more non-quoted non-alphabetical
     * characters, eg "-" or "+++". When parsing, literal sections just cause
     * the same number of characters in the input stream to be skipped. When
     * formatting, they are just output literally.
     * <p>
     * The elements of the string array returned are of form "f:xxxx" (format
     * section), "q:text" (quote section), or "l:-" (literal section).
     * <p>
     * TODO: when formatting, should literal chars really just cause skipping?
     */
    private static String[] analysePattern(String pattern)
    {
        int patternIndex = 0;
        int patternLen = pattern.length();
        char lastChar = 0;
        StringBuffer patternSub = null;
        boolean quoteMode = false;

        List ops = new LinkedList();

        while (patternIndex < patternLen)
        {
            char currentChar = pattern.charAt(patternIndex);
            char nextChar;

            if (patternIndex < patternLen - 1)
            {
                nextChar = pattern.charAt(patternIndex + 1);
            }
            else
            {
                nextChar = 0;
            }

            if (currentChar == '\'' && lastChar != '\\')
            {
                if (patternSub != null)
                {
                    ops.add(patternSub.toString());
                    patternSub = null;
                }
                quoteMode = !quoteMode;
            }
            else if (quoteMode)
            {
                if (patternSub == null)
                {
                    patternSub = new StringBuffer("q:");
                }
                patternSub.append(currentChar);
            }
            else
            {
                if (currentChar == '\\' && lastChar != '\\')
                {
                    // do nothing
                }
                else
                {
                    if (patternSub == null)
                    {
                        if (Character.isLetter(currentChar))
                        {
                            patternSub = new StringBuffer("f:");
                        }
                        else
                        {
                            patternSub = new StringBuffer("l:");
                        }
                    }

                    patternSub.append(currentChar);
                    if (currentChar != nextChar)
                    {
                        ops.add(patternSub.toString());
                        patternSub = null;
                    }
                }
            }

            patternIndex++;
            lastChar = currentChar;
        }

        if (patternSub != null)
        {
            ops.add(patternSub.toString());
        }

        String[] data = new String[ops.size()];
        return (String[]) ops.toArray(data);
    }

    /**
     * Determine whether to make the "yyyy" pattern behave in a non-standard manner.
     * <p>
     * The java.text.SimpleDateFormat class has no option to output the "weekyear"
     * property, ie the year in which the "ww" value occurs. This makes the "ww"
     * formatter basically useless.
     * <p>
     * This class therefore implements the JODA "xxxx" formatter that does exactly
     * that. However many people will use "ww/yyyy" patterns without realising that
     * this generates garbage (eg 01/2000 when it should output 01/2001 because the
     * week has rolled over from one year to the next). This therefore checks whether
     * ww is present in the pattern string, and if so makes yy work like xx. Of
     * course this does not allow patterns like "xxxx-ww yyyy-MM-dd", so we then
     * disable this hack if "xx" is also present.
     */
    private static boolean hasWeekPattern(String[] ops)
    {
        boolean wwPresent = false;
        boolean xxPresent = false;
        for(int i=0; i<ops.length; ++i)
        {
            String s = ops[i];
            wwPresent = wwPresent || s.startsWith("f:ww");
            xxPresent = xxPresent || s.startsWith("f:xx");
        }

        return wwPresent && !xxPresent;
    }

    // ======================================================================
    // Instance methods
    // ======================================================================

    private DateFormatSymbols symbols;

    private String[] ops;
    boolean yearIsWeekYear;
    int firstDayOfWeek;

    /**
     * Constructor.
     * 
     * @param firstDayOfWeek uses java.util.Date convention, ie 0=sun, 1=mon, 6=sat.
     */
    public SimpleDateFormatter(String pattern, DateFormatSymbols symbols, int firstDayOfWeek)
    {
        if (symbols == null)
        {
            this.symbols = new DateFormatSymbols();
        }
        else
        {
            this.symbols = symbols;
        }

        this.ops = analysePattern(pattern);
        this.yearIsWeekYear = hasWeekPattern(ops);

        this.firstDayOfWeek = firstDayOfWeek;
    }

    /**
     * Constructor that sets the firstDayOfWeek to the ISO standard (1=monday).
     */
    public SimpleDateFormatter(String pattern, DateFormatSymbols symbols)
    {
        this(pattern, symbols, 1);
    }

    public void setFirstDayOfWeek(int dow)
    {
        this.firstDayOfWeek = dow;
    }

    public Date parse(String dateStr)
    {
        if ((dateStr==null) || (dateStr.length() == 0))
            return null;

        ParserContext context = parseOps(symbols, yearIsWeekYear, firstDayOfWeek, ops, dateStr);

        if (context.invalid)
        {
            return null;
        }

        parsePostProcess(symbols, context);
        return createDateFromContext(context);
    }

    public String format(Date date)
    {
        if (date instanceof java.sql.Date)
        {
            return formatOps(symbols, yearIsWeekYear, firstDayOfWeek, ops, new Date(date.getTime()));
        }
        else
        {
            return formatOps(symbols, yearIsWeekYear, firstDayOfWeek, ops, date);
        }
    }
    
    public WeekDate getWeekDate(Date date)
    {
        if (date instanceof java.sql.Date)
        {
            return getWeekDate(new Date(date.getTime()), this.firstDayOfWeek);
        }
        else
        {
            return getWeekDate(date, this.firstDayOfWeek);
        }
    }
    
    public Date getDateForWeekDate(WeekDate wdate)
    {
        return getDateForWeekDate(
            wdate.getYear(), wdate.getWeek(), 1,
            0, 0, 0,
            this.firstDayOfWeek);
    }
}
