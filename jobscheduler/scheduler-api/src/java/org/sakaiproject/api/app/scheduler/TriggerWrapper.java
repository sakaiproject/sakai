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

import org.quartz.Trigger;

/**
 * @author <a href="mailto:jlannan.iupui.edu">Jarrod Lannan</a>
 * @version $Id: TriggerWrapper.java,v 1.1 2005/05/31 18:04:09 jlannan.iupui.edu Exp $
 * 
 */
public interface TriggerWrapper
{
  /**
   * @return Returns the trigger.
   */
  public Trigger getTrigger();

  /**
   * @param trigger The trigger to set.
   */
  public void setTrigger(Trigger trigger);

  /**
   * @return Returns the isSelected.
   */
  public boolean getIsSelected();

  /**
   * @param isSelected The isSelected to set.
   */
  public void setIsSelected(boolean isSelected);
  
  /**
   * @return <code>true</code> if the wrapped trigger is a CronTrigger. 
   */
  public boolean isCron();

  /**
   * @return <code>true</code> if the wrapped trigger is a SimpleTrigger. 
   */
  public boolean isSimple();
}

