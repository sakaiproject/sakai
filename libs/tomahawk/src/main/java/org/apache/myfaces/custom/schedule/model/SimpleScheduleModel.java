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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.myfaces.custom.schedule.util.ScheduleEntryComparator;

/**
 * <p>
 * A simple implementation of the ScheduleModel, not backed by any kind of
 * datasource: entries have to be added manually.
 * </p>
 * 
 * @author Jurgen Lust (latest modification by $Author: werpu $)
 * @version $Revision: 371736 $
 */
public class SimpleScheduleModel extends AbstractScheduleModel implements
        Serializable
{
    // ~ Instance fields
    // --------------------------------------------------------

    /**
     * serial id for serialisation versioning
     */
    private static final long serialVersionUID = 1L;

    private final TreeSet entries;

    private final HashMap holidays;

    private final DateFormat holidayFormat = new SimpleDateFormat("yyyyMMdd");

    // ~ Constructors
    // -----------------------------------------------------------

    /**
     * Creates a new SimpleScheduleModel object.
     */
    public SimpleScheduleModel()
    {
        this.entries = new TreeSet(new ScheduleEntryComparator());
        this.holidays = new HashMap();
    }

    // ~ Methods
    // ----------------------------------------------------------------

    /**
     * Set the name of a holiday.
     * 
     * @param date
     *            the date
     * @param holidayName
     *            the name of the holiday
     */
    public void setHoliday(Date date, String holidayName)
    {
        if (date == null)
        {
            return;
        }

        String key = holidayFormat.format(date);
        holidays.put(key, holidayName);
    }

    /**
     * Add an entry to the model.
     * 
     * @param entry
     *            the entry to add
     */
    public void addEntry(ScheduleEntry entry)
    {
        entries.add(entry);
    }

    /**
     * Remove an entry from the model.
     * 
     * @param entry
     *            the entry to remove
     */
    public void removeEntry(ScheduleEntry entry)
    {
        entries.remove(entry);
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.ScheduleModel#removeSelectedEntry()
     */
    public void removeSelectedEntry()
    {
        if (!isEntrySelected())
            return;
        removeEntry(getSelectedEntry());
        setSelectedEntry(null);
        refresh();
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.AbstractScheduleModel#loadEntries(java.util.Date,
     *      java.util.Date)
     */
    protected Collection loadEntries(Date startDate, Date endDate)
    {
        ArrayList selection = new ArrayList();

        for (Iterator entryIterator = entries.iterator(); entryIterator
                .hasNext();)
        {
            ScheduleEntry entry = (ScheduleEntry) entryIterator.next();

            if (entry.getEndTime().before(startDate)
                    || entry.getStartTime().after(endDate))
            {
                continue;
            }

            selection.add(entry);
        }

        return selection;
    }

    /**
     * @see org.apache.myfaces.custom.schedule.model.AbstractScheduleModel#loadDayAttributes(org.apache.myfaces.custom.schedule.model.Day)
     */
    protected void loadDayAttributes(Day day)
    {
        if (day == null)
            return;
        String key = holidayFormat.format(day.getDate());
        String holiday = (String) holidays.get(key);
        if (holiday != null)
        {
            day.setSpecialDayName(holiday);
            day.setWorkingDay(false);
        } else
        {
            day.setSpecialDayName(null);
            day.setWorkingDay(true);
        }
    }
}
// The End
