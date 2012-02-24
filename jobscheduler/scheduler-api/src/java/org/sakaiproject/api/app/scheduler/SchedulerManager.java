/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.scheduler;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.TriggerListener;

/**
 * @version $Id: SchedulerManager.java,v 1.1 2005/05/31 18:04:09 jlannan.iupui.edu Exp $
 */
public interface SchedulerManager
{

  /**
   * @deprecated use {@link #getGlobalTriggerListeners()}
   * @return Returns the globalTriggerListener.
   */
  public TriggerListener getGlobalTriggerListener();

  /**
   * @deprecated use {@link #setGlobalTriggerListeners(List<TriggerListener>)}
   * @param globalTriggerListener The globalTriggerListener to set.
   */
  public void setGlobalTriggerListener(TriggerListener globalTriggerListener);

    /**
     * Registers a List of TriggerListeners which will be registered to respond to events on all Triggers
     * 
     * @param globalTriggerListeners
     */
  public void setGlobalTriggerListeners(List<TriggerListener> globalTriggerListeners);

  public List<TriggerListener> getGlobalTriggerListeners();

    /**
     * Registers a List of JobListeners which will be registered to respond to events on all Triggers
     * 
     * @param globalJobListeners
     */
  public void setGlobalJobListeners(List<JobListener> globalJobListeners);

  public List<JobListener> getGlobalJobListeners();
  
  /**
   * @return Returns the serverId.
   */
  public String getServerId();

  /**
   * @param serverId The serverId to set.
   */
  public void setServerId(String serverId);

  /**
   * @return Returns the dataSource.
   */
  public DataSource getDataSource();

  /**
   * @param dataSource The dataSource to set.
   */
  public void setDataSource(DataSource dataSource);

  /**
   * @return Returns the qrtzQualifiedJobs.
   */
  public Map<String, String> getQrtzQualifiedJobs();

  /**
   * @param qrtzQualifiedJobs The qrtzQualifiedJobs to set.
   */
  public void setQrtzQualifiedJobs(Map<String, String> qrtzQualifiedJobs);

  /**
   * @return Returns the qrtzJobs.
   */
  public Set<String>getQrtzJobs();

  /**
   * @param qrtzJobs The qrtzJobs to set.
   */
  public void setQrtzJobs(Set<String> qrtzJobs);

  /**
   * @return Returns the qrtzPropFile.
   */
  public String getQrtzPropFile();

  /**
   * @param qrtzPropFile The qrtzPropFile to set.
   */
  public void setQrtzPropFile(String qrtzPropFile);

  /**
   * @return Returns the scheduler.
   */
  public Scheduler getScheduler();

  /**
   * @param sched The sched to set.
   */
  public void setScheduler(Scheduler scheduler);

  /**
   * set autoDdl
   */
  public void setAutoDdl(Boolean b);

   public Map<String, JobBeanWrapper> getBeanJobs();

   public void registerBeanJob(String jobName, JobBeanWrapper job);

   public JobBeanWrapper getJobBeanWrapper(String beanWrapperId);
}

