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
