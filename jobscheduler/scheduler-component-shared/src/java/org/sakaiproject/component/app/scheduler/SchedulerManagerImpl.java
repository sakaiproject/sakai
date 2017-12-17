/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.quartz.spi.JobFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import org.springframework.context.Lifecycle;

import org.sakaiproject.api.app.scheduler.ConfigurableJobProperty;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidationException;
import org.sakaiproject.api.app.scheduler.ConfigurableJobPropertyValidator;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.scheduler.jobs.SpringConfigurableJobBeanWrapper;
import org.sakaiproject.component.app.scheduler.jobs.SpringInitialJobSchedule;
import org.sakaiproject.component.app.scheduler.jobs.SpringJobBeanWrapper;
import org.sakaiproject.db.api.SqlService;

@Slf4j
public class SchedulerManagerImpl implements SchedulerManager, SchedulerFactory, Lifecycle
{
  public final static String
        SCHEDULER_LOADJOBS      = "scheduler.loadjobs";
  private DataSource dataSource;
  private String serverId;
  private Set<String> qrtzJobs;
  private Map<String, String> qrtzQualifiedJobs = new TreeMap<String, String>(); // map for SelectItems
  /** The properties file from the classpath */
  private String qrtzPropFile;
  /** The properties file from sakai.home */
  private String qrtzPropFileSakai;
  private TriggerListener globalTriggerListener;
  private Boolean autoDdl;
  private boolean startScheduler = true;
  private Map<String, JobBeanWrapper> beanJobs = new Hashtable<String, JobBeanWrapper>();

  private static final String JOB_INTERFACE = "org.quartz.Job";
  private static final String STATEFULJOB_INTERFACE = "org.quartz.StatefulJob";
  

  // Service dependencies
  private ServerConfigurationService serverConfigurationService;
  private SqlService sqlService;

  private Scheduler scheduler;
  private JobFactory jobFactory;

  private LinkedList<TriggerListener>
      globalTriggerListeners = new LinkedList<TriggerListener>();
  private LinkedList<JobListener>
      globalJobListeners = new LinkedList<JobListener>();

  private LinkedList<SpringInitialJobSchedule>
      initialJobSchedule = null;


  // Map from a spring bean ID to a job class.
  private HashMap<String,Class<? extends Job>> migration;

