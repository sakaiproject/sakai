/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/

package edu.amc.sakai.user;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * Tests for an unitialized {@link SearchExecutingLdapConnectionLivenessValidator}
 * fixture. That is, it tests the initialization logic itself. This is
 * as opposed to the fixture in {@link SearchExecutingLdapConnectionLivenessValidatorTest}
 * which initializes the fixture in 
 * {@link SearchExecutingLdapConnectionLivenessValidatorTest#setUp()}.
 * 
 * @author dmccallum
 *
 */
public class SearchExecutingLdapConnectionLivenessValidatorInitializationTest extends MockObjectTestCase {

	private static final String UNIQUE_SEARCH_FILTER_TERM = "TESTING";
	private static final String LOCALHOST_IDENTIFIER = "LOCALHOST_IDENTIFIER";
	private SearchExecutingLdapConnectionLivenessValidator validator;
	private Mock mockServerConfigService;
	private ServerConfigurationService serverConfigService;
	
	protected void setUp() {
		validator = new SearchExecutingLdapConnectionLivenessValidator() {
			// we need this to be a predictable value
			protected String generateUniqueToken() {
				return UNIQUE_SEARCH_FILTER_TERM;
			}
			/**
			 * Overridden to always return a static String. This allows
			 * us to side-step issues related to host name lookup
			 * failures in the actual implementation. See
			 * http://bugs.sakaiproject.org/jira/browse/SAK-14773
			 */
			@Override
			protected String getLocalhostName() throws UnknownHostException {
				return LOCALHOST_IDENTIFIER;
			}
		};
		mockServerConfigService = new Mock(ServerConfigurationService.class);
		serverConfigService = (ServerConfigurationService) mockServerConfigService.proxy();
		validator.setServerConfigService(serverConfigService);
	}
	
	public void testInitHonorsExplicitlyInjectedHostName() {
		final String EXPECTED_HOST_NAME = "EXPECTED_HOST_NAME";
		validator.setHostName(EXPECTED_HOST_NAME);
		validator.init();
		assertEquals(EXPECTED_HOST_NAME, validator.getHostName());
	}
	
	public void testInitDefaultsHostNameToInetAddressLocalhostIfNoHostNameExplicitlyInjected() 
	throws UnknownHostException {
		validator.init();
		assertEquals(validator.getLocalhostName(), validator.getHostName());
	}
	
	public void testInitDefaultsHostNameToSakaiServerNameIfNoHostNameExplicitlyInjectedAndLocalHostLookupFails() {
		validator = new SearchExecutingLdapConnectionLivenessValidator() {
			// we need this to be a predictable value
			protected String generateUniqueToken() {
				return UNIQUE_SEARCH_FILTER_TERM;
			}
			
			protected String getLocalhostName() throws UnknownHostException {
				throw new UnknownHostException();
			}
		};
		final String EXPECTED_HOST_NAME = "EXPECTED_HOST_NAME";
		mockServerConfigService.expects(once()).method("getServerName").will(returnValue(EXPECTED_HOST_NAME));
		validator.setServerConfigService(serverConfigService);
		validator.init();
		assertEquals(EXPECTED_HOST_NAME, validator.getHostName());
	}
	
	public void testInitDefaultsHostNameToInetAddressLocalhostIfNoHostNameExplicitlyInjectedAndNoSakaiServerNameInjected() 
	throws UnknownHostException {
		validator.setServerConfigService(null);
		assertNull(validator.getServerConfigService()); // sanity check
		validator.init();
		assertEquals(validator.getLocalhostName(), 
				validator.getHostName());
	}
	
	public void testDefaultsHostNameToConstantDefaultIfNeitherInitNorSetHostNameCalled() {
		assertEquals(SearchExecutingLdapConnectionLivenessValidator.DEFAULT_HOST_NAME,
				validator.getHostName());
	}
	
}
