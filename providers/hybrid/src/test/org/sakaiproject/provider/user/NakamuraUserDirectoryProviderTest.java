/**
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.provider.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.hybrid.util.NakamuraAuthenticationHelper;
import org.sakaiproject.hybrid.util.NakamuraAuthenticationHelper.AuthInfo;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.user.api.UserEdit;

/**
 * @see NakamuraUserDirectoryProvider
 */
@RunWith(MockitoJUnitRunner.class)
public class NakamuraUserDirectoryProviderTest {
	static final String MOCK_PRINCIPAL = "admin";
	static final String MOCK_FIRSTNAME = "Admin";
	static final String MOCK_LASTNAME = "User";
	static final String MOCK_EMAIL = "admin@sakai.invalid";

	NakamuraUserDirectoryProvider nakamuraUserDirectoryProvider;

	@Mock
	HttpServletRequest request;
	@Mock
	ComponentManager componentManager;
	@Mock
	ThreadLocalManager threadLocalManager;
	@Mock
	ServerConfigurationService serverConfigurationService;
	@Mock
	NakamuraAuthenticationHelper nakamuraAuthenticationHelper;
	@Mock
	AuthInfo authInfo;
	@Mock
	UserEdit userEdit;

	@BeforeClass
	public static void setupClass() {
		Properties log4jProperties = new Properties();
		log4jProperties.put("log4j.rootLogger", "ALL, A1");
		log4jProperties.put("log4j.appender.A1",
				"org.apache.log4j.ConsoleAppender");
		log4jProperties.put("log4j.appender.A1.layout",
				"org.apache.log4j.PatternLayout");
		log4jProperties.put("log4j.appender.A1.layout.ConversionPattern",
				PatternLayout.TTCC_CONVERSION_PATTERN);
		log4jProperties.put("log4j.threshold", "ALL");
		PropertyConfigurator.configure(log4jProperties);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// perform mock injection
		nakamuraUserDirectoryProvider = new NakamuraUserDirectoryProvider();
		nakamuraUserDirectoryProvider.setComponentManager(componentManager);
		nakamuraUserDirectoryProvider.setThreadLocalManager(threadLocalManager);
		nakamuraUserDirectoryProvider
				.setServerConfigurationService(serverConfigurationService);
		nakamuraUserDirectoryProvider.nakamuraAuthenticationHelper = nakamuraAuthenticationHelper;
		// mock sakai.proeprties settings.
		when(
				serverConfigurationService.getString(
						NakamuraUserDirectoryProvider.CONFIG_VALIDATE_URL,
						nakamuraUserDirectoryProvider.validateUrl)).thenReturn(
				nakamuraUserDirectoryProvider.validateUrl);
		when(
				serverConfigurationService.getString(
						NakamuraUserDirectoryProvider.CONFIG_PRINCIPAL,
						nakamuraUserDirectoryProvider.principal)).thenReturn(
				nakamuraUserDirectoryProvider.principal);
		when(
				serverConfigurationService.getString(
						NakamuraUserDirectoryProvider.CONFIG_HOST_NAME,
						nakamuraUserDirectoryProvider.hostname)).thenReturn(
				nakamuraUserDirectoryProvider.hostname);
		;
		when(
				nakamuraAuthenticationHelper
						.getPrincipalLoggedIntoNakamura(request)).thenReturn(
				authInfo);
		when(authInfo.getPrincipal()).thenReturn(MOCK_PRINCIPAL);
		when(authInfo.getFirstName()).thenReturn(MOCK_FIRSTNAME);
		when(authInfo.getLastName()).thenReturn(MOCK_LASTNAME);
		when(authInfo.getEmailAddress()).thenReturn(MOCK_EMAIL);

		// return HttpServletRequest from ThreadLocal
		when(
				threadLocalManager
						.get(NakamuraUserDirectoryProvider.CURRENT_HTTP_REQUEST))
				.thenReturn(request);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.provider.user.NakamuraUserDirectoryProvider#init()}
	 */
	@Test
	public void testInit() {
		// grab original default values
		final String originalValidateUrl = nakamuraUserDirectoryProvider.validateUrl;
		final String originalPrincipal = nakamuraUserDirectoryProvider.principal;
		final String originalHostname = nakamuraUserDirectoryProvider.hostname;
		try {
			nakamuraUserDirectoryProvider.init();
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		verify(serverConfigurationService, times(1)).getString(
				NakamuraUserDirectoryProvider.CONFIG_VALIDATE_URL,
				originalValidateUrl);
		verify(serverConfigurationService, times(1)).getString(
				NakamuraUserDirectoryProvider.CONFIG_PRINCIPAL,
				originalPrincipal);
		verify(serverConfigurationService, times(1)).getString(
				NakamuraUserDirectoryProvider.CONFIG_HOST_NAME,
				originalHostname);
		assertEquals(originalValidateUrl,
				nakamuraUserDirectoryProvider.validateUrl);
		assertEquals(originalPrincipal, nakamuraUserDirectoryProvider.principal);
		assertEquals(originalHostname, nakamuraUserDirectoryProvider.hostname);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.provider.user.NakamuraUserDirectoryProvider#authenticateUser(java.lang.String, org.sakaiproject.user.api.UserEdit, java.lang.String)}
	 */
	@Test
	public void testAuthenticateUser() {
		// test good path first
		Boolean answer = null;
		try {
			answer = nakamuraUserDirectoryProvider.authenticateUser(
					MOCK_PRINCIPAL, userEdit, "password not used");
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		assertNotNull(answer);
		assertTrue(answer);
		verify(userEdit, times(1)).setEid(MOCK_PRINCIPAL);
		verify(userEdit, times(1)).setFirstName(MOCK_FIRSTNAME);
		verify(userEdit, times(1)).setLastName(MOCK_LASTNAME);
		verify(userEdit, times(1)).setEmail(MOCK_EMAIL);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.provider.user.NakamuraUserDirectoryProvider#authenticateUser(java.lang.String, org.sakaiproject.user.api.UserEdit, java.lang.String)}
	 */
	@Test
	public void testAuthenticateUserNullEid() {
		Boolean answer = null;
		// test null eid
		try {
			answer = nakamuraUserDirectoryProvider.authenticateUser(null,
					userEdit, "password not used");
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		assertNotNull(answer);
		assertFalse(answer);
		verify(userEdit, never()).setFirstName(any(String.class));
		verify(userEdit, never()).setLastName(MOCK_LASTNAME);
		verify(userEdit, never()).setEmail(MOCK_EMAIL);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.provider.user.NakamuraUserDirectoryProvider#authenticateUser(java.lang.String, org.sakaiproject.user.api.UserEdit, java.lang.String)}
	 */
	@Test
	public void testAuthenticateUserNotAuthenticated() {
		when(
				nakamuraAuthenticationHelper
						.getPrincipalLoggedIntoNakamura(request)).thenReturn(
				null);
		Boolean answer = null;
		try {
			answer = nakamuraUserDirectoryProvider.authenticateUser(
					MOCK_PRINCIPAL, userEdit, "password not used");
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		assertNotNull(answer);
		assertFalse(answer);
		verify(userEdit, never()).setFirstName(any(String.class));
		verify(userEdit, never()).setLastName(MOCK_LASTNAME);
		verify(userEdit, never()).setEmail(MOCK_EMAIL);
	}

	// eid.equalsIgnoreCase(authInfo.getPrincipal()

	/**
	 * Test method for
	 * {@link org.sakaiproject.provider.user.NakamuraUserDirectoryProvider#authenticateUser(java.lang.String, org.sakaiproject.user.api.UserEdit, java.lang.String)}
	 */
	@Test
	public void testAuthenticateEidNotEqualToAuthInfoPrincipal() {
		/*
		 * An edge case where the passed eid does not match the user logged into
		 * Nakamura
		 */
		when(authInfo.getPrincipal()).thenReturn("lance");
		Boolean answer = null;
		try {
			answer = nakamuraUserDirectoryProvider.authenticateUser(
					MOCK_PRINCIPAL, userEdit, "password not used");
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		assertNotNull(answer);
		assertFalse(answer);
		verify(userEdit, never()).setFirstName(any(String.class));
		verify(userEdit, never()).setLastName(MOCK_LASTNAME);
		verify(userEdit, never()).setEmail(MOCK_EMAIL);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.provider.user.NakamuraUserDirectoryProvider#authenticateWithProviderFirst(java.lang.String)}
	 */
	@Test
	public void testAuthenticateWithProviderFirst() {
		Boolean answer = null;
		try {
			answer = nakamuraUserDirectoryProvider
					.authenticateWithProviderFirst(MOCK_PRINCIPAL);
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		assertNotNull(answer);
		assertFalse("Implementation always returns false for now", answer);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.provider.user.NakamuraUserDirectoryProvider#findUserByEmail(org.sakaiproject.user.api.UserEdit, java.lang.String)}
	 */
	@Test
	public void testFindUserByEmail() {
		Boolean answer = null;
		try {
			answer = nakamuraUserDirectoryProvider.findUserByEmail(userEdit,
					MOCK_EMAIL);
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		assertNotNull(answer);
		assertTrue(answer);
		verify(userEdit, times(1)).setEid(MOCK_PRINCIPAL);
		verify(userEdit, times(1)).setFirstName(MOCK_FIRSTNAME);
		verify(userEdit, times(1)).setLastName(MOCK_LASTNAME);
		verify(userEdit, times(1)).setEmail(MOCK_EMAIL);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.provider.user.NakamuraUserDirectoryProvider#findUserByEmail(org.sakaiproject.user.api.UserEdit, java.lang.String)}
	 */
	@Test
	public void testFindUserByEmailNullEmail() {
		Boolean answer = null;
		try {
			answer = nakamuraUserDirectoryProvider.findUserByEmail(userEdit,
					null);
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		assertNotNull(answer);
		assertFalse(answer);
		verify(userEdit, never()).setEid(MOCK_PRINCIPAL);
		verify(userEdit, never()).setFirstName(MOCK_FIRSTNAME);
		verify(userEdit, never()).setLastName(MOCK_LASTNAME);
		verify(userEdit, never()).setEmail(MOCK_EMAIL);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.provider.user.NakamuraUserDirectoryProvider#findUserByEmail(org.sakaiproject.user.api.UserEdit, java.lang.String)}
	 */
	@Test
	public void testFindUserByEmailNotAuthenticated() {
		when(
				nakamuraAuthenticationHelper
						.getPrincipalLoggedIntoNakamura(request)).thenReturn(
				null);
		Boolean answer = null;
		try {
			answer = nakamuraUserDirectoryProvider.findUserByEmail(userEdit,
					MOCK_EMAIL);
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		assertNotNull(answer);
		assertFalse(answer);
		verify(userEdit, never()).setEid(MOCK_PRINCIPAL);
		verify(userEdit, never()).setFirstName(MOCK_FIRSTNAME);
		verify(userEdit, never()).setLastName(MOCK_LASTNAME);
		verify(userEdit, never()).setEmail(MOCK_EMAIL);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.provider.user.NakamuraUserDirectoryProvider#findUserByEmail(org.sakaiproject.user.api.UserEdit, java.lang.String)}
	 */
	@Test
	public void testFindUserByEmailEidNotEqualToAuthInfoPrincipal() {
		when(authInfo.getEmailAddress()).thenReturn("foo@bar.edu");
		Boolean answer = null;
		try {
			answer = nakamuraUserDirectoryProvider.findUserByEmail(userEdit,
					MOCK_EMAIL);
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		assertNotNull(answer);
		assertFalse(answer);
		verify(userEdit, never()).setEid(MOCK_PRINCIPAL);
		verify(userEdit, never()).setFirstName(MOCK_FIRSTNAME);
		verify(userEdit, never()).setLastName(MOCK_LASTNAME);
		verify(userEdit, never()).setEmail(MOCK_EMAIL);
	}

	/**
	 * Test method for
	 * {@link NakamuraUserDirectoryProvider#getHttpServletRequest()}
	 */
	@Test
	public void testGetHttpServletRequest() {
		HttpServletRequest httpServletRequest = null;
		try {
			httpServletRequest = nakamuraUserDirectoryProvider
					.getHttpServletRequest();
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		assertNotNull(httpServletRequest);
		assertTrue(request == httpServletRequest);
	}

	/**
	 * Test method for
	 * {@link NakamuraUserDirectoryProvider#getHttpServletRequest()}
	 */
	@Test
	public void testGetHttpServletRequestNullHttpServletRequest() {
		when(
				threadLocalManager
						.get(NakamuraUserDirectoryProvider.CURRENT_HTTP_REQUEST))
				.thenReturn(null);
		try {
			nakamuraUserDirectoryProvider.getHttpServletRequest();
			fail("IllegalStateException should be thrown");
		} catch (IllegalStateException e) {
			assertNotNull("IllegalStateException should be thrown", e);
		} catch (Throwable e) {
			fail("IllegalStateException should be thrown");
		}
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.provider.user.NakamuraUserDirectoryProvider#getUser(org.sakaiproject.user.api.UserEdit)}
	 */
	@Test
	public void testGetUser() {
		// test good path
		when(userEdit.getEid()).thenReturn(MOCK_PRINCIPAL);
		Boolean answer = null;
		try {
			answer = nakamuraUserDirectoryProvider.getUser(userEdit);
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		assertNotNull(answer);
		assertTrue(answer);
		verify(userEdit, times(1)).setFirstName(MOCK_FIRSTNAME);
		verify(userEdit, times(1)).setLastName(MOCK_LASTNAME);
		verify(userEdit, times(1)).setEmail(MOCK_EMAIL);
		// test bad parameter
		try {
			answer = nakamuraUserDirectoryProvider.getUser(null);
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
		assertFalse(answer);
	}

	/**
	 * Test method for
	 * {@link org.sakaiproject.provider.user.NakamuraUserDirectoryProvider#getUsers(java.util.Collection)}
	 */
	@Test
	public void testGetUsers() {
		nakamuraUserDirectoryProvider.getUsers(null);
	}

}
