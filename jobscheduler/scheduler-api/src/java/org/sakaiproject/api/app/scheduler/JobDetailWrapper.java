/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

import org.quartz.JobDetail;

/**
 * @author <a href="mailto:jlannan.iupui.edu">Jarrod Lannan</a>
 * @version $Id: JobDetailWrapper.java,v 1.1 2005/05/31 18:04:09 jlannan.iupui.edu Exp $
 * 
 */
public interface JobDetailWrapper
{
  /**
   * @return Returns the triggerCount.
   */
  public Integer getTriggerCount();

  /**
   * @param triggerCount The triggerCount to set.
   */
  public void setTriggerCount(Integer triggerCount);

  /**
   * @return Returns the triggerWrapperList.
   */
  public List<TriggerWrapper> getTriggerWrapperList();

  /**
   * @param triggerWrapperList The triggerWrapperList to set.
   */
  public void setTriggerWrapperList(List<TriggerWrapper> triggerWrapperList);

  /**
   * @return Returns the jobDetail.
   */
  public JobDetail getJobDetail();

  /**
   * @param jobDetail The jobDetail to set.
   */
  public void setJobDetail(JobDetail jobDetail);

  /**
   * @return Returns the isSelected.
   */
  public boolean getIsSelected();

  /**
   * @param isSelected The isSelected to set.
   */
  public void setIsSelected(boolean isSelected);

   /**
    * @return bean name if this is a Bean Job or class if not
    */
   public String getJobType();
}

