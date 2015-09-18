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

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;
import org.sakaiproject.tool.api.Session;

public class SakaiScriptCheckSessionTest extends AbstractCXFTest {
	public static final String SESSION_ID = "***SESSION_HAS_BEEN_MOCKERIZED***";
	public static final String NULL_SESSION = "***NULL_SESSION***";
	private static final String SOAP_OPERATION = "checkSession";

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
		Session mockSession = mock(Session.class);
		when(service.sessionManager.getSession(SESSION_ID)).thenReturn(mockSession);
		when(service.sessionManager.getSession(NULL_SESSION)).thenReturn(null);
	}

	@Test
	public void testCheckSession() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);

		// client result
		String result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals(SESSION_ID, result);
	}

	@Test
	public void testCheckSessionFail() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", NULL_SESSION);

		// client result
		String result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals("", result);
	}
}
