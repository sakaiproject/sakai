/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/content/util/BaseServiceLevelAction.java $
 * $Id: BaseServiceLevelAction.java 51317 2008-08-24 04:38:02Z csev@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.util;

import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.content.api.ServiceLevelAction;

/**
 * A basic implementation of ServiceLevelAction which means for simple cases
 * subclassing isn't needed.
 * @see ServiceLevelAction
 */
public class BaseServiceLevelAction extends BaseResourceAction implements ServiceLevelAction 
{

   private boolean multipleItemAction;

   public BaseServiceLevelAction(String id, ActionType actionType, String typeId, boolean multipleItemAction, Localizer localizer) 
   {
      super(id, actionType, typeId);
      this.multipleItemAction = multipleItemAction;
      setLocalizer(localizer);
   }
   
   public BaseServiceLevelAction(String id, ActionType actionType, String typeId, boolean multipleItemAction) 
   {
      super(id, actionType, typeId);
      this.multipleItemAction = multipleItemAction;
   }

   /**
    * This method effects this action on the entity specified.
    *
    * @param reference
    */
   public void invokeAction(Reference reference) 
   {
	   	// do nothing
   }

   public boolean isMultipleItemAction() 
   {
      return multipleItemAction;
   }

   /**
    * This method is invoked before the Resources tool does its part of the action
    * in case the registrant needs to participate in the action at that point.
    *
    * @param reference A reference to the entity with respect to which the action is taken
    */
   public void initializeAction(Reference reference) 
   {
      // do nothing
   }

   /**
    * This method is invoked after the Resources tool does its part of the action
    * in case the registrant needs to participate in the action at that point.  Will
    * not be invoked after cancelAction(Reference reference) is invoked.
    *
    * @param entity A reference to the entity  with respect to which the action is taken
    */
   public void finalizeAction(Reference reference) 
   {
      // do nothing
   }

   /**
    * This method is invoked if the Resources tool cancels the action after invoking
    * the initializeAction(Reference reference) method in case the registrant needs to
    * clean up a canceled action at that point. Will not be invoked after
    * finalizeAction(Reference reference) is invoked.
    *
    * @param entity A reference to the entity  with respect to which the action is taken
    */
   public void cancelAction(Reference reference) 
   {
      // do nothing
   }

}