  public void init()
  {
    try
    {
      Properties qrtzProperties = initQuartzConfiguration();

      qrtzProperties.setProperty("org.quartz.scheduler.instanceId", serverId);

      // note: becuase job classes are jarred , it is impossible to iterate
      // through a directory by calling listFiles on a file object.
      // Therefore, we need the class list list from spring.

      // find quartz jobs from specified 'qrtzJobs' and verify they
      // that these jobs implement the Job interface
      Iterator<String> qrtzJobsIterator = qrtzJobs.iterator();
      while (qrtzJobsIterator.hasNext())
      {
        String className = (String) qrtzJobsIterator.next();
        Class cl = null;
        try
        {
          cl = Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
          log.warn("Could not locate class: " + className + " on classpath");
        }
        if (cl != null)
        {
          // check that each class implements the Job interface           
          if (doesImplementJobInterface(cl))
          {
            qrtzQualifiedJobs.put(cl.getName(), cl.getName());
          }
          else
          {
            log.warn("Class: " + className
                + " does not implement quartz Job interface");
          }
        }
      }
   // run ddl
      if (autoDdl.booleanValue()){
        try
        {
           sqlService.ddl(this.getClass().getClassLoader(), "quartz2");
        }
        catch (Throwable t)
        {
          log.warn(this + ".init(): ", t);
        }
      }

      boolean isInitialStartup = isInitialStartup(sqlService);
      if (isInitialStartup && autoDdl.booleanValue())
      {
    	  log.info("Performing initial population of the Quartz tables.");
    	  sqlService.ddl(this.getClass().getClassLoader(), "init_locks2");
      }
      /*
         Determine whether or not to load the jobs defined in the initialJobSchedules list. These jobs will be loaded
         under the following conditions:
            1) the server configuration property "scheduler.loadjobs" is "true"
            2) "scheduler.loadjobs" is "init" and this is the first startup for the scheduler (eg. this is a new Sakai instance)
         "scheduler.loadjobs" is set to "init" by default
       */
      String
          loadJobs = serverConfigurationService.getString(SCHEDULER_LOADJOBS, "init").trim();

      List<SpringInitialJobSchedule>
          initSchedules = getInitialJobSchedules();
      
      boolean
          loadInitSchedules = (initSchedules != null) && (initSchedules.size() > 0) &&
                                (("init".equalsIgnoreCase(loadJobs) && isInitialStartup) ||
                                 "true".equalsIgnoreCase(loadJobs));

      if (loadInitSchedules)
          log.debug ("Preconfigured jobs will be loaded");
      else
          log.debug ("Preconfigured jobs will not be loaded");
      
      


      // start scheduler and load jobs
      SchedulerFactory schedFactory = new StdSchedulerFactory(qrtzProperties);
      scheduler = schedFactory.getScheduler();
      scheduler.setJobFactory(jobFactory);

      // loop through persisted jobs removing both the job and associated
      // triggers for jobs where the associated job class is not found
      Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Scheduler.DEFAULT_GROUP));
      for (JobKey key : jobKeys) {
        try
        {
          JobDetail detail = scheduler.getJobDetail(key);
          String bean = detail.getJobDataMap().getString(JobBeanWrapper.SPRING_BEAN_NAME);
          // We now have jobs that don't explicitly reference a spring bean
          if (bean != null && !bean.isEmpty()) {
            Job job = (Job) ComponentManager.get(bean);
            if (job == null) {
                // See if we should be migrating this job.
                Class<? extends Job> newClass = migration.get(bean);
                if (newClass != null) {
                    JobDataMap jobDataMap = detail.getJobDataMap();
                    jobDataMap.remove(JobBeanWrapper.SPRING_BEAN_NAME);
                    JobDetail newJob = JobBuilder.newJob(newClass)
                            .setJobData(jobDataMap)
                            .requestRecovery(detail.requestsRecovery())
                            .storeDurably(detail.isDurable())
                            .withDescription(detail.getDescription())
                            .withIdentity(key).build();
                    // Update the existing job by replacing it with the same identity.
                    scheduler.addJob(newJob, true);
                    log.info("Migrated job of {} to {}", detail.getJobClass(), newClass);
                } else {
                    log.warn("scheduler cannot load class for persistent job:" + key);
                    scheduler.deleteJob(key);
                    log.warn("deleted persistent job:" + key);
                }
            }
          }
        }
        catch (SchedulerException e)
        {
          log.warn("scheduler cannot load class for persistent job:" + key);
          scheduler.deleteJob(key);
          log.warn("deleted persistent job:" + key);
        }
      }

      for (TriggerListener tListener : globalTriggerListeners)
      {
          scheduler.getListenerManager().addTriggerListener(tListener);
      }

      for (JobListener jListener : globalJobListeners)
      {
          scheduler.getListenerManager().addJobListener(jListener);
      }

      if (loadInitSchedules)
      {
          log.debug ("Loading preconfigured jobs");
          loadInitialSchedules();
      }

