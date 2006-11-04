/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
 */
public interface InteractionAction extends ResourceToolAction
{
	/**
	 * Setup to invoke a helper to handle the user interaction for this action. The method returns a
	 * URL that renders the UI for the action.  The Resources tool will use the URL as the src attribute
	 * for a frame or window as part of a wizard, so the URL should render a full XHTML document or 
	 * a sequence of documents that elicit information needed to complete the action.  
	 * The helper eventually return to the caller
	 * 
	 * @param req The request from which the helper is invoked.
	 * @param reference Identifies the ContentEntity with respect to which the action is to be invoked. 
	 * @return
	 */
	public String getActionUrl(HttpServletRequest req, Reference reference);
	
	/**
	 * @param reference
	 */
	public void finalizeAction(Reference reference);
	
	/**
	 * @param reference
	 */
	public void cancelAction(Reference reference);
	
	/**
	 * @return
	 */
	public String getHelperId();
	
	/**
	 * @param controller
	 */
	public void setController(ResourceToolActionController controller);
	
	/**
	 * @return
	 */
	public ResourceToolActionController getController();

	/**
	 * @param request
	 * @param helperId TODO
	 * @param doneURL The address to which the helper should redirect when done.
	 */
	public void startHelper(HttpServletRequest request, String helperId, String doneURL);
	
	/**
	 * Access a list of properties that should be provided to the helper if they are defined. 
	 * Returning null or empty list indicates no properties are needed by the helper.
	 * @return a List of Strings if property values are required. 
	 */
	public List getRequiredPropertyKeys();
	
}
