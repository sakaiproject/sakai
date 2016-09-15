/**
 * Copyright (c) 2005 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webservices;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.withSettings;


import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Site;
import org.junit.rules.ExpectedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.RuntimeException;

public class SakaiScriptRemoveMemberFromSiteBatchTest extends AbstractCXFTest {
	public static final String SESSION_ID = "***SESSION_HAS_BEEN_MOCKERIZED***";
	public static final String NULL_SESSION = "***NULL_SESSION***";
	private static final String SOAP_OPERATION = "removeMemberFromSiteBatch";

	public ExpectedException thrown = ExpectedException.none();

	@Override
	protected <T extends AbstractWebService> Class<T> getTestClass() {
		return (Class<T>) SakaiScript.class;
	}

	@Override
	protected String getOperation() {
		return SOAP_OPERATION;
	}

	private void addClientMocks(WebClient client) {
		addCXFClientMocks(client);
	}

	@Override
	protected void addServiceMocks(AbstractWebService service) {

		User mockUser1 = mock(User.class);
		User mockUser2 = mock(User.class);
		Site mockSite = mock(Site.class);

		when(service.securityService.isSuperUser("admin")).thenReturn(true);

		try {
			when(service.siteService.getSite("site1")).thenReturn(mockSite);
			when(service.siteService.getSite("nosite")).thenReturn(null);
			when(service.userDirectoryService.getUserByEid("user1")).thenReturn(mockUser1);
			when(service.userDirectoryService.getUserByEid("user2")).thenReturn(mockUser2);
			when(service.userDirectoryService.getUserByEid("nouser")).thenReturn(null);
			when(mockUser1.getId()).thenReturn("user1");
			when(mockUser2.getId()).thenReturn("user2");
		} catch (Exception e) {
		}


		Session mockSession = mock(Session.class);
		when(service.sessionManager.getSession(SESSION_ID)).thenReturn(mockSession);
		when(service.sessionManager.getSession(NULL_SESSION)).thenReturn(null);
		when(mockSession.getUserId()).thenReturn("admin");

	}

	@Test
	public void testRemoveMemberFromSiteBatch() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);
		client.query("siteid", "site1");
		client.query("eids", "user1,user2");

		// client result
		String result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals("success", result);
	}

	@Test
	public void testRemoveMemberFromSiteBatchNotExistingUser() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);
		client.query("siteid", "site1");
		client.query("eids", "user1,nouser");

		// client result
		thrown.expect(RuntimeException.class);
		client.get(String.class);

	}

	@Test
	public void testRemoveMemberFromSiteBatchNotExistingSite() {
		WebClient client = WebClient.create(getFullEndpointAddress());
//MODIFY
		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);
		client.query("siteid", "nosite");
		client.query("eids", "user1,nouser");

		// client result
		thrown.expect(RuntimeException.class);
		client.get(String.class);

	}


}
