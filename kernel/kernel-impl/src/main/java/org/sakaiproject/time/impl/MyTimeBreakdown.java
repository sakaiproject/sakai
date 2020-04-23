/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.time.impl;

import org.sakaiproject.time.api.TimeBreakdown;

/**********************************************************************************************************************************************************************************************************************************************************
 * TimeBreakdown implementation
 *********************************************************************************************************************************************************************************************************************************************************/

public class MyTimeBreakdown implements TimeBreakdown
{
    /** The parts. */
    protected int year;

    protected int month;

    protected int day;

    protected int hour;

    protected int min;

    protected int sec;

    protected int ms;

    public MyTimeBreakdown(int y, int m, int d, int h, int minutes, int s, int milliseconds)
    {
        year = y;
        month = m;
        day = d;
        hour = h;
        min = minutes;
        sec = s;
        ms = milliseconds;
    }

    public MyTimeBreakdown(TimeBreakdown other)
    {
        year = ((MyTimeBreakdown) other).year;
        month = ((MyTimeBreakdown) other).month;
        day = ((MyTimeBreakdown) other).day;
        hour = ((MyTimeBreakdown) other).hour;
        min = ((MyTimeBreakdown) other).min;
        sec = ((MyTimeBreakdown) other).sec;
        ms = ((MyTimeBreakdown) other).ms;
    }

    public String toString()
    {
        return "year: " + year + " month: " + month + " day: " + day + " hour: " + hour + " min: " + min + " sec: " + sec
        + " ms: " + ms;
    }

    public int getYear()
    {
        return year;
    }

    public int getMonth()
    {
        return month;
    }

    public int getDay()
    {
        return day;
    }

    public int getHour()
    {
        return hour;
    }

    public int getMin()
    {
        return min;
    }

    public int getSec()
    {
        return sec;
    }

    public int getMs()
    {
        return ms;
    }

    /**
     * @param i
     */
    public void setDay(int i)
    {
        day = i;
    }

    /**
     * @param i
     */
    public void setHour(int i)
    {
        hour = i;
    }

    /**
     * @param i
     */
    public void setMin(int i)
    {
        min = i;
    }

    /**
     * @param i
     */
    public void setMonth(int i)
    {
        month = i;
    }

    /**
     * @param i
     */
    public void setMs(int i)
    {
        ms = i;
    }

    /**
     * @param i
     */
    public void setSec(int i)
    {
        sec = i;
    }

    /**
     * @param i
     */
    public void setYear(int i)
    {
        year = i;
    }
}
