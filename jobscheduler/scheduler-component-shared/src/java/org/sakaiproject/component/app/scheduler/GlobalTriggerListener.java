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

package org.sakaiproject.component.app.scheduler;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.sakaiproject.api.app.scheduler.events.TriggerEvent;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;
import org.sakaiproject.component.api.ServerConfigurationService;

public class GlobalTriggerListener implements TriggerListener
{

  private TriggerEventManager eventManager = null;
  private ServerConfigurationService serverConfigurationService = null;

  public void setTriggerEventManager (TriggerEventManager eMgr)
  {
      eventManager = eMgr;
  }

  public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
  {
    this.serverConfigurationService = serverConfigurationService;
  }

  public String getName()
  {
    return "GlobalTriggerListener";
  }

  /**
   * Get the id of this instance
   * @return
   */
  private String getServerId() {
	  return serverConfigurationService.getServerId();
  }

  @Override
  public void triggerFired(Trigger trigger, JobExecutionContext jobExecutionContext)
  {
      Date fired = jobExecutionContext.getFireTime();
      eventManager.createTriggerEvent (TriggerEvent.TRIGGER_EVENT_TYPE.FIRED, jobExecutionContext.getJobDetail().getKey(), trigger.getKey(), fired, "Trigger fired", getServerId());
  }

  public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jobExecutionContext)
  {
    return false;
  }

  public void triggerMisfired(Trigger trigger)
  {
    eventManager.createTriggerEvent (TriggerEvent.TRIGGER_EVENT_TYPE.ERROR, trigger.getJobKey(), trigger.getKey(), new Date(), "Trigger misfired", getServerId());
  }
  
  @Override
  public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode) {
      Date complete = Date.from(context.getFireTime().toInstant().plusMillis(context.getJobRunTime()));
      eventManager.createTriggerEvent (TriggerEvent.TRIGGER_EVENT_TYPE.COMPLETE, context.getJobDetail().getKey(), trigger.getKey(), complete, "Trigger complete", getServerId());
  }

}