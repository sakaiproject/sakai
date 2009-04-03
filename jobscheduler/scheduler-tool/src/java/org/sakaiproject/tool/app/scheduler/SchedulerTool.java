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

package org.sakaiproject.tool.app.scheduler;

import java.text.ParseException;
import java.util.*;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.JobExecutionContext;
import org.sakaiproject.api.app.scheduler.JobDetailWrapper;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.api.app.scheduler.TriggerWrapper;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.component.app.scheduler.JobDetailWrapperImpl;
import org.sakaiproject.component.app.scheduler.TriggerWrapperImpl;

public class SchedulerTool
{

  private static final Log LOG = LogFactory.getLog(SchedulerTool.class);

  private static final String CRON_CHECK_ASTERISK = "**";
  private static final String CRON_CHECK_QUESTION_MARK = "??";

  private SchedulerManager schedulerManager;
  private Set jobClasses;
  private String jobName;
  private String triggerName;
  private String triggerExpression;
  private String selectedClass;

  private List jobDetailWrapperList;
  private List filteredJobDetailWrapperList;
  private JobDetailWrapper selectedJobDetailWrapper;
  private boolean isSelectAllJobsSelected = false;
  private boolean isSelectAllTriggersSelected = false;

  private List filteredTriggersWrapperList;

  public SchedulerTool()
  {
  }

  /**
   * @return Returns the filteredTriggersWrapperList.
   */
  public List getFilteredTriggersWrapperList()
  {
    return filteredTriggersWrapperList;
  }

  /**
   * @param filteredTriggerWrapperList The filteredTriggersWrapperList to set.
   */
  public void setFilteredTriggersWrapperList(List filteredTriggersWrapperList)
  {
    this.filteredTriggersWrapperList = filteredTriggersWrapperList;
  }

  /**
   * @return Returns the isSelectAllJobsSelected.
   */
  public boolean isSelectAllJobsSelected()
  {
    return isSelectAllJobsSelected;
  }

  /**
   * @param isSelectAllJobsSelected
   *          The isSelectAllJobsSelected to set.
   */
  public void setSelectAllJobsSelected(boolean isSelectAllJobsSelected)
  {
    this.isSelectAllJobsSelected = isSelectAllJobsSelected;
  }

  /**
   * @return Returns the selectedClass.
   */
  public String getSelectedClass()
  {
    return selectedClass;
  }

  /**
   * @param selectedClass
   *          The selectedClass to set.
   */
  public void setSelectedClass(String selectedClass)
  {
    this.selectedClass = selectedClass;
  }

  /**
   * @return Returns the jobName.
   */
  public String getJobName()
  {
    return jobName;
  }

  /**
   * @param jobName
   *          The jobName to set.
   */
  public void setJobName(String jobName)
  {
    this.jobName = jobName;
  }

  /**
   * @return Returns the triggerName.
   */
  public String getTriggerName()
  {
    return triggerName;
  }

  /**
   * @param triggerName The triggerName to set.
   */
  public void setTriggerName(String triggerName)
  {
    this.triggerName = triggerName;
  }

  /**
   * @return Returns the triggerExpression.
   */
  public String getTriggerExpression()
  {
    return triggerExpression;
  }

  /**
   * @param triggerExpression The triggerExpression to set.
   */
  public void setTriggerExpression(String triggerExpression)
  {
    this.triggerExpression = triggerExpression;
  }

  /**
   * @return Returns the jobClasses.
   */
  public Map getJobClasses()
  {
    return schedulerManager.getQrtzQualifiedJobs();
  }

  /**
   * @return Returns the schedulerManager.
   */
  public SchedulerManager getSchedulerManager()
  {
    return schedulerManager;
  }

  /**
   * @param schedulerManager
   *          The schedulerManager to set.
   */
  public void setSchedulerManager(SchedulerManager schedulerManager)
  {
    this.schedulerManager = schedulerManager;
  }

  /**
   * @return Returns the jobDetailWrapperList.
   */
  public List getJobDetailWrapperList()
  {
    return jobDetailWrapperList;
  }

  /**
   * @param jobDetailWrapperList
   *          The jobDetailWrapperList to set.
   */
  public void setJobDetailWrapperList(List jobDetailWrapperList)
  {
    this.jobDetailWrapperList = jobDetailWrapperList;
  }

