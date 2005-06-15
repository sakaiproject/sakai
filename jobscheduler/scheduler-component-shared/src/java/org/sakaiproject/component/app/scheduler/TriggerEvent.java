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