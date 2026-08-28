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
import java.util.Date;
import java.util.TimeZone;

import org.apache.myfaces.custom.schedule.util.ScheduleUtil;

/**
 * <p>
 * This class represents a period of time, which may be given a label.
 * An interval is inclusive of the start time, but excludes the end time.
 * </p>
 * @since 1.1.7
 * @author Peter Mahoney
 * @version $Revision: 371736 $
 */
public class Interval implements Serializable, Comparable
{

    private String label;
    private Date startTime;
    private Date endTime;
    
    public Interval(String label, Date startTime, Date endTime)
    {
        this.label = label;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public String getLabel()
    {
        return label;
    }
    public void setLabel(String label)
    {
        this.label = label;
    }
    
    public Date getStartTime()
    {
        return startTime;
    }
    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }
    
    public Date getEndTime()
    {
        return endTime;
    }
    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public boolean containsDate(Date clickedDate)
    {
        return !getStartTime().after(clickedDate) && clickedDate.before(getEndTime());
    }
    
    public int compareTo(Object o)
    {
        if (o instanceof Interval)
        {
            
            return startTime.compareTo(((Interval) o).startTime);
        }

        return 1;
    }

    public boolean after(Interval last)
    {
        
        return !startTime.before(last.getEndTime());
    }

    public int getStartHours(TimeZone timeZone)
    {
        
        return ScheduleUtil.getCalendarInstance(getStartTime(), timeZone).get(Calendar.HOUR_OF_DAY);
    }

    public int getStartMinutes(TimeZone timeZone)
    {
        
        return ScheduleUtil.getCalendarInstance(getStartTime(), timeZone).get(Calendar.MINUTE);
    }
    
    public long getDuration()
    {
        
        return getEndTime().getTime() - getStartTime().getTime();
    }
    
    /**
     * <p>
     * Intervals are equivalent if their label is the same and they begin and end
     * at the same time of day.
     * </p>
     *
     * @param other the interval to compare with
     *
     * @return true, if this interval is equivalent to the given interval
     */
    public boolean isEquivalent(Interval other)
    {
            
        return label.equals(other.label) 
                && ScheduleUtil.isSameTime(startTime, other.startTime) 
                && ScheduleUtil.isSameTime(endTime, other.endTime);
    }
    
    public int hashCode() {

        return label.hashCode() + startTime.hashCode() + endTime.hashCode();
    }
    
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof Interval) {
            Interval other = (Interval) obj;
            
            return label.equals(other.label) 
                    && startTime.equals(other.startTime) 
                    && endTime.equals(other.endTime);
        }
        
        return false;
    }
    
    public String toString()
    {
        
        return this.getClass().getName() + " Start:" + startTime + " End:" + endTime;
    }
}
