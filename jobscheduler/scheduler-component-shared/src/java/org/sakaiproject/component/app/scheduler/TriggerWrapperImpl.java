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

import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
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

  public boolean isCron() {
    return trigger instanceof CronTrigger;
  }

  public boolean isSimple() {
    return trigger instanceof SimpleTrigger;
  }
}