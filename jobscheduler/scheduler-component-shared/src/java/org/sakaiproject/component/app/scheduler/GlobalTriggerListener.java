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