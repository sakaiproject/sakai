/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2005-2009 The Sakai Foundation
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
 */

package org.sakaiproject.portlet.util;

import java.util.Map;

import javax.portlet.PortletRequest;

/**
 * The gridsphere attribute information is available from the following:
 * http://www.gridsphere.org/gridsphere/docs/FAQ/FAQ.html question #5
 * 
 * The uPortal attribute information is available from
 * http://www.uportal.org/implementors/portlets/workingWithPortlets.html#User_Information
 * Note that with uPortal you need to configure it to export user information to
 * portlets, so the user attribute names used is somewhat arbitrary but here I
 * am trying to stick to the suggestions in the JSR 168 Portlet Standard (PLT.D).
 */


public class PortalUser {

	// If we 
	public static final int NOTSET = -1;

	public static final int UNKNOWN = 0;
	public static final int PLUTO = 0;
	public static final int SAKAI = 0;

	public static final int GRIDSPHERE = 1;

	public static final int UPORTAL = 2;

	public static final int ORACLEPORTAL = 3;

	private int portalType = NOTSET;

	private boolean doDebug = true;

	// Leave not set
	public PortalUser() {
	}

	// Ultimately - this should go away
	public PortalUser(int portalType) {
		this.portalType = portalType;
	}

	public int lookupPortalType(PortletRequest request)
	{
		String portalInfo = request.getPortalContext().getPortalInfo();
		if ( portalInfo.toLowerCase().startsWith("sakai-charon") ) {
			return SAKAI;
		} else {
			return PLUTO;  // Assume a Pluto-based portal
		}
	}

	private void fixPortalType(PortletRequest request)
	{
		if ( portalType != NOTSET ) return;
		portalType = lookupPortalType(request);
		System.out.println("Setting portalType="+portalType);
	}

	public String getUsername(PortletRequest request) {
		fixPortalType(request);
		String username = null;
		Map userInfo = (Map) request.getAttribute(PortletRequest.USER_INFO);

		switch (portalType) {
			case GRIDSPHERE:
				if (userInfo != null) {
					username = (String) userInfo.get("user.name");
				}
				break;
			case ORACLEPORTAL:
				//System.out.println("userInfo" + userInfo); // Changes by Venkatesh for Oracle Portal
				//System.out.println("Remote User=" + username); // Oracle portal is populating user name with [1] at the end
				// the following code will get rid of the unnecessary characters
				username = request.getRemoteUser();
				if(username != null && username.indexOf("[") != -1)
				{
					if ( doDebug ) System.out.println("Modifying user name for Oracle Portal=" + username);
					int corruptIndex = username.indexOf('[');
					username = username.substring(0,corruptIndex);
				}
				break;
			case PLUTO:  
			case UPORTAL:
				username = request.getRemoteUser();
				break;
		}
		if ( doDebug) System.out.println("Remote User=" + username);
		return username;
	}

	// for backwards compatibility
	public String getPortalUsername(PortletRequest request) {
		return getUsername(request);
	}

	public String getFirstName(PortletRequest request) {
		fixPortalType(request);
		String firstName = null;
		Map userInfo = (Map) request.getAttribute(PortletRequest.USER_INFO);

		switch (portalType) {
			case GRIDSPHERE:
				String fullName = getGridsphereFullName(request);
				firstName = fullName.trim().substring(0, fullName.indexOf(" "));
				break;
			case PLUTO:
			case UPORTAL:
				if (userInfo != null) {
					firstName = (String) userInfo.get("user.name.given");
				}
				break;
		}
		if ( doDebug ) System.out.println("First Name="+firstName);
		return firstName;
	}

	public String getLastName(PortletRequest request) {
		fixPortalType(request);
		String lastName = null;
		Map userInfo = (Map) request.getAttribute(PortletRequest.USER_INFO);

		switch (portalType) {
			case GRIDSPHERE:
				String fullName = getGridsphereFullName(request);
				lastName = fullName.substring(fullName.trim().lastIndexOf(" ") + 1);
				break;
			case PLUTO:
			case UPORTAL:
				if (userInfo != null) { 
					lastName =  (String) userInfo.get("user.name.family");
				}
				break;
		}
		if ( doDebug ) System.out.println("Last Name="+lastName);
		return lastName;
	}

	public String getEmail(PortletRequest request) {
		fixPortalType(request);
		String email = null;
		Map userInfo = (Map) request.getAttribute(PortletRequest.USER_INFO);

		switch (portalType) {
			case GRIDSPHERE:
				if (userInfo != null) {
					email = (String) userInfo.get("user.email");
				}
				break;
			case PLUTO:
			case UPORTAL:
				if (userInfo != null) {
					email = (String) userInfo.get("user.home-info.online.email");
				}
		}

		if ( doDebug ) System.out.println("EMail="+email);
		return email;
	}

	private String getGridsphereFullName(PortletRequest request) {
		String fullName = null;
		Map userInfo = (Map) request.getAttribute(PortletRequest.USER_INFO);
		if (userInfo != null) {
			fullName = (String) userInfo.get("user.name.full");
		}
		return fullName;
	}
}
