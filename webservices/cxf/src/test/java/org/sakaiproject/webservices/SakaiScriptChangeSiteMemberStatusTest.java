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
import org.sakaiproject.user.api.User;
import org.junit.rules.ExpectedException;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.RuntimeException;

public class SakaiScriptChangeSiteMemberStatusTest extends AbstractCXFTest {
	public static final String SESSION_ID = "***SESSION_HAS_BEEN_MOCKERIZED***";
	public static final String NULL_SESSION = "***NULL_SESSION***";
	private static final String SOAP_OPERATION = "changeSiteMemberStatus";

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

		User mockUser = mock(User.class);
		User mockUser2 = mock(User.class);
		AuthzGroup mockAuthzGroup = mock(AuthzGroup.class);
		Member mockMember = mock(Member.class);


		try {
			when(service.userDirectoryService.getUserByEid("userEid")).thenReturn(mockUser);
			when(service.userDirectoryService.getUserByEid("nouser")).thenReturn(null);
			when(service.userDirectoryService.getUserByEid("userEidNoPerm")).thenReturn(mockUser2);
			when(service.siteService.siteReference("siteid")).thenReturn("realmId");
			when(service.authzGroupService.allowUpdate("realmId")).thenReturn(true);
			when(service.siteService.allowUpdateSiteMembership("siteid")).thenReturn(true);
			when(service.authzGroupService.getAuthzGroup("realmId")).thenReturn(mockAuthzGroup);
			when(mockUser.getId()).thenReturn("userId");
			when(mockUser2.getId()).thenReturn("userEidNoPerm");
			when(mockAuthzGroup.getMember("userId")).thenReturn(mockMember);
			when(mockAuthzGroup.getMember("userIdNoPerm")).thenReturn(null);
		} catch (Exception e) {

		}


		Session mockSession = mock(Session.class);
		when(service.sessionManager.getSession(SESSION_ID)).thenReturn(mockSession);
		when(service.sessionManager.getSession(NULL_SESSION)).thenReturn(null);
		when(mockSession.getUserId()).thenReturn("admin");
	}

	@Test
	public void testChangeSiteMemberStatus() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);
		client.query("siteid", "siteid");
		client.query("eid", "userEid");
		client.query("active", true);


		// client result
		String result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals("success", result);
	}

	@Test
	public void testChangeSiteMemberStatusNotExitingUser() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);
		client.query("siteid", "siteid");
		client.query("eid", "nouser");
		client.query("active", true);

		// client result
		thrown.expect(RuntimeException.class);
		client.get(String.class);

	}

	@Test
	public void testSetChangeSiteMemberStatusNotMember() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);
		client.query("siteid", "siteid");
		client.query("eid", "userEidNoPerm");
		client.query("active", true);

		// client result
		String result = client.get(String.class);

		// client result
		assertNotNull(result);
		assertEquals("WS changeSiteMemberStatus(): User: " + "userEidNoPerm" + " does not exist in site : " + "siteid", result);


	}
}
