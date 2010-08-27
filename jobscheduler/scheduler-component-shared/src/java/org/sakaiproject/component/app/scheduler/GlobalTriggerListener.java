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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;

public class GlobalTriggerListener implements TriggerListener
{

  private boolean isViewAllSelected = false;
  private TriggerEventManager eventManager = null;

  public void setTriggerEventManager (TriggerEventManager eMgr)
  {
      eventManager = eMgr;
  }

  public TriggerEventManager getTriggerEventManager()
  {
      return eventManager;
  }

  public String getName()
  {
    return "GlobalTriggerListener";
  }

  public void triggerFired(Trigger trigger,
      JobExecutionContext jobExecutionContext)
  {
      eventManager.createTriggerEvent (TriggerEvent.TRIGGER_EVENT_TYPE.FIRED, jobExecutionContext.getJobDetail().getName(), trigger.getName(), new Date(), "Trigger fired");
  }

  public boolean vetoJobExecution(Trigger trigger,
      JobExecutionContext jobExecutionContext)
  {
    return false;
  }

  public void triggerMisfired(Trigger trigger)
  {
  }

  public void triggerComplete(Trigger trigger,
      JobExecutionContext jobExecutionContext, int triggerInstructionCode)
  {
      eventManager.createTriggerEvent (TriggerEvent.TRIGGER_EVENT_TYPE.COMPLETE, jobExecutionContext.getJobDetail().getName(), trigger.getName(), new Date(), "Trigger complete");
  }

  /**
   * @return Returns the triggerEvents.
   */
  public List<TriggerEvent> getTriggerEvents()
  {
    if (isViewAllSelected)
    {
      return eventManager.getTriggerEvents();
    }
    else
    {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      
      Date midnightToday = new Date(cal.getTimeInMillis());      

      return eventManager.getTriggerEvents (midnightToday, null, null, null, null);
/*
      for (Iterator<TriggerEventImpl> i = triggerEvents.iterator(); i.hasNext();)
      {
        TriggerEventImpl te = (TriggerEventImpl) i.next();
        if (te.getTime().after(midnightToday))
        {
          todaysTriggerEvents.add(te);
        }
      }
      return todaysTriggerEvents;
*/
    }
  }

  /**
   * @return Returns the isViewAllSelected.
   */
  public boolean getIsViewAllSelected()
  {
    return isViewAllSelected;
  }

  /**
   * @param isViewAllSelected The isViewAllSelected to set.
   */
  public void setIsViewAllSelected(boolean isViewAllSelected)
  {
    this.isViewAllSelected = isViewAllSelected;
  }

  public String processSelect()
  {
    isViewAllSelected = !isViewAllSelected;
    return null;
  }
}