/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.util;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * <p>
 * Login / Logout bookeeping utility.
 * </p>
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
