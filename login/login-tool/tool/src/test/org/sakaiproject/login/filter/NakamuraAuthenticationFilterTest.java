/**********************************************************************************
 * Copyright (c) 2010 Sakai Foundation
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
package org.sakaiproject.login.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.hybrid.util.NakamuraAuthenticationHelper;
import org.sakaiproject.hybrid.util.NakamuraAuthenticationHelper.AuthInfo;
import org.sakaiproject.login.filter.NakamuraAuthenticationFilter.NakamuraHttpServletRequestWrapper;
import org.sakaiproject.login.filter.NakamuraAuthenticationFilter.NakamuraPrincipal;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * @see NakamuraAuthenticationFilter
 */
@RunWith(MockitoJUnitRunner.class)
public class NakamuraAuthenticationFilterTest {
	static final String MOCK_PRINCIPAL = "admin";
	static final String MOCK_FIRSTNAME = "Admin";
	static final String MOCK_LASTNAME = "User";
	static final String MOCK_EMAIL = "admin@sakai.invalid";

	NakamuraAuthenticationFilter nakamuraAuthenticationFilter;

	@Mock
	SessionManager sessionManager;
	@Mock
	UserDirectoryService userDirectoryService;
	@Mock
	UsageSessionService usageSessionService;
	@Mock
	EventTrackingService eventTrackingService;
	@Mock
	AuthzGroupService authzGroupService;
	@Mock
	ComponentManager componentManager;
	@Mock
	ServerConfigurationService serverConfigurationService;
	@Mock
	FilterConfig filterConfig;
	@Mock
	HttpServletRequest request;
	@Mock
	HttpServletResponse response;
	@Mock
	FilterChain chain;
	@Mock
	NakamuraAuthenticationHelper nakamuraAuthenticationHelper;
	@Mock
	AuthInfo authInfo;

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
		when(componentManager.get(SessionManager.class)).thenReturn(
				sessionManager);
		when(componentManager.get(UserDirectoryService.class)).thenReturn(
				userDirectoryService);
		when(componentManager.get(UsageSessionService.class)).thenReturn(
				usageSessionService);
		when(componentManager.get(EventTrackingService.class)).thenReturn(
				eventTrackingService);
		when(componentManager.get(AuthzGroupService.class)).thenReturn(
				authzGroupService);
		when(componentManager.get(ServerConfigurationService.class))
				.thenReturn(serverConfigurationService);

		nakamuraAuthenticationFilter = new NakamuraAuthenticationFilter();

		when(
				serverConfigurationService.getBoolean(
						NakamuraAuthenticationFilter.CONFIG_ENABLED, false))
				.thenReturn(true);
		when(
				serverConfigurationService.getString(
						NakamuraAuthenticationFilter.CONFIG_VALIDATE_URL,
						nakamuraAuthenticationFilter.validateUrl)).thenReturn(
				nakamuraAuthenticationFilter.validateUrl);
		when(
				serverConfigurationService.getString(
						NakamuraAuthenticationFilter.CONFIG_PRINCIPAL,
						nakamuraAuthenticationFilter.principal)).thenReturn(
				nakamuraAuthenticationFilter.principal);
		when(
				serverConfigurationService.getString(
						NakamuraAuthenticationFilter.CONFIG_HOST_NAME,
						nakamuraAuthenticationFilter.hostname)).thenReturn(
				nakamuraAuthenticationFilter.hostname);
		when(serverConfigurationService.getBoolean("container.login", false))
				.thenReturn(true);

		when(
				nakamuraAuthenticationHelper
						.getPrincipalLoggedIntoNakamura(request)).thenReturn(
				authInfo);
		when(authInfo.getPrincipal()).thenReturn(MOCK_PRINCIPAL);
		when(authInfo.getFirstName()).thenReturn(MOCK_FIRSTNAME);
		when(authInfo.getLastName()).thenReturn(MOCK_LASTNAME);
		when(authInfo.getEmailAddress()).thenReturn(MOCK_EMAIL);

