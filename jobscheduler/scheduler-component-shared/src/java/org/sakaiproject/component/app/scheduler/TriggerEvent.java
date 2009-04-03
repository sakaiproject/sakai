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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.scheduler;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TriggerEvent
{
  private String jobName;
  private Date time;
  private String eventType;

  protected static String TRIGGER_FIRED = "Fired";
  protected static String TRIGGER_COMPLETE = "Complete";

  private static final Log LOG = LogFactory.getLog(TriggerEvent.class);

  /**
   * @return Returns the eventType.
   */
  public String getEventType()
  {
    return eventType;
  }

  /**
   * @param eventType The eventType to set.
   */
  public void setEventType(String eventType)
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
}