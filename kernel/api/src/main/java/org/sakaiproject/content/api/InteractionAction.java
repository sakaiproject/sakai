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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.entity.api.Reference;

/**
 * An InteractionAction defines a kind of ResourceToolAction which involves 
 * user interaction to complete the action. The Resources tool will invoke 
 * a helper to render an html page (or possibly a series of pages), process 
 * the response(s) and turn control back to the Resources tool when done.  
 * 
 * Before invoking the helper, ResourcesAction will call initializeAction() 
 * supplying a Reference onject as a parameter. Implementations of this 
 * interface may do whatever is necessary to prepare for invocation of the
 * helper and they may return an identifier for that initialization. 
 * 
 * After starting the helper and getting back control from the helper, 
 * ResourcesAction will call either finalizeAction or cancelAction to indicate 
 * that the user either finalized the action or canceled it. The registrant may  
 * do whatever is necessary to commit any changes in persistant storage (other
 * than changes to the referenced resource in ContentHosting) or reverse them.
 */
public interface InteractionAction extends ResourceToolAction
{
	/**
	 * Access the unique identifier for the tool that will handle this action. 
	 * This is the identifier by which the helper is registered with the 
	 * ToolManager.
	 * @return
	 */
	public String getHelperId();
	
	/**
	 * Access a list of properties that should be provided to the helper if they are defined. 
	 * Returning null or empty list indicates no properties are needed by the helper.
	 * @return a List of Strings if property values are required. 
	 */
	public List<String> getRequiredPropertyKeys();
	
	/**
	 * ResourcesAction calls this method before starting the helper. This is intended to give
	 * the registrant a chance to do any preparation needed before the helper starts with respect
	 * to this action and the reference specified in the parameter. The method returns a String
	 * (possibly null) which will be provided as the "initializationId" parameter to other
	 * methods and in 
	 * @param reference
	 * @return 
	 */
	public String initializeAction(Reference reference);
	
	/**
	 * ResourcesAction calls this method after completion of its portion of the action. 
	 * @param reference The 
	 * @param initializationId 
	 */
	public void finalizeAction(Reference reference, String initializationId);
	
	/**
	 * ResourcesAction calls this method if the user cancels out of the action or some error 
	 * occurs preventing completion of the action after the helper completes its part of the 
	 * action.    
	 * @param reference
	 * @param initializationId 
	 */
	public void cancelAction(Reference reference, String initializationId);
	
}