  /**
   * @return Returns the filteredJobDetailWrapperList.
   */
  public List getFilteredJobDetailWrapperList()
  {
    return filteredJobDetailWrapperList;
  }

  /**
   * @param filteredJobDetailWrapperList
   *          The filteredJobDetailWrapperList to set.
   */
  public void setFilteredJobDetailWrapperList(List filteredJobDetailWrapperList)
  {
    this.filteredJobDetailWrapperList = filteredJobDetailWrapperList;
  }

  /**
   * @return Returns the selectedJobDetailWrapper.
   */
  public JobDetailWrapper getSelectedJobDetailWrapper()
  {
    return selectedJobDetailWrapper;
  }

  /**
   * @param selectedJobDetailWrapper
   *          The selectedJobDetailWrapper to set.
   */
  public void setSelectedJobDetailWrapper(
      JobDetailWrapper selectedJobDetailWrapper)
  {
    this.selectedJobDetailWrapper = selectedJobDetailWrapper;
  }
  
  /**
   * This method runs the current job only once, right now
   * @return int 0 if it's not running, 1 if it is, 2 if there is an error
   */
  public int getSelectedJobRunning()
  {
     Scheduler scheduler = schedulerManager.getScheduler();
     if (scheduler == null)
     {
       LOG.error("Scheduler is down!");
       return 2;
     }
     try
     {
        List executingJobs = scheduler.getCurrentlyExecutingJobs();
        
        for(Iterator i = executingJobs.iterator(); i.hasNext(); ) {
           JobExecutionContext jobExecContext = (JobExecutionContext)i.next();
           if(selectedJobDetailWrapper.getJobDetail().getFullName().equals(
                    jobExecContext.getJobDetail().getFullName()) )
              return 1;
        }
       return 0;
     }
     catch (Exception e)
     {
       LOG.error("Failed to trigger job now");
       return 2;
     }
  }

  public String processCreateJob()
  {
    Scheduler scheduler = schedulerManager.getScheduler();
    if (scheduler == null)
    {
      LOG.error("Scheduler is down!");
      return "error";
    }
    try
    {
       JobDetail jd = null;
       JobBeanWrapper job = getSchedulerManager().getJobBeanWrapper(selectedClass);
       if (job != null) {
          jd = new JobDetail(jobName, Scheduler.DEFAULT_GROUP,
             job.getJobClass(), false, true, true);
          jd.getJobDataMap().put(JobBeanWrapper.SPRING_BEAN_NAME, job.getBeanId());
          jd.getJobDataMap().put(JobBeanWrapper.JOB_TYPE, job.getJobType());
       }
       else {
          jd = new JobDetail(jobName, Scheduler.DEFAULT_GROUP,
             Class.forName(selectedClass.toString()), false, true, true);
       }
      scheduler.addJob(jd, false);
      processRefreshJobs();
      return "jobs";
    }
    catch (Exception e)
    {
      LOG.error("Failed to create job");
      return "error";
    }
  }

  public String processCreateTrigger()
  {
    Scheduler scheduler = schedulerManager.getScheduler();
    if (scheduler == null)
    {
      LOG.error("Scheduler is down!");
      return "error";
    }
    try
    {
      Trigger trigger = new CronTrigger(triggerName, Scheduler.DEFAULT_GROUP,
          selectedJobDetailWrapper.getJobDetail().getName(),
          Scheduler.DEFAULT_GROUP, triggerExpression);
      scheduler.scheduleJob(trigger);
      TriggerWrapper tempTriggerWrapper = new TriggerWrapperImpl();
      tempTriggerWrapper.setTrigger(trigger);
      selectedJobDetailWrapper.getTriggerWrapperList().add(tempTriggerWrapper);
      int currentTriggerCount = selectedJobDetailWrapper.getTriggerCount()
          .intValue();
      selectedJobDetailWrapper.setTriggerCount(new Integer(
          currentTriggerCount + 1));

      triggerName = null;
      triggerExpression = null;
      return "edit_triggers";
    }
    catch (Exception e)
    {
      triggerName = null;
      triggerExpression = null;
      LOG.error("Failed to create trigger");
      return "error";
    }
  }

