/***********************************************************************************
 *
 * $Header: $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

public class GlobalTriggerListener implements TriggerListener
{

  private List triggerEvents = new ArrayList();
  private List todaysTriggerEvents = new ArrayList();
  private boolean isViewAllSelected = false;

  private static final Log LOG = LogFactory.getLog(GlobalTriggerListener.class);

  public String getName()
  {
    return "GlobalTriggerListener";
  }

  public void triggerFired(Trigger trigger,
      JobExecutionContext jobExecutionContext)
  {
    TriggerEvent te = new TriggerEvent();
    te.setEventType(TriggerEvent.TRIGGER_FIRED);
    te.setJobName(jobExecutionContext.getJobDetail().getName());
    te.setTime(new Date());
    triggerEvents.add(0, te);
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
    TriggerEvent te = new TriggerEvent();
    te.setEventType(TriggerEvent.TRIGGER_COMPLETE);
    te.setJobName(jobExecutionContext.getJobDetail().getName());
    te.setTime(new Date());
    triggerEvents.add(0, te);
  }

  /**
   * @return Returns the triggerEvents.
   */
  public List getTriggerEvents()
  {
    if (isViewAllSelected)
    {
      return triggerEvents;
    }
    else
    {
      todaysTriggerEvents = new ArrayList();
      Date midnightToday = new Date();
      midnightToday.setHours(0);
      midnightToday.setMinutes(0);
      midnightToday.setSeconds(0);
      for (Iterator i = triggerEvents.iterator(); i.hasNext();)
      {
        TriggerEvent te = (TriggerEvent) i.next();
        if (te.getTime().after(midnightToday))
        {
          todaysTriggerEvents.add(te);
        }
      }
      return todaysTriggerEvents;
    }
  }

  /**
   * @param triggerEvents The triggerEvents to set.
   */
  public void setTriggerEvents(List triggerEvents)
  {
    this.triggerEvents = triggerEvents;
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