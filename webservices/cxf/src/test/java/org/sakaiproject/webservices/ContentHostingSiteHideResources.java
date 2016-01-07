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

import java.util.Collections;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;

public class ContentHostingSiteHideResources extends AbstractCXFTest {
	public static final String SESSION_ID = "***SESSION_HAS_BEEN_MOCKERIZED***";
	public static final String NULL_SESSION = "***NULL_SESSION***";
	public static final String SITE_ID = "***SITE-1234567890-SITE***";
	public static final String SITE_ID_PERM_ERROR = "***SITE-THROWS_PERMISSION_EXCEPTION***";
	public static final String ROOT_COLLECTION = "/group/";
	private static final String SOAP_OPERATION = "siteHideResources";
	

	public ExpectedException thrown = ExpectedException.none();

	@Override
	protected <T extends AbstractWebService> Class<T> getTestClass() {
		return (Class<T>) ContentHosting.class;
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
		Site mockSite = mock(Site.class);
		Site mockSiteThrowsPermissionException = mock(Site.class);
		ContentCollection mockCollection = mock(ContentCollection.class);
		when(service.sessionManager.getSession(SESSION_ID)).thenReturn(mockSession);
		when(service.sessionManager.getSession(NULL_SESSION)).thenReturn(null);
		when(service.contentHostingService.getSiteCollection(SITE_ID)).thenReturn(ROOT_COLLECTION + SITE_ID);
		when(service.contentHostingService.getSiteCollection(SITE_ID_PERM_ERROR)).thenReturn(ROOT_COLLECTION + SITE_ID_PERM_ERROR);
		when(mockCollection.getMemberResources()).thenReturn(Collections.emptyList());
		when(mockSite.getId()).thenReturn(SITE_ID);
		when(mockSiteThrowsPermissionException.getId()).thenReturn(SITE_ID_PERM_ERROR);

		try {
			when(service.siteService.getSite(SITE_ID)).thenReturn(mockSite);
			when(service.siteService.getSite(SITE_ID_PERM_ERROR)).thenReturn(mockSiteThrowsPermissionException);
			when(service.contentHostingService.getCollection(ROOT_COLLECTION + SITE_ID)).thenReturn(mockCollection);
			when(service.contentHostingService.getCollection(ROOT_COLLECTION + SITE_ID_PERM_ERROR)).thenThrow(
					new PermissionException("user", ContentHostingService.AUTH_RESOURCE_READ, ROOT_COLLECTION + SITE_ID_PERM_ERROR));
		} catch (Exception e) {
			throw new RuntimeException("Exception while adding service mocks, " + e.getMessage());
		}
	}

	@Test
	public void testSiteHideResources() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);
		client.query("siteid", SITE_ID);
		client.query("hidden", "true");

		// client result
		String result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals("success", result);
	}

	@Test
	public void testSiteHideResourcesParams() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());

		// test empty sessionid
		client.query("sessionid", "");
		client.query("siteid", SITE_ID);
		client.query("hidden", "true");

		// client result
		String result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals("failure", result);
		
		// test empty siteid
		client.query("sessionid", SESSION_ID);
		client.query("siteid", "");
		client.query("hidden", "true");

		// client result
		result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals("failure", result);
		
		// test empty hidden
		client.query("sessionid", SESSION_ID);
		client.query("siteid", SITE_ID);
		client.query("hidden", "");

		// client result
		result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals("failure", result);
	}

	@Test
	public void testSiteHideResourcesWithPermissionError() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("sessionid", SESSION_ID);
		client.query("siteid", SITE_ID_PERM_ERROR);
		client.query("hidden", "true");

		// client result
		String result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals("failure", result);
	}
}
