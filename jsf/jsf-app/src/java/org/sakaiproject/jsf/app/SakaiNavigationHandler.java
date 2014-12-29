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

package org.sakaiproject.jsf.app;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * SakaiNavigationHandler catches the response that is being redirected to preserve the faces messages for the return request.
 * </p>
 * 
 */
public class SakaiNavigationHandler extends NavigationHandler
{
	/** The other navigation handler. */
	protected NavigationHandler m_chain = null;

	/**
	 * Construct.
	 */
	public SakaiNavigationHandler()
	{
	}

	/**
	 * Construct.
	 */
	public SakaiNavigationHandler(NavigationHandler chain)
	{
		m_chain = chain;
	}

	/**
	 * Handle the navigation
	 * 
	 * @param context
	 *        The Faces context.
	 * @param fromAction
	 *        The action string that triggered the action.
	 * @param outcome
	 *        The logical outcome string, which is the new tool mode, or if null, the mode does not change.
	 */
	public void handleNavigation(FacesContext context, String fromAction, String outcome)
	{
		m_chain.handleNavigation(context, fromAction, outcome);

		// if we have a redirect scheduled, we need to preserve the messages
		// if we are coming from the Sakai RequestFilter, and a redirect was requested, the request object
		// will have this attribute set.
		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
		if (req.getAttribute("sakai.redirect") != null)
		{
			// save messages from the context for restoration on the next rendering
			MessageSaver.saveMessages(context);
		}
	}
}