  public String processSelectAllJobs()
  {

    isSelectAllJobsSelected = !isSelectAllJobsSelected;
    processRefreshJobs();
    return "jobs";
  }

  public String processSelectAllTriggers()
  {

    isSelectAllTriggersSelected = !isSelectAllTriggersSelected;
    for (Iterator i = selectedJobDetailWrapper.getTriggerWrapperList()
        .iterator(); i.hasNext();)
    {
      if (isSelectAllTriggersSelected)
      {
        ((TriggerWrapper) i.next()).setIsSelected(true);
      }
      else
      {
        ((TriggerWrapper) i.next()).setIsSelected(false);
      }
    }
    return "edit_triggers";
  }

  public String processDeleteJobs()
  {
    try
    {
      for (Iterator i = filteredJobDetailWrapperList.iterator(); i.hasNext();)
      {
        JobDetailWrapper jobDetailWrapper = (JobDetailWrapper) i.next();
        schedulerManager.getScheduler().deleteJob(
            jobDetailWrapper.getJobDetail().getName(), Scheduler.DEFAULT_GROUP);
      }
    }
    catch (SchedulerException e)
    {
      LOG.error("Scheduler Down");
    }
    processRefreshJobs();
    return "jobs";
  }

  public String processDeleteTriggers()
  {
    try
    {
      TriggerWrapper triggerWrapper;
      for (Iterator i = filteredTriggersWrapperList.iterator(); i.hasNext();)
      {
        triggerWrapper = (TriggerWrapper) i.next();
        schedulerManager.getScheduler().unscheduleJob(
            triggerWrapper.getTrigger().getName(), Scheduler.DEFAULT_GROUP);
        selectedJobDetailWrapper.getTriggerWrapperList().remove(triggerWrapper);
      }
    }
    catch (SchedulerException e)
    {
      LOG.error("Scheduler Down");
    }
    return "edit_triggers";
  }

  public String processRefreshJobs()
  {
    try
    {
      Scheduler scheduler = schedulerManager.getScheduler();
      String[] jobNames = scheduler.getJobNames(Scheduler.DEFAULT_GROUP);
      jobDetailWrapperList = new ArrayList();
      for (int i = 0; i < jobNames.length; i++)
      {
        JobDetailWrapper jobDetailWrapper = new JobDetailWrapperImpl();
        jobDetailWrapper.setJobDetail(scheduler.getJobDetail(jobNames[i],
            Scheduler.DEFAULT_GROUP));
        Trigger[] triggerArr = scheduler.getTriggersOfJob(jobNames[i],
            Scheduler.DEFAULT_GROUP);
        List triggerWrapperList = new ArrayList();
        TriggerWrapper tw;
        for (int j = 0; j < triggerArr.length; j++)
        {
          tw = new TriggerWrapperImpl();
          tw.setTrigger(triggerArr[j]);
          triggerWrapperList.add(tw);
        }

        jobDetailWrapper.setTriggerWrapperList(triggerWrapperList);
        jobDetailWrapperList.add(jobDetailWrapper);
      }
    }
    catch (SchedulerException e)
    {
      LOG.error("scheduler error while getting job detail");
    }

    // test for select all
    if (isSelectAllJobsSelected)
    {
      for (Iterator i = jobDetailWrapperList.iterator(); i.hasNext();)
      {
        if (isSelectAllJobsSelected)
        {
          ((JobDetailWrapper) i.next()).setIsSelected(true);
        }
        else
        {
          ((JobDetailWrapper) i.next()).setIsSelected(false);
        }
      }
    }
    return "jobs";
  }
  
  /**
   * This method runs the current job only once, right now
   * @return String
   */
  public String processRunJobNow()
  {
     Scheduler scheduler = schedulerManager.getScheduler();
     if (scheduler == null)
     {
       LOG.error("Scheduler is down!");
       return "error";
     }
     try
     {
       
       scheduler.triggerJob(
             selectedJobDetailWrapper.getJobDetail().getName(), 
             selectedJobDetailWrapper.getJobDetail().getGroup());

       return "success";
     }
     catch (Exception e)
     {
       LOG.error("Failed to trigger job now");
       return "error";
     }
  }

