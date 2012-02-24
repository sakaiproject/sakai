/**********************************************************************************
 * $URL:  $
 * $Id:  $
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

package org.sakaiproject.content.api;

import org.sakaiproject.entity.api.Reference;


/**
 * 
 *
 */
public interface ServiceLevelAction extends ResourceToolAction
{
	/**
	 * This method is invoked before the Resources tool does its part of the action
	 * in case the registrant needs to participate in the action at that point.
	 * @param reference A reference to the entity with respect to which the action is taken
	 */
	public void initializeAction(Reference reference);
	
	/**
	 * This method is invoked after the Resources tool does its part of the action
	 * in case the registrant needs to participate in the action at that point.  Will
	 * not be invoked after cancelAction(Reference reference) is invoked.
	 * @param reference A reference to the entity  with respect to which the action is taken
	 */
	public void finalizeAction(Reference reference);
	
	/**
	 * This method is invoked if the Resources tool cancels the action after invoking 
	 * the initializeAction(Reference reference) method in case the registrant needs to 
	 * clean up a canceled action at that point. Will not be invoked after 
	 * finalizeAction(Reference reference) is invoked.
	 * @param reference A reference to the entity  with respect to which the action is taken
	 */
	public void cancelAction(Reference reference);
	
	// ignored for now
	// will deal with actions on multiple items (like copy, move, delete)
	public boolean isMultipleItemAction();
	
}
