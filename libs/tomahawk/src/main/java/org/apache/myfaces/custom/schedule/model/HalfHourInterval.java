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

import java.util.Date;

/**
 * <p>
 * This class represents an interval of up to half an hour.
 * The interval will always round up to the next half hour
 * e.g. a start time of 14:15 will generate an interval with
 * end time of 14:30.
 * </p>
 * @since 1.1.7
 * @author Peter Mahoney
 * @version $Revision: 371736 $
 */
public class HalfHourInterval extends Interval {

    public static final long HALF_HOUR = 1000 * 60 * 30;

    public HalfHourInterval(Date startTime, Date maxEnd)
    {
        super(null, startTime, new Date(Math.min(startTime.getTime() + HALF_HOUR, maxEnd.getTime())));
    }

    private HalfHourInterval(String label, Date startTime, Date endTime)
    {
        super(label, startTime, endTime); 
    }
    
    /**
     * Create a new half hour interval following on from the supplied interval.
     * The interval will be anything up to half an hour, depending on when the
     * end of the previous interval was. If an interval cannot be fitted between
     * the end of the last interval and the maximum end time, null will be returned.
     * 
     * @param interval The previous interval
     * @param maxEnd The maximum end time of the new interval
     * @return The next half hour interval
     */
    public static Interval next(Interval interval, Date maxEnd) {
        Date startTime = interval.getEndTime();
        
        if (startTime.before(maxEnd))
        {
            Date endTime = new Date(Math.min(startTime.getTime() - (startTime.getTime() % HALF_HOUR) + HALF_HOUR, maxEnd.getTime()));
            
            return new HalfHourInterval(null, startTime, endTime);
        }
        else
        {
            
            return null;
        }
    }
}
