/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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
package org.sakaiproject.api.app.scheduler.events;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 3:39:51 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TriggerEvent
{
    public static enum TRIGGER_EVENT_TYPE
    {
        FIRED, COMPLETE, INFO, DEBUG, ERROR
    }

    /**
     * 
     * @return
     */
    public TRIGGER_EVENT_TYPE getEventType();

    /**
     * 
     * @return
     */
    public String getJobName();

    /**
     * 
     * @return
     */
    public String getTriggerName();

    /**
     * 
     * @return
     */
    public Date getTime();

    /**
     * 
     * @return
     */
    public String getMessage();
    
    /**
     * Get the id of the server the trigger ran on
     * @return
     */
    public String getServerId();
}
