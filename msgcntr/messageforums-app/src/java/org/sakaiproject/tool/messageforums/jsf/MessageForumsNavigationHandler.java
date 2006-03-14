package org.sakaiproject.tool.messageforums.jsf;

/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/


import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 * <p>
 * SakaiNavigationHandler catches the response that is being redirected to preserve the faces messages for the return request.
 * </p>
 * 
 */
public class MessageForumsNavigationHandler extends NavigationHandler
{
	/** The other navigation handler. */
	protected NavigationHandler m_chain = null;

	/**
	 * Construct.
	 */
	public MessageForumsNavigationHandler()
	{
	}

	/**
	 * Construct.
	 */
	public MessageForumsNavigationHandler(NavigationHandler chain)
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
		
		ExternalContext exContext = context.getExternalContext();
    HttpSession session = (HttpSession) exContext.getSession(false);

    if (session == null){
      return;
    }
		
		// add previous navigationString (outcome) to session
		session.setAttribute("MC_PREVIOUS_NAV", outcome);
	}
}



