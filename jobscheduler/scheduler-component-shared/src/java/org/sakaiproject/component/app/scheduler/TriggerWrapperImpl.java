package org.sakaiproject.component.app.scheduler;

import org.quartz.Trigger;
import org.sakaiproject.api.app.scheduler.TriggerWrapper;

public class TriggerWrapperImpl implements TriggerWrapper
{
  private Trigger trigger;
  private boolean isSelected = false;

  public TriggerWrapperImpl()
  {
  }

  /**
   * @return Returns the trigger.
   */
  public Trigger getTrigger()
  {
    return trigger;
  }

  /**
   * @param trigger The trigger to set.
   */
  public void setTrigger(Trigger trigger)
  {
    this.trigger = trigger;
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