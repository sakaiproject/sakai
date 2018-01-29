/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
import java.util.List;

import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 3:40:13 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TriggerEventManager
{

    /**
     * @param type
     * @param jobName
     * @param triggerName
     * @param time
     * @param message
     * @return a TriggerEvent object
     * @deprecated use {@link #createTriggerEvent(org.sakaiproject.api.app.scheduler.events.TriggerEvent.TRIGGER_EVENT_TYPE, String, String, Date, String, String)} instead
     */
    public TriggerEvent createTriggerEvent(TriggerEvent.TRIGGER_EVENT_TYPE type, JobKey jobKey, TriggerKey triggerKey, Date time, String message);

    /**
     * @param type
     * @param jobName
     * @param triggerName
     * @param time
     * @param message
     * @param serverId the id of the server the job runs on
     * @return a TriggerEvent object
     */
    public TriggerEvent createTriggerEvent(TriggerEvent.TRIGGER_EVENT_TYPE type, JobKey jobKey, TriggerKey triggerKey, Date time, String message, String ServerId);

    /**
     * @return list of all TriggerEvent
     */
    public List<TriggerEvent> getTriggerEvents ();

    /**
     * Get All triggers
     * @param after
     * @param before
     * @param jobs
     * @param triggerName
     * @param types
     * @return list of all TriggerEvent
     */
    public List<TriggerEvent> getTriggerEvents (Date after, Date before, List<String> jobs, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types);
    
    public int getTriggerEventsSize ();
    
    public int getTriggerEventsSize (Date after, Date before, List<String> jobs, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types);
    
    public List<TriggerEvent> getTriggerEvents (int first, int size);
    
    public List<TriggerEvent> getTriggerEvents (Date after, Date before, List<String> jobs, String triggerName, TriggerEvent.TRIGGER_EVENT_TYPE[] types, int first, int size);

    /**
     * @param before
     */
    public void purgeEvents (Date before);

}
