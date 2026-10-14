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
 * A schedule entry is an appointment or event.
 * </p>
 *
 * @author Jurgen Lust (latest modification by $Author: skitching $)
 * @version $Revision: 349804 $
 */
public interface ScheduleEntry
{
    //~ Methods ----------------------------------------------------------------

    /**
     * @return Returns the description.
     */
    public abstract String getDescription();

    /**
     * @return Returns the endTime.
     */
    public abstract Date getEndTime();

    /**
     * @return Returns the id.
     */
    public abstract String getId();

    /**
     * @return Returns the startTime.
     */
    public abstract Date getStartTime();

    /**
     * @return Returns the subtitle.
     */
    public abstract String getSubtitle();

    /**
     * @return Returns the title.
     */
    public abstract String getTitle();

    /**
     * @return Returns true if the entry last all day.
     */
    public abstract boolean isAllDay();
}
//The End