      //scheduler.addGlobalTriggerListener(globalTriggerListener);

    }
    catch (Exception e)
    {
      log.error("Failed to start scheduler.", e);
      throw new IllegalStateException("Scheduler cannot start!", e);
    }
  }

  /**
   * This loads the configurations for quartz.
   * It loads the defaults from the classpath and then loads override values from
   * sakai.home.
   * @return The quartz properties.
   * @throws IOException When we can't load the default values.
   */
  private Properties initQuartzConfiguration() throws IOException
  {
    InputStream propertiesInputStream = null;
    Properties properties = new Properties();
    // load the default quartz properties file
    // if this fails we want to propogate the error to stop startup.
    try
    {
      propertiesInputStream = this.getClass().getResourceAsStream(qrtzPropFile);
      properties.load(propertiesInputStream);
    }
    finally
    {
      if (propertiesInputStream != null)
      {
        try
        {
          propertiesInputStream.close();
        }
        catch (IOException e)
        {
          log.debug("Failed to close stream.", e);
        }
      }
    }
  
    // load the configuration out of sakai home
    // any failures here shouldn't result in startup failing.
    File file = new File(serverConfigurationService.getSakaiHomePath(), qrtzPropFileSakai);
    if (file.exists() && file.isFile()) {
      try
      {
        propertiesInputStream = new FileInputStream(file);
        properties.load(propertiesInputStream);
        log.info("Loaded extra configuration from: "+ file.getAbsolutePath());
      }
      catch (IOException e)
      {
        log.warn("Failed to load file: "+ file, e);
      }
      finally
      {
        if (propertiesInputStream != null)
        {
          try
          {
            propertiesInputStream.close();
          }
          catch (IOException e)
          {
            log.debug("Failed to close stream.", e);
          }
        }
      }
    }
    return properties;
  }

  private boolean doesImplementJobInterface(Class cl)
  {
    Class[] classArr = cl.getInterfaces();
    for (int i = 0; i < classArr.length; i++)
    {
    	if (classArr[i].getName().equals(JOB_INTERFACE) || 
    			classArr[i].getName().equals(STATEFULJOB_INTERFACE))
      {
        return true;
      }
    }
    return false;
  }

    /**
     * Runs an SQL select statement to determine if the Quartz lock rows exist in the database. If the rows do not exist
     * this method assumes this is the first time the scheduler has been started. The select statement will be defined
     * in the {vendor}/checkTables.sql file within the shared library deployed by this project. The statement should be
     * of the form "SELECT COUNT(*) from QUARTZ_LOCKS;". If the count is zero it is assumed this is a new install. 
     * If the count is non-zero it is assumed the QUARTZ_LOCKS table has been initialized and this is not a new install.
     *
     * @param sqlService
     * @return
     */
  private boolean isInitialStartup(SqlService sqlService)
  {
      String
          checkTablesScript = sqlService.getVendor() + "/checkTables.sql";
      ClassLoader
          loader = this.getClass().getClassLoader();
      String
          chkStmt = null;
      InputStream
          in = null;
      BufferedReader
          r = null;

      try
      {

          // find the resource from the loader
          in = loader.getResourceAsStream(checkTablesScript);

          r = new BufferedReader(new InputStreamReader(in));

          chkStmt = r.readLine();
      }
      catch (Exception e)
      {
          log.error("Could not read the file " + checkTablesScript + " to determine if this is a new installation. Preconfigured jobs will only be loaded if the server property scheduler.loadjobs is \"true\"", e);
          return false;
      }
      finally
      {
          try
          {
              r.close();
          }
          catch (Exception e){}
          try
          {
              in.close();
          }
          catch (Exception e){}
      }

      List<String> l = sqlService.dbRead(chkStmt);
      if (l != null && l.size() > 0) 
      {
    	  return (l.get(0).equalsIgnoreCase("0"));
      }
      else 
      {
    	  return false;
      }
  }

    /**
     * Loads jobs and schedules triggers for preconfigured jobs.
     */
  private void loadInitialSchedules()
  {
      for (SpringInitialJobSchedule sched : getInitialJobSchedules())
      {
          SpringJobBeanWrapper
              wrapper = sched.getJobBeanWrapper();

          log.debug ("Loading schedule for preconfigured job \"" + wrapper.getJobType() + "\"");

          JobDetail jd = JobBuilder.newJob(wrapper.getJobClass())
                  .withIdentity(sched.getJobName(), Scheduler.DEFAULT_GROUP)
                  .storeDurably()
                  .requestRecovery()
                  .build();

          JobDataMap map = jd.getJobDataMap();

          map.put(JobBeanWrapper.SPRING_BEAN_NAME, wrapper.getBeanId());
          map.put(JobBeanWrapper.JOB_TYPE, wrapper.getJobType());

          if (SpringConfigurableJobBeanWrapper.class.isAssignableFrom(wrapper.getClass()))
          {
              SpringConfigurableJobBeanWrapper
                  confJob = (SpringConfigurableJobBeanWrapper) wrapper;
              ConfigurableJobPropertyValidator
                  validator = confJob.getConfigurableJobPropertyValidator();
              Map<String, String>
                  conf = sched.getConfiguration();
              boolean
                  fail = false;

              for (ConfigurableJobProperty cProp : confJob.getConfigurableJobProperties())
              {
                  String
                      key = cProp.getLabelResourceKey(),
                      val = conf.get(key);

                  log.debug ("job property '" + key + "' is set to '" + val + "'");

                  if (val == null && cProp.isRequired())
                  {
                      val = cProp.getDefaultValue();

                      if (val == null)
                      {
                          log.error ("job property '" + key + "' is required but has no value; job '" + sched.getJobName() + "' of type '" + wrapper.getJobClass() + "' will not be configured");

                          fail = true;
                          break;
                      }

                      log.debug ("job property '" + key + "' set to default value '" + val + "'");
                  }

                  if (val != null)
                  {

                      try
                      {
                          validator.assertValid(key, val);
                      }
                      catch (ConfigurableJobPropertyValidationException cjpve)
                      {
                          log.error ("job property '" + key + "' was set to an invalid value '" + val + "'; job '" + sched.getJobName() + "' of type '" + wrapper.getJobClass() + "' will not be configured");

                          fail = true;
                          break;
                      }

                      map.put (key, val);
                  }

              }
              if (fail) continue;
          }

          try
          {
              scheduler.addJob(jd, false);
          }
          catch (SchedulerException e)
          {
              log.error ("Failed to schedule job '" + sched.getJobName() + "' of type '" + wrapper.getJobClass() + "'");
              continue;
          }

          Trigger trigger = null;
          trigger = TriggerBuilder.newTrigger()
                  .withIdentity(sched.getTriggerName(), Scheduler.DEFAULT_GROUP)
                  .forJob(jd.getKey())
                  .withSchedule(CronScheduleBuilder.cronSchedule(sched.getCronExpression()))
                  .build();

          try
          {
              scheduler.scheduleJob(trigger);
          }
          catch (SchedulerException e)
          {
              log.error ("Trigger could not be scheduled. Failed to schedule job '" + sched.getJobName() + "' of type '" + wrapper.getJobClass() + "'");
          }

      }
  }


  /**
   * @see org.sakaiproject.api.app.scheduler.SchedulerManager#destroy()
   */
  public void destroy()
  {

  }


  public List<SpringInitialJobSchedule> getInitialJobSchedules()
  {
      return initialJobSchedule;
  }

  public void setInitialJobSchedules(List<SpringInitialJobSchedule> jobSchedule)
  {
      if(jobSchedule == null || jobSchedule.size() < 1)
        return;
      
      this.initialJobSchedule = new LinkedList<SpringInitialJobSchedule> ();

      initialJobSchedule.addAll(jobSchedule);
  }
  /**
   * @deprecated use {@link #setGlobalTriggerListeners(Set<TriggerListener>)}
   * @return Returns the globalTriggerListener.
   */
  public TriggerListener getGlobalTriggerListener()
  {
    return globalTriggerListener;
  }

  /**
   * @deprecated use {@link #getGlobalTriggerListeners()}
   * @param globalTriggerListener The globalTriggerListener to set.
   */
  public void setGlobalTriggerListener(TriggerListener globalTriggerListener)
  {
    this.globalTriggerListener = globalTriggerListener;

      if (globalTriggerListeners != null)
      {
          globalTriggerListeners.addFirst(globalTriggerListener);
      }
  }

  public void setGlobalTriggerListeners (final List<TriggerListener> listeners)
  {
      globalTriggerListeners.clear();

      if (globalTriggerListener != null)
      {
          globalTriggerListeners.add(globalTriggerListener);
      }

      if (listeners != null)
      {
          globalTriggerListeners.addAll(listeners);
      }
  }

  public List<TriggerListener> getGlobalTriggerListeners()
  {
      return Collections.unmodifiableList(globalTriggerListeners);
  }

  public void setGlobalJobListeners (final List<JobListener> listeners)
  {
      globalJobListeners.clear();

      if (listeners != null)
      {
          globalJobListeners.addAll(listeners);
      }
  }

  public List<JobListener> getGlobalJobListeners()
  {
      return Collections.unmodifiableList(globalJobListeners);
  }

  /**
   * @return Returns the serverId.
   */
  public String getServerId()
  {
    return serverId;
  }

  /**
   * @param serverId The serverId to set.
   */
  public void setServerId(String serverId)
  {
    this.serverId = serverId;
  }

  /**
   * @return Returns the dataSource.
   */
  public DataSource getDataSource()
  {
    return dataSource;
  }

  /**
   * @param dataSource The dataSource to set.
   */
  public void setDataSource(DataSource dataSource)
  {
    this.dataSource = dataSource;
  }

  public void setSqlService(SqlService sqlService)
  {
    this.sqlService = sqlService;
  }

  /**
   * @return Returns the qrtzQualifiedJobs.
   */
  public Map<String, String> getQrtzQualifiedJobs()
  {
    return qrtzQualifiedJobs;
  }

  /**
   * @param qrtzQualifiedJobs The qrtzQualifiedJobs to set.
   */
  public void setQrtzQualifiedJobs(Map<String, String> qrtzQualifiedJobs)
  {
    this.qrtzQualifiedJobs = qrtzQualifiedJobs;
  }

  /**
   * @return Returns the qrtzJobs.
   */
  public Set<String> getQrtzJobs()
  {
    return qrtzJobs;
  }

  /**
   * @param qrtzJobs The qrtzJobs to set.
   */
  public void setQrtzJobs(Set<String> qrtzJobs)
  {
    this.qrtzJobs = qrtzJobs;
  }

  /**
   * @return Returns the qrtzPropFile.
   */
  public String getQrtzPropFile()
  {
    return qrtzPropFile;
  }

  /**
   * @param qrtzPropFile The qrtzPropFile to set.
   */
  public void setQrtzPropFile(String qrtzPropFile)
  {
    this.qrtzPropFile = qrtzPropFile;
  }

  public void setQrtzPropFileSakai(String qrtzPropFileSakai)
  {
    this.qrtzPropFileSakai = qrtzPropFileSakai;
  }

  /**
   * @return Returns the scheduler.
   */
  public Scheduler getScheduler()
  {
    return scheduler;
  }

  /**
   * @param scheduler The sched to set.
   */
  public void setScheduler(Scheduler scheduler)
  {
    this.scheduler = scheduler;
  }

  @Override
  public Scheduler getScheduler(String schedName) throws SchedulerException
  {
    if (scheduler.getSchedulerName().equals(schedName))
    {
      return getScheduler();
    }
    return null;
  }

  @Override
  public Collection<Scheduler> getAllSchedulers() throws SchedulerException
  {
    return Collections.singleton(getScheduler());
  }

  /**
   * @param serverConfigurationService The ServerConfigurationService to get our configuation from.
   */
  public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
  {
    this.serverConfigurationService = serverConfigurationService;
  }

  /**
   * @see org.sakaiproject.api.app.scheduler.SchedulerManager#setAutoDdl(java.lang.Boolean)
   */
  public void setAutoDdl(Boolean b)
  {
    autoDdl = b;
  }

   public Map<String, JobBeanWrapper> getBeanJobs() {
      return beanJobs;
   }

   public void registerBeanJob(String jobName, JobBeanWrapper job) {
      getBeanJobs().put(jobName, job);
   }

   public JobBeanWrapper getJobBeanWrapper(String beanWrapperId) {
      return getBeanJobs().get(beanWrapperId);
   }

    public void setJobFactory(JobFactory jobFactory) {
        this.jobFactory = jobFactory;
    }

    public void setMigration(HashMap<String, Class<? extends Job>> migration) {
        this.migration = migration;
    }

    public boolean isStartScheduler() {
       return startScheduler;
   }

   public void setStartScheduler(boolean startScheduler) {
       this.startScheduler = startScheduler;
   }

    @Override
    public void start() {
        if (isStartScheduler()) {
            try {
                scheduler.start();
            } catch (SchedulerException e) {
                log.error("Failed to start the scheduler.", e);
            }
        } else {
            log.info("Scheduler Not Started, startScheduler=false");
        }
    }

    @Override
    public void stop() {
        try{
            if (!scheduler.isShutdown()){
                scheduler.shutdown();
            }
        }
        catch (SchedulerException e){
            log.error("Failed to stop the scheduler", e);
        }
    }

    @Override
    public boolean isRunning() {
        try {
            return scheduler.isStarted();
        } catch (SchedulerException e) {
            log.debug("Failed to find if the scheduler is running", e);
        }
        return false;
    }
}
