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
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.portlet.util;

import java.io.PrintStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.util.FormattedText;

/**
 * Some Utility Functions
 */
@Slf4j
public class PortletHelper {

	public static String snoopPortlet(PortletRequest request)
	{
		String retval = "==== Portlet Request Snoop:\n";

		String remoteUser = request.getRemoteUser();
		retval += "getRemoteUser()="+remoteUser+"\n";

		Map userInfo = (Map) request.getAttribute(PortletRequest.USER_INFO);
		retval += "UserInfo (needs Pluto 1.1.1 or later)\n"+userInfo+"\n";

		retval += "isUserInRole(admin)="+request.isUserInRole("admin")+"\n";
		retval += "isUserInRole(access)="+request.isUserInRole("access")+"\n";
		retval += "isUserInRole(maintain)="+request.isUserInRole("maintain")+"\n";
		retval += "isUserInRole(student)="+request.isUserInRole("student")+"\n";
		retval += "isUserInRole(instructor)="+request.isUserInRole("instructor")+"\n";
		retval += "isUserInRole(site.upd)="+request.isUserInRole("site.upd")+"\n";
		retval += "isUserInRole(content.read)="+request.isUserInRole("content.read")+"\n";

		return retval;
	}

	// Error Message
	public static void clearErrorMessage(PortletRequest request)
	{   
		PortletSession pSession = request.getPortletSession(true);
		pSession.removeAttribute("error.message");
		pSession.removeAttribute("error.output");
		pSession.removeAttribute("error.map");
	}

	public static String getErrorMessage(PortletRequest request)
	{   
		PortletSession pSession = request.getPortletSession(true);
		try {
			return (String) pSession.getAttribute("error.message");
		} catch (Throwable t) {
			return null;
		}
	}

	public static String getErrorOutput(PortletRequest request)
	{   
		PortletSession pSession = request.getPortletSession(true);
		try {
			return (String) pSession.getAttribute("error.output");
		} catch (Throwable t) {
			return null;
		}
	}

	public static Map getErrorMap(PortletRequest request)
	{   
		PortletSession pSession = request.getPortletSession(true);
		try {
			return (Map) pSession.getAttribute("error.map");
		} catch (Throwable t) {
			return null;
		}
	}

	public static void setErrorMessage(PortletRequest request, String errorMsg)
	{
		Throwable t = new Throwable("Just to generate a traceback");
		setErrorMessage(request, errorMsg, t);
	}   

	public static void setErrorMessage(PortletRequest request, String errorMsg, Throwable t)
	{
		if ( errorMsg == null ) errorMsg = "null";
		PortletSession pSession = request.getPortletSession(true);
		pSession.setAttribute("error.message",errorMsg);

		OutputStream oStream = new ByteArrayOutputStream();
		PrintStream pStream = new PrintStream(oStream);

		log.error("{}", oStream);
		log.error("{}", pStream);

		// errorMsg = errorMsg .replaceAll("<","&lt;").replaceAll(">","&gt;");

		StringBuffer errorOut = new StringBuffer();
		errorOut.append("<p class=\"portlet-msg-error\">\n");
		errorOut.append(FormattedText.escapeHtmlFormattedText(errorMsg));
		errorOut.append("\n</p>\n<!-- Traceback for this error\n");
		errorOut.append(oStream.toString());
		errorOut.append("\n-->\n");

		pSession.setAttribute("error.output",errorOut.toString());

		Map map = request.getParameterMap();
		pSession.setAttribute("error.map",map);
	}  

	public static void clearDebugOutput(PortletRequest request)
	{   
		PortletSession pSession = request.getPortletSession(true);
		pSession.removeAttribute("debug.print");
	}

	public static String getDebugOutput(PortletRequest request)
	{   
		PortletSession pSession = request.getPortletSession(true);
		try {
			return (String) pSession.getAttribute("debug.print");
		} catch (Throwable t) {
			return null;
		}
	}

	public static void debugPrint(PortletRequest request, String line)
	{
		if ( line == null ) return;
		line = line.replaceAll("<","&lt;").replaceAll(">","&gt;");

		PortletSession pSession = request.getPortletSession(true);
		String debugOut = null;
		try {
			debugOut = (String) pSession.getAttribute("debug.print");
		} catch (Throwable t) {
			debugOut = null;
		}
		if ( debugOut == null ) {
			debugOut = line;
		} else {
			debugOut = debugOut + "\n" + line;
		}
		pSession.setAttribute("debug.print",debugOut);
	}

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

	public static final int UNKNOWN = 0;
	public static final int PLUTO = 0;
	public static final int SAKAI = 0;
	public static final int GRIDSPHERE = 1;
	public static final int UPORTAL = 2;
	public static final int ORACLEPORTAL = 3;

	// TODO: Need to grok Oracle Portal and GridSphere
	public static int lookupPortalType(PortletRequest request)
	{
		String portalInfo = request.getPortalContext().getPortalInfo();
		if ( portalInfo.toLowerCase().startsWith("sakai-charon") ) {
			return SAKAI;
		} else {
			return PLUTO;  // Assume a Pluto-based portal
		}
	}

	public static String getUsername(PortletRequest request) {
		String username = null;
		Map userInfo = (Map) request.getAttribute(PortletRequest.USER_INFO);

		switch (lookupPortalType(request)) {
			case GRIDSPHERE:
				if (userInfo != null) {
					username = (String) userInfo.get("user.name");
				}
				break;
			case ORACLEPORTAL:
				log.debug("userInfo {}", userInfo); // Changes by Venkatesh for Oracle Portal
				log.debug("Remote User={}", username); // Oracle portal is populating user name with [1] at the end
				// the following code will get rid of the unnecessary characters
				username = request.getRemoteUser();
				if(username != null && username.indexOf("[") != -1)
				{
					debugPrint(request,"Modifying user name for Oracle Portal=" + username);
					int corruptIndex = username.indexOf('[');
					username = username.substring(0,corruptIndex);
				}
				break;
			case PLUTO:  
			case UPORTAL:
				username = request.getRemoteUser();
				break;
		}
		debugPrint(request,"Remote User=" + username);
		return username;
	}

	public static String getFirstName(PortletRequest request) {
		String firstName = null;
		Map userInfo = (Map) request.getAttribute(PortletRequest.USER_INFO);

		switch (lookupPortalType(request)) {
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
		debugPrint(request,"First Name="+firstName);
		return firstName;
	}

	public static String getLastName(PortletRequest request) {
		String lastName = null;
		Map userInfo = (Map) request.getAttribute(PortletRequest.USER_INFO);

		switch (lookupPortalType(request)) {
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
		debugPrint(request,"Last Name="+lastName);
		return lastName;
	}

	@SuppressWarnings("unchecked")
		public static String getEmail(PortletRequest request) {
			String email = null;
			Map<String,String> userInfo = (Map<String,String>) request.getAttribute(PortletRequest.USER_INFO);

			switch (lookupPortalType(request)) {
				case GRIDSPHERE:
					if (userInfo != null) {
						email = userInfo.get("user.email");
					}
					break;
				case PLUTO:
				case UPORTAL:
					if (userInfo != null) {
						email = userInfo.get("user.home-info.online.email");
					}
			}

			debugPrint(request,"EMail="+email);
			return email;
		}

	@SuppressWarnings("unchecked")
		private static String getGridsphereFullName(PortletRequest request) {
			String fullName = null;
			Map<String,String> userInfo = (Map<String,String>) request.getAttribute(PortletRequest.USER_INFO);
			if (userInfo != null) {
				fullName = userInfo.get("user.name.full");
			}
			return fullName;
		}

}
