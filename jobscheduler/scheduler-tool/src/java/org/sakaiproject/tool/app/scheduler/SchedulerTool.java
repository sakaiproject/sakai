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
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.JobExecutionContext;
import org.sakaiproject.api.app.scheduler.ConfigurableJobBeanWrapper;
import org.sakaiproject.api.app.scheduler.ConfigurableJobProperty;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidationException;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidator;
import org.sakaiproject.api.app.scheduler.JobDetailWrapper;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.api.app.scheduler.TriggerWrapper;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.api.app.scheduler.events.TriggerEventManager;
import org.sakaiproject.component.app.scheduler.JobDetailWrapperImpl;
import org.sakaiproject.component.app.scheduler.TriggerWrapperImpl;
import org.sakaiproject.util.ResourceLoader;

public class SchedulerTool
{

  private static final Log LOG = LogFactory.getLog(SchedulerTool.class);

  private static final String CRON_CHECK_ASTERISK = "**";
  private static final String CRON_CHECK_QUESTION_MARK = "??";

  private SchedulerManager schedulerManager;
  private String jobName;
  private String triggerName;
  private String triggerExpression;
  private String selectedClass;

  private List<JobDetailWrapper> jobDetailWrapperList;
  private List<JobDetailWrapper> filteredJobDetailWrapperList;
  private JobDetailWrapper selectedJobDetailWrapper;
  private boolean isSelectAllJobsSelected = false;
  private boolean isSelectAllTriggersSelected = false;

  private List<TriggerWrapper> filteredTriggersWrapperList;

  private LinkedList<String> configurableJobErrorMessages = null;
  private ConfigurableJobBeanWrapper configurableJobBeanWrapper;
  private Map<String, String> configurableJobResources;
  private List<ConfigurablePropertyWrapper> configurableJobProperties;
  private JobDetail jobDetail;
  private ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.scheduler.bundle.Messages");
  private TriggerWrapper triggerWrapper = null;

  private TriggerEventManager
      triggerEventManager = null;
  private EventPager evtPager = new EventPager();

  public SchedulerTool()
  {
  }

  public void setTriggerEventManager (TriggerEventManager tem)
  {
      triggerEventManager = tem;

      evtPager.setTriggerEventManager(triggerEventManager);
  }

  public TriggerEventManager getTriggerEventManager()
  {
      return triggerEventManager;
  }

  public EventPager getEventPager()
  {
      return evtPager;
  }
    
  /**
   * @return Returns the filteredTriggersWrapperList.
   */
  public List<TriggerWrapper> getFilteredTriggersWrapperList()
  {
    return filteredTriggersWrapperList;
  }

  /**
   * @param filteredTriggersWrapperList The filteredTriggersWrapperList to set.
   */
  public void setFilteredTriggersWrapperList(List<TriggerWrapper> filteredTriggersWrapperList)
  {
    this.filteredTriggersWrapperList = filteredTriggersWrapperList;
  }

    /**
     * Returns a List of ConfigurablePropertyWrapper objects which correspond to the properties which
     * can be configured for the current ConfigurableJobBeanWrapper. {@link #setConfigurableJobBeanWrapper(ConfigurableJobBeanWrapper)}
     * must be called first, or this method will return null.
     *
     * @return List of ConfigurablePropertyWrappers, or null.
     */
  public List<ConfigurablePropertyWrapper> getConfigurableProperties()
  {
      return configurableJobProperties;
  }

