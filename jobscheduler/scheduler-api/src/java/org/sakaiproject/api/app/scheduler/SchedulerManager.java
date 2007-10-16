/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.api.app.scheduler;

import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import org.quartz.Scheduler;
import org.quartz.TriggerListener;

/**
 * @version $Id: SchedulerManager.java,v 1.1 2005/05/31 18:04:09 jlannan.iupui.edu Exp $
 */
public interface SchedulerManager
{

  /**
   * @return Returns the globalTriggerListener.
   */
  public TriggerListener getGlobalTriggerListener();

  /**
   * @param globalTriggerListener The globalTriggerListener to set.
   */
  public void setGlobalTriggerListener(TriggerListener globalTriggerListener);

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
  public Map getQrtzQualifiedJobs();

  /**
   * @param qrtzQualifiedJobs The qrtzQualifiedJobs to set.
   */
  public void setQrtzQualifiedJobs(Map qrtzQualifiedJobs);

  /**
   * @return Returns the qrtzJobs.
   */
  public Set getQrtzJobs();

  /**
   * @param qrtzJobs The qrtzJobs to set.
   */
  public void setQrtzJobs(Set qrtzJobs);

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

   public Map getBeanJobs();

   public void registerBeanJob(String jobName, JobBeanWrapper job);

   public JobBeanWrapper getJobBeanWrapper(String beanWrapperId);
}

