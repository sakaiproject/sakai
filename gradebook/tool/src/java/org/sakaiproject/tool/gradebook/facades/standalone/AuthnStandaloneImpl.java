/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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