    /**
     * This method runs internally to refresh the set of ConfigurablePropertyWrappers whenever
     * {@link #setConfigurableJobBeanWrapper(ConfigurableJobBeanWrapper)} or
     * {@link #setJobDetail(JobDetail)} is called. {@link getConfigurableJobBeanWrapper()} must not be null
     * for this operation to succeed.
     */
  private void refreshProperties ()
  {
      final ConfigurableJobBeanWrapper
          job = getConfigurableJobBeanWrapper();

      if (job == null)
      {
          configurableJobProperties = null;
          return;
      }
      
      final Set<ConfigurableJobProperty>
          props = job.getConfigurableJobProperties();

      final JobDetail
          jd = getJobDetail();

      final JobDataMap
          dataMap = (jd != null) ? jd.getJobDataMap() : null;


      if (configurableJobResources == null)
      {
          LOG.error ("no resource bundle provided for jobs of type: " + job.getJobType() + ". Labels will not be rendered correctly in the scheduler UI");
      }


      //create a List of jobs, b/c JSF can't handle Sets as a backing bean for a dataTable
      if (props != null)
      {
          configurableJobProperties = new LinkedList<ConfigurablePropertyWrapper>();

          for (ConfigurableJobProperty prop : props)
          {
              ConfigurablePropertyWrapper
                  wrapper = new ConfigurablePropertyWrapper();
              String
                  value = null;

              wrapper.setJobProperty(prop);

              if (dataMap == null || (value = (String) dataMap.get(prop.getLabelResourceKey())) == null)
              {
                  wrapper.setValue (prop.getDefaultValue());
              }
              else
              {
                  wrapper.setValue (value);
              }

              configurableJobProperties.add(wrapper);


              if (configurableJobResources != null)
              {
                  //check for resource strings for label and desc - warn if they are not present
                  final ConfigurableJobProperty
                      property = wrapper.getJobProperty();
                  final String
                      labelKey = property.getLabelResourceKey(),
                      descKey = property.getDescriptionResourceKey();

                  if (labelKey == null)
                  {
                      LOG.error ("no resource key provided for property label - NullPointerExceptions may occur in scheduler when processing jobs of type " + job.getJobType());
                  }
                  else if (configurableJobResources.get(labelKey) == null)
                  {
                      LOG.warn("no resource string provided for the property label key '" + labelKey + "' for the job type " + job.getJobType());
                  }

                  if (descKey == null)
                  {
                      LOG.warn ("no resource key provided for property description in job type " + job.getJobType());
                  }
                  else if (configurableJobResources.get(descKey) == null)
                  {
                      LOG.warn("no resource string provided for the property description key '" + descKey + "' for the job type " + job.getJobType());
                  }
              }
          }
      }
  }

    /**
     *  Sets the JobDetail object for the job presently being editted or sor which properties or triggers are being
     *  editted.
     *
     * @param detail
     */
  public void setJobDetail (JobDetail detail)
  {
      jobDetail = detail;

      refreshProperties();
  }

    /**
     *  Returns the JobDetail object for the job presently being editted or sor which properties or triggers are being
     *  editted.
     *
     * @returns JobDetail
     */
  public JobDetail getJobDetail()
  {
      return jobDetail;
  }

    /**
     * Sets the TriggerWrapper being editted.
     *
     * @param tw
     */
  public void setTriggerWrapper (TriggerWrapper tw)
  {
	  triggerWrapper = tw;
  }

    /**
     * Returns the TriggerWrapper being editted.
     *
     * @return TroggerWrapper
     */
  public TriggerWrapper getTriggerWrapper ()
  {
	  return triggerWrapper;
  }

    /**
     * Sets the ConfigurableJobBeanWrapper to establish the job whose properties are being editted
     * during a job or trigger creation process.
     *
     * @param job
     */
  public void setConfigurableJobBeanWrapper(ConfigurableJobBeanWrapper job)
  {
      configurableJobBeanWrapper = job;

      if (job != null)
      {
          final String
              rbBase = job.getResourceBundleBase();

          //process the ResourceBundle into a map, b/c JSF won't allow method calls like rb.getString()
          final ResourceBundle
              rb = ResourceBundle.getBundle(rbBase);

          if(rb != null)
          {
              configurableJobResources = new HashMap<String, String> ();

              final Enumeration
                  keyIt = rb.getKeys();

              while (keyIt.hasMoreElements())
              {
                  final String
                      key = (String)keyIt.nextElement();

                  configurableJobResources.put(key, rb.getString(key));
              }
          }
          else
          {
              configurableJobResources = null;
          }
      }
      else
      {
          configurableJobResources = null;
      }
      refreshProperties();
  }

