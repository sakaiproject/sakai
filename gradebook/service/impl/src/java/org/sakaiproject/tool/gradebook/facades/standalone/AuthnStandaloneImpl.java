/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades.standalone;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.facades.Authn;

/**
 * An implementation of the Authn facade to support demos and UI tests.
 */
public class AuthnStandaloneImpl implements Authn {
	private static Log log = LogFactory.getLog(AuthnStandaloneImpl.class);

	private static String USER_ID_PARAMETER = "userUid";

	private ThreadLocal authnContext = new ThreadLocal();

	public String getUserUid() {
		Object whatToAuthn = authnContext.get();
		if (log.isDebugEnabled()) log.debug("whatToAuthn=" + whatToAuthn);

        // If we got a null, get the request from the faces context
        if(whatToAuthn == null) {
            whatToAuthn = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        }

		HttpServletRequest request = (HttpServletRequest)whatToAuthn;
		HttpSession session = request.getSession();

		// Try to get the user ID from a session variable.
		String userUid = (String)session.getAttribute(USER_ID_PARAMETER);

		if (userUid == null) {
			// Try to get the user ID from a request parameter.
			userUid = request.getParameter(USER_ID_PARAMETER);
			if (userUid != null) {
				// Copy the request parameter into the session.
				session.setAttribute(USER_ID_PARAMETER, userUid);
			}
		}

		return userUid;
	}

	/**
	 * This is usually redundant, since all the necessary information
	 * is available through the FacesContext object.
	 * Unfortunately, servlets and filters might call this service before
	 * the Faces context is fully initialized.
	 */
	public void setAuthnContext(Object whatToAuthn) {
		authnContext.set(whatToAuthn);
	}

}


