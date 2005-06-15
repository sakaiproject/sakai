package org.sakaiproject.component.app.scheduler;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.sakaiproject.api.app.scheduler.JobDetailWrapper;

public class JobDetailWrapperImpl implements JobDetailWrapper
{
  private JobDetail jobDetail;
  private boolean isSelected = false;
  private List triggerWrapperList;
  private Integer triggerCount;

  private static final Log LOG = LogFactory.getLog(JobDetailWrapperImpl.class);

  public JobDetailWrapperImpl()
  {
  }

  /**
   * @return Returns the triggerCount.
   */
  public Integer getTriggerCount()
  {
    return triggerCount;
  }

  /**
   * @param triggerCount The triggerCount to set.
   */
  public void setTriggerCount(Integer triggerCount)
  {
    this.triggerCount = triggerCount;
  }

  /**
   * @return Returns the triggerWrapperList.
   */
  public List getTriggerWrapperList()
  {
    return triggerWrapperList;
  }

  /**
   * @param triggerWrapperList The triggerWrapperList to set.
   */
  public void setTriggerWrapperList(List triggerWrapperList)
  {
    this.triggerCount = new Integer(triggerWrapperList.size());
    this.triggerWrapperList = triggerWrapperList;
  }

  /**
   * @return Returns the jobDetail.
   */
  public JobDetail getJobDetail()
  {
    return jobDetail;
  }

  /**
   * @param jobDetail The jobDetail to set.
   */
  public void setJobDetail(JobDetail jobDetail)
  {
    this.jobDetail = jobDetail;
  }

  /**
   * @return Returns the isSelected.
   */
  public boolean getIsSelected()
  {
    return isSelected;
  }

  /**
   * @param isSelected The isSelected to set.
   */
  public void setIsSelected(boolean isSelected)
  {
    this.isSelected = isSelected;
  }
}