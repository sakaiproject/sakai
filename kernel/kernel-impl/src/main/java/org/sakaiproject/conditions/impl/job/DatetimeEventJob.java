package org.sakaiproject.conditions.impl.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

import org.sakaiproject.event.cover.EventTrackingService;

public class DatetimeEventJob implements Job
{

  private static final Log LOG = LogFactory.getLog(DatetimeEventJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  public void execute(JobExecutionContext arg0) throws JobExecutionException
  {
	Long now = new Long(new Date().getTime());
	EventTrackingService.post(EventTrackingService.newEvent("datetime.update", now.toString(), true));
  }

}