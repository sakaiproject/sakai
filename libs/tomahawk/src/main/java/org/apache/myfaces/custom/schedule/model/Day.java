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

package org.apache.myfaces.custom.schedule.model;


import java.io.Serializable;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.myfaces.custom.schedule.util.ScheduleUtil;


/**
 * <p>
 * This class represents a day in the Schedule component
 * </p>
 *
 * @author Jurgen Lust (latest modification by $Author: werpu $)
 * @version $Revision: 371736 $
 */
public class Day
    implements Serializable, Comparable
{
    //~ Instance fields --------------------------------------------------------

    /**
     * serial id for serialisation versioning
     */
    private static final long serialVersionUID = 1L;
    private final Date date;
    private final Date dayEnd;
    private final Date dayStart;
    private String specialDayName;
    private boolean workingDay;
    private TreeSet intervals;
    private final TimeZone timeZone;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Day object.
     *
     * @param date the date
     *
     * @throws NullPointerException when the date is null
     */
    public Day(Date date)
    {
        this(date, TimeZone.getDefault());
    }

    /**
     * Creates a new Day object.
     *
     * @param date the date
     * @param timeZone The timezone
     *
     * @throws NullPointerException when the date is null
     */
    public Day(Date date, TimeZone timeZone)
    {
        this.date = date;
        this.timeZone = timeZone;

        if (date == null) {
            throw new NullPointerException("date should not be null");
        }
        Calendar cal = getCalendarInstance(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.dayStart = cal.getTime();
        cal.add(Calendar.DATE, 1);
        this.dayEnd = cal.getTime();
    }
    
    //~ Methods ----------------------------------------------------------------

    protected Calendar getCalendarInstance(Date date) {

        return ScheduleUtil.getCalendarInstance(date, timeZone);
    }
    
    /**
     * @return Returns the date.
     */
    public Date getDate()
    {
        return (date == null) ? new Date() : date;
    }

    /**
     * @return Returns 12PM of this day
     */
    public Date getDayEnd()
    {
        return dayEnd;
    }

    /**
     * @return Returns 0AM of this day
     */
    public Date getDayStart()
    {
        return dayStart;
    }

    /**
     * <p>
     * If this day is a holiday of some kind, this sets the name
     * </p>
     *
     * @param specialDayName The specialDayName to set.
     */
    public void setSpecialDayName(String specialDayName)
    {
        this.specialDayName = specialDayName;
    }

    /**
     * <p>
     * If this day is a holiday of some kind, this gets the name
     * </p>
     *
     * @return Returns the specialDayName.
     */
    public String getSpecialDayName()
    {
        return specialDayName;
    }

    /**
     * <p>
     * Is this day a working day?
     * </p>
     *
     * @param workingDay The workingDay to set.
     */
    public void setWorkingDay(boolean workingDay)
    {
        this.workingDay = workingDay;
    }

    /**
     * <p>
     * Is this day a working day?
     * </p>
     *
     * @return Returns the workingDay.
     */
    public boolean isWorkingDay()
    {
        Calendar cal = getCalendarInstance(date);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        if ((dayOfWeek == Calendar.SATURDAY) || (dayOfWeek == Calendar.SUNDAY)) {
            return false;
        }

        return workingDay;
    }

    /**
     * 
     * @return A chronologically ordered set of intervals.
     */
    public TreeSet getIntervals()
    {
        return intervals;
    }
    
    /**
     * Set user defined intervals during the day.
     * 
     * @param intervals A Collection<Interval> of intervals during the day
     */
    public void setIntervals(Collection intervals)
    {
        if (intervals instanceof TreeSet)
        {
            this.intervals = (TreeSet) intervals;
        }
        else
        {    
            this.intervals = new TreeSet(intervals);
        }
    }
    
    public void addInterval(String label, Date startTime, Date endTime)
    {
        if (intervals == null)
        {
            intervals = new TreeSet();
        }
        intervals.add(new Interval(label, startTime, endTime));
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        if (o instanceof Day) {
            Day other = (Day) o;

            int returnint = ScheduleUtil.compareDays(date, other.getDate(), timeZone);

            return returnint;
        }

        return 1;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
        if (o instanceof Day) {
            Day other = (Day) o;

            return ScheduleUtil.isSameDay(date, other.getDate(), timeZone);
        }

        return false;
    }

    /**
     * <p>
     * Check if the specified date is on this day
     * </p>
     *
     * @param date the date to check
     *
     * @return if the date is on this day
     */
    public boolean equalsDate(Date date)
    {
        if (date == null) {
            return false;
        }

        return ScheduleUtil.isSameDay(date, this.date, timeZone);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return ScheduleUtil.getHashCodeForDay(date, timeZone);
    }
}
//The End