  public String processRefreshFilteredJobs()
  {
    filteredJobDetailWrapperList = new ArrayList();
    for (Iterator i = jobDetailWrapperList.iterator(); i.hasNext();)
    {
      JobDetailWrapper jobDetailWrapper = (JobDetailWrapper) i.next();
      if (jobDetailWrapper.getIsSelected())
      {
        filteredJobDetailWrapperList.add(jobDetailWrapper);
      }
    }
    return "delete_jobs";
  }

  public String processRefreshFilteredTriggers()
  {
    filteredTriggersWrapperList = new ArrayList();
    for (Iterator i = selectedJobDetailWrapper.getTriggerWrapperList()
        .iterator(); i.hasNext();)
    {
      TriggerWrapper triggerWrapper = (TriggerWrapper) i.next();
      if (triggerWrapper.getIsSelected())
      {
        filteredTriggersWrapperList.add(triggerWrapper);
      }
    }
    return "delete_triggers";
  }

  public void validateJobName(FacesContext context, UIComponent component,
      Object value)
  {
    if (value != null)
    {
      try
      {
        JobDetail jd = schedulerManager.getScheduler().getJobDetail(
            (String) value, Scheduler.DEFAULT_GROUP);
        if (jd != null)
        {
          FacesMessage message = new FacesMessage("Existing Job Name");
          message.setSeverity(FacesMessage.SEVERITY_WARN);
          throw new ValidatorException(message);
        }
      }
      catch (SchedulerException e)
      {
        LOG.error("Scheduler down!");
      }
    }
  }

  public void validateTriggerName(FacesContext context, UIComponent component,
      Object value)
  {
    if (value != null)
    {
      try
      {
        Trigger trigger = schedulerManager.getScheduler().getTrigger(
            (String) value, Scheduler.DEFAULT_GROUP);
        if (trigger != null)
        {
          FacesMessage message = new FacesMessage("Existing Trigger Name");
          message.setSeverity(FacesMessage.SEVERITY_WARN);
          throw new ValidatorException(message);
        }
      }
      catch (SchedulerException e)
      {
        LOG.error("Scheduler down!");
      }
    }
  }

  public void validateTriggerExpression(FacesContext context,
      UIComponent component, Object value)
  {
    if (value != null)
    {
      try
      {
        String expression = (String) value;
        CronTrigger trigger = new CronTrigger();
        trigger.setCronExpression(expression);

        // additional checks 
        // quartz does not check for more than 7 tokens in expression
        String[] arr = expression.split("\\s");
        if (arr.length > 7)
        {
          throw new ParseException("Expression has more than 7 tokens", 7);
        }

        //(check that last 2 entries are not both * or ? 
        String trimmed_expression = expression.replaceAll("\\s", ""); // remove whitespace
        if (trimmed_expression.endsWith(CRON_CHECK_ASTERISK)
            || trimmed_expression.endsWith(CRON_CHECK_QUESTION_MARK))
        {
          throw new ParseException("Cannot End in * * or ? ?", 1);
        }
      }
      catch (ParseException e)
      {
        // not giving a detailed message to prevent line wraps
        FacesMessage message = new FacesMessage("Parse Exception");
        message.setSeverity(FacesMessage.SEVERITY_WARN);
        throw new ValidatorException(message);
      }
    }
  }

  public void editTriggersListener(ActionEvent e)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map requestParams = context.getExternalContext().getRequestParameterMap();
    String jobName = (String) requestParams.get("jobName");

    //loop through jobDetailWrapperList finding the one selected by user.
    for (Iterator i = jobDetailWrapperList.iterator(); i.hasNext();)
    {
      JobDetailWrapper jobDetailWrapper = (JobDetailWrapper) i.next();
      if (jobDetailWrapper.getJobDetail().getName().equals(jobName))
      {
        selectedJobDetailWrapper = jobDetailWrapper;
        break;
      }
    }
  }

   public Map getBeanJobs() {
      Map beanJobs = new Hashtable();
      Map serverJobs = getSchedulerManager().getBeanJobs();
      for (Iterator i=serverJobs.keySet().iterator();i.hasNext();) {
         Object job = i.next();
         beanJobs.put(job, job);
      }
      return beanJobs;
   }

}

