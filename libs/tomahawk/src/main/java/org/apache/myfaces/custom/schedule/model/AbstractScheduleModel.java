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
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.myfaces.custom.schedule.util.ScheduleUtil;

/**
 * <p>
 * Extend this class to create your own implementation of a ScheduleModel
 * </p>
 * 
 * @author Jurgen Lust (latest modification by $Author: baranda $)
 * @version $Revision: 372589 $
 */
public abstract class AbstractScheduleModel implements ScheduleModel,
        Serializable
{
    // ~ Instance fields
    // --------------------------------------------------------

    private TreeSet days;

    private Date selectedDate = new Date();

    private ScheduleEntry selectedEntry;

    private int mode;

    protected final TimeZone timeZone;

    // ~ Constructors
    // -----------------------------------------------------------

    /**
     * Creates a new AbstractScheduleModel object.
     */
    public AbstractScheduleModel()
    {
        this(TimeZone.getDefault());
    }

    public AbstractScheduleModel(TimeZone timeZone)
    {
      this.timeZone = timeZone;
    }

    // ~ Methods
    // ----------------------------------------------------------------

    /**
     * Get the set of days. Initialise on first call.
     * 
     */
    protected SortedSet getDays()
    {
        if (days == null)
        {
            days = new TreeSet();
            
            switch (mode)
            {
            case DAY:
                setDay(this.selectedDate);

                break;

            case WORKWEEK:
                setWorkWeek(this.selectedDate);

                break;

            case WEEK:
                setWeek(this.selectedDate);

                break;

            case MONTH:
                setMonth(this.selectedDate);

                break;

            default:
                setDay(this.selectedDate);
            }        
        }
        
        return days;
    }
    
    /**
     * Returns the timezone setting for this model
     */
    public TimeZone getTimeZone ()
    {
      return this.timeZone;
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#isEmpty()
     */
    public boolean isEmpty()
    {
        return getDays().isEmpty();
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#setMode(int)
     */
    public void setMode(int mode)
    {
        this.mode = mode;
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#getMode()
     */
    public int getMode()
    {
        return mode;
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#setSelectedDate(java.util.Date)
     */
    public void setSelectedDate(Date date)
    {
        if (date == null)
        {
            // do nothing when the date is null
            return;
        }

        this.selectedDate = date;
        this.days = null;
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#getSelectedDate()
     */
    public Date getSelectedDate()
    {
        return selectedDate;
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#setSelectedEntry(org.apache.myfaces.custom.schedule.model.ScheduleEntry)
     */
    public void setSelectedEntry(ScheduleEntry selectedEntry)
    {
        this.selectedEntry = selectedEntry;
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#getSelectedEntry()
     */
    public ScheduleEntry getSelectedEntry()
    {
        return selectedEntry;
    }
    
    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#isEntrySelected()
     */
    public boolean isEntrySelected() {
        return getSelectedEntry() != null;
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#containsDate(java.util.Date)
     */
    public boolean containsDate(Date date)
    {
        for (Iterator iterator = iterator(); iterator.hasNext();)
        {
            ScheduleDay day = (ScheduleDay) iterator.next();

            if (day.equalsDate(date))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#get(int)
     */
    public Object get(int index)
    {
        Object[] dayArray = getDays().toArray();

        Object returnObject = dayArray[index];

        return returnObject;
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#iterator()
     */
    public Iterator iterator()
    {
        return getDays().iterator();
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#size()
     */
    public int size()
    {
        return getDays().size();
    }

    /**
     * <p>
     * Set the day
     * </p>
     * 
     * @param date
     *            the new day
     */
    protected void setDay(Date date)
    {
        if (date == null)
        {
            return;
        }

        clear();

        ScheduleDay day = add(date);
        load(day.getDayStart(), day.getDayEnd());
    }

    /**
     * <p>
     * navigate to the specified month
     * </p>
     * 
     * @param date
     *            the date to navigate to
     */
    protected void setMonth(Date date)
    {
        if (date == null)
        {
            return;
        }

        clear();

        Calendar cal = getCalendarInstance(date);

        // go back to the first day of the month;
        cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DAY_OF_MONTH));

        Date temp = cal.getTime();
        cal.add(Calendar.MONTH, 1);
        int nextMonth = cal.get(Calendar.MONTH);
        cal.setTime(temp);

        ScheduleDay firstDay = null;
        ScheduleDay lastDay = null;
        
        // we want to show the whole first week, including the days from the previous month
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        while (true)
        {
            ScheduleDay addedDay = add(cal.getTime());

            if (firstDay == null)
            {
                firstDay = addedDay;
            }

            lastDay = addedDay;
            cal.add(Calendar.DATE, 1);
            
            //stop when we are at the last day of the last week
            if (cal.get(Calendar.MONTH) == nextMonth && cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
            {
                   break;
            }
        }

        load(firstDay.getDayStart(), lastDay.getDayEnd());
    }

    /**
     * <p>
     * navigate to the specified week
     * </p>
     * 
     * @param date
     *            the date to navigate to
     */
    protected void setWeek(Date date)
    {
        if (date == null)
        {
            return;
        }

        clear();

        Calendar cal = getCalendarInstance(date);

        // go back to the monday of this week
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        ScheduleDay firstDay = null;
        ScheduleDay lastDay = null;

        for (int i = 0; i < 7; i++)
        {
            ScheduleDay addedDay = add(cal.getTime());

            if (firstDay == null)
            {
                firstDay = addedDay;
            }

            lastDay = addedDay;
            cal.add(Calendar.DATE, 1);
        }

        load(firstDay.getDayStart(), lastDay.getDayEnd());
    }

    /**
     * <p>
     * navigate to the specified workweek
     * </p>
     * 
     * @param date
     *            the date to navigate to
     */
    protected void setWorkWeek(Date date)
    {
        if (date == null)
        {
            return;
        }

        clear();

        Calendar cal = getCalendarInstance(date);

        // go back to the monday of this week
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        ScheduleDay firstDay = null;
        ScheduleDay lastDay = null;

        for (int i = 0; i < 5; i++)
        {
            ScheduleDay addedDay = add(cal.getTime());

            if (firstDay == null)
            {
                firstDay = addedDay;
            }

            lastDay = addedDay;
            cal.add(Calendar.DATE, 1);
        }

        load(firstDay.getDayStart(), lastDay.getDayEnd());
    }

    /**
     * <p>
     * Add a day to the schedule
     * </p>
     * 
     * @param date
     *            the day to add
     * 
     * @return the day that was added
     */
    protected ScheduleDay add(Date date)
    {
        if (date == null)
        {
            return null;
        }

        ScheduleDay day = new ScheduleDay(date, getTimeZone());
        loadDayAttributes(day);
        getDays().add(day);

        return day;
    }

    /**
     * <p>
     * Remove all days
     * </p>
     */
    protected void clear()
    {
        if (days != null)
        {
            for (Iterator dayIterator = days.iterator(); dayIterator.hasNext();)
            {
                ScheduleDay day = (ScheduleDay) dayIterator.next();
                day.clear();
            }

            days.clear();
        }
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#refresh()
     */
    public void refresh()
    {
        setSelectedDate(selectedDate);
    }

    /**
     * <p>
     * Load the schedule entries that fall between the startDate and the
     * endDate.
     * </p>
     * 
     * @param startDate
     *            0 AM on the start date
     * @param endDate
     *            12 PM on the end date
     */
    protected abstract Collection loadEntries(Date startDate, Date endDate);

    /**
     * <p>
     * Load any attributes for this day: is it a working day or a holiday, and
     * what is the name of the day (e.g. "Christmas").
     * </p>
     * 
     * @param day
     *            the day that should be loaded
     */
    protected abstract void loadDayAttributes(Day day);

    /**
     * @return true, if each day contains the same set of intervals
     */
    public boolean containsRepeatedIntervals() {
        boolean firstDay = true;
        TreeSet firstDayIntervals = null;
        
        if (mode == DAY)
        {
            return true;
        }
        
        for (Iterator dayIterator = iterator(); dayIterator.hasNext(); )
        {
            Day day = (Day) dayIterator.next();
            
            if (firstDay)
            {
                firstDayIntervals = day.getIntervals();
                firstDay = false;
            }
            else
            {
                if (firstDayIntervals == null || firstDayIntervals.size() == 0) {
                    if (day.getIntervals() != null && day.getIntervals().size() > 0)
                    {
                        return false;
                    }
                }
                else if (!ScheduleUtil.areEquivalentIntervals(firstDayIntervals, day.getIntervals()))
                {
                    
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private void load(Date startDate, Date endDate)
    {
        Collection entries = loadEntries(startDate, endDate);

        for (Iterator entryIterator = entries.iterator(); entryIterator
                .hasNext();)
        {
            ScheduleEntry entry = (ScheduleEntry) entryIterator.next();

            for (Iterator dayIterator = getDays().iterator(); dayIterator.hasNext();)
            {
                ScheduleDay day = (ScheduleDay) dayIterator.next();

                day.addEntry(entry);
            }
        }
    }
    
    protected Calendar getCalendarInstance(Date date) {

        return ScheduleUtil.getCalendarInstance(date, getTimeZone());
    }
}
// The End
