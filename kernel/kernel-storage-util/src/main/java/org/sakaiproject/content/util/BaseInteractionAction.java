/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/content/util/BaseInteractionAction.java $
 * $Id: BaseInteractionAction.java 101634 2011-12-12 16:44:33Z aaronz@vt.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.util;

import org.sakaiproject.content.api.InteractionAction;
import org.sakaiproject.entity.api.Reference;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 26, 2007
 * Time: 10:33:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class BaseInteractionAction extends BaseResourceAction implements InteractionAction {

   private String helperId;
   private List<String> requiredPropertyKeys;

   public BaseInteractionAction(String id, ActionType actionType, String typeId,
                                String helperId, List<String> requiredPropertyKeys) {
      super(id, actionType, typeId);
      this.helperId = helperId;
      this.requiredPropertyKeys = requiredPropertyKeys;
   }

   /**
    * Access the unique identifier for the tool that will handle this action.
    * This is the identifier by which the helper is registered with the
    * ToolManager.
    *
    * @return
    */
   public String getHelperId() {
      return helperId;
   }

   /**
    * Access a list of properties that should be provided to the helper if they are defined.
    * Returning null or empty list indicates no properties are needed by the helper.
    *
    * @return a List of Strings if property values are required.
    */
   public List<String> getRequiredPropertyKeys() {
      return requiredPropertyKeys;
   }

   /**
    * ResourcesAction calls this method before starting the helper. This is intended to give
    * the registrant a chance to do any preparation needed before the helper starts with respect
    * to this action and the reference specified in the parameter. The method returns a String
    * (possibly null) which will be provided as the "initializationId" parameter to other
    * methods and in
    *
    * @param reference
    * @return
    */
   public String initializeAction(Reference reference) {
      return getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
   }

   /**
    * Construct a m initialization id for an action
    * @param refStr
    * @param typeId
    * @param actionId
    * @return
    */
	public static String getInitializationId(String refStr, String typeId, String actionId) 
	{
		StringBuilder buf = new StringBuilder();
		
		buf.append(refStr);
		buf.append("?type=");
		buf.append(typeId);
		buf.append("&action=");
		buf.append(actionId);
		
		return  buf.toString();
	}

   /**
    * ResourcesAction calls this method after completion of its portion of the action.
    *
    * @param reference        The
    * @param initializationId
    */
   public void finalizeAction(Reference reference, String initializationId) {
   }

   /**
    * ResourcesAction calls this method if the user cancels out of the action or some error
    * occurs preventing completion of the action after the helper completes its part of the
    * action.
    *
    * @param reference
    * @param initializationId
    */
   public void cancelAction(Reference reference, String initializationId) {
   }

}
