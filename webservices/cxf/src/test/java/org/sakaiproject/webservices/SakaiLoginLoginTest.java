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
import static org.mockito.Mockito.eq;

import org.apache.cxf.jaxrs.client.WebClient;
import javax.ws.rs.core.Form;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.IdPwEvidence;

public class SakaiLoginLoginTest extends AbstractCXFTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public static final String SESSION_ID = "***SESSION_HAS_BEEN_MOCKERIZED***";
	private static final String SOAP_OPERATION = "login";

	@Override
	protected <T extends AbstractWebService> Class<T> getTestClass() {
		return (Class<T>) SakaiLogin.class;
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
		when(service.serverConfigurationService.getBoolean("webservices.allowlogin", false)).thenReturn(true);

		Session mockSession = mock(Session.class);
		when(mockSession.getId()).thenReturn(SESSION_ID);
		when(service.sessionManager.startSession()).thenReturn(mockSession);

		Authentication mockAuth = mock(Authentication.class);
		when(mockAuth.getUid()).thenReturn("admin");

		IdPwEvidence e1 = new IdPwEvidence("admin", "admin", "127.0.0.1");
		IdPwEvidence e2 = new IdPwEvidence("admin", "fail", "127.0.0.1");

		try {
			when(service.authenticationManager.authenticate(eq(e1))).thenReturn(mockAuth);
			when(service.authenticationManager.authenticate(eq(e2))).thenThrow(new AuthenticationException("invalid password"));
         	} catch (AuthenticationException ae) {
		}
	}

	@Test
	public void testLogin() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		Form form = new Form();
		form.param("id", "admin").param("pw", "admin"); 

		// client result
		String result = client.post(form, String.class);

		// test verifications
		assertNotNull(result);
		assertEquals(SESSION_ID, result);
	}

	@Test
	public void testLoginFailed() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		Form form = new Form();
		form.param("id", "admin").param("pw", "fail"); 

		// client result
		thrown.expect(RuntimeException.class);
		client.post(form, String.class);
	}
}
