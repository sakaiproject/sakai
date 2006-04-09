/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.util;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.api.common.authentication.Authentication;
import org.sakaiproject.api.kernel.session.Session;
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.service.framework.session.UsageSession;
import org.sakaiproject.service.framework.session.cover.UsageSessionService;
import org.sakaiproject.service.legacy.authzGroup.cover.AuthzGroupService;
import org.sakaiproject.service.legacy.event.cover.EventTrackingService;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;

/**
 * <p>
 * Login / Logout bookeeping utility.
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 */
public class LoginUtil
{
	/**
	 * Do the book-keeping associated with a user login that has already been authenticated.
	 * 
	 * @param authn
	 *        The user authentication.
	 * @param req
	 *        The servlet request.
	 * @return true if all went well, false if not (may fail if the userId is not a valid User)
	 */
	public static boolean login(Authentication authn, HttpServletRequest req)
	{
		// establish the user's session - this has been known to fail
		UsageSession session = UsageSessionService.startSession(authn.getUid(), req.getRemoteAddr(), req.getHeader("user-agent"));
		if (session == null)
		{
			return false;
		}

		// set the user information into the current session
		Session sakaiSession = SessionManager.getCurrentSession();
		sakaiSession.setUserId(authn.getUid());
		sakaiSession.setUserEid(authn.getEid());

		// update the user's externally provided realm definitions
		AuthzGroupService.refreshUser(authn.getUid());

		// post the login event
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN, null, true));

		return true;
	}

	/**
	 * Do the book-keeping associated with logging out the current user.
	 */
	public static void logout()
	{
		// TODO: through authn manager?
		UserDirectoryService.destroyAuthentication();

		// invalidate the sakai session, which makes it unavailable, unbinds all the bound objects,
		// including the session, which will close and generate the logout event
		Session sakaiSession = SessionManager.getCurrentSession();
		sakaiSession.invalidate();
	}

	/**
	 * Generate the logout event.
	 */
	public static void logoutEvent(UsageSession session)
	{
		if (session == null)
		{
			// generate a logout event (current session)
			EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGOUT, null, true));
		}
		else
		{
			// generate a logout event (this session)
			EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGOUT, null, true), session);
		}
	}
}