		nakamuraAuthenticationFilter.setupTestCase(componentManager,
				nakamuraAuthenticationHelper);
		nakamuraAuthenticationFilter.init(filterConfig);
	}

	/**
	 * @see NakamuraAuthenticationFilter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, FilterChain)
	 * @throws Exception
	 */
	@Test
	public void testDoFilterEnabled() throws Exception {
		try {
			nakamuraAuthenticationFilter.doFilter(request, response, chain);
			// verify custom logic executed
			verify(chain, times(1)).doFilter(
					isA(NakamuraHttpServletRequestWrapper.class), eq(response));
			// verify we did not fall through custom logic
			verify(chain, never()).doFilter(request, response);
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
	}

	/**
	 * @see NakamuraAuthenticationFilter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, FilterChain)
	 * @throws Exception
	 */
	@Test
	public void testDoFilterDisabled() throws Exception {
		when(
				serverConfigurationService.getBoolean(
						NakamuraAuthenticationFilter.CONFIG_ENABLED, false))
				.thenReturn(false);
		nakamuraAuthenticationFilter.init(filterConfig);
		try {
			nakamuraAuthenticationFilter.doFilter(request, response, chain);
			// verify we did not execute custom logic
			verify(chain, never()).doFilter(
					isA(NakamuraHttpServletRequestWrapper.class), eq(response));
			// verify we did not execute custom logic
			verify(chain, times(1)).doFilter(request, response);
		} catch (Throwable e) {
			fail("No exception should be thrown");
		}
	}

	/**
	 * @see NakamuraAuthenticationFilter#init(FilterConfig)
	 */
	@Test
	public void testInitNullSessionManager() {
		when(componentManager.get(SessionManager.class)).thenReturn(null);
		try {
			nakamuraAuthenticationFilter.init(filterConfig);
			fail("IllegalStateException should be thrown");
		} catch (IllegalStateException e) {
			assertNotNull(e);
		} catch (Throwable e) {
			fail("Throwable should not be thrown");
		}
	}

	/**
	 * @see NakamuraAuthenticationFilter#init(FilterConfig)
	 */
	@Test
	public void testInitNullUserDirectoryService() {
		when(componentManager.get(UserDirectoryService.class)).thenReturn(null);
		try {
			nakamuraAuthenticationFilter.init(filterConfig);
			fail("IllegalStateException should be thrown");
		} catch (IllegalStateException e) {
			assertNotNull(e);
		} catch (Throwable e) {
			fail("Throwable should not be thrown");
		}
	}

	/**
	 * @see NakamuraAuthenticationFilter#init(FilterConfig)
	 */
	@Test
	public void testInitNullUsageSessionService() {
		when(componentManager.get(UsageSessionService.class)).thenReturn(null);
		try {
			nakamuraAuthenticationFilter.init(filterConfig);
			fail("IllegalStateException should be thrown");
		} catch (IllegalStateException e) {
			assertNotNull(e);
		} catch (Throwable e) {
			fail("Throwable should not be thrown");
		}
	}

	/**
	 * @see NakamuraAuthenticationFilter#init(FilterConfig)
	 */
	@Test
	public void testInitNullEventTrackingService() {
		when(componentManager.get(EventTrackingService.class)).thenReturn(null);
		try {
			nakamuraAuthenticationFilter.init(filterConfig);
			fail("IllegalStateException should be thrown");
		} catch (IllegalStateException e) {
			assertNotNull(e);
		} catch (Throwable e) {
			fail("Throwable should not be thrown");
		}
	}

	/**
	 * @see NakamuraAuthenticationFilter#init(FilterConfig)
	 */
	@Test
	public void testInitNullAuthzGroupService() {
		when(componentManager.get(AuthzGroupService.class)).thenReturn(null);
		try {
			nakamuraAuthenticationFilter.init(filterConfig);
			fail("IllegalStateException should be thrown");
		} catch (IllegalStateException e) {
			assertNotNull(e);
		} catch (Throwable e) {
			fail("Throwable should not be thrown");
		}
	}

	/**
	 * @see NakamuraAuthenticationFilter#init(FilterConfig)
	 */
	@Test
	public void testInitNullServerConfigurationService() {
		when(componentManager.get(ServerConfigurationService.class))
				.thenReturn(null);
		try {
			nakamuraAuthenticationFilter.init(filterConfig);
			fail("IllegalStateException should be thrown");
		} catch (IllegalStateException e) {
			assertNotNull(e);
		} catch (Throwable e) {
			fail("Throwable should not be thrown");
		}
	}

	/**
	 * @see NakamuraAuthenticationFilter#init(FilterConfig)
	 */
	@Test
	public void testContainerLoginDisabled() {
		/*
		 * need a new one otherwise filterEnabled already == true and we end up
		 * accidentally disabling the filter
		 */
		nakamuraAuthenticationFilter = new NakamuraAuthenticationFilter();
		nakamuraAuthenticationFilter.setupTestCase(componentManager,
				nakamuraAuthenticationHelper);
		when(serverConfigurationService.getBoolean("container.login", false))
				.thenReturn(false);
		try {
			nakamuraAuthenticationFilter.init(filterConfig);
			fail("IllegalStateException should be thrown");
		} catch (IllegalStateException e) {
			assertNotNull("IllegalStateException should be thrown", e);
		} catch (Throwable e) {
			fail("IllegalStateException should be thrown");
		}
	}

	/**
	 * @see NakamuraAuthenticationFilter#init(FilterConfig)
	 */
	@Test
	public void testTopLoginDisabled() {
		/*
		 * need a new one otherwise filterEnabled already == true and we end up
		 * accidentally disabling the filter
		 */
		nakamuraAuthenticationFilter = new NakamuraAuthenticationFilter();
		nakamuraAuthenticationFilter.setupTestCase(componentManager,
				nakamuraAuthenticationHelper);
		when(serverConfigurationService.getBoolean("top.login", false))
				.thenReturn(true);
		try {
			nakamuraAuthenticationFilter.init(filterConfig);
		} catch (Throwable e) {
			fail("Throwable should NOT be thrown");
		}
	}

	/**
	 * @see NakamuraPrincipal
	 */
	@Test
	public void testNakamuraPrincipal() {
		// test good paths first
		NakamuraPrincipal nakamuraPrincipal1 = new NakamuraPrincipal(
				MOCK_PRINCIPAL);
		NakamuraPrincipal nakamuraPrincipal2 = new NakamuraPrincipal(
				MOCK_PRINCIPAL);
		NakamuraPrincipal nakamuraPrincipal3 = new NakamuraPrincipal("lance");
		assertNotNull(nakamuraPrincipal1);
		assertNotNull(nakamuraPrincipal2);
		assertNotNull(nakamuraPrincipal3);
		assertEquals(MOCK_PRINCIPAL, nakamuraPrincipal1.getName());
		assertEquals(MOCK_PRINCIPAL, nakamuraPrincipal2.getName());
		assertEquals("lance", nakamuraPrincipal3.getName());
		assertEquals(nakamuraPrincipal1, nakamuraPrincipal1);
		assertEquals(nakamuraPrincipal1, nakamuraPrincipal2);
		assertEquals(nakamuraPrincipal1.toString(),
				nakamuraPrincipal2.toString());
		assertEquals(nakamuraPrincipal1.hashCode(),
				nakamuraPrincipal2.hashCode());
		assertNotSame(nakamuraPrincipal1, nakamuraPrincipal3);
		assertNotSame(nakamuraPrincipal2, nakamuraPrincipal3);
		assertNotSame(nakamuraPrincipal1.toString(),
				nakamuraPrincipal3.toString());
		assertNotSame(nakamuraPrincipal1.hashCode(),
				nakamuraPrincipal3.hashCode());
		assertFalse(nakamuraPrincipal1.equals(userDirectoryService));
		// test bad constructor arguments
		try { // null parameter
			new NakamuraPrincipal(null);
			fail("IllegalArgumentException should be thrown");
		} catch (IllegalArgumentException e) {
			assertNotNull("IllegalArgumentException should be thrown", e);
		} catch (Throwable e) {
			fail("IllegalArgumentException should be thrown");
		}
		try { // empty string parameter
			new NakamuraPrincipal("");
			fail("IllegalArgumentException should be thrown");
		} catch (IllegalArgumentException e) {
			assertNotNull("IllegalArgumentException should be thrown", e);
		} catch (Throwable e) {
			fail("IllegalArgumentException should be thrown");
		}
	}

	/**
	 * @see NakamuraAuthenticationFilter#setupTestCase(ComponentManager,
	 *      NakamuraAuthenticationHelper)
	 */
	@Test
	public void testSetupTestCase() {
		try {
			nakamuraAuthenticationFilter.setupTestCase(null,
					nakamuraAuthenticationHelper);
			fail("IllegalArgumentException should be thrown");
		} catch (IllegalArgumentException e) {
			assertNotNull("IllegalArgumentException should be thrown", e);
		} catch (Throwable e) {
			fail("IllegalArgumentException should be thrown");
		}
		try {
			nakamuraAuthenticationFilter.setupTestCase(componentManager, null);
			fail("IllegalArgumentException should be thrown");
		} catch (IllegalArgumentException e) {
			assertNotNull("IllegalArgumentException should be thrown", e);
		} catch (Throwable e) {
			fail("IllegalArgumentException should be thrown");
		}
	}

	/**
	 * @see NakamuraHttpServletRequestWrapper
	 */
	@Test
	public void testNakamuraHttpServletRequestWrapper() {
		// test good path
		NakamuraHttpServletRequestWrapper nakamuraHttpServletRequestWrapper = new NakamuraHttpServletRequestWrapper(
				request, MOCK_PRINCIPAL);
		assertNotNull(nakamuraHttpServletRequestWrapper);
		assertEquals(MOCK_PRINCIPAL,
				nakamuraHttpServletRequestWrapper.getRemoteUser());
		assertEquals(MOCK_PRINCIPAL, nakamuraHttpServletRequestWrapper
				.getUserPrincipal().getName());
		assertFalse(nakamuraHttpServletRequestWrapper.isUserInRole("foo"));
		// test bad parameters
		try {
			nakamuraHttpServletRequestWrapper = new NakamuraHttpServletRequestWrapper(
					null, MOCK_PRINCIPAL);
			fail("IllegalArgumentException should be thrown");
		} catch (IllegalArgumentException e) {
			assertNotNull("IllegalArgumentException should be thrown", e);
		} catch (Throwable e) {
			fail("IllegalArgumentException should be thrown");
		}
		try {
			nakamuraHttpServletRequestWrapper = new NakamuraHttpServletRequestWrapper(
					request, null);
			fail("IllegalArgumentException should be thrown");
		} catch (IllegalArgumentException e) {
			assertNotNull("IllegalArgumentException should be thrown", e);
		} catch (Throwable e) {
			fail("IllegalArgumentException should be thrown");
		}
		try {
			nakamuraHttpServletRequestWrapper = new NakamuraHttpServletRequestWrapper(
					request, "");
			fail("IllegalArgumentException should be thrown");
		} catch (IllegalArgumentException e) {
			assertNotNull("IllegalArgumentException should be thrown", e);
		} catch (Throwable e) {
			fail("IllegalArgumentException should be thrown");
		}
	}
}