    /**
     * Returns the ConfigurableJobBeanWrapper currently the focus for editting properties either to create a job
     * or to schedule a trigger for a job.
     *
     * @return ConfigurableJobBeanWrapper
     */
  public ConfigurableJobBeanWrapper getConfigurableJobBeanWrapper ()
  {
      return configurableJobBeanWrapper;
  }

    /**
     * Returns the resource map which will provide labels for the ConfigurableJobBeanWrapper currently the focus
     * of the job or trigger creation process. This is a read-only property which is set by {@link #setConfigurableJobBeanWrapper(ConfigurableJobBeanWrapper)}
     *
     * @return
     */
  public Map<String, String> getConfigurableJobResources()
  {
      return configurableJobResources;
  }

    /**
     * Returns validation errors which have occured during the editting of properties for a ConfigurableJobBeanWrapper.
     *
     * @return List of validation error Strings
     */
  public List<String> getConfigurableJobErrorMessages()
  {
      return configurableJobErrorMessages;
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
  public List<JobDetailWrapper> getJobDetailWrapperList()
  {
    return jobDetailWrapperList;
  }

  /**
   * @param jobDetailWrapperList
   *          The jobDetailWrapperList to set.
   */
  public void setJobDetailWrapperList(List<JobDetailWrapper> jobDetailWrapperList)
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

    /**
     * Convenience method for creating a JobDetail object from a JobBeanWrapper. The JobDetail object is
     * used to actually create a job within Quartz, and is also tracked by the {@link getJobDetail()} property
     * for use during the property editting process.
     *
     * @param job
     * @return JobDetail object constructed from the job argument
     */
  private JobDetail createJobDetail (JobBeanWrapper job)
  {
      JobDetail
          jd = new JobDetail (jobName, Scheduler.DEFAULT_GROUP, job.getJobClass(), false, true, true);
      JobDataMap
          map = jd.getJobDataMap();

      map.put(JobBeanWrapper.SPRING_BEAN_NAME, job.getBeanId());
      map.put(JobBeanWrapper.JOB_TYPE, job.getJobType());

      return jd;
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
        //get a JobDetail object in case one is already in the Session
        //  (eg. if we have returned her from a validation error
       JobDetail jd = getJobDetail();
       JobBeanWrapper job = getSchedulerManager().getJobBeanWrapper(selectedClass);

       if (job != null)
       {
           // create a new JobDetail object for this job
           jd = createJobDetail(job);

    	   //we have a job, so check to see if properties need to be set    	   
           if (ConfigurableJobBeanWrapper.class.isAssignableFrom(job.getClass()))
           {
        	   //this job is configurable, provide a screen to edit properties
               final ConfigurableJobBeanWrapper
                   configurableJob = (ConfigurableJobBeanWrapper)job;

               // prepare properties for use within the property configuration UI
               setConfigurableJobBeanWrapper (configurableJob);
               setJobDetail(jd);

               return "edit_properties";
           }
           else
           {
        	   //not a configurable job, create the job and move on
               setConfigurableJobBeanWrapper(null);
           }
       }
       else
       {
    	   // this is not a job configured via a JobBeanWrapper
    	   // assume the class is a Job and schedule its execution
          setConfigurableJobBeanWrapper(null);
          jd = new JobDetail(jobName, Scheduler.DEFAULT_GROUP,
             Class.forName(selectedClass.toString()), false, true, true);
       }
       
       // create the job and show the list of jobs
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

    /**
     * Processes the properties which have been set during creation of a job.
     *
     * @return
     */
  public String processSetProperties()
  {
      Scheduler scheduler = schedulerManager.getScheduler();
      if (scheduler == null)
      {
        LOG.error("Scheduler is down!");
        return "error";
      }

      configurableJobErrorMessages = new LinkedList<String>();

      try
      {
          JobDetail
              jd = getJobDetail();
          JobBeanWrapper
              job = getSchedulerManager().getJobBeanWrapper(selectedClass);

          final ConfigurableJobBeanWrapper
              configurableJob = getConfigurableJobBeanWrapper();

          final List<ConfigurablePropertyWrapper>
              properties = getConfigurableProperties();

          final ResourceBundle
              jobRb = ResourceBundle.getBundle(configurableJob.getResourceBundleBase());

          final ConfigurableJobPropertyValidator
              validator = configurableJob.getConfigurableJobPropertyValidator();

          if (jd != null)
          {
              final JobDataMap
                  dataMap = jd.getJobDataMap();

              for (ConfigurablePropertyWrapper wrapper : properties)
              {
                  final ConfigurableJobProperty
                      property = wrapper.getJobProperty();
                  final String
                      label = property.getLabelResourceKey(),
                      value = wrapper.getValue();

                  if (property.isRequired() && (value == null || value.trim().length() == 0))
                  {
                      String
                          propName = (jobRb != null)?jobRb.getString(label):label,
                          msg = null;

                      if (propName == null)
                          propName = label;

                      try
                      {
                          msg = rb.getString("properties_required");
                      }
                      catch (MissingResourceException mre)
                      {
                          msg = "&lt;Missing resource string: properties_required&gt;";
                      }

                      configurableJobErrorMessages.add(msg + ": " + propName);
                  }
                  else
                  {
                      try
                      {
                          validator.assertValid(label, value);
                      }
                      catch (ConfigurableJobPropertyValidationException cjpve)
                      {
                          String
                              errorKey = cjpve.getMessage(),
                              errorMessage = jobRb.getString(errorKey);

                          configurableJobErrorMessages.add ((errorMessage == null)?errorKey:errorMessage);
                          continue;
                      }
                      dataMap.put(property.getLabelResourceKey(), value);
                  }
              }

              if (!configurableJobErrorMessages.isEmpty())
              {
                  return null;
              }
          }
          else
          {
              setJobDetail (createJobDetail(job));
              return null;
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

    /**
     * Convenience method for scheduling a Trigger with the Quartz Scheduler object.
     * @param wrapper
     * @throws SchedulerException
     */
  private void scheduleTrigger(TriggerWrapper wrapper) throws SchedulerException
  {
	  Trigger
	  	trigger = wrapper.getTrigger();
	  
	  Scheduler 
	  	scheduler = schedulerManager.getScheduler();
	  
      scheduler.scheduleJob(trigger);
      selectedJobDetailWrapper.getTriggerWrapperList().add(wrapper);
      int currentTriggerCount = selectedJobDetailWrapper.getTriggerCount()
          .intValue();
      selectedJobDetailWrapper.setTriggerCount(Integer.valueOf(
          currentTriggerCount + 1));
  }

    /**
     * Creates a trigger for the currently selected JobDetailWrapper. This method redirects to UI
     * to override configured properties if the Job is configurable.
     *
     * @return
     */
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
    	JobDetail
    		jd = selectedJobDetailWrapper.getJobDetail();

    	Trigger trigger = new CronTrigger(triggerName, Scheduler.DEFAULT_GROUP,
    		  						    jd.getName(),
    		  						    Scheduler.DEFAULT_GROUP, triggerExpression);
    	
    	TriggerWrapper tempTriggerWrapper = new TriggerWrapperImpl();
    	tempTriggerWrapper.setTrigger(trigger);
      
      	JobBeanWrapper
      		job = getSchedulerManager().getJobBeanWrapper(selectedJobDetailWrapper.getJobType());

      	if (job != null)
      	{
		    if (ConfigurableJobBeanWrapper.class.isAssignableFrom(job.getClass()))
		    {
		        final ConfigurableJobBeanWrapper
		            configurableJob = (ConfigurableJobBeanWrapper)job;
		
                setJobDetail (jd);
		        setConfigurableJobBeanWrapper (configurableJob);
		        setTriggerWrapper (tempTriggerWrapper);
		        
		        return "edit_trigger_properties";
		    }
		    else
		    {
		    	setConfigurableJobBeanWrapper(null);
                setJobDetail(null);
		    	setTriggerWrapper(null);
		    }
		}
      	
      	scheduleTrigger (tempTriggerWrapper);

      	return "edit_triggers";
    }
    catch (Exception e)
    {
      LOG.error("Failed to create trigger");
      return "error";
    }
    finally
    {
    	triggerName = null;
    	triggerExpression = null;
    }
  }

    /**
     * Validates and sets the properties for a Trigger once the property configuration UI has been completed.
     *
     * @return
     */
  public String processSetTriggerProperties ()
  {
      Scheduler scheduler = schedulerManager.getScheduler();
      if (scheduler == null)
      {
        LOG.error("Scheduler is down!");
        return "error";
      }

      configurableJobErrorMessages = new LinkedList<String>();

      try
      {
      	  final JobDetail 
      	  	  jd = selectedJobDetailWrapper.getJobDetail();
      	  
          final ConfigurableJobBeanWrapper
              configurableJob = getConfigurableJobBeanWrapper();

          final TriggerWrapper
      	  	  triggerWrapper = getTriggerWrapper();
          
          final Trigger
          	  trigger = triggerWrapper.getTrigger();

          final List<ConfigurablePropertyWrapper>
              properties = getConfigurableProperties();

          final ResourceBundle
              jobRb = ResourceBundle.getBundle(configurableJob.getResourceBundleBase());

          final ConfigurableJobPropertyValidator
              validator = configurableJob.getConfigurableJobPropertyValidator();
          
          final JobDataMap
              dataMap = trigger.getJobDataMap();

          for (ConfigurablePropertyWrapper wrapper : properties)
          {
              final ConfigurableJobProperty
                  property = wrapper.getJobProperty();
              final String
                  label = property.getLabelResourceKey(),
                  value = wrapper.getValue();

              if (property.isRequired() && (value == null || value.trim().length() == 0))
              {
                  String
                      propName = (jobRb != null)?jobRb.getString(label):label,
                      msg = null;

                  if (propName == null)
                      propName = label;

                  try
                  {
                      msg = rb.getString("properties_required");
                  }
                  catch (MissingResourceException mre)
                  {
                      msg = "&lt;Missing resource string: properties_required&gt;";
                  }

                  configurableJobErrorMessages.add(msg + ": " + propName);
              }
              else
              {
                  try
                  {
                      validator.assertValid(label, value);
                  }
                  catch (ConfigurableJobPropertyValidationException cjpve)
                  {
                      String
                          errorKey = cjpve.getMessage(),
                          errorMessage = jobRb.getString(errorKey);

                      configurableJobErrorMessages.add ((errorMessage == null)?errorKey:errorMessage);
                      continue;
                  }
                  dataMap.put(property.getLabelResourceKey(), value);
              }
          }

          if (!configurableJobErrorMessages.isEmpty())
          {
              return null;
          }
          
          scheduleTrigger(triggerWrapper);
      }
      catch (Exception e)
      {
          LOG.error("Failed to create job");
          return "error";          
      }

	  return "edit_triggers";
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
      for (Iterator<TriggerWrapper> i = filteredTriggersWrapperList.iterator(); i.hasNext();)
      {
        triggerWrapper = (TriggerWrapper) i.next();
        schedulerManager.getScheduler().unscheduleJob(
            triggerWrapper.getTrigger().getName(), triggerWrapper.getTrigger().getGroup());
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
        List<TriggerWrapper> triggerWrapperList = new ArrayList<TriggerWrapper>();
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
      for (Iterator<JobDetailWrapper> i = jobDetailWrapperList.iterator(); i.hasNext();)
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
     * Determines if the Job has configuration properties which might need to be overriden when the
     * job is run. If so this redirects to a property configuration screen. Otherwise it simply continues
     * to a confirmation screen.
     * 
     * @return
     */
  public String processPrepRunJobNow()
  {
      Scheduler scheduler = schedulerManager.getScheduler();
      if (scheduler == null)
      {
        LOG.error("Scheduler is down!");
        return "error";
      }

      try
      {
          JobDetail
              jd = selectedJobDetailWrapper.getJobDetail();

          JobBeanWrapper
              job = getSchedulerManager().getJobBeanWrapper(selectedJobDetailWrapper.getJobType());

          if (job != null)
          {
              if (ConfigurableJobBeanWrapper.class.isAssignableFrom(job.getClass()))
              {
                  final ConfigurableJobBeanWrapper
                      configurableJob = (ConfigurableJobBeanWrapper)job;

                  setJobDetail (jd);
                  setConfigurableJobBeanWrapper (configurableJob);

                  return "edit_runnow_properties";
              }
              else
              {
                  setConfigurableJobBeanWrapper(null);
                  setJobDetail(null);
              }
          }
          return "run_job_confirm";
      }
      catch (Exception e)
      {
        LOG.error("Failed to run job now");
        return "error";
      }
  }

    /**
     * Processes the properties set for a Job that will be run now.
     *
     * @return
     */
  public String processSetRunNowProperties()
  {
      Scheduler scheduler = schedulerManager.getScheduler();
      if (scheduler == null)
      {
        LOG.error("Scheduler is down!");
        return "error";
      }
      try
      {
          JobDetail
              jd = getJobDetail();

          JobBeanWrapper
              job = getSchedulerManager().getJobBeanWrapper(selectedJobDetailWrapper.getJobType());

          if (job != null)
          {
              if (ConfigurableJobBeanWrapper.class.isAssignableFrom(job.getClass()))
              {
                  configurableJobErrorMessages = new LinkedList<String>();

                  final ConfigurableJobBeanWrapper
                      configurableJob = (ConfigurableJobBeanWrapper) job;

                  final List<ConfigurablePropertyWrapper>
                      properties = getConfigurableProperties();

                  final ResourceLoader
                      jobRb = new ResourceLoader(configurableJob.getResourceBundleBase());

                  final ConfigurableJobPropertyValidator
                      validator = configurableJob.getConfigurableJobPropertyValidator();

                  for (ConfigurablePropertyWrapper wrapper : properties)
                  {
                      final ConfigurableJobProperty
                          property = wrapper.getJobProperty();
                      final String
                          label = property.getLabelResourceKey(),
                          value = wrapper.getValue();

                      if (property.isRequired() && (value == null || value.trim().length() == 0))
                      {
                          String
                              propName = (jobRb != null)?jobRb.getString(label):label,
                              msg = null;

                          if (propName == null)
                              propName = label;

                          try
                          {
                              msg = rb.getString("properties_required");
                          }
                          catch (MissingResourceException mre)
                          {
                              msg = "&lt;Missing resource string: properties_required&gt;";
                          }

                          configurableJobErrorMessages.add(msg + ": " + propName);
                      }
                      else
                      {
                          try
                          {
                              validator.assertValid(label, value);
                          }
                          catch (ConfigurableJobPropertyValidationException cjpve)
                          {
                              String
                                  errorKey = cjpve.getMessage(),
                                  errorMessage = jobRb.getString(errorKey);

                              configurableJobErrorMessages.add ((errorMessage==null)?errorKey:errorMessage);
                              continue;
                          }
                      }
                  }

                  if (!configurableJobErrorMessages.isEmpty())
                  {
                      return null;
                  }
              }
          }
          return "run_now_confirm";
      }
      catch (Exception e)
      {
          LOG.error("Failed to trigger job now");
          return "error";
      }
  }

  /**
   * This method runs the current job only once, right now. It will set properties on the Job execution if the
   * Job has configurable properties.
   * 
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
         JobDetail
             jd = getJobDetail();

         JobBeanWrapper
             job = getSchedulerManager().getJobBeanWrapper(selectedJobDetailWrapper.getJobType());

         JobDataMap
             dataMap = null;

         if (job != null)
         {
             if (ConfigurableJobBeanWrapper.class.isAssignableFrom(job.getClass()))
             {
                 configurableJobErrorMessages = new LinkedList<String>();
                 
                 final List<ConfigurablePropertyWrapper>
                     properties = getConfigurableProperties();

                 dataMap = new JobDataMap (jd.getJobDataMap());

                 for (ConfigurablePropertyWrapper wrapper : properties)
                 {
                     final ConfigurableJobProperty
                         property = wrapper.getJobProperty();
                     final String
                         label = property.getLabelResourceKey(),
                         value = wrapper.getValue();

                     dataMap.put(label, value);
                 }
             }
         }

         if (dataMap == null)
         {
             scheduler.triggerJob(selectedJobDetailWrapper.getJobDetail().getName(),
                                  selectedJobDetailWrapper.getJobDetail().getGroup());
         }
         else
         {
             scheduler.triggerJob(selectedJobDetailWrapper.getJobDetail().getName(),
                                  selectedJobDetailWrapper.getJobDetail().getGroup(),
                                  dataMap);
         }

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
    filteredJobDetailWrapperList = new ArrayList<JobDetailWrapper>();
    for (Iterator<JobDetailWrapper> i = jobDetailWrapperList.iterator(); i.hasNext();)
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
    filteredTriggersWrapperList = new ArrayList<TriggerWrapper>();
    for (Iterator<TriggerWrapper> i = selectedJobDetailWrapper.getTriggerWrapperList()
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
          if (((String) value).length() > 80) {
              FacesMessage message = new FacesMessage(rb.getString("too_long_job_name"));
              message.setSeverity(FacesMessage.SEVERITY_WARN);
              throw new ValidatorException(message);
          }
        JobDetail jd = schedulerManager.getScheduler().getJobDetail(
            (String) value, Scheduler.DEFAULT_GROUP);
        if (jd != null)
        {
          FacesMessage message = new FacesMessage(rb.getString("existing_job_name"));
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
          FacesMessage message = new FacesMessage(rb.getString("existing_trigger_name"));
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
        FacesMessage message = new FacesMessage(rb.getString("parse_exception"));
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

   public Map<String, String> getBeanJobs() {
      Map<String, String> beanJobs = new TreeMap<String, String>();
      Map<String, JobBeanWrapper> serverJobs = getSchedulerManager().getBeanJobs();
      for (Iterator<String> i=serverJobs.keySet().iterator();i.hasNext();) {
         String job = i.next();
         beanJobs.put(job, job);
      }
      return beanJobs;
   }

   public String processSetFilters()
   {
       getEventPager().setFilterEnabled(true);

       return "events";
   }

   public String processClearFilters()
   {
       getEventPager().setFilterEnabled(false);

       return "events";
   }

   public List<SelectItem> getScheduledJobs() throws SchedulerException
   {
       ArrayList<SelectItem>
           scheduledJobs = new ArrayList<SelectItem> ();
       String[]
           jArr = schedulerManager.getScheduler().getJobNames(Scheduler.DEFAULT_GROUP);

       for (int i = 0; i < jArr.length; i++)
       {
           scheduledJobs.add(new SelectItem(jArr[i]));
       }

       return scheduledJobs;
   }
}

