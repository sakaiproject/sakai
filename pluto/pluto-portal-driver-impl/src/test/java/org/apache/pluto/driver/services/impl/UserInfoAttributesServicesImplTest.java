/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.driver.services.impl;

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;

import org.apache.pluto.PortletContainerException;
import org.apache.pluto.spi.optional.P3PAttributes;

import junit.framework.TestCase;

/**
 * JUnit test class for UserInfoAttributesServicesImpl.
 *
 */
public class UserInfoAttributesServicesImplTest extends TestCase {
	/* Represents properties in user-info-attributes.properties */
	private Properties props = new Properties();
	//Test data
	/* Test user - used in MockPortletRequest */
	private static final String TEST_REMOTE_USER = "tomcat";
	private static final String TEST_USER_GENDER = "male";
	private static final String TEST_USER_NAME_GIVEN = "Catalina";
	private static final String TEST_USER_NAME_FAMILY = "Tomcat";

	protected void setUp() throws Exception {
		super.setUp();
		props.setProperty(TEST_REMOTE_USER + "." + P3PAttributes.USER_GENDER, TEST_USER_GENDER);
		props.setProperty(TEST_REMOTE_USER + "." + P3PAttributes.USER_NAME_GIVEN, TEST_USER_NAME_GIVEN);
		props.setProperty(TEST_REMOTE_USER + "." + P3PAttributes.USER_NAME_FAMILY, TEST_USER_NAME_FAMILY);
	}

	/*
	 * Test method for 'org.apache.pluto.services.optional.UserInfoAttributesServiceImpl.getUserInfo(PortletRequest)'
	 */
	public void testGetAttributes() {
		try {
			Map map = null;
			UserInfoAttributesServiceImpl uias = UserInfoAttributesServiceImpl.getInstance(props);
			PortletRequest pr = new MockPortletRequest();
			map = uias.getUserInfo(pr);
			String sex = (String)map.get(P3PAttributes.USER_GENDER);
			assertTrue(sex.equals(TEST_USER_GENDER));
			System.out.println("Sex: " + sex);
			String fname = (String)map.get(P3PAttributes.USER_NAME_GIVEN);
			assertTrue(fname.equals(TEST_USER_NAME_GIVEN));
			System.out.println("first name: " + fname);
			String lname = (String)map.get(P3PAttributes.USER_NAME_FAMILY);
			assertTrue(lname.equals(TEST_USER_NAME_FAMILY));
			System.out.println("last name: " + lname);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (PortletContainerException e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}

	/**
	 * Stubbed implementation used only by this class where only
	 * getRemoteUser() has been implemented to return a constant.
	 *
	 */
	public class MockPortletRequest implements PortletRequest {

		public boolean isWindowStateAllowed(WindowState arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isPortletModeAllowed(PortletMode arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		public PortletMode getPortletMode() {
			// TODO Auto-generated method stub
			return null;
		}

		public WindowState getWindowState() {
			// TODO Auto-generated method stub
			return null;
		}

		public PortletPreferences getPreferences() {
			// TODO Auto-generated method stub
			return null;
		}

		public PortletSession getPortletSession() {
			// TODO Auto-generated method stub
			return null;
		}

		public PortletSession getPortletSession(boolean arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getProperty(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public Enumeration getProperties(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public Enumeration getPropertyNames() {
			// TODO Auto-generated method stub
			return null;
		}

		public PortalContext getPortalContext() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getAuthType() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getContextPath() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRemoteUser() {
			return TEST_REMOTE_USER;
		}

		public Principal getUserPrincipal() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isUserInRole(String arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		public Object getAttribute(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public Enumeration getAttributeNames() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getParameter(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public Enumeration getParameterNames() {
			// TODO Auto-generated method stub
			return null;
		}

		public String[] getParameterValues(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		public Map getParameterMap() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isSecure() {
			// TODO Auto-generated method stub
			return false;
		}

		public void setAttribute(String arg0, Object arg1) {
			// TODO Auto-generated method stub

		}

		public void removeAttribute(String arg0) {
			// TODO Auto-generated method stub

		}

		public String getRequestedSessionId() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isRequestedSessionIdValid() {
			// TODO Auto-generated method stub
			return false;
		}

		public String getResponseContentType() {
			// TODO Auto-generated method stub
			return null;
		}

		public Enumeration getResponseContentTypes() {
			// TODO Auto-generated method stub
			return null;
		}

		public Locale getLocale() {
			// TODO Auto-generated method stub
			return null;
		}

		public Enumeration getLocales() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getScheme() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getServerName() {
			// TODO Auto-generated method stub
			return null;
		}

		public int getServerPort() {
			// TODO Auto-generated method stub
			return 0;
		}

	}
}
