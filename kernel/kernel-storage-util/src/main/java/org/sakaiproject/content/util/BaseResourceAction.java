/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/content/util/BaseResourceAction.java $
 * $Id: BaseResourceAction.java 51317 2008-08-24 04:38:02Z csev@umich.edu $
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

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ResourceToolAction;

/**
 * Created by IntelliJ IDEA.
 * User: johnellis
 * Date: Jan 26, 2007
 * Time: 10:38:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class BaseResourceAction implements ResourceToolAction {
   protected String id;
   protected ActionType actionType;
   protected String typeId;
   protected boolean available = true;
   protected Localizer localizer = null;


   public BaseResourceAction(String id, ActionType actionType, String typeId) {
      this.id = id;
      this.actionType = actionType;
      this.typeId = typeId;
   }

   /**
    * Access the id of this action (which must be unique within this type and must be limited to alphnumeric characters).
    *
    * @return
    */
   public String getId() {
      return id;
   }

   /**
    * Access the id of the ResourceType this action relates to.
    *
    * @return
    */
   public String getTypeId() {
      return typeId;
   }

   /**
    * Access the enumerated constant for this action.
    *
    * @return
    */
   public ActionType getActionType() {
      return actionType;
   }

   /**
    * @return
    */
   public String getLabel() {
	   	// if a Localizer is defined, use it to get the localized label
	   	// otherwise return null to let the resources tool handle the label for standard actions
		String label = null;
		if(this.localizer != null)
		{
			label = this.localizer.getLabel();
		}
		return label;
   }

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceToolAction#available(java.lang.String)
	 */
	public boolean available(ContentEntity entity)
	{
		return this.available;
	}
	
	public void setLocalizer(Localizer localizer)
	{
		this.localizer = localizer;
	}
	
	/**
	 * Localizer provides a way for the registrant to take charge of localizing labels 
	 * without extending BaseResourceAction.  In defining actions, a registrant can create
	 * instances of BaseResourceAction, implement the Localizer interface with a method
	 * that provides localized strings, and set the localizer.  Subsequent invocation of
	 * BaseResourceAction.getLabel() will use the Localizer to supply labels.
	 */
	public interface Localizer
	{
		/**
		 *
		 * @return
		 */
		public String getLabel();
	}

	/**
     * @param available the available to set
     */
    public void setAvailable(boolean available)
    {
    	this.available = available;
    }
	
}
