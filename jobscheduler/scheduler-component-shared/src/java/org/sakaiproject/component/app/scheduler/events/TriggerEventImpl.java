/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.scheduler.events;

import org.sakaiproject.api.app.scheduler.events.TriggerEvent;

import java.util.Date;

public class TriggerEventImpl implements TriggerEvent
{
  private String jobName;
  private String triggerName;
  private Date time;
  private TRIGGER_EVENT_TYPE eventType;
  private String message;
  private String serverId;

  /**
   * 
   */
  public String getServerId() {
	  return serverId;
  }

  /**
   * Set the serverId
   * @param serverId
   */
  public void setServerId(String serverId) {
	  this.serverId = serverId;
  }

  /**
   * @return Returns the eventType.
   */
  public TRIGGER_EVENT_TYPE getEventType()
  {
    return eventType;
  }

  /**
   * @param eventType The eventType to set.
   */
  public void setEventType(TRIGGER_EVENT_TYPE eventType)
  {
    this.eventType = eventType;
  }

  /**
   * @return Returns the time.
   */
  public Date getTime()
  {
    return time;
  }

    /**
   * @param time The time to set.
   */
  public void setTime(Date time)
  {
    this.time = time;
  }

  /**
   * @return Returns the jobName.
   */
  public String getJobName()
  {
    return jobName;
  }

  /**
   * @param jobName The jobName to set.
   */
  public void setJobName(String jobName)
  {
    this.jobName = jobName;
  }

  public void setTriggerName(String name)
  {
      triggerName = name;
  }

  public String getTriggerName()
  {
      return triggerName;
  }

  public void setMessage (String m)
  {
      message = m;
  }

  public String getMessage()
  {
      return message;
  }


}